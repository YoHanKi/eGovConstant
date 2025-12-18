# GovVar Tool Window - UI Mockup

## Visual Layout

```
┌────────────────────────────────────────────────────────────────────────────┐
│ GovVar                                                               [─][□][×] │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  Keyword:  [_______________________________]  [ Search ]                  │
│                                                                            │
├────────────────────────────────────────────────────────────────────────────┤
│ Variable Name    │ Description              │ Type    │ Standard         │
├──────────────────┼──────────────────────────┼─────────┼──────────────────┤
│ userId           │ User identifier          │ string  │ Gov Standard v1.0│
│ userName         │ User's full name         │ string  │ Gov Standard v1.0│
│ userEmail        │ User's email address     │ string  │ Gov Standard v1.0│
│ userPhone        │ User's phone number      │ string  │ Gov Standard v1.0│
│ userAddress      │ User's residential addr. │ string  │ Gov Standard v1.0│
│                  │                          │         │                  │
│                  │                          │         │                  │
│                  │                          │         │                  │
│                  │                          │         │                  │
│                  │                          │         │                  │
├────────────────────────────────────────────────────────────────────────────┤
│ ℹ️ Enter a keyword and click Search to find government/public-standard    │
│   variable names. (Using mock data)                                       │
└────────────────────────────────────────────────────────────────────────────┘
```

## Example Usage Scenarios

### Scenario 1: Searching for "user"
**User Action:** Types "user" in the keyword field and clicks Search

**Result:**
```
┌────────────────────────────────────────────────────────────────────────────┐
│ Variable Name    │ Description              │ Type    │ Standard         │
├──────────────────┼──────────────────────────┼─────────┼──────────────────┤
│ userId           │ User identifier          │ string  │ Gov Standard v1.0│
│ userName         │ User's full name         │ string  │ Gov Standard v1.0│
│ userEmail        │ User's email address     │ string  │ Gov Standard v1.0│
│ userPhone        │ User's phone number      │ string  │ Gov Standard v1.0│
│ userAddress      │ User's residential addr. │ string  │ Gov Standard v1.0│
└────────────────────────────────────────────────────────────────────────────┘
```
**Found:** 5 results

---

### Scenario 2: Searching for "document"
**User Action:** Types "document" in the keyword field and clicks Search

**Result:**
```
┌────────────────────────────────────────────────────────────────────────────┐
│ Variable Name    │ Description              │ Type    │ Standard         │
├──────────────────┼──────────────────────────┼─────────┼──────────────────┤
│ documentId       │ Document identifier      │ string  │ Gov Standard v1.0│
│ documentType     │ Type of document         │ string  │ Gov Standard v1.0│
└────────────────────────────────────────────────────────────────────────────┘
```
**Found:** 2 results

---

### Scenario 3: Searching for "application"
**User Action:** Types "application" in the keyword field and clicks Search

**Result:**
```
┌────────────────────────────────────────────────────────────────────────────┐
│ Variable Name    │ Description              │ Type    │ Standard         │
├──────────────────┼──────────────────────────┼─────────┼──────────────────┤
│ applicationId    │ Application identifier   │ string  │ Gov Standard v1.0│
│ applicationStatus│ Application proc. status │ enum    │ Gov Standard v1.0│
└────────────────────────────────────────────────────────────────────────────┘
```
**Found:** 2 results

---

### Scenario 4: Searching for "identifier"
**User Action:** Types "identifier" in the keyword field and clicks Search

**Result:** (Searches both name AND description)
```
┌────────────────────────────────────────────────────────────────────────────┐
│ Variable Name    │ Description              │ Type    │ Standard         │
├──────────────────┼──────────────────────────┼─────────┼──────────────────┤
│ userId           │ User identifier          │ string  │ Gov Standard v1.0│
│ organizationId   │ Organization identifier  │ string  │ Gov Standard v1.0│
│ departmentId     │ Department identifier    │ string  │ Gov Standard v1.0│
│ applicationId    │ Application identifier   │ string  │ Gov Standard v1.0│
│ documentId       │ Document identifier      │ string  │ Gov Standard v1.0│
└────────────────────────────────────────────────────────────────────────────┘
```
**Found:** 5 results (matches description field)

---

### Scenario 5: Empty search
**User Action:** Leaves the keyword field empty and clicks Search

**Result:**
```
┌────────────────────────────────────────────────────────────────────────────┐
│ Variable Name    │ Description              │ Type    │ Standard         │
├──────────────────┼──────────────────────────┼─────────┼──────────────────┤
│                  │                          │         │                  │
│                  │                          │         │                  │
│                  │         (No results)     │         │                  │
│                  │                          │         │                  │
│                  │                          │         │                  │
└────────────────────────────────────────────────────────────────────────────┘
```
**Found:** 0 results

---

## UI Components Description

### 1. Search Panel (Top)
- **Label**: "Keyword:" - Indicates the purpose of the input field
- **Text Field**: Single-line input for entering search keywords
  - Supports Enter key press to trigger search
  - Tooltip: "Enter a keyword to search for government/public-standard variable names"
- **Search Button**: Triggers the search operation
  - Text: "Search"
  - Positioned to the right of the text field

### 2. Results Panel (Center - Scrollable)
- **Table**: Displays search results in a structured format
  - Column 1: **Variable Name** (150px) - The standard variable name
  - Column 2: **Description** (250px) - Detailed description of the variable
  - Column 3: **Type** (80px) - Data type (string, integer, date, enum, etc.)
  - Column 4: **Standard** (120px) - The standard specification reference
  - Features:
    - Single selection mode
    - Non-editable cells
    - Scrollable when results exceed viewport
    - Auto-resize columns

### 3. Info Panel (Bottom)
- **Info Text**: Contextual help text for users
  - Message: "Enter a keyword and click Search to find government/public-standard variable names. (Using mock data)"
  - Font: Slightly smaller than regular text
  - Purpose: Provide usage instructions and indicate mock data usage

## Tool Window Properties

- **ID**: GovVar
- **Title**: GovVar
- **Position**: Right sidebar
- **Icon**: Information icon (ℹ️)
- **Availability**: Always available in any project

## Interaction Flow

```
User Opens Project
    │
    ├─> Opens "GovVar" tool window from right sidebar
    │
    ├─> Enters keyword (e.g., "user")
    │
    ├─> Clicks "Search" button OR presses Enter
    │
    ├─> GovVarToolWindowFactory.performSearch() called
    │
    ├─> GovVarService.searchGovVariables(keyword) called
    │
    ├─> Service filters mock data
    │
    ├─> Results returned to UI
    │
    └─> Table updated with results
```

## Future UI Enhancements (TODOs)

1. **Advanced Filters**
   - Dropdown for data type filtering
   - Dropdown for standard version filtering

2. **Actions**
   - "Copy" button to copy variable name to clipboard
   - "Insert" button to insert variable into editor at cursor
   - "Export" button to export results to CSV/JSON

3. **Search Enhancements**
   - Search history dropdown
   - Recent searches
   - Saved searches

4. **Results View Options**
   - List view vs. Grid view toggle
   - Sort by name, type, or standard
   - Pagination for large result sets

5. **Detail Panel**
   - Click on a result to show detailed information
   - Usage examples
   - Related variables

6. **Settings**
   - Configure which standards to search
   - Set default filters
   - Customize table columns
