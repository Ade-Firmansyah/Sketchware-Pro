package pro.sketchware.ai.config;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import pro.sketchware.ai.model.AiConfiguration;
import pro.sketchware.ai.model.AiProviderType;
import pro.sketchware.ai.security.AiSecretStore;

public final class AiConfigurationStore {
    private static final String PREFS = "ai_agent_settings";

    private AiConfigurationStore() {
    }

    public static AiConfiguration load(Context context) {
        SharedPreferences prefs = preferences(context);
        AiProviderType provider =
                AiProviderType.fromName(prefs.getString("provider", null));
        return load(context, provider, prefs);
    }

    public static AiConfiguration load(Context context, AiProviderType provider) {
        return load(context, provider, preferences(context));
    }

    private static AiConfiguration load(
            Context context, AiProviderType provider, SharedPreferences prefs) {
        AiConfiguration config = new AiConfiguration();
        config.enabled = prefs.getBoolean("enabled", false);
        config.provider = provider == null ? AiProviderType.OPENAI : provider;
        String prefix = config.provider.name().toLowerCase(Locale.ROOT) + "_";
        config.baseUrl = prefs.getString(
                prefix + "base_url", config.provider.getDefaultBaseUrl());
        config.model = prefs.getString(prefix + "model", config.provider.getDefaultModel());
        config.modelVersion = prefs.getString(prefix + "model_version", "");
        config.maxTokens = clamp(prefs.getInt(prefix + "max_tokens", 2048), 64, 32768);
        config.temperature = Math.max(
                0f, Math.min(2f, prefs.getFloat(prefix + "temperature", 0.2f)));
        config.debugMode = prefs.getBoolean("debug_mode", false);
        config.allowCurrentCode = prefs.getBoolean("allow_current_code", true);
        config.allowCurrentLayout = prefs.getBoolean("allow_current_layout", true);
        config.allowSelectedBlocks = prefs.getBoolean("allow_selected_blocks", true);
        config.allowBuildLog = prefs.getBoolean("allow_build_log", true);
        config.allowFullProject = prefs.getBoolean("allow_full_project", false);
        config.apiKey = AiSecretStore.get(context, config.provider);
        return config;
    }

    public static void save(Context context, AiConfiguration config) {
        String prefix = config.provider.name().toLowerCase(Locale.ROOT) + "_";
        preferences(context).edit()
                .putBoolean("enabled", config.enabled)
                .putString("provider", config.provider.name())
                .putString(prefix + "base_url", trimTrailingSlash(config.baseUrl))
                .putString(prefix + "model", config.model == null ? "" : config.model.trim())
                .putString(prefix + "model_version",
                        config.modelVersion == null ? "" : config.modelVersion.trim())
                .putInt(prefix + "max_tokens", clamp(config.maxTokens, 64, 32768))
                .putFloat(prefix + "temperature",
                        Math.max(0f, Math.min(2f, config.temperature)))
                .putBoolean("debug_mode", config.debugMode)
                .putBoolean("allow_current_code", config.allowCurrentCode)
                .putBoolean("allow_current_layout", config.allowCurrentLayout)
                .putBoolean("allow_selected_blocks", config.allowSelectedBlocks)
                .putBoolean("allow_build_log", config.allowBuildLog)
                .putBoolean("allow_full_project", config.allowFullProject)
                .apply();
        AiSecretStore.put(context, config.provider, config.apiKey);
    }

    public static void removeCredential(Context context, AiProviderType provider) {
        AiSecretStore.remove(context, provider);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String trimTrailingSlash(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
