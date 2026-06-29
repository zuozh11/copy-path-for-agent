# Copy Path for Agent

A JetBrains plugin that copies file and folder references in configurable formats for coding agents.

It is forked from [Copy Path for Claude Code](https://github.com/inwpasit619/copy-path-for-claude-code) and keeps the original editor, project tree, multi-caret, multi-file, shortcut, and notification workflow while replacing the fixed Claude Code formatter with switchable profiles and templates.

Current version: `1.1.1`.

## Features

- Copy references from the editor, project tree, or the `Alt+C` / `Option+C` shortcut.
- Include line numbers for editor selections and multi-caret positions.
- Copy multiple selected files or folders.
- Switch profiles from the status bar.
- Keep a separate template and multi-reference separator per profile.
- Use the built-in `Claude Code` and `Codex App` profiles.
- Customize the copied text with a small set of generic template variables.
- Configure copy notifications and notification duration.

## Built-in Profiles

| Profile | Template | Example |
|---|---|---|
| `Claude Code` | `@{relativePath}#L{lineRange}` | `@src/utils/auth.ts#L42-68` |
| `Codex App` | `{fileUri}#L{startLine}` | `file:///Users/me/project/src/utils/auth.ts#L42` |

The `Codex App` profile uses local `file://` URIs because Codex can open those file targets reliably. Markdown links such as `[App.kt:42](/absolute/path/App.kt:42)` may look tidy in a prompt, but they are not dependable for clickable local preview in Codex.

For Codex CLI prompt mentions, a useful custom template is:

```text
@{relativePath}:{startLine}
```

## Template Variables

Templates are plain string replacement. There is no conditional syntax, and literal spaces, punctuation, quotes, slashes, and `#L` prefixes should be typed directly into the template.

| Variable | Description |
|---|---|
| `{relativePath}` | Project-relative path exactly as provided by the IDE. |
| `{absolutePath}` | Absolute local path exactly as provided by the IDE. |
| `{relativeDirectory}` | Project-relative parent directory, or empty at project root. |
| `{absoluteDirectory}` | Absolute parent directory, or empty when unavailable. |
| `{fileName}` | The file or folder name only. |
| `{startLine}` | The 1-based start line, or empty when no line is selected. |
| `{endLine}` | The 1-based end line, or empty when no line is selected. |
| `{lineRange}` | The selected line or line range, such as `5` or `5-10`. |
| `{fileUri}` | Local file URI, such as `file:///Users/me/project/src/App.kt`. |

When there is no editor caret or selection line, line variables are empty. If you often copy whole files from the Project tool window, create a separate profile without `#L{lineRange}` or `#L{startLine}`.

## Usage

### Editor Context Menu

Right-click in any editor and choose **Copy Path for Agent**.

When text is selected, the output includes the selected line range. With multiple carets, each caret generates a separate reference.

### Project Tree Context Menu

Right-click files or folders in the Project tool window and choose **Copy Path for Agent**.

### Keyboard Shortcut

Press <kbd>Alt+C</kbd> or <kbd>Option+C</kbd> on macOS.

## Settings

Open **Settings -> Tools -> Copy Path for Agent**.

| Setting | Description | Default |
|---|---|---|
| Profile | Active copy configuration | `Claude Code` |
| Template | Template used by the current profile | `@{relativePath}#L{lineRange}` |
| Multiple references separator | Separator for multi-file or multi-caret copies in the current profile | Space |
| Show notification after copy | Toggle balloon notification | Enabled |
| Notification duration | How long notifications stay visible | 3 seconds |

You can add, rename, and delete profiles in settings. The active profile can also be switched directly from the IDE status bar.

## Local Install

Build the plugin:

```bash
./gradlew test buildPlugin
```

Install the generated ZIP into IntelliJ IDEA from **Settings -> Plugins -> Install Plugin from Disk...**.

For local development on macOS with IntelliJ IDEA 2026.1, the plugin can also be unpacked into:

```text
~/Library/Application Support/JetBrains/IntelliJIdea2026.1/plugins/
```

Restart the IDE after replacing the plugin files.

## Build

```bash
./gradlew test buildPlugin
```

The plugin ZIP is generated under `build/distributions/`.

## Compatibility

JetBrains IDEs 2024.1+.
