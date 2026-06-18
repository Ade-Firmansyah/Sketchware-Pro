package pro.sketchware.ai.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public final class AiAgentLauncher {
    public static final String EXTRA_MODE = "ai_mode";
    public static final String EXTRA_TITLE = "ai_title";
    public static final String EXTRA_CONTEXT = "ai_context";
    public static final String EXTRA_CURRENT_CONTENT = "ai_current_content";
    public static final String EXTRA_ALLOW_APPLY = "ai_allow_apply";
    public static final String EXTRA_RESULT_CONTENT = "ai_result_content";

    public static final String MODE_CODE = "code";
    public static final String MODE_BLOCKS = "blocks";
    public static final String MODE_BUILD = "build";
    public static final String MODE_VIEW = "view";
    public static final String MODE_COMPONENT = "component";

    private AiAgentLauncher() {
    }

    public static Intent createIntent(
            Context context,
            String mode,
            String title,
            String projectContext,
            String currentContent,
            boolean allowApply) {
        return new Intent(context, AiAgentActivity.class)
                .putExtra(EXTRA_MODE, mode)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_CONTEXT, bounded(projectContext))
                .putExtra(EXTRA_CURRENT_CONTENT, bounded(currentContent))
                .putExtra(EXTRA_ALLOW_APPLY, allowApply);
    }

    public static void open(
            Activity activity, String mode, String title, String projectContext) {
        activity.startActivity(createIntent(
                activity, mode, title, projectContext, "", false));
    }

    private static String bounded(String value) {
        if (value == null) {
            return "";
        }
        int max = 60_000;
        return value.length() <= max
                ? value
                : value.substring(0, max) + "\n[Context truncated locally]";
    }
}
