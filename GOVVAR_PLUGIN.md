# GovVar IntelliJ Plugin

## Overview

The GovVar (Government Variable) plugin is an IntelliJ IDEA tool window that helps developers search and discover government/public-standard variable names for their projects.

## Features

- **Tool Window "GovVar"**: A dedicated tool window accessible from the right sidebar
- **Keyword Search**: Enter a keyword to search for matching variable names
- **Results Table**: Display search results in a structured table with:
  - Variable Name
  - Description
  - Data Type
  - Standard Reference
- **Clean UI/Service Separation**: Clear separation between UI layer and service layer

## Architecture

### Service Layer
- **GovVarService**: Project-level service that handles variable name searches
  - Location: `src/main/kotlin/org/jetbrains/plugins/template/services/GovVarService.kt`
  - Provides `searchGovVariables(keyword: String)` method
  - Returns `List<GovVariable>` with matching results

### UI Layer
- **GovVarToolWindowFactory**: Factory class that creates the tool window
  - Location: `src/main/kotlin/org/jetbrains/plugins/template/toolWindow/GovVarToolWindowFactory.kt`
  - Creates UI components: search field, button, results table
  - Handles user interactions and updates the display

### Configuration
- **plugin.xml**: Registers the GovVar tool window
  - Tool window ID: "GovVar"
  - Anchor: Right sidebar
  - Icon: Information icon

## Current Implementation

The plugin currently uses **mock data** for demonstration purposes. The mock data includes common government/public-standard variable names such as:
- User identifiers (userId, userName, userEmail, etc.)
- Organization identifiers (organizationId, organizationName, etc.)
- Application data (applicationId, applicationStatus, etc.)
- Document information (documentId, documentType, etc.)
- Citizen information (citizenId, citizenName, taxId, etc.)

## Future Enhancements (TODOs)

The following areas are marked with TODOs for future development:

1. **Data Integration**
   - Replace mock data with actual government/public-standard data sources
   - Integrate with external APIs or databases
   - Support multiple standard specifications

2. **Caching**
   - Add caching mechanism for frequently searched terms
   - Improve search performance

3. **Advanced Features**
   - Filter by data type
   - Filter by standard version
   - Export search results
   - Copy variable name to clipboard
   - Insert variable into code editor

## Requirements

- IntelliJ IDEA 2024.3+ (build 252+)
- Kotlin
- IntelliJ Platform Plugin SDK

## Usage

1. Open the project in IntelliJ IDEA
2. Locate the "GovVar" tool window in the right sidebar
3. Enter a keyword in the search field (e.g., "user", "organization", "document")
4. Click the "Search" button or press Enter
5. View the results in the table below

## Development Notes

### Extending the Plugin

To add new features or integrate real data sources:

1. **Update GovVarService**: Modify the `searchGovVariables` method to connect to your data source
2. **Update UI**: Add new UI components in `GovVarToolWindowFactory` as needed
3. **Update Resources**: Add new strings to `MyBundle.properties` for internationalization

### Testing

The plugin structure follows IntelliJ Platform Plugin Template conventions. Tests can be added in the `src/test/kotlin` directory.

### Building

```bash
./gradlew build
```

### Running

```bash
./gradlew runIde
```

## Structure

```
src/main/kotlin/org/jetbrains/plugins/template/
├── services/
│   └── GovVarService.kt          # Service layer with search logic
└── toolWindow/
    └── GovVarToolWindowFactory.kt # UI layer with tool window

src/main/resources/
├── META-INF/
│   └── plugin.xml                # Plugin configuration
└── messages/
    └── MyBundle.properties       # I18n strings
```

## License

This project follows the license of the parent repository.
