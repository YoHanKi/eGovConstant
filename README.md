# eGovConstant

![Build](https://github.com/YoHanKi/eGovConstant/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.
- [ ] Configure the [CODECOV_TOKEN](https://docs.codecov.com/docs/quick-start) secret for automated test coverage reports on PRs

<!-- Plugin description -->
A production-grade IntelliJ IDEA plugin that recommends standardized variable names (fields, parameters, locals, constants) based on Korean government/public standard dictionaries.

Key features:
- **Bundled Canonical Dataset**: Works out-of-the-box with a default, authoritative dataset. No XLSX required for normal operation.
- **XLSX Importer**: Import additional data into the canonical store with validation, merging, and deduplication.
- Unified model across Terms, Words, and Domains with synonyms and forbidden words.
- Fast search with ranking: exact > prefix > contains > fuzzy, synonym-boosted; filters by type and domain.
- Variable name generation with consistent acronym handling and suffix rules; outputs camelCase, snake_case, PascalCase.
- Tool Window "eGovConstant" to manage datasets, search, copy or insert names; shows load status and statistics.
- Code Completion contributor suggests names during declarations (min 2 chars).
- Intention: "Rename to eGovConstant recommended name" with chooser when multiple candidates exist.
- Persistent Canonical Store for user-added or imported entries.

Usage:
1) Open the eGovConstant tool window.
2) The plugin is ready with the default dataset.
3) To import additional data:
   - Select an XLSX file and click "Import XLSX".
   - The plugin validates the headers (ignoring column order, matching by name).
   - New entries are added; existing entries are merged (non-empty fields are preserved, sets like synonyms are unioned).
4) Use "Reset to Default" to clear all imported/user data and return to the baseline dataset.
5) Use the search box to find terms and copy/insert camelCase names.
6) In code, use completion to get suggestions, or the intention action to rename to recommended names.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "eGovConstant"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/YoHanKi/eGovConstant/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
