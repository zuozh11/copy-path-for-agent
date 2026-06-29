# Copy Path for Agent

A JetBrains plugin that copies file and folder references in configurable formats for coding agents.

It is forked from [Copy Path for Claude Code](https://github.com/inwpasit619/copy-path-for-claude-code) and keeps the original editor, project tree, multi-caret, multi-file, shortcut, and notification workflow while replacing the fixed Claude Code formatter with switchable profiles and templates.

Current version: `1.1.4`.

[Install from JetBrains Marketplace](https://plugins.jetbrains.com/plugin/32535-copy-path-for-agent)

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
| `Claude Code` | `@{relativePath}{{#lineRange}}#L{lineRange}{{/lineRange}}` | `@src/utils/auth.ts#L42-68` |
| `Codex App` | `[{fileName}]({absolutePath}{{#lineRange}}:{lineRange}{{/lineRange}})` | `[auth.ts](/Users/me/project/src/utils/auth.ts:42-68)` |

The `Codex App` profile uses Markdown file links so Codex renders a blue clickable file reference. For selected ranges, the link target includes the full line range.

For Codex CLI prompt mentions, a useful custom template is:

```text
@{relativePath}:{startLine}
```

## Template Variables

Templates support plain variable replacement and one small optional-section syntax:

```text
{{#variable}}text{{/variable}}
```

The text inside an optional section is output only when the named variable is not empty. Literal spaces, punctuation, quotes, slashes, and `#L` prefixes should be typed directly into the template.

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

When there is no editor caret or selection line, line variables are empty. The built-in profiles wrap line markers and line targets in optional sections, so copying a whole file does not leave a dangling line suffix.

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
| Template | Template used by the current profile | `@{relativePath}{{#lineRange}}#L{lineRange}{{/lineRange}}` |
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
