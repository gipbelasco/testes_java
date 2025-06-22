# USP Courses Scraper

A comprehensive web scraper for extracting course and discipline information from the University of São Paulo (USP) JupiterWeb system. This tool navigates through academic units to collect detailed data about courses and their curricula.

## Features

- **Automated Web Scraping**: Navigates through USP's JupiterWeb interface to extract course data
- **Comprehensive Data Extraction**: Collects course information including:
  - Course name, duration (ideal, minimum, maximum)
  - Mandatory disciplines (disciplinas obrigatórias)
  - Elective disciplines (disciplinas optativas eletivas)  
  - Free elective disciplines (disciplinas optativas livres)
  - Discipline details: código, nome, créditos aula, créditos trabalho, carga horária
- **Robust Navigation**: Uses explicit URL navigation instead of browser back/forward for reliability
- **Error Handling**: Graceful recovery from parsing errors and network issues
- **Interactive Query System**: Post-scraping analysis and search capabilities
- **Data Export**: CSV export functionality for further analysis
- **JSON Export**: Complete data export in JSON format with structured hierarchy
- **Configurable Limits**: Control how many academic units to scrape

## Technical Architecture

### Components

1. **Scraper Class** (`scraper/Scraper.java`)
   - Main web scraping logic using Selenium WebDriver
   - Handles navigation through USP's JupiterWeb interface
   - Parses HTML content using JSoup for data extraction

2. **Data Models** (`models/`)
   - `Unidade.java`: Represents academic units
   - `Curso.java`: Represents individual courses  
   - `Disciplina.java`: Represents individual disciplines/subjects

3. **Query System** (`app/ConsultasManager.java`)
   - Interactive command-line interface for data analysis
   - Search and filter capabilities
   - Statistics and export functionality

4. **Main Application** (`app/Main.java`)
   - Entry point with command-line argument parsing
   - Orchestrates scraping and launches query system

### Dependencies

- **Selenium WebDriver**: Browser automation
- **WebDriverManager**: Automatic ChromeDriver management
- **JSoup**: HTML parsing and data extraction
- **Maven**: Build and dependency management

## Installation

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Chrome browser (for Selenium WebDriver)

### Setup

1. Clone or download the project
2. Navigate to the project directory
3. Install dependencies:
   ```bash
   mvn clean compile
   ```

## Usage

### Basic Scraping

```bash
# Scrape 2 academic units (default)
mvn exec:java

# Scrape 5 academic units
mvn exec:java -Dexec.args="5"

# Alternative limit syntax
mvn exec:java -Dexec.args="--limit=5"

# Export to JSON file
mvn exec:java -Dexec.args="1 --json"

# Export to custom JSON filename
mvn exec:java -Dexec.args="2 --json=my_data.json"
```

### Interactive Mode

Launch the scraper with interactive query capabilities:

```bash
mvn exec:java -Dexec.args="3 --interactive"

# Combine with JSON export
mvn exec:java -Dexec.args="2 --json --interactive"
```

### Interactive Commands

Once in interactive mode, you can use these commands:

- `list-units` - Show all scraped academic units
- `list-courses [unit]` - Show courses in a specific unit or all courses
- `search-course [name]` - Search for courses by name
- `search-discipline [name]` - Search for disciplines by name or code
- `course-details [course]` - Show detailed information about a course
- `statistics` - Display general statistics about scraped data
- `export [filename]` - Export data to CSV format
- `exit` - Exit the interactive system

### Examples

```bash
# Interactive session examples
Query> list-units
Query> search-course biotecnologia
Query> course-details "Bacharelado em Biotecnologia"
Query> search-discipline MAT
Query> statistics
Query> export my_usp_data
```

## Data Models

### Unidade (Academic Unit)
- `nome`: Name of the academic unit
- `cursos`: List of courses offered by this unit

