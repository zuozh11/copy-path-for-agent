# Copy Path for Agent

A JetBrains plugin that copies file and folder references in configurable formats for coding agents.

It is forked from [Copy Path for Claude Code](https://github.com/inwpasit619/copy-path-for-claude-code) and keeps the original editor, project tree, multi-caret, multi-file, shortcut, and notification workflow while replacing the fixed Claude Code formatter with switchable profiles and templates.

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

The `Codex App` profile uses local `file://` URIs, which match the agent client file protocol more directly than Markdown links typed into a prompt.

For Codex CLI prompt mentions, a useful custom template is:

```text
@{relativePath}:{startLine}
```

## Template Variables

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

## Build

```bash
./gradlew test buildPlugin
```

The plugin ZIP is generated under `build/distributions/`.

## Compatibility

JetBrains IDEs 2024.1+.
