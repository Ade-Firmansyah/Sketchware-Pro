package pro.sketchware.ai.security;

import java.net.URI;
import java.util.Locale;

import pro.sketchware.ai.model.AiProviderType;

public final class AiEndpointValidator {
    private AiEndpointValidator() {
    }

    public static void validate(AiProviderType provider, String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Provider base URL is required");
        }
        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Provider base URL is invalid");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null) {
            throw new IllegalArgumentException("Provider base URL must include scheme and host");
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return;
        }
        if (provider != AiProviderType.LOCAL || !"http".equalsIgnoreCase(scheme)
                || !isPrivateHost(host)) {
            throw new IllegalArgumentException(
                    "Cloud URLs require HTTPS; local HTTP is limited to loopback or private LAN");
        }
    }

    private static boolean isPrivateHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(normalized)
                || "127.0.0.1".equals(normalized)
                || "::1".equals(normalized)) {
            return true;
        }
        if (normalized.startsWith("10.") || normalized.startsWith("192.168.")) {
            return true;
        }
        if (normalized.startsWith("172.")) {
            String[] parts = normalized.split("\\.");
            if (parts.length == 4) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return normalized.startsWith("fc") || normalized.startsWith("fd")
                || normalized.startsWith("fe80:");
    }
}
