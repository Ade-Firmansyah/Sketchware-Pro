package pro.sketchware.ai.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AiPatchUtils {
    private static final Pattern FENCED_CODE =
            Pattern.compile("```(?:[a-zA-Z0-9_+.-]+)?\\s*\\n([\\s\\S]*?)```");

    private AiPatchUtils() {
    }

    public static String extractProposedContent(String response) {
        if (response == null) {
            return "";
        }
        Matcher matcher = FENCED_CODE.matcher(response);
        String longest = "";
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (candidate.length() > longest.length()) {
                longest = candidate;
            }
        }
        return longest;
    }

    public static String createPreview(String current, String proposed) {
        String[] oldLines = safe(current).split("\\R", -1);
        String[] newLines = safe(proposed).split("\\R", -1);
        StringBuilder preview = new StringBuilder();
        preview.append("--- Current\n+++ AI proposal\n");
        int max = Math.max(oldLines.length, newLines.length);
        int shown = 0;
        for (int index = 0; index < max && shown < 240; index++) {
            String oldLine = index < oldLines.length ? oldLines[index] : null;
            String newLine = index < newLines.length ? newLines[index] : null;
            if (oldLine != null && oldLine.equals(newLine)) {
                continue;
            }
            if (oldLine != null) {
                preview.append("- ").append(oldLine).append('\n');
                shown++;
            }
            if (newLine != null) {
                preview.append("+ ").append(newLine).append('\n');
                shown++;
            }
        }
        if (shown >= 240) {
            preview.append("[Preview truncated]\n");
        }
        if (shown == 0) {
            preview.append("No textual changes detected.");
        }
        return preview.toString();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
