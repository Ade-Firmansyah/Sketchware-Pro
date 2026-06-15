# Sketchware Complete RC1 - AI Provider Setup

The AI Agent uses provider API credentials. It does not log in to ChatGPT,
Claude, Gemini, or another consumer account.

Open:

```text
Settings > AI Agent
```

Enable the agent, choose a provider, enter its configuration, tap
`Test connection`, then tap `Save`.

## OpenAI

1. Open `https://platform.openai.com/api-keys`.
2. Create a project API key.
3. Select `OpenAI` in Sketchware Complete.
4. Keep the default base URL `https://api.openai.com/v1`.
5. Enter an API model available to the OpenAI project.
6. Paste the API key, test the connection, and save.

OpenAI API billing and ChatGPT subscriptions are separate products.

## Claude

1. Open `https://console.anthropic.com/settings/keys`.
2. Create an Anthropic API key.
3. Select `Claude`.
4. Keep the default base URL `https://api.anthropic.com/v1`.
5. Enter a Claude API model available to the account.
6. Paste the key, test the connection, and save.

## Gemini

1. Open `https://aistudio.google.com/app/apikey`.
2. Create a Gemini API key.
3. Select `Gemini`.
4. Keep the default base URL
   `https://generativelanguage.googleapis.com/v1beta`.
5. Enter a supported Gemini model.
6. Paste the key, test the connection, and save.

## DeepSeek

1. Open `https://platform.deepseek.com/api_keys`.
2. Create a DeepSeek API key.
3. Select `DeepSeek`.
4. Keep the default base URL `https://api.deepseek.com`.
5. Enter a supported model such as `deepseek-chat`.
6. Paste the key, test the connection, and save.

## Qwen

1. Open Alibaba Cloud Model Studio/DashScope.
2. Activate Model Studio and create an API key.
3. Select `Qwen`.
4. Choose the base URL for the same region as the API key. The international
   default is:

   ```text
   https://dashscope-intl.aliyuncs.com/compatible-mode/v1
   ```

5. Enter a supported Qwen model.
6. Paste the key, test the connection, and save.

API keys and endpoints can be region-specific.

## Local LLM

1. Start an OpenAI-compatible local server.
2. Make the server reachable from the Android device.
3. Select `Local LLM`.
4. Enter the server `/v1` base URL, for example:

   ```text
   http://127.0.0.1:11434/v1
   ```

5. Enter the exact model name exposed by the server.
6. Add a token only if the local server requires one.
7. Test the connection and save.

`127.0.0.1` means the Android device itself. When the model runs on another
computer, use that computer's LAN address and ensure its firewall permits the
connection.

## Context Permissions

Permissions are independent:

- Current Java/Kotlin/XML.
- Current View layout.
- Current selected blocks/event.
- Build error log.
- Full project context.

Full project context is disabled by default. Before project context is sent,
Sketchware Complete asks for confirmation.

## Debug Mode

Debug mode records local metadata only:

- Provider and model.
- Request status.
- Response duration.
- HTTP/error category.

Prompts, source code, API keys, and full response bodies are not written to the
debug log.

## Applying AI Results

Code editors support:

- Preview.
- Apply.
- Copy only.
- Cancel.

Applying a proposal loads it into the editor. The user must still review and
save it. Block, View, Build Error, and Component integrations initially return
reviewable plans and do not mutate project models automatically.

