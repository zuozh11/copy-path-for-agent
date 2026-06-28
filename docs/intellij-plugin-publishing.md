# Publishing to JetBrains Marketplace

This project is ready to publish as a JetBrains plugin after you create a JetBrains Marketplace account and plugin entry.

## 1. Verify metadata

Check these files before each release:

- `gradle.properties`
  - `pluginVersion`
  - `platformVersion`
- `src/main/resources/META-INF/plugin.xml`
  - plugin `id`
  - plugin `name`
  - `vendor`
  - `description`
  - `change-notes`

## 2. Build and test

```bash
./gradlew clean test buildPlugin
```

The uploadable ZIP is created in:

```text
build/distributions/copy-path-for-agent-<version>.zip
```

## 3. Create a JetBrains Marketplace token

1. Open [JetBrains Marketplace vendor page](https://plugins.jetbrains.com/vendor).
2. Create or select your vendor profile.
3. Open the account token page and create a permanent token.
4. Keep the token private. Do not commit it to the repository.

## 4. Publish manually

1. Open [Upload Plugin](https://plugins.jetbrains.com/plugin/add).
2. Upload the ZIP from `build/distributions/`.
3. Fill in the description, screenshots, license, and repository URL.
4. Submit for JetBrains review.

## 5. Publish with Gradle

Export your token in the shell:

```bash
export PUBLISH_TOKEN="your-jetbrains-marketplace-token"
```

Publish:

```bash
./gradlew publishPlugin
```

This project does not store the token in Gradle files. Keep using an environment variable or your local private Gradle properties.

## 6. Release checklist

- `./gradlew clean test buildPlugin` passes.
- `README.md` matches the release behavior.
- `pluginVersion` is bumped.
- `change-notes` in `plugin.xml` describe the release.
- Git tag exists for the version.
- GitHub Release includes the plugin ZIP from `build/distributions/`.
- JetBrains Marketplace upload succeeds or is submitted for review.
