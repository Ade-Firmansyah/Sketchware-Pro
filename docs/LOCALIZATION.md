# Localization

Sketchware Complete stores the selected application language in the
`localization` SharedPreferences file under the `app_language` key. English is
the default and fallback language. The current pre-release supports English,
Indonesian, Hindi, Spanish, Portuguese, Arabic, Russian, Japanese, Korean,
Simplified Chinese, Turkish, German, French, and Italian.

## Architecture

- `mod.localization.LocaleHelper` validates, persists, and applies locales.
- `SketchApplication.attachBaseContext()` restores the locale before startup.
- `BaseAppCompatActivity.attachBaseContext()` applies it to every app activity.
- `SettingsLanguageFragment` provides the language selector and recreates its
  host activity after a change.
- `res/xml/locales_config.xml` exposes supported languages to Android 13+.

Indonesian is stored as the requested legacy code `in`, then mapped to the
modern Java locale `id`. Simplified Chinese is stored as `zh-rCN` and mapped to
the `zh-CN` locale.

## Add A Language

1. Add the language code to `LocaleHelper.SUPPORTED_LANGUAGES`.
2. Add it to `SettingsLanguageFragment.LANGUAGE_CODES`.
3. Add its native display name to `R.array.language_names` in the same order.
4. Create `app/src/main/res/values-xx/strings.xml`.
5. Add the locale tag to `res/xml/locales_config.xml`.
6. Build and test selection, restart persistence, fallback, and text direction.

Untranslated resources intentionally fall back to English. Translate
navigation, Settings, project management, build messages, common actions, and
toasts first as coverage expands.

## Verification

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
```

On a device, test English, Indonesian, Arabic, and Simplified Chinese first.
For Arabic, verify toolbar navigation, cards, lists, and text alignment in RTL.
