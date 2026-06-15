package pro.sketchware.ai.security;

import java.util.regex.Pattern;

public final class AiContextRedactor {
    private static final Pattern API_KEY = Pattern.compile(
            "(?i)(api[_ -]?key|authorization|token|password|secret)(\\s*[:=]\\s*)[^\\s,;\"']+");
    private static final Pattern BEARER =
            Pattern.compile("(?i)bearer\\s+[a-z0-9._~+\\-/]+=*");
    private static final Pattern PRIVATE_KEY = Pattern.compile(
            "-----BEGIN [^-]*PRIVATE KEY-----[\\s\\S]*?-----END [^-]*PRIVATE KEY-----");
    private static final Pattern WINDOWS_USER =
            Pattern.compile("(?i)[a-z]:\\\\users\\\\[^\\\\\\s]+");

    private AiContextRedactor() {
    }

    public static String redact(String input) {
        if (input == null) {
            return "";
        }
        String redacted = PRIVATE_KEY.matcher(input).replaceAll("[REDACTED_PRIVATE_KEY]");
        redacted = BEARER.matcher(redacted).replaceAll("Bearer [REDACTED]");
        redacted = API_KEY.matcher(redacted).replaceAll("$1$2[REDACTED]");
        return WINDOWS_USER.matcher(redacted).replaceAll("[USER_HOME]");
    }
}
