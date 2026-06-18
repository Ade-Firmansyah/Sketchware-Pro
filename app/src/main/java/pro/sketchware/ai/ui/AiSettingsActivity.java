package pro.sketchware.ai.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;

import pro.sketchware.R;
import pro.sketchware.ai.config.AiConfigurationStore;
import pro.sketchware.ai.model.AiConfiguration;
import pro.sketchware.ai.model.AiProviderType;
import pro.sketchware.ai.provider.AiProviderClient;
import pro.sketchware.ai.security.AiEndpointValidator;
import pro.sketchware.utility.SketchwareUtil;

public class AiSettingsActivity extends BaseAppCompatActivity {
    private MaterialSwitch enabled;
    private MaterialSwitch debugMode;
    private Spinner providerSpinner;
    private EditText apiKey;
    private EditText baseUrl;
    private EditText model;
    private EditText modelVersion;
    private EditText maxTokens;
    private EditText temperature;
    private MaterialCheckBox allowCode;
    private MaterialCheckBox allowLayout;
    private MaterialCheckBox allowBlocks;
    private MaterialCheckBox allowBuildLog;
    private MaterialCheckBox allowFullProject;
    private TextView instruction;
    private AiProviderType selectedProvider;
    private boolean loadingProvider;
    private AiProviderClient testClient;
    private androidx.appcompat.app.AlertDialog testDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);
        setContentView(createContentView());
        load(AiConfigurationStore.load(this));
    }

    private View createContentView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle("AI Agent Settings");
        toolbar.setSubtitle("RC1 local testing");
        toolbar.setNavigationIcon(R.drawable.ic_back_ios_24);
        toolbar.setNavigationOnClickListener(v -> finish());
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        form.setPadding(padding, padding, padding, padding);

        enabled = new MaterialSwitch(this);
        enabled.setText("Enable AI Agent");
        form.addView(enabled, matchWidthWrap());

        debugMode = new MaterialSwitch(this);
        debugMode.setText("Debug mode (local metadata logs only)");
        form.addView(debugMode, matchWidthWrap());

        TextView providerLabel = label("Provider");
        form.addView(providerLabel);
        providerSpinner = new Spinner(this);
        String[] providerNames = new String[AiProviderType.values().length];
        for (int i = 0; i < providerNames.length; i++) {
            providerNames[i] = AiProviderType.values()[i].getDisplayName();
        }
        providerSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, providerNames));
        form.addView(providerSpinner, matchWidthWrap());

        apiKey = input(form, "API key / access token", true);
        baseUrl = input(form, "Base URL", false);
        model = input(form, "Model", false);
        modelVersion = input(form, "Model version (optional)", false);
        maxTokens = input(form, "Maximum output tokens", false);
        maxTokens.setInputType(InputType.TYPE_CLASS_NUMBER);
        temperature = input(form, "Temperature (0.0 - 2.0)", false);
        temperature.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        instruction = new TextView(this);
        instruction.setPadding(0, dp(12), 0, dp(12));
        instruction.setTextIsSelectable(true);
        form.addView(instruction, matchWidthWrap());

        MaterialButton openConsole = button("Open provider console");
        openConsole.setOnClickListener(v -> openProviderConsole());
        form.addView(openConsole, matchWidthWrap());

        form.addView(label("Project context permissions"));
        allowCode = checkBox(form, "Current Java/Kotlin/XML");
        allowLayout = checkBox(form, "Current View layout");
        allowBlocks = checkBox(form, "Current selected blocks/event");
        allowBuildLog = checkBox(form, "Build error log");
        allowFullProject = checkBox(form, "Full project context (high privacy impact)");

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END);
        MaterialButton remove = button("Remove key");
        remove.setOnClickListener(v -> removeCredential());
        actions.addView(remove);
        MaterialButton test = button("Test connection");
        test.setOnClickListener(v -> testConnection());
        actions.addView(test);
        MaterialButton save = button("Save");
        save.setOnClickListener(v -> save());
        actions.addView(save);
        form.addView(actions, matchWidthWrap());

        TextView privacy = new TextView(this);
        privacy.setText("API keys are encrypted with Android Keystore. "
                + "This is provider configuration, not ChatGPT account login. "
                + "AI is optional and never applies project changes without preview and approval.");
        privacy.setPadding(0, dp(16), 0, dp(32));
        form.addView(privacy, matchWidthWrap());

        providerSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent, View view, int position, long id) {
                        AiProviderType provider = AiProviderType.values()[position];
                        if (!loadingProvider && selectedProvider != null
                                && provider != selectedProvider) {
                            load(AiConfigurationStore.load(AiSettingsActivity.this, provider));
                        }
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                });

        scroll.addView(form);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return root;
    }

    private void load(AiConfiguration config) {
        loadingProvider = true;
        selectedProvider = config.provider;
        providerSpinner.setSelection(config.provider.ordinal());
        enabled.setChecked(config.enabled);
        debugMode.setChecked(config.debugMode);
        apiKey.setText(config.apiKey);
        baseUrl.setText(config.baseUrl);
        model.setText(config.model);
        modelVersion.setText(config.modelVersion);
        maxTokens.setText(String.valueOf(config.maxTokens));
        temperature.setText(String.valueOf(config.temperature));
        allowCode.setChecked(config.allowCurrentCode);
        allowLayout.setChecked(config.allowCurrentLayout);
        allowBlocks.setChecked(config.allowSelectedBlocks);
        allowBuildLog.setChecked(config.allowBuildLog);
        allowFullProject.setChecked(config.allowFullProject);
        apiKey.setHint(config.provider.requiresApiKey()
                ? "Required"
                : "Optional for local endpoint");
        instruction.setText(providerInstructions(config.provider));
        loadingProvider = false;
    }

    private AiConfiguration readForm() {
        AiConfiguration config = new AiConfiguration();
        config.enabled = enabled.isChecked();
        config.debugMode = debugMode.isChecked();
        config.provider = AiProviderType.values()[providerSpinner.getSelectedItemPosition()];
        config.apiKey = text(apiKey);
        config.baseUrl = text(baseUrl);
        config.model = text(model);
        config.modelVersion = text(modelVersion);
        try {
            config.maxTokens = Integer.parseInt(text(maxTokens));
        } catch (NumberFormatException ignored) {
            config.maxTokens = 2048;
        }
        try {
            config.temperature = Float.parseFloat(text(temperature));
        } catch (NumberFormatException ignored) {
            config.temperature = 0.2f;
        }
        config.allowCurrentCode = allowCode.isChecked();
        config.allowCurrentLayout = allowLayout.isChecked();
        config.allowSelectedBlocks = allowBlocks.isChecked();
        config.allowBuildLog = allowBuildLog.isChecked();
        config.allowFullProject = allowFullProject.isChecked();
        return config;
    }

    private void save() {
        try {
            AiConfiguration config = readForm();
            validate(config);
            AiConfigurationStore.save(this, config);
            selectedProvider = config.provider;
            SketchwareUtil.toast("AI Agent settings saved");
        } catch (Exception error) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cannot save settings")
                    .setMessage(error.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void testConnection() {
        AiConfiguration config = readForm();
        try {
            validate(config);
        } catch (Exception error) {
            SketchwareUtil.toastError(error.getMessage());
            return;
        }
        MaterialAlertDialogBuilder progress = new MaterialAlertDialogBuilder(this)
                .setTitle("Testing " + config.provider.getDisplayName())
                .setMessage("Waiting for provider response...")
                .setCancelable(false);
        testDialog = progress.show();
        testClient = new AiProviderClient(this);
        testClient.send(
                config,
                "Reply with exactly: CONNECTION_OK",
                "Connection test",
                new AiProviderClient.CallbackHandler() {
                    @Override
                    public void onSuccess(String response, long durationMs) {
                        if (!canShowDialog()) return;
                        testDialog.dismiss();
                        testDialog = null;
                        testClient = null;
                        new MaterialAlertDialogBuilder(AiSettingsActivity.this)
                                .setTitle("Connection successful")
                                .setMessage("Provider responded in " + durationMs + " ms.\n\n"
                                        + response.substring(0, Math.min(200, response.length())))
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }

                    @Override
                    public void onError(String message) {
                        if (!canShowDialog()) return;
                        testDialog.dismiss();
                        testDialog = null;
                        testClient = null;
                        new MaterialAlertDialogBuilder(AiSettingsActivity.this)
                                .setTitle("Connection failed")
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (testClient != null) {
            testClient.cancel();
            testClient = null;
        }
        if (testDialog != null) {
            testDialog.dismiss();
            testDialog = null;
        }
        super.onDestroy();
    }

    private boolean canShowDialog() {
        return !isFinishing() && !isDestroyed();
    }

    private void removeCredential() {
        AiProviderType provider =
                AiProviderType.values()[providerSpinner.getSelectedItemPosition()];
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove API key?")
                .setMessage("The encrypted credential for " + provider.getDisplayName()
                        + " will be deleted from this device.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    AiConfigurationStore.removeCredential(this, provider);
                    apiKey.setText("");
                    SketchwareUtil.toast("Credential removed");
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void validate(AiConfiguration config) {
        AiEndpointValidator.validate(config.provider, config.baseUrl);
        if (config.model.isBlank()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (config.provider.requiresApiKey() && config.apiKey.isBlank()) {
            throw new IllegalArgumentException("API key is required");
        }
    }

    private void openProviderConsole() {
        AiProviderType provider =
                AiProviderType.values()[providerSpinner.getSelectedItemPosition()];
        if (provider.getKeyUrl().isBlank()) {
            SketchwareUtil.toast("Local LLM does not require a provider console");
            return;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(provider.getKeyUrl())));
    }

    private String providerInstructions(AiProviderType provider) {
        return switch (provider) {
            case OPENAI -> "OpenAI: open the OpenAI Platform API Keys page, create an API key, "
                    + "paste it here, select an API model, test the connection, then save.";
            case CLAUDE -> "Claude: open Anthropic Console, create an API key, paste it here, "
                    + "select a Claude API model, test the connection, then save.";
            case GEMINI -> "Gemini: open Google AI Studio, create an API key, paste it here, "
                    + "select a Gemini API model, test the connection, then save.";
            case DEEPSEEK -> "DeepSeek: open DeepSeek Platform, create an API key, paste it here, "
                    + "select a model, test the connection, then save.";
            case QWEN -> "Qwen: open Alibaba Cloud Model Studio/DashScope, create an API key, "
                    + "choose the endpoint region and Qwen model, test, then save.";
            case LOCAL -> "Local LLM: start an OpenAI-compatible server, enter its /v1 base URL "
                    + "and model name, then test. A token is optional.";
        };
    }

    private EditText input(LinearLayout parent, String hint, boolean password) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(hint);
        EditText input = new EditText(this);
        if (password) {
            input.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            input.setSingleLine(true);
        }
        layout.addView(input, matchWidthWrap());
        parent.addView(layout, matchWidthWrap());
        return input;
    }

    private MaterialCheckBox checkBox(LinearLayout parent, String text) {
        MaterialCheckBox checkBox = new MaterialCheckBox(this);
        checkBox.setText(text);
        parent.addView(checkBox, matchWidthWrap());
        return checkBox;
    }

    private MaterialButton button(String text) {
        MaterialButton button = new MaterialButton(this);
        button.setText(text);
        return button;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setPadding(0, dp(12), 0, dp(4));
        return label;
    }

    private LinearLayout.LayoutParams matchWidthWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static String text(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
