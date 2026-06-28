# Copy Path for Agent

A JetBrains plugin that copies file and folder references in configurable formats for coding agents.

It is forked from [Copy Path for Claude Code](https://github.com/inwpasit619/copy-path-for-claude-code) and keeps the original editor, project tree, multi-caret, multi-file, separator, shortcut, and notification workflow while replacing the fixed Claude Code formatter with templates.

## Features

- Copy references from the editor, project tree, or the `Alt+C` / `Option+C` shortcut.
- Include line numbers for editor selections and multi-caret positions.
- Copy multiple selected files or folders.
- Choose a separator for multiple references: space or newline.
- Use the built-in `claudecode` and `codex` presets.
- Customize the copied text with template variables.
- Add trailing spaces or directory slashes from the template instead of separate hidden toggles.
- Configure copy notifications and notification duration.

## Presets

| Preset | Template | Example |
|---|---|---|
| `claudecode` | `{claudeReference}` | `@src/utils/auth.ts#L42-68` |
| `codex` | `[{codexLabel}]({codexTarget})` | `[auth.ts:42-68](/Users/me/project/src/utils/auth.ts:42)` |

The `codex` preset is designed for Codex desktop/app-style Markdown file links. For Codex CLI prompt mentions, a useful custom template is:

```text
@{relativePath} {lineText}
```

## Template Variables

| Variable | Description |
|---|---|
| `{relativePath}` | Project-relative path exactly as provided by the IDE. |
| `{absolutePath}` | Absolute local path exactly as provided by the IDE. |
| `{relativePathWithDirectorySlash}` | Project-relative path plus `{directorySlash}`. |
| `{absolutePathWithDirectorySlash}` | Absolute local path plus `{directorySlash}`. |
| `{fileName}` | The file or folder name only. |
| `{isDirectory}` | `true` for folders, `false` for files. |
| `{directorySlash}` | `/` for folders, empty for files. |
| `{startLine}` | The 1-based start line, or empty when no line is selected. |
| `{endLine}` | The 1-based end line, or empty when no line is selected. |
| `{lineRange}` | The selected line or line range, such as `5` or `5-10`. |
| `{lineText}` | Human-readable line text, such as `line 5` or `lines 5-10`. |
| `{claudeLineSuffix}` | Claude Code line suffix, such as `#L5` or `#L5-10`. |
| `{claudeReference}` | Complete Claude Code reference, such as `@src/App.kt#L5`. |
| `{claudeReferenceWithDirectorySlash}` | Claude Code reference using `{relativePathWithDirectorySlash}`. |
| `{codexLabel}` | Markdown link label for Codex, such as `App.kt:5-10`. |
| `{codexTarget}` | Markdown link target for Codex, using the absolute path and start line. |
| `{codexTargetWithDirectorySlash}` | Markdown link target for Codex using `{absolutePathWithDirectorySlash}`. |
| `{space}` | A literal space, useful when you want a trailing space. |

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
| Preset | Fill the template with a built-in preset | `claudecode` |
| Template | Template used for copied references | `{claudeReference}` |
| Multiple references separator | Separator for multi-file or multi-caret copies | Space |
| Show notification after copy | Toggle balloon notification | Enabled |
| Notification duration | How long notifications stay visible | 3 seconds |

## Build

```bash
./gradlew test buildPlugin
```

The plugin ZIP is generated under `build/distributions/`.

## Compatibility

JetBrains IDEs 2024.1+.
