package pro.sketchware.utility.autosave;

import android.content.Context;
import android.content.SharedPreferences;

public final class EditorAutoSaveManager {

    public static final long OFF = 0L;
    public static final long THIRTY_SECONDS = 30_000L;
    public static final long ONE_MINUTE = 60_000L;
    public static final long THREE_MINUTES = 180_000L;
    public static final long FIVE_MINUTES = 300_000L;

    private static final String PREFERENCES = "editor_preferences";
    private static final String KEY_INTERVAL = "auto_save_interval";

    private EditorAutoSaveManager() {
    }

    public static long getInterval(Context context) {
        return preferences(context).getLong(KEY_INTERVAL, ONE_MINUTE);
    }

    public static void setInterval(Context context, long interval) {
        preferences(context).edit().putLong(KEY_INTERVAL, interval).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
