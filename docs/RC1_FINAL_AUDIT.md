# Sketchware Complete RC1 Final Audit

Date: 2026-06-15
Status: Internal local test only

## Verified

- Debug APK build: passed
- Release-test APK build: passed
- Release AAB build: passed
- Full debug lint: passed with 0 errors
- Release vital lint: passed
- Debug unit test task: passed (no test sources are currently present)
- APK Signature Scheme v2: verified
- Git diff integrity check: passed

## Stability fixes

- Fixed Android 12+ build notification crash by making the cancel PendingIntent immutable.
- Guarded notification permission failures so they cannot stop the builder.
- Ensured BuildTask always reaches a finished state and shuts down its executor.
- Validated local APK copies before reporting build success.
- Preserved project data before source/resource generation.
- Kept third-party FilePicker restoration safe through the global FragmentFactory.
- Removed malformed drawable XML and corrected AppCompat menu resources.
- Fixed image and sound picker intents that previously cleared their content URI.
- Registered the internal log receiver as not exported and guarded unregister.
- Reduced CodeEditor layout work during drawing and switched to AppCompatEditText.
- Fixed invalid format-string calls.
- Hardened AI connection-test lifecycle, provider switching, secret-key creation,
  debug-log growth, endpoint construction, and Gemini credential transport.

## Remaining private-test checks

- Install and cold-start testing requires a connected Android device.
- Provider runtime tests require valid API keys or a running Local LLM endpoint.
- Generated user projects must still be tested across representative widgets,
  blocks, components, resources, debug builds, and signed builds.
- The app still targets Android SDK 28. Raising the target SDK is a separate
  compatibility migration and is required before a future Play Store release.
- Remaining lint warnings are legacy maintenance items, primarily unused
  resources, icon sizing, locale handling, and deprecated API usage.

No commit, push, tag, GitHub release, or public upload was performed.
