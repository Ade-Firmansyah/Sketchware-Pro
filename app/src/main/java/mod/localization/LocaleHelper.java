package mod.localization;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class LocaleHelper {

    public static final String PREFERENCE_KEY = "app_language";
    public static final String DEFAULT_LANGUAGE = "en";

    private static final String PREFERENCES_NAME = "localization";
    private static final Set<String> SUPPORTED_LANGUAGES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "en", "in", "hi", "es", "pt", "ar", "ru", "ja", "ko", "zh-rCN", "tr",
                    "de", "fr", "it"
            ))
    );

    private LocaleHelper() {
    }

    @NonNull
    public static Context applySavedLocale(@NonNull Context context) {
        return updateResources(context, getCurrentLanguage(context));
    }

    @NonNull
    public static Context setLocale(@NonNull Context context, String languageCode) {
        String supportedLanguage = normalizeLanguageCode(languageCode);
        getPreferences(context)
                .edit()
                .putString(PREFERENCE_KEY, supportedLanguage)
                .apply();
        updateApplicationResources(context, supportedLanguage);
        return updateResources(context, supportedLanguage);
    }

    @NonNull
    public static String getCurrentLanguage(@NonNull Context context) {
        String languageCode = getPreferences(context)
                .getString(PREFERENCE_KEY, DEFAULT_LANGUAGE);
        return normalizeLanguageCode(languageCode);
    }

    public static boolean isSupported(String languageCode) {
        return languageCode != null && SUPPORTED_LANGUAGES.contains(languageCode);
    }

    @NonNull
    private static SharedPreferences getPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    private static String normalizeLanguageCode(String languageCode) {
        return isSupported(languageCode) ? languageCode : DEFAULT_LANGUAGE;
    }

    @NonNull
    private static Context updateResources(@NonNull Context context, @NonNull String languageCode) {
        Locale locale = toLocale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static void updateApplicationResources(
            @NonNull Context context,
            @NonNull String languageCode
    ) {
        Context applicationContext = context.getApplicationContext();
        Configuration configuration = new Configuration(
                applicationContext.getResources().getConfiguration()
        );
        Locale locale = toLocale(languageCode);
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        applicationContext.getResources().updateConfiguration(
                configuration,
                applicationContext.getResources().getDisplayMetrics()
        );
    }

    @NonNull
    private static Locale toLocale(@NonNull String languageCode) {
        if ("in".equals(languageCode)) {
            return new Locale("id");
        }
        if ("zh-rCN".equals(languageCode)) {
            return new Locale.Builder()
                    .setLanguage("zh")
                    .setRegion("CN")
                    .build();
        }
        return Locale.forLanguageTag(languageCode);
    }
}