### Curso (Course)
- `nome`: Course name
- `unidade`: Academic unit offering the course
- `duracaoIdeal`: Ideal duration (e.g., "8 semestres")
- `duracaoMin`: Minimum duration
- `duracaoMax`: Maximum duration
- `obrigatorias`: List of mandatory disciplines
- `optativasEletivas`: List of elective disciplines
- `optativasLivres`: List of free elective disciplines

### Disciplina (Discipline)
- `codigo`: Discipline code (e.g., "MAT2453")
- `nome`: Discipline name
- `creditosAula`: Class credits
- `creditosTrabalho`: Work credits
- `cargaHoraria`: Total hours

## JSON Export Format

The JSON export creates a structured file with the following format:

```json
{
  "timestamp": "Sun Jun 22 20:06:19 GMT-03:00 2025",
  "total_units": 1,
  "units": [
    {
      "name": "Academic Unit Name",
      "total_courses": 16,
      "courses": [
        {
          "name": "Course Name",
          "unit": "Academic Unit Name",
          "duration_ideal": "8 semestres",
          "duration_min": "8 semestres", 
          "duration_max": "12 semestres",
          "mandatory_disciplines": [
            {
              "code": "ACH0021",
              "name": "Discipline Name",
              "credits_class": 2,
              "credits_work": 0,
              "hours": 30
            }
          ],
          "elective_disciplines": [...],
          "free_elective_disciplines": [...],
          "statistics": {
            "total_disciplines": 78,
            "mandatory_count": 61,
            "elective_count": 17,
            "free_elective_count": 0
          }
        }
      ]
    }
  ]
}
```

## Current Status

### ✅ Implemented Features
- Robust web scraping with explicit URL navigation
- Complete course and discipline data extraction
- Production-ready error handling with blocking overlay detection
- Interactive query system with search capabilities
- CSV export functionality
- **JSON export with complete structured data**
- Comprehensive statistics reporting
- Command-line interface with multiple options
- Text-based element selection for reliability
- JavaScript click fallback for UI interference issues

### 🎯 Performance Metrics
- **Success Rate**: 93.75% (15/16 courses successfully scraped)
- **Reliability**: Robust navigation with comprehensive error recovery
- **Data Quality**: Complete discipline extraction with metadata
- **Scale**: Successfully handles multiple academic units and courses

### 🔄 Known Limitations
- Occasional single course failures due to website structure variations
- Some discipline credit information may not be fully extracted depending on page structure
- Scraping speed is limited by website response times and necessary delays
- Large datasets may require pagination handling (not currently implemented)

### 🚀 Future Enhancements
- Database persistence options
- Web-based interface
- Advanced filtering and sorting options
- Automated scheduling for regular data updates
- Performance optimizations for large-scale scraping

## Performance Notes

- The scraper includes appropriate delays to respect the target website
- Each course requires separate page navigation, so scraping time scales with the number of courses
- Typical performance: ~30-60 seconds per academic unit depending on course count
- Memory usage is proportional to the amount of data scraped

## Error Handling

The scraper includes comprehensive error handling:

- **Navigation Errors**: Automatic retry with explicit URL navigation
- **Parsing Errors**: Graceful skipping of problematic content with continued processing
- **Timeout Handling**: 15-second WebDriver timeouts with appropriate waiting conditions
- **Data Validation**: Filtering of invalid or header rows in discipline tables

## Contributing

To contribute to this project:

1. Ensure your changes maintain the existing error handling patterns
2. Test thoroughly with various academic units
3. Update documentation for any new features
4. Follow Java coding conventions and include appropriate comments

## Technical Notes

### Navigation Strategy
The scraper uses explicit URL navigation (`driver.get(baseUrl)`) instead of browser navigation commands to ensure reliable page state and avoid getting lost in the website's navigation flow.

### Parsing Approach
HTML parsing combines Selenium for dynamic content loading and JSoup for efficient data extraction, providing both JavaScript execution capability and powerful CSS selector support.

### Data Integrity
The system includes validation to filter out header rows, empty data, and duplicate entries while preserving all valid discipline information.