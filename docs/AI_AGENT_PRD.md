# Sketchware Complete AI Agent - Product Requirements Document

## Document Status

| Field | Value |
| --- | --- |
| Product | Sketchware Complete |
| Feature | AI Agent Foundation |
| Status | RC1 internal implementation plan |
| Version | 0.2 |
| Owner | Ade Firmansyah (LotusVolt) |
| Target | RC1 local test build |
| Scope | RC1 implementation and private device testing |

This document defines the AI coding agent inside Sketchware Complete RC1.
Implementation and local APK testing are authorized, but public release is not.
All work remains behind an experimental flag and must preserve the stabilized
RC1 builder.

RC1 is a long-running internal candidate. Code Editor, Block Editor, AI Agent,
builder fixes, and UI fixes remain RC1 work until personal device testing is
complete. Do not create RC2/RC3 tags, GitHub releases, public prereleases, or
store uploads from this work.

## 1. Product Vision

Sketchware Complete AI Agent helps Android creators describe an intent in
natural language and safely turn it into source code, visual blocks, layouts,
or build fixes without leaving the mobile IDE.

The agent should feel like an engineering assistant, not an uncontrolled code
generator. It must understand the current project context, explain its plan,
show proposed changes, request approval, apply changes transactionally, and
allow the user to undo them.

## 2. Goals

- Provide one consistent AI experience across Code Editor, Block Editor, Build
  Error, and View Editor.
- Support cloud and local model providers through one provider-neutral
  interface.
- Generate changes that follow Sketchware project models instead of editing
  generated output blindly.
- Preserve existing projects and prevent silent data loss.
- Make every AI action reviewable, cancellable, auditable, and reversible.
- Keep provider credentials private and never include secrets in prompts,
  logs, analytics, backups, or exported projects.
- Allow future tools and extensions without coupling the editor to one model
  vendor.

## 3. Non-Goals For The First Release

- Fully autonomous background coding without user approval.
- Direct login to a user's ChatGPT consumer account.
- Copying, embedding, or presenting ChatGPT/Codex extensions as open source.
- Training or fine-tuning models inside Sketchware Complete.
- Executing arbitrary shell commands from model output.
- Sending the complete project to a provider by default.
- Automatically publishing APKs, commits, releases, or store submissions.
- Replacing the existing compiler, code generator, block engine, or layout
  engine.
- Building an extension marketplace in the initial AI Agent milestone.
- Guaranteeing that every generated answer is correct.

## 4. Prerequisite Editor Foundation

AI must not be used to hide limitations in the existing editors. Code and
Block editor foundations should be stable independently before AI modification
tools are enabled.

### 4.1 Code Viewer To Code Editor

Upgrade the existing Code Viewer into a performant editor with:

- Java, Kotlin, and XML syntax highlighting.
- Line numbers.
- Search and replace.
- Bracket matching.
- Automatic indentation.
- Code folding.
- Copy and paste.
- Format code.
- Error markers.
- Read-only and edit-mode toggle.

Requirements:

- Large files must be processed incrementally and must not freeze the UI.
- Syntax highlighting and diagnostics must run outside the main thread.
- Generated code opens read-only by default.
- Entering edit mode for generated code must show an overwrite warning.
- Manual source files and generated source must have visibly different status.
- Block-generated source must remain reproducible and protected from silent
  overwrite.

### 4.2 Block Editor Upgrade

Improve the Block Editor without changing existing block semantics:

- Clearer block category UI.
- Block search.
- Favorite and recent blocks.
- Cleaner bottom block drawer.
- Zoom controls.
- More precise dragging.
- Smoother workspace scrolling.
- Stronger selected-block highlight.
- Block validation indicator.
- Missing component warning.

Initial categories:

- Variable
- List
- Control
- Operator
- Math
- File
- View
- Component
- XML Strings
- Custom Blocks
- AI Suggestions

`AI Suggestions` must remain hidden when AI is disabled.

### 4.3 Code And Block Synchronization

Blocks remain the authoritative source for block-generated Java or Kotlin.
Manual source must be stored separately from generated block output.

The editor must never silently overwrite a manual change. When generated and
manual versions diverge, show:

> Generated code and manual code are different. Choose which version to keep.

Actions:

