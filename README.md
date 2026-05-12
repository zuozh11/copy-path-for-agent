# Copy Path for Claude Code

A JetBrains plugin that copies file and folder path references in the [Claude Code](https://docs.anthropic.com/en/docs/claude-code) `@`-mention format — ready to paste into Claude Code conversations.

## Features

- Copy file or folder references in `@path/to/file#Lx-y` format
- Smart line detection based on editor text selection
- Multi-cursor support — each caret generates a separate reference with line numbers
- Multi-file selection support — select multiple files/folders and copy all paths at once
- Paths containing spaces are automatically wrapped in double quotes (e.g. `@"my file.ts"`)
- Directory paths include a trailing slash to distinguish folders from files (e.g. `@src/`)
- Optional trailing space after copied references for easy pasting
- Configurable separator for multiple references (space or newline)
- Works from editor context menu, project tree context menu, and keyboard shortcut
- Configurable balloon notification with adjustable duration
- Compatible with all JetBrains IDEs

## Reference Format

| Context | Output |
|---|---|
| File (no selection) | `@src/utils/auth.ts` |
| File (single line selected) | `@src/utils/auth.ts#L42` |
| File (multiple lines selected) | `@src/utils/auth.ts#L42-68` |
| File with spaces in path | `@"my file.ts"` |
| File with spaces + line range | `@"my file.ts#L42-68"` |
| Folder | `@src/components/` |
| Folder with spaces in path | `@"my components/"` |
| Multi-cursor (with selections) | `@src/utils/auth.ts#L5 @src/utils/auth.ts#L20-25` |
| Multi-cursor (carets only) | `@src/utils/auth.ts#L5 @src/utils/auth.ts#L42` |
| Multiple files selected | `@src/Button.tsx @src/Input.tsx` (separator configurable) |

## Usage

### Editor Context Menu

Right-click in any editor → **Copy Path for Claude Code**

The output includes line numbers based on your current text selection. If nothing is selected, only the file path is copied.

### Project Tree Context Menu

Right-click any file or folder in the Project tool window → **Copy Path for Claude Code**

Copies the relative path without line numbers.

### Keyboard Shortcut

Press <kbd>Alt+C</kbd> (<kbd>⌥C</kbd> on macOS).

Works in both the editor and project tree — the output adapts automatically based on focus and selection.

## Examples

```
# Select lines 10–25 in src/components/Button.tsx, then press ⌥C
@src/components/Button.tsx#L10-25

# Place caret without selecting text, press ⌥C
@src/utils/auth.ts

# Right-click a file in the project tree
@package.json

# Right-click a folder in the project tree
@src/components/

# Right-click a file with spaces in its name
@"my file.ts"

# Right-click a folder with spaces in its name
@"my components/"

# Select lines 5–10 in a file with spaces, press ⌥C
@"my file.ts#L5-10"

# Place multiple cursors (Alt+Click) on lines 5, 20, 42, press ⌥C
@src/utils/auth.ts#L5 @src/utils/auth.ts#L20 @src/utils/auth.ts#L42

# Select text at multiple cursors (e.g. lines 5–10 and 30–35), press ⌥C
@src/utils/auth.ts#L5-10 @src/utils/auth.ts#L30-35

# Select multiple files in the project tree, then press ⌥C
@src/components/Button.tsx @src/components/Input.tsx @src/utils/auth.ts
```

## Settings

**Settings → Tools → Copy Path for Claude Code**

| Setting | Description | Default |
|---|---|---|
| Append trailing space | Add a space after the copied reference for easy pasting | Enabled |
| Multiple references separator | Separator between references when copying multiple files or multi-cursor selections: Space or Newline | Space |
| Show notification after copy | Toggle balloon notification on/off | Enabled |
| Notification duration (seconds) | How long the notification stays visible (1–30s) | 3 seconds |

## Compatibility

Works with **all JetBrains IDEs** (2024.1+):

IntelliJ IDEA · WebStorm · PyCharm · GoLand · PhpStorm · Rider · CLion · RustRover · DataGrip · Android Studio

## Additional Info

- **Issue Tracker**: [GitHub Issues](https://github.com/inwpasit619/copy-path-for-claude-code/issues)
- **Documentation**: [GitHub Repository](https://github.com/inwpasit619/copy-path-for-claude-code)
- **License**: [MIT](https://github.com/inwpasit619/copy-path-for-claude-code/blob/main/LICENSE)
