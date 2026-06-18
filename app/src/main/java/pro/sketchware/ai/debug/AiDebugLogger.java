package pro.sketchware.ai.debug;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public final class AiDebugLogger {
    private static final long MAX_LOG_BYTES = 512L * 1024L;

    private AiDebugLogger() {
    }

    public static void append(Context context, boolean enabled, String message) {
        if (!enabled || message == null) {
            return;
        }
        File directory = new File(context.getFilesDir(), "ai_debug");
        if (!directory.exists() && !directory.mkdirs()) {
            return;
        }
        File log = new File(directory, "agent.log");
        if (log.length() > MAX_LOG_BYTES && !log.delete()) {
            return;
        }
        try (FileWriter writer = new FileWriter(log, true)) {
            writer.write(Instant.now() + " " + message.replace('\n', ' ') + "\n");
        } catch (IOException ignored) {
            // Debug logging must never break the editor.
        }
    }

    public static File getLogFile(Context context) {
        return new File(new File(context.getFilesDir(), "ai_debug"), "agent.log");
    }
}
