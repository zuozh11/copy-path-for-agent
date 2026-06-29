# Changelog

## 1.1.3

- Changed the `Codex App` built-in profile to output Markdown file links that render as blue clickable references in Codex.
- Kept the link label to the file name and put the selected line or line range in the link target.

## 1.1.2

- Fixed a crash when switching profiles from the status bar popup.
- Added optional template sections, for example `{{#startLine}}#L{startLine}{{/startLine}}`.
- Updated built-in profile templates so whole-file copies do not leave a dangling `#L`.

## 1.1.1

- Added switchable copy profiles.
- Added built-in `Claude Code` and `Codex App` profiles.
- Added status bar quick switching with the `Copy Profile` label.
- Added Simplified Chinese UI strings.
- Replaced preset-specific template variables with a minimal generic variable set.
- Changed the Codex App default output to local `file://` URIs with line fragments.
- Removed the preset selector; each profile now owns its template and multi-reference separator.

## 1.0.0

- Renamed the fork to Copy Path for Agent.
- Added configurable templates for agent-friendly copied references.
