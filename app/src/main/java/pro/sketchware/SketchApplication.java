package pro.sketchware;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.besome.sketch.tools.CollectErrorActivity;

import mod.localization.LocaleHelper;
import pro.sketchware.utility.theme.ThemeManager;

public class SketchApplication extends Application {
    private static Context mApplicationContext;

    public static Context getContext() {
        return mApplicationContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationContext = getApplicationContext();
        Thread.UncaughtExceptionHandler systemExceptionHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                try {
                    Intent intent = new Intent(getApplicationContext(), CollectErrorActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("error", Log.getStackTraceString(throwable));
                    startActivity(intent);
                } catch (Exception errorActivityFailure) {
                    if (systemExceptionHandler != null) {
                        systemExceptionHandler.uncaughtException(thread, throwable);
                        return;
                    }
                }
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        });
        ThemeManager.applyTheme(this, ThemeManager.getCurrentTheme(this));
    }
}
