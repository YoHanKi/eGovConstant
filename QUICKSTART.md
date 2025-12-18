# GovVar Plugin - Quick Start Guide

## What is GovVar?

GovVar is an IntelliJ IDEA plugin that helps developers quickly find and use government/public-standard variable names in their code. Instead of wondering "What should I name this user ID field?", you can search for "user" and see all the standard variable names related to users.

## Installation & Running

### For Developers

1. **Clone the repository**
   ```bash
   git clone https://github.com/YoHanKi/eGovConstant.git
   cd eGovConstant
   ```

2. **Build the plugin** (requires internet for dependencies)
   ```bash
   ./gradlew build
   ```

3. **Run in development IDE**
   ```bash
   ./gradlew runIde
   ```
   This will start a new IntelliJ IDEA instance with the plugin installed.

### Using the Plugin

1. **Open the GovVar Tool Window**
   - Look for "GovVar" in the right sidebar
   - Click to open the tool window

2. **Search for Variables**
   - Enter a keyword (e.g., "user", "document", "application")
   - Press Enter or click the "Search" button

3. **View Results**
   - Results appear in a table below
   - Each result shows:
     - Variable Name (e.g., `userId`)
     - Description (e.g., "User identifier")
     - Type (e.g., "string")
     - Standard (e.g., "Gov Standard v1.0")

## Example Searches

| Search Term | Expected Results | Count |
|-------------|------------------|-------|
| `user` | userId, userName, userEmail, userPhone, userAddress | 5 |
| `organization` | organizationId, organizationName | 2 |
| `document` | documentId, documentType | 2 |
| `application` | applicationId, applicationStatus, submissionDate, approvalDate | 4 |
| `citizen` | citizenId, citizenName | 2 |
| `tax` | taxId, fiscalYear | 2 |

## Current Mock Data

The plugin currently includes 20 government-standard variable names:

### User Management
- `userId` - User identifier (string)
- `userName` - User's full name (string)
- `userEmail` - User's email address (string)
- `userPhone` - User's phone number (string)
- `userAddress` - User's residential address (string)

### Organization
- `organizationId` - Organization identifier (string)
- `organizationName` - Organization name (string)
- `departmentId` - Department identifier (string)
- `departmentName` - Department name (string)

### Application Processing
- `applicationId` - Application identifier (string)
- `applicationStatus` - Application processing status (enum)
- `submissionDate` - Date of submission (date)
- `approvalDate` - Date of approval (date)

### Document Management
- `documentId` - Document identifier (string)
- `documentType` - Type of document (string)

### Citizen Information
- `citizenId` - Citizen national ID (string)
- `citizenName` - Citizen full name (string)

### Financial
- `taxId` - Tax identification number (string)
- `fiscalYear` - Fiscal year (integer)
- `budgetAmount` - Budget amount (decimal)

## Key Features

✅ **Case-Insensitive Search** - Search for "USER" or "user", same results  
✅ **Description Search** - Finds results in both name and description  
✅ **No Network Required** - All data is local (mock data)  
✅ **Fast** - Instant search results  
✅ **Clean UI** - Simple and intuitive interface  

## Project Structure

```
eGovConstant/
├── GOVVAR_PLUGIN.md           # Detailed plugin documentation
├── IMPLEMENTATION_SUMMARY.md  # Technical implementation details
├── UI_MOCKUP.md              # UI design and mockups
├── QUICKSTART.md             # This file
│
├── src/main/kotlin/org/jetbrains/plugins/template/
│   ├── services/
│   │   └── GovVarService.kt              # Service layer (business logic)
│   └── toolWindow/
│       └── GovVarToolWindowFactory.kt    # UI layer (tool window)
│
├── src/main/resources/
│   ├── META-INF/
│   │   └── plugin.xml                    # Plugin registration
│   └── messages/
│       └── MyBundle.properties           # UI strings
│
└── src/test/kotlin/org/jetbrains/plugins/template/
    └── GovVarServiceTest.kt              # Unit tests
```

## Testing

Run the test suite:
```bash
./gradlew test
```

The test suite includes:
- Service existence tests
- Valid keyword search tests
- Empty/blank keyword handling
- Case-insensitive search verification
- Description search tests
- Result structure validation

## Future Development

The plugin is designed to be extensible. See TODOs in the code for:

1. **Data Integration**
   - Connect to real government standard databases
   - API integration for live data
   - Support for multiple standard specifications

2. **Enhanced Features**
   - Filter by data type
   - Filter by standard version
   - Copy variable to clipboard
   - Insert into code editor
   - Export results

3. **Performance**
   - Caching for frequent searches
   - Pagination for large datasets
   - Background indexing

## Requirements

- **IDE**: IntelliJ IDEA 2024.3 or later (build 252+)
- **Language**: Kotlin
- **JVM**: Java 21
- **Gradle**: 9.2.1 (included via wrapper)

## Troubleshooting

### Plugin doesn't appear
- Make sure you're running IntelliJ IDEA 2024.3 or later
- Check if the tool window is hidden (View → Tool Windows → GovVar)

### No search results
- Verify you're entering a keyword that matches the mock data
- Try common terms like "user", "document", or "application"

### Build fails
- Ensure you have internet access for downloading dependencies
- Check that Java 21 is installed and configured

## Documentation

For more information, see:
- [GOVVAR_PLUGIN.md](GOVVAR_PLUGIN.md) - Comprehensive plugin documentation
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Technical details
- [UI_MOCKUP.md](UI_MOCKUP.md) - UI design and examples

## Contributing

Contributions are welcome! To add new features:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

See [LICENSE](LICENSE) for details.

## Support

For issues or questions:
- Create an issue on GitHub
- Check existing documentation
- Review the source code (it's well-commented!)

---

**Note**: This plugin currently uses mock data for demonstration. Future versions will integrate with real government standard databases and APIs.
