package pro.sketchware.ai.model;

public final class AiConfiguration {
    public boolean enabled;
    public AiProviderType provider = AiProviderType.OPENAI;
    public String baseUrl = "";
    public String apiKey = "";
    public String model = "";
    public String modelVersion = "";
    public int maxTokens = 2048;
    public float temperature = 0.2f;
    public boolean debugMode;
    public boolean allowCurrentCode = true;
    public boolean allowCurrentLayout = true;
    public boolean allowSelectedBlocks = true;
    public boolean allowBuildLog = true;
    public boolean allowFullProject;

    public AiConfiguration copy() {
        AiConfiguration copy = new AiConfiguration();
        copy.enabled = enabled;
        copy.provider = provider;
        copy.baseUrl = baseUrl;
        copy.apiKey = apiKey;
        copy.model = model;
        copy.modelVersion = modelVersion;
        copy.maxTokens = maxTokens;
        copy.temperature = temperature;
        copy.debugMode = debugMode;
        copy.allowCurrentCode = allowCurrentCode;
        copy.allowCurrentLayout = allowCurrentLayout;
        copy.allowSelectedBlocks = allowSelectedBlocks;
        copy.allowBuildLog = allowBuildLog;
        copy.allowFullProject = allowFullProject;
        return copy;
    }
}
