package pro.sketchware.ai.model;

public enum AiProviderType {
    OPENAI(
            "OpenAI",
            "https://api.openai.com/v1",
            "gpt-4.1-mini",
            "https://platform.openai.com/api-keys"),
    CLAUDE(
            "Claude",
            "https://api.anthropic.com/v1",
            "claude-sonnet-4-5",
            "https://console.anthropic.com/settings/keys"),
    GEMINI(
            "Gemini",
            "https://generativelanguage.googleapis.com/v1beta",
            "gemini-2.5-flash",
            "https://aistudio.google.com/app/apikey"),
    DEEPSEEK(
            "DeepSeek",
            "https://api.deepseek.com",
            "deepseek-chat",
            "https://platform.deepseek.com/api_keys"),
    QWEN(
            "Qwen",
            "https://dashscope-intl.aliyuncs.com/compatible-mode/v1",
            "qwen3-coder-plus",
            "https://modelstudio.console.alibabacloud.com/"),
    LOCAL(
            "Local LLM",
            "http://127.0.0.1:11434/v1",
            "",
            "");

    private final String displayName;
    private final String defaultBaseUrl;
    private final String defaultModel;
    private final String keyUrl;

    AiProviderType(
            String displayName, String defaultBaseUrl, String defaultModel, String keyUrl) {
        this.displayName = displayName;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModel = defaultModel;
        this.keyUrl = keyUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public String getKeyUrl() {
        return keyUrl;
    }

    public boolean requiresApiKey() {
        return this != LOCAL;
    }

    public boolean isOpenAiCompatible() {
        return this == OPENAI || this == DEEPSEEK || this == QWEN || this == LOCAL;
    }

    public static AiProviderType fromName(String value) {
        try {
            return value == null ? OPENAI : valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return OPENAI;
        }
    }
}