- `Keep Blocks`
- `Keep Manual Code`
- `Save as Custom Java File`

Conflict resolution must create a recovery snapshot and remain undoable. A
future bidirectional code-to-block converter is not part of the initial
foundation; unsupported code should be preserved as manual code.

## 5. Target Users

### Beginner Creator

Needs plain-language assistance to create screens, blocks, events, and common
Android behavior while learning what the generated project does.

### Advanced Sketchware User

Needs faster code generation, repetitive block creation, refactoring, and
diagnosis of compiler or runtime errors.

### Offline Or Privacy-Focused User

Needs an OpenAI-compatible local endpoint and explicit control over what
project context leaves the device.

## 6. Supported Providers

The architecture must support these providers without embedding provider logic
inside editor screens:

| Provider | Authentication | Initial transport |
| --- | --- | --- |
| OpenAI API | API key | Native adapter |
| Anthropic Claude API | API key | Native adapter |
| Google Gemini API | API key | Native adapter |
| DeepSeek API | API key | OpenAI-compatible or native adapter |
| Qwen AI | API key or compatible endpoint | OpenAI-compatible or native adapter |
| Local LLM API | Optional token | User-defined OpenAI-compatible base URL |

An API key field is provider configuration, not user authentication into
ChatGPT, Claude, Gemini, or another consumer application. The UI should use
terms such as `Connect Provider`, `Save API Configuration`, and
`Remove Credential` instead of implying a consumer-account login.

For production deployments, a project-controlled backend proxy may be offered
as an alternative to entering a personal provider key. A proxy must not become
a hard dependency for local LLM use or bring-your-own-key mode.

Provider availability, endpoint formats, model names, token limits, and API
features change over time. They must be loaded through configuration and
capability discovery where possible, not permanently assumed in UI code.

### Provider Requirements

- A shared `AiProvider` contract must expose model discovery, chat completion,
  streaming, cancellation, structured output support, and error mapping.
- Provider-specific request and response objects must remain inside adapters.
- The user must select a provider and model explicitly.
- A connection test must be available before saving a configuration.
- Custom base URLs must require HTTPS, except loopback and local-network
  endpoints explicitly approved by the user.
- Timeouts, retry policy, maximum output, temperature, and context limits must
  have safe defaults.
- Unsupported capabilities must be disabled in the UI instead of failing after
  a request starts.
- Provider failures must use normalized error categories such as
  authentication, quota, rate limit, network, timeout, context limit, invalid
  response, and server error.

## 7. Core User Experience

The four integrations use a common agent panel with context-specific tools.

### Shared Agent Panel

The panel must contain:

- Conversation history for the current editor session.
- Multiline instruction text box.
- Send and Stop controls.
- Provider and model indicator.
- Context summary showing which files, blocks, layout, or errors will be sent.
- Attach Context control for adding selected project items.
- Plan, Changes, and Result states.
- Diff or structured preview before applying changes.
- Apply, Apply Selected, Regenerate, Reject, Copy, and Undo actions.
- Clear explanation of network use and estimated context size.
- Error display with retry guidance.
- Quick actions for Explain, Generate, Fix Error, and Refactor where relevant.

The text box should support prompt history, multiline input, and example
prompts relevant to the active editor. The panel must remain usable on phones
and expand into a side panel on larger screens.

## 8. Integration Requirements

### 8.1 Code Editor Agent

Add an AI button and agent text box to the code editor.

Primary use cases:

- Generate a method, class, listener, or code snippet.
- Explain selected code.
- Complete code at the cursor.
- Refactor selected code.
- Find likely bugs and propose fixes.
- Add imports and resolve straightforward compiler errors.
- Generate Java first, while preserving Kotlin where a project already uses it.

Required context:

- Current file path and language.
- Selected code or bounded lines around the cursor.
- Relevant activity, component, event, manifest, and dependency metadata.
- Current diagnostics when available.

Safety rules:

- Never replace the complete file when a smaller edit is possible.
- Show a textual diff before applying changes.
- Preserve encoding and line endings.
- Validate syntax after applying.
- Revert the complete transaction if parsing or persistence fails.
- Generated build files must not be edited unless the user explicitly includes
  them in scope.

### 8.2 Block Editor Agent

Add an AI button and agent text box to the Block Editor.

Primary use cases:

- Create a block sequence from natural language.
- Add or update events, variables, lists, components, and More Blocks.
- Explain selected blocks.
- Simplify or reorganize a selected block sequence.
- Convert a supported code concept into blocks.
- Create a reusable palette or custom block definition only after explicit
  confirmation.

For risky or unsupported automatic insertion, the agent must return a manual
block recipe first. For example, a request to open `HomeActivity` after 2.5
seconds should identify the Timer, Intent target, Start Activity, and Finish
Activity operations before proposing structured insertion.

Required context:

- Active screen, event, component, and block workspace.
- Selected block IDs and their structured model.
- Available block opcodes, argument types, return types, and palette metadata.
- Project variables, lists, More Blocks, and component instances.

Safety rules:

- The model must return a structured block plan, not serialized internal files.
- Block creation must use the existing block registry and model APIs.
- Every opcode and argument must be validated before insertion.
- Unknown blocks must be rejected or presented as an unsupported requirement.
- Generated block coordinates must avoid overlap and preserve readable order.
- The complete block operation must be a single undoable transaction.

### 8.3 Build Error Agent

Add an AI Fix button and agent text box to the build error screen.

Primary use cases:

- Explain compiler, resource, manifest, dependency, signing, and packaging
  errors in user-friendly language.
- Identify the likely root cause and affected project items.
- Propose one or more ranked fixes.
- Apply a reviewed fix and rerun validation.

Required context:

- Build stage and normalized diagnostic.
- Relevant log excerpt with repeated stack frames removed.
- File name and line number where available.
- Bounded generated Java, XML, or manifest snippets referenced by the error.
- Source or resource locations referenced by the diagnostic.
- Project SDK, dependencies, manifest, and build configuration.
- Recent user or agent changes when available.

Safety rules:

- Redact API keys, tokens, passwords, signing data, local paths, and personal
  identifiers before sending logs.
- Never upload complete build logs by default.
- Never change signing credentials.
- Never lower SDK or security settings merely to suppress an error without
  explaining the impact.
- Apply fixes only after preview and user approval.
- Run the narrowest validation first, then offer a complete build.
- If the fix fails, preserve the original diagnostic and support one-tap undo.

### 8.4 View Editor Agent

Add an AI button and agent text box to the View Editor.

Primary use cases:

- Create a screen from a natural-language description.
- Add, remove, or configure widgets.
- Improve spacing, alignment, hierarchy, accessibility, and responsive layout.
- Apply project theme tokens and consistent Material styling.
- Connect views to existing components or events through a reviewed plan.
- Explain why a layout behaves incorrectly.

Required context:

- Active layout's structured `ViewBean` hierarchy.
- Widget registry and valid parent-child rules.
- Current theme, colors, dimensions, strings, drawables, and screen metadata.
- Device preview dimensions and orientation.
- Selected widgets and their properties.

Safety rules:

- Changes must target the editor model first; generated XML is an output.
- Validate IDs, parents, cycles, supported properties, and resource references.
- Never silently delete a view containing event or block references.
- Show a hierarchy/property preview and list destructive operations separately.
- Preserve a snapshot before applying.
- Run layout export validation after applying.
- The complete layout operation must be undoable.

## 9. Agent Operating Model

Every modifying request must follow this state machine:

1. `Idle`: Waiting for user input.
2. `CollectingContext`: Resolve only the minimum required context.
3. `Planning`: Model produces a human-readable plan and requested tools.
4. `AwaitingApproval`: User reviews scope and potentially destructive actions.
5. `Generating`: Model produces structured edits.
6. `Validating`: Local validators check schema, references, and syntax.
7. `Previewing`: UI displays diff or structured changes.
8. `Applying`: Changes are committed as one local transaction.
9. `Verifying`: Run editor-specific checks.
10. `Completed`, `Failed`, or `Canceled`.

The agent must not skip approval for project-modifying operations. Read-only
actions such as explaining selected code may run without an apply approval.

## 10. Proposed Architecture

The implementation should be modular and primarily live under:

```text
app/src/main/java/pro/sketchware/ai/
  agent/
  context/
  model/
  provider/
  security/
  session/
  tools/
  transaction/
  ui/
  validation/
```

### Core Components

| Component | Responsibility |
| --- | --- |
| `AiProvider` | Provider-neutral request, streaming, model, and cancellation API |
| `AiProviderRegistry` | Registers built-in and future provider adapters |
| `AiConfigurationStore` | Stores non-secret preferences and provider selection |
| `AiSecretStore` | Stores API credentials using Android Keystore-backed encryption |
| `AiAgentOrchestrator` | Runs state machine, tool requests, approvals, and retries |
| `AiContextBuilder` | Builds minimal context for the active editor |
| `AiContextRedactor` | Removes secrets and sensitive device/project information |
| `AiToolRegistry` | Exposes allow-listed read and write capabilities |
| `AiChangeSet` | Provider-neutral structured proposal |
| `AiTransactionManager` | Snapshot, atomic apply, rollback, and undo |
| `AiValidator` | Dispatches code, block, layout, and build validations |
| `AiSessionStore` | Stores bounded local conversation and action history |
| `AiUsageMeter` | Shows request size, latency, and optional estimated cost |

Editor Activities and Fragments must communicate with the orchestrator through
a stable facade. They must not call OpenAI, Claude, Gemini, DeepSeek, Qwen, or
local HTTP APIs directly.

## 11. Tool And Change Schemas

Model output must use versioned structured schemas. Free-form model text must
never be executed as a command.

Initial read-only tools:

- `read_current_code`
- `read_selected_code`
- `read_project_summary`
- `read_block_workspace`
- `read_layout_hierarchy`
- `read_build_diagnostics`
- `search_project_symbols`
- `list_available_widgets`
- `list_available_blocks`

Initial modifying tools:

- `propose_code_patch`
- `propose_block_changes`
- `propose_layout_changes`
- `propose_resource_changes`
- `propose_project_setting_changes`

Each proposed operation must include:

- Schema version.
- Target editor and project ID.
- Preconditions or expected revision.
- Ordered operations.
- Human-readable summary.
- Risk level.
- Destructive-operation flag.
- Validation requirements.
- Expected affected files or model records.

No initial tool may execute shell commands, install arbitrary packages, access
other apps, read unrelated device storage, publish builds, use Git credentials,
or make provider requests outside the configured endpoint.

## 12. Context Management

- Default to the active selection and only nearby relevant context.
- Ask before attaching additional files, full layouts, or complete build logs.
- Show a context manifest before sending.
- Enforce provider/model context limits locally.
- Summarize long histories and allow the user to start a clean session.
- Do not send binary assets; send names, dimensions, and metadata unless a
  future multimodal action is explicitly approved.
- Do not include `.gradle`, build outputs, keystores, credentials, backups, or
  unrelated projects.
- Mark generated data with its source and project revision to detect stale
  proposals.
- Before sending any project context to a cloud provider, show:

> This will send selected project context to the AI provider.

Actions:

- `Continue`
- `Cancel`

## 13. Security And Privacy

### Credential Storage

- API keys must be encrypted using Android Keystore-backed storage.
- API keys must never be stored in plain SharedPreferences, source files,
  exported project archives, crash reports, or analytics.
- The UI may display only a masked key.
- Users must be able to delete a provider configuration and its credential.
- Local endpoint tokens receive the same protection as cloud API keys.
- Consumer ChatGPT session cookies, browser tokens, or account credentials must
  never be requested, imported, or stored.

### Network Controls

- AI functionality is opt-in and disabled by default.
- The first cloud request must display a privacy notice.
- Every request must go only to the selected provider endpoint.
- Redirects to an unapproved host must be rejected.
- TLS certificate validation must not be disabled.
- Local cleartext HTTP may only be allowed for loopback or explicitly approved
  LAN endpoints and must show a warning.
- A backend proxy configuration must authenticate the app without exposing the
  proxy's upstream provider credential to the device.

### Prompt Injection Defense

- Project text and build logs are untrusted data, not system instructions.
- Provider responses cannot change tool permissions.
- Tool calls must be checked against the active editor, approved scope, schema,
  and current project revision.
- Destructive changes require an additional explicit confirmation.
- Hidden instructions found in project files must be displayed as content and
  never treated as authority.

## 14. Settings

Add a future settings section:

