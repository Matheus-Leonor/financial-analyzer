# Changelog - Financial Analyzer

All notable changes to this project will be documented in this file.

## [Unreleased] - 2025-08-02

### Added
- Feature-based package structure (shared/ and features/)
- NavigationItem enum with Material icons
- NavigationDrawer component with theme-aware styling
- DashboardScreen with file upload area and stats cards
- App.kt integration with navigation state management

### Architecture
- Organized packages following modern Android patterns
- Separation of concerns between shared components and features
- Material 3 theming with proper color schemes
- Responsive layout with Row-based desktop navigation

### Technical
- Added compose.materialIconsExtended dependency
- Implemented state management with remember and mutableStateOf
- Created reusable UI components (StatCard, FileUploadArea)
- Established foundation for theme customization system

### UI/UX
- Clean navigation drawer with selection states
- Professional dashboard layout with upload functionality
- Consistent spacing and Material Design principles
- Proper icon usage and typography hierarchy

## [0.1.0] - 2025-08-02

### Added
- Initial project setup with Kotlin Compose Desktop base
- Python engine with Claude 4.0 integration
- LangChain agent with chart generation capabilities
- Project structure configured for hybrid architecture

### Python Engine
- FinancialAnalysisAgent with Claude 4.0 Sonnet integration
- ChartGenerator with 5 chart types (bar, line, pie, heatmap, data info)
- API bridge for Kotlin-Python communication via JSON
- Comprehensive test suite for all components
- Environment configuration templates

### Infrastructure
- Gradle build configuration with KMP setup
- Git repository initialization with proper line ending handling
- Python virtual environment with all required dependencies
- Shared data directories for cross-language communication

### Dependencies
- LangChain and Anthropic Claude libraries
- Chart generation libraries (matplotlib, seaborn, plotly)
- Data processing libraries (pandas, numpy)
- PDF and CSV processing capabilities

### Development Environment
- IntelliJ IDEA configuration for hybrid development
- Python interpreter setup with virtual environment
- Module structure for both Kotlin and Python components
- Git ignore rules for sensitive data and build artifacts