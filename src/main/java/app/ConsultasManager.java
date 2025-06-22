package app;

import models.*;
import java.util.*;

/**
 * ConsultasManager provides interactive query capabilities for scraped USP course data.
 * Allows users to search and analyze course and discipline information.
 */
public class ConsultasManager {
    private List<Unidade> unidades;
    private Scanner scanner;

    public ConsultasManager(List<Unidade> unidades) {
        this.unidades = unidades;
        this.scanner = new Scanner(System.in);
    }

    public void startInteractiveMode() {
        System.out.println("\n=== USP Courses Query System ===");
        System.out.println("Available commands:");
        System.out.println("1. list-units - Show all academic units");
        System.out.println("2. list-courses [unit] - Show courses in a unit");
        System.out.println("3. search-course [name] - Search for courses by name");
        System.out.println("4. search-discipline [name] - Search for disciplines by name or code");
        System.out.println("5. course-details [course] - Show detailed info about a course");
        System.out.println("6. statistics - Show general statistics");
        System.out.println("7. export [filename] - Export data to CSV");
        System.out.println("8. exit - Exit the system");
        System.out.println();

        while (true) {
            System.out.print("Query> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String parameter = parts.length > 1 ? parts[1] : "";

            try {
                switch (command) {
                    case "list-units":
                        listUnits();
                        break;
                    case "list-courses":
                        listCourses(parameter);
                        break;
                    case "search-course":
                        searchCourses(parameter);
                        break;
                    case "search-discipline":
                        searchDisciplines(parameter);
                        break;
                    case "course-details":
                        showCourseDetails(parameter);
                        break;
                    case "statistics":
                        showStatistics();
                        break;
                    case "export":
                        exportData(parameter);
                        break;
                    case "exit":
                        System.out.println("Exiting query system. Goodbye!");
                        return;
                    default:
                        System.out.println("Unknown command: " + command);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error executing command: " + e.getMessage());
            }
            
            System.out.println();
        }
    }

    private void listUnits() {
        System.out.println("Academic Units (" + unidades.size() + " total):");
        for (int i = 0; i < unidades.size(); i++) {
            Unidade unidade = unidades.get(i);
            System.out.printf("%d. %s (%d courses)\n", 
                i + 1, unidade.nome, unidade.cursos.size());
        }
    }

    private void listCourses(String unitName) {
        if (unitName.isEmpty()) {
            System.out.println("All courses:");
            for (Unidade unidade : unidades) {
                System.out.println("\n" + unidade.nome + ":");
                for (Curso curso : unidade.cursos) {
                    int totalDisciplines = curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size();
                    System.out.printf("  - %s (%d disciplines)\n", curso.nome, totalDisciplines);
                }
            }
        } else {
            Unidade foundUnit = findUnitByName(unitName);
            if (foundUnit != null) {
                System.out.println("Courses in " + foundUnit.nome + ":");
                for (Curso curso : foundUnit.cursos) {
                    int totalDisciplines = curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size();
                    System.out.printf("  - %s (%d disciplines)\n", curso.nome, totalDisciplines);
                }
            } else {
                System.out.println("Unit not found: " + unitName);
            }
        }
    }

    private void searchCourses(String searchTerm) {
        if (searchTerm.isEmpty()) {
            System.out.println("Please provide a search term.");
            return;
        }

        List<Curso> matches = new ArrayList<>();
        for (Unidade unidade : unidades) {
            for (Curso curso : unidade.cursos) {
                if (curso.nome.toLowerCase().contains(searchTerm.toLowerCase())) {
                    matches.add(curso);
                }
            }
        }

        if (matches.isEmpty()) {
            System.out.println("No courses found matching: " + searchTerm);
        } else {
            System.out.println("Found " + matches.size() + " course(s) matching '" + searchTerm + "':");
            for (Curso curso : matches) {
                int totalDisciplines = curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size();
                System.out.printf("  - %s (%s) - %d disciplines\n", 
                    curso.nome, curso.unidade, totalDisciplines);
            }
        }
    }

    private void searchDisciplines(String searchTerm) {
        if (searchTerm.isEmpty()) {
            System.out.println("Please provide a search term.");
            return;
        }

        Map<String, List<String>> disciplineMap = new HashMap<>();
        
        for (Unidade unidade : unidades) {
            for (Curso curso : unidade.cursos) {
                List<Disciplina> allDisciplines = new ArrayList<>();
                allDisciplines.addAll(curso.obrigatorias);
                allDisciplines.addAll(curso.optativasEletivas);
                allDisciplines.addAll(curso.optativasLivres);
                
                for (Disciplina disciplina : allDisciplines) {
                    if (disciplina.codigo.toLowerCase().contains(searchTerm.toLowerCase()) ||
                        disciplina.nome.toLowerCase().contains(searchTerm.toLowerCase())) {
                        
                        String key = disciplina.codigo + " - " + disciplina.nome;
                        disciplineMap.computeIfAbsent(key, k -> new ArrayList<>()).add(curso.nome);
                    }
                }
            }
        }

        if (disciplineMap.isEmpty()) {
            System.out.println("No disciplines found matching: " + searchTerm);
        } else {
            System.out.println("Found disciplines matching '" + searchTerm + "':");
            for (Map.Entry<String, List<String>> entry : disciplineMap.entrySet()) {
                System.out.printf("  - %s\n", entry.getKey());
                System.out.printf("    Found in: %s\n", String.join(", ", entry.getValue()));
            }
        }
    }

    private void showCourseDetails(String courseName) {
        if (courseName.isEmpty()) {
            System.out.println("Please provide a course name.");
            return;
        }

        Curso foundCourse = null;
        for (Unidade unidade : unidades) {
            for (Curso curso : unidade.cursos) {
                if (curso.nome.toLowerCase().contains(courseName.toLowerCase())) {
                    foundCourse = curso;
                    break;
                }
            }
            if (foundCourse != null) break;
        }

        if (foundCourse == null) {
            System.out.println("Course not found: " + courseName);
            return;
        }

        System.out.println("=== Course Details ===");
        System.out.println("Name: " + foundCourse.nome);
        System.out.println("Unit: " + foundCourse.unidade);
        System.out.println("Ideal Duration: " + (foundCourse.duracaoIdeal != null ? foundCourse.duracaoIdeal : "N/A"));
        System.out.println("Min Duration: " + (foundCourse.duracaoMin != null ? foundCourse.duracaoMin : "N/A"));
        System.out.println("Max Duration: " + (foundCourse.duracaoMax != null ? foundCourse.duracaoMax : "N/A"));
        
        System.out.println("\nMandatory Disciplines (" + foundCourse.obrigatorias.size() + "):");
        for (Disciplina d : foundCourse.obrigatorias) {
            System.out.printf("  - %s - %s\n", d.codigo, d.nome);
        }
        
        System.out.println("\nElective Disciplines (" + foundCourse.optativasEletivas.size() + "):");
        for (Disciplina d : foundCourse.optativasEletivas) {
            System.out.printf("  - %s - %s\n", d.codigo, d.nome);
        }
        
        System.out.println("\nFree Elective Disciplines (" + foundCourse.optativasLivres.size() + "):");
        for (Disciplina d : foundCourse.optativasLivres) {
            System.out.printf("  - %s - %s\n", d.codigo, d.nome);
        }
    }

    private void showStatistics() {
        int totalCourses = 0;
        int totalMandatory = 0;
        int totalElectives = 0;
        int totalFreeElectives = 0;
        
        for (Unidade unidade : unidades) {
            totalCourses += unidade.cursos.size();
            for (Curso curso : unidade.cursos) {
                totalMandatory += curso.obrigatorias.size();
                totalElectives += curso.optativasEletivas.size();
                totalFreeElectives += curso.optativasLivres.size();
            }
        }

        System.out.println("=== USP Course Statistics ===");
        System.out.println("Academic Units: " + unidades.size());
        System.out.println("Total Courses: " + totalCourses);
        System.out.println("Total Disciplines: " + (totalMandatory + totalElectives + totalFreeElectives));
        System.out.println("  - Mandatory: " + totalMandatory);
        System.out.println("  - Electives: " + totalElectives);
        System.out.println("  - Free Electives: " + totalFreeElectives);
        
        if (totalCourses > 0) {
            System.out.printf("Average Courses per Unit: %.1f\n", (double) totalCourses / unidades.size());
            System.out.printf("Average Disciplines per Course: %.1f\n", 
                (double) (totalMandatory + totalElectives + totalFreeElectives) / totalCourses);
        }
    }

    private void exportData(String filename) {
        if (filename.isEmpty()) {
            filename = "usp_courses_export.csv";
        }
        if (!filename.endsWith(".csv")) {
            filename += ".csv";
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println("Unit,Course,Discipline_Type,Code,Name,Credits_Class,Credits_Work,Hours");
            
            for (Unidade unidade : unidades) {
                for (Curso curso : unidade.cursos) {
                    exportDisciplines(writer, unidade.nome, curso.nome, "Mandatory", curso.obrigatorias);
                    exportDisciplines(writer, unidade.nome, curso.nome, "Elective", curso.optativasEletivas);
                    exportDisciplines(writer, unidade.nome, curso.nome, "Free_Elective", curso.optativasLivres);
                }
            }
            
            System.out.println("Data exported to: " + filename);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Error creating export file: " + e.getMessage());
        }
    }

    private void exportDisciplines(java.io.PrintWriter writer, String unit, String course, 
                                 String type, List<Disciplina> disciplines) {
        for (Disciplina disciplina : disciplines) {
            writer.printf("%s,%s,%s,%s,%s,%d,%d,%d\n",
                escapeCsv(unit), escapeCsv(course), type, escapeCsv(disciplina.codigo),
                escapeCsv(disciplina.nome), disciplina.creditosAula, 
                disciplina.creditosTrabalho, disciplina.cargaHoraria);
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private Unidade findUnitByName(String name) {
        for (Unidade unidade : unidades) {
            if (unidade.nome.toLowerCase().contains(name.toLowerCase())) {
                return unidade;
            }
        }
        return null;
    }
}