```text
Settings
└── AI Agent
    ├── Enable AI Agent
    ├── Provider
    ├── API Key / Access Token
    ├── Base URL
    ├── Model
    ├── Test Connection
    ├── Remove Credential
    ├── Default Context Policy
    ├── Streaming Responses
    ├── Request Timeout
    ├── Conversation Storage
    ├── Clear AI History
    └── Privacy And Data Usage
```

Provider presets must remain editable where the provider supports custom
gateways. Model selection should allow both discovered models and a manual
model ID.

## 15. Offline And Local LLM Requirements

- Support an OpenAI-compatible chat endpoint as the initial local protocol.
- Allow loopback and configurable LAN base URLs.
- Do not require an internet connectivity check for local providers.
- Show endpoint reachability and model capability results.
- Support streaming when the endpoint exposes it.
- Provide a low-context mode suitable for mobile or smaller local models.
- Clearly report when a local model cannot reliably produce the required
  structured schema.
- Future support may add on-device inference, but it is outside the first
  milestone due to APK size, memory, thermal, and model licensing concerns.

## 16. Reliability Requirements

- Agent cancellation must stop network streaming and prevent unapplied changes.
- Network requests must not block the main thread.
- Activity recreation must restore the visible session state without replaying
  a modifying operation.
- Duplicate responses must not apply the same transaction twice.
- Applying a stale proposal must be blocked if the project revision changed.
- An automatic snapshot must be created before every change transaction.
- Failed apply or validation must roll back completely.
- Undo must survive returning between editor tabs during the active project
  session.
- AI unavailability must never block normal Sketchware editing or building.

## 17. Accessibility And Localization

- All AI UI strings must use Android string resources.
- English is the fallback language.
- The agent panel must support existing application locales and RTL layout.
- Streaming output and errors must be usable with screen readers.
- Controls must have content descriptions and adequate touch targets.
- Provider and model names remain proper nouns; actions and diagnostics are
  localized.
- AI-generated explanations should use the current application language when
  the selected model supports it.

## 18. Observability

Local diagnostics may record:

- Provider type, not API key.
- Model ID.
- Request duration.
- Approximate input and output size.
- Agent state transitions.
- Tool names and validation outcomes.
- Normalized error category.

Diagnostics must not record prompts, generated source, project names, user
files, credentials, complete endpoints containing secrets, or response bodies
unless the user explicitly exports a redacted debug report.

## 19. Success Metrics

The initial controlled test should measure:

- Crash-free AI sessions.
- Percentage of responses producing schema-valid changes.
- Percentage of applied changes passing local validation.
- Successful undo and rollback rate.
- Build success rate after AI-generated fixes.
- Median response latency by provider category.
- User acceptance versus rejection of proposed changes.
- Number of secret-redaction failures, which must remain zero.

Metrics are local by default. Any remote telemetry requires separate consent
and a documented privacy policy.

## 20. Acceptance Criteria

The AI Agent foundation is ready for an experimental release when:

- All six provider categories can be configured through the common interface.
- Provider credentials can be added, tested, replaced, and removed without
  consumer-account login.
- At least one cloud provider and one local OpenAI-compatible provider pass the
  provider contract test suite.
- Code, Block, Build Error, and View editors display the shared agent panel.
- Read-only explanation works without project modification.
- Modifying operations always present a preview and require approval.
- Code edits are syntax-validated and undoable.
- Block edits reject unknown opcodes and are undoable.
- View edits pass layout export validation and are undoable.
- Build fixes redact secrets and preserve the original diagnostic.
- Credentials survive restart securely and are excluded from backup/export.
- Rotation, process recreation, timeout, cancellation, offline mode, and
  provider errors do not crash the application.
- Disabling AI removes its editor actions without affecting existing features.
- Existing blocks still generate equivalent Java/Kotlin when AI is disabled.
- APK generation passes the existing RC1 builder regression suite.

## 21. Testing Strategy

### Unit Tests

- Provider request and error mapping.
- Secret redaction.
- Context limit enforcement.
- Change schema parsing and validation.
- Project revision conflict detection.
- Transaction apply, rollback, and idempotency.
- Block opcode and layout hierarchy validation.

### Integration Tests

- Mock streaming and cancellation for every provider adapter.
- API authentication and quota errors.
- Code diff apply and undo.
- Structured block insertion and undo.
- Layout hierarchy update and XML export.
- Build diagnostic collection and redaction.
- Activity recreation during planning, streaming, preview, and apply.

