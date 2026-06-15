package pro.sketchware.ai.provider;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pro.sketchware.ai.debug.AiDebugLogger;
import pro.sketchware.ai.model.AiConfiguration;
import pro.sketchware.ai.model.AiProviderType;
import pro.sketchware.ai.security.AiContextRedactor;
import pro.sketchware.ai.security.AiEndpointValidator;

public final class AiProviderClient {
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");
    private final Context context;
    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile Call activeCall;

    public AiProviderClient(Context context) {
        this.context = context.getApplicationContext();
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    public void send(
            AiConfiguration config,
            String systemPrompt,
            String userPrompt,
            CallbackHandler callback) {
        try {
            validate(config);
            Request request = buildRequest(
                    config,
                    AiContextRedactor.redact(systemPrompt),
                    AiContextRedactor.redact(userPrompt));
            long startedAt = SystemClock.elapsedRealtime();
            AiDebugLogger.append(context, config.debugMode,
                    "request provider=" + config.provider
                            + " model=" + config.model
                            + " endpoint=" + request.url().host());
            cancel();
            Call call = client.newCall(request);
            activeCall = call;
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException error) {
                    clearActiveCall(call);
                    if (call.isCanceled()) {
                        post(() -> callback.onError("Request canceled"));
                        return;
                    }
                    logAndPostError(config, callback, "Network error: " + error.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    clearActiveCall(call);
                    try (response) {
                        String body = response.body() == null ? "" : response.body().string();
                        long duration = SystemClock.elapsedRealtime() - startedAt;
                        AiDebugLogger.append(context, config.debugMode,
                                "response provider=" + config.provider
                                        + " status=" + response.code()
                                        + " duration_ms=" + duration);
                        if (!response.isSuccessful()) {
                            logAndPostError(config, callback,
                                    "HTTP " + response.code() + ": " + extractApiError(body));
                            return;
                        }
                        String text = extractText(config.provider, body);
                        if (text.isBlank()) {
                            logAndPostError(config, callback,
                                    "Provider returned an empty or unsupported response");
                            return;
                        }
                        post(() -> callback.onSuccess(text, duration));
                    } catch (Exception error) {
                        logAndPostError(config, callback,
                                "Invalid provider response: " + error.getMessage());
                    }
                }
            });
        } catch (Exception error) {
            post(() -> callback.onError(error.getMessage()));
        }
    }

    public void cancel() {
        Call call = activeCall;
        activeCall = null;
        if (call != null) {
            call.cancel();
        }
    }

    private void clearActiveCall(Call call) {
        if (activeCall == call) {
            activeCall = null;
        }
    }

    private Request buildRequest(
            AiConfiguration config, String systemPrompt, String userPrompt) {
        return switch (config.provider) {
            case CLAUDE -> buildClaudeRequest(config, systemPrompt, userPrompt);
            case GEMINI -> buildGeminiRequest(config, systemPrompt, userPrompt);
            default -> buildOpenAiCompatibleRequest(config, systemPrompt, userPrompt);
        };
    }

    private Request buildOpenAiCompatibleRequest(
            AiConfiguration config, String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.model);
        body.addProperty("max_tokens", config.maxTokens);
        body.addProperty("temperature", config.temperature);
        body.addProperty("stream", false);
        JsonArray messages = new JsonArray();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userPrompt));
        body.add("messages", messages);

        Request.Builder builder = new Request.Builder()
                .url(endpoint(config.baseUrl, "chat/completions"))
                .post(RequestBody.create(body.toString(), JSON))
                .header("Content-Type", "application/json");
        if (!config.apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + config.apiKey);
        }
        return builder.build();
    }

    private Request buildClaudeRequest(
            AiConfiguration config, String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.model);
        body.addProperty("max_tokens", config.maxTokens);
        body.addProperty("temperature", config.temperature);
        body.addProperty("system", systemPrompt);
        JsonArray messages = new JsonArray();
        messages.add(message("user", userPrompt));
        body.add("messages", messages);
        return new Request.Builder()
                .url(endpoint(config.baseUrl, "messages"))
                .post(RequestBody.create(body.toString(), JSON))
                .header("Content-Type", "application/json")
                .header("x-api-key", config.apiKey)
                .header("anthropic-version", "2023-06-01")
                .build();
    }

    private Request buildGeminiRequest(
            AiConfiguration config, String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        systemParts.add(textPart(systemPrompt));
        systemInstruction.add("parts", systemParts);
        body.add("systemInstruction", systemInstruction);

        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        content.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        parts.add(textPart(userPrompt));
        content.add("parts", parts);
        contents.add(content);
        body.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", config.temperature);
        generationConfig.addProperty("maxOutputTokens", config.maxTokens);
        body.add("generationConfig", generationConfig);

        String url = endpoint(
                config.baseUrl, "models/" + config.model + ":generateContent");
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", config.apiKey)
                .build();
    }

    private JsonObject message(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private JsonObject textPart(String text) {
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        return part;
    }

    private String extractText(AiProviderType provider, String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (provider == AiProviderType.CLAUDE) {
            JsonArray content = root.getAsJsonArray("content");
            StringBuilder result = new StringBuilder();
            if (content != null) {
                for (JsonElement element : content) {
                    JsonObject item = element.getAsJsonObject();
                    if (item.has("text")) {
                        result.append(item.get("text").getAsString());
                    }
                }
            }
            return result.toString();
        }
        if (provider == AiProviderType.GEMINI) {
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "";
            }
            JsonArray parts = candidates.get(0).getAsJsonObject()
                    .getAsJsonObject("content").getAsJsonArray("parts");
            StringBuilder result = new StringBuilder();
            for (JsonElement element : parts) {
                JsonObject part = element.getAsJsonObject();
                if (part.has("text")) {
                    result.append(part.get("text").getAsString());
                }
            }
            return result.toString();
        }
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        return choices.get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString();
    }

    private String extractApiError(String body) {
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonElement error = root.get("error");
            if (error != null && error.isJsonObject()) {
                JsonElement message = error.getAsJsonObject().get("message");
                return message == null ? "Provider rejected the request" : message.getAsString();
            }
            if (error != null && error.isJsonPrimitive()) {
                return error.getAsString();
            }
        } catch (Exception ignored) {
            // Return a bounded raw error below.
        }
        String compact = body == null ? "" : body.replaceAll("\\s+", " ").trim();
        return compact.substring(0, Math.min(compact.length(), 500));
    }

    private void validate(AiConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("AI configuration is missing");
        }
        AiEndpointValidator.validate(config.provider, config.baseUrl);
        if (config.model == null || config.model.isBlank()) {
            throw new IllegalArgumentException("Model name is required");
        }
        if (config.provider.requiresApiKey()
                && (config.apiKey == null || config.apiKey.isBlank())) {
            throw new IllegalArgumentException("API key is required for " + config.provider.getDisplayName());
        }
    }

    private void logAndPostError(
            AiConfiguration config, CallbackHandler callback, String message) {
        AiDebugLogger.append(context, config.debugMode,
                "error provider=" + config.provider + " message="
                        + AiContextRedactor.redact(message));
        post(() -> callback.onError(message));
    }

    private String endpoint(String baseUrl, String path) {
        return baseUrl.endsWith("/") ? baseUrl + path : baseUrl + "/" + path;
    }

    private void post(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public interface CallbackHandler {
        void onSuccess(String response, long durationMs);

        void onError(String message);
    }
}