### Device Matrix

- Android 8 through Android 16 where supported by the application.
- Low-memory phone.
- Modern Android 16 device, including the reported HONOR test device.
- Phone portrait and landscape.
- Tablet or large-screen emulator.
- RTL language.
- Offline mode and unstable network.
- Local LLM over loopback and LAN.

## 22. RC1 Internal Delivery Phases

### Phase 0 - RC1 Editor Foundation

- Upgrade Code Viewer to Code Editor.
- Add generated-code read-only protection.
- Add Code and Block conflict handling.
- Improve Block Editor categories, search, favorites, recent items, dragging,
  zoom, selection, and validation UI.
- Prove that existing projects and generated APKs remain equivalent.

### Phase A - Foundation

- Provider contract and registry.
- Secure configuration storage.
- Shared session state machine.
- Context redaction.
- Versioned change schemas.
- Transaction, rollback, and undo foundation.
- Experimental feature flag.

### Phase B - Code Editor Pilot

- Shared agent panel.
- Selected-code explain and code patch proposal.
- Preview, apply, syntax validation, and undo.
- One cloud adapter plus local OpenAI-compatible adapter.

### Phase C - Build Error Assistant

- Diagnostic normalization and redaction.
- Root-cause explanation.
- Reviewed patch proposal.
- Targeted validation and rebuild action.

### Phase D - View Editor

- Structured layout context.
- Hierarchy and property operations.
- Layout validation, preview, and undo.

### Phase E - Block Editor

- Block schema and registry tools.
- Natural-language block planning.
- Validated insertion, arrangement, and undo.
- Custom palette creation only after the standard block workflow is stable.

### Phase F - Provider Expansion And Hardening

- Claude, Gemini, DeepSeek, and Qwen adapters.
- Capability discovery and model management.
- Full device matrix, localization, accessibility, performance, and security
  review.

## 23. Release Gates

The feature must remain experimental until:

- RC1 core editor and build stability is confirmed independently of AI.
- Security review finds no plaintext credential or secret leakage.
- All modifying tools enforce approval and rollback.
- No critical or high-severity issue remains open.
- Crash-free and validation targets are met on the device matrix.
- Provider terms, privacy requirements, model licensing, and endpoint branding
  have been reviewed before public distribution.
- The UI and documentation never describe an API-key configuration as direct
  ChatGPT login.
- APK, AAB, logs, API configuration, and test results remain local.
- No Git commit, push, tag, GitHub release, Play Store upload, or public
  announcement occurs without a separate explicit instruction.

## 24. Open Product Decisions

- Whether conversation history is stored per project or per editor session.
- Maximum local history retention.
- Whether estimated provider cost should be shown before each request.
- Which cloud provider is used for the first pilot.
- Whether users may define arbitrary OpenAI-compatible cloud gateways.
- Whether image input will be supported for screenshot-to-layout workflows.
- How generated custom palettes are versioned and shared.
- Whether future extension tools use the same permission and transaction model.
- Whether the production default should be bring-your-own-key, a backend proxy,
  or both.

## 25. Recommended First Milestone

Within RC1, first preserve and extend the existing Code Editor and Block Editor
foundations. Then enable the Code Editor AI pilot using all configured provider
adapters, beginning validation with one cloud provider and one local
OpenAI-compatible endpoint. Implement secure key storage, context redaction,
preview, explicit apply, validation, and undo before allowing broader edits.

This path proves the safety-critical foundation with the smallest model surface.
Block and View editors should follow only after their structured model
transactions are covered by tests. Build Error integration may reuse the same
code patch pipeline but must add dedicated log redaction first.

## 26. Local Test Build Policy

RC1 test artifacts must be stored locally only.

Suggested device output:

```text
/storage/emulated/0/SketchwareComplete/builds/
```

Suggested APK name:

```text
SketchwareComplete-RC1-debug-yyyyMMdd-HHmm.apk
```

Every local test build should preserve:

- APK path.
- Build log.
- Layout validation log.
- Error log when the build fails.

The desktop development build may also copy the APK to a local `outputs`
directory outside version control. Test artifacts must not be committed or
uploaded automatically.
