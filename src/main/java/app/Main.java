package app;

import scraper.Scraper;
import models.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== USP Courses Scraper ===");
        
        // Parse command line arguments
        int limite = 2; // Default limit
        boolean interactiveMode = false;
        boolean exportJson = false;
        String jsonFileName = "usp_courses_data.json";
        
        for (String arg : args) {
            if (arg.equals("--interactive") || arg.equals("-i")) {
                interactiveMode = true;
            } else if (arg.startsWith("--limit=")) {
                limite = Integer.parseInt(arg.substring(8));
            } else if (arg.equals("--json") || arg.equals("-j")) {
                exportJson = true;
            } else if (arg.startsWith("--json=")) {
                exportJson = true;
                jsonFileName = arg.substring(7);
            } else if (arg.matches("\\d+")) {
                limite = Integer.parseInt(arg);
            }
        }
        
        System.out.println("Scraping " + limite + " academic units...");
        
        Scraper scraper = new Scraper();
        scraper.start(limite);

        // Display summary
        System.out.println("\n=== Scraping Summary ===");
        int totalCourses = 0;
        int totalDisciplines = 0;
        
        for (Unidade unidade : scraper.unidades) {
            System.out.println("Unit: " + unidade.nome + " (" + unidade.cursos.size() + " courses)");
            totalCourses += unidade.cursos.size();
            
            for (Curso curso : unidade.cursos) {
                int courseDisciplines = curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size();
                totalDisciplines += courseDisciplines;
                System.out.printf("  - %s (%d disciplines: %d mandatory, %d elective, %d free)\n",
                    curso.nome, courseDisciplines, curso.obrigatorias.size(), 
                    curso.optativasEletivas.size(), curso.optativasLivres.size());
            }
        }
        
        System.out.println("\nTotal: " + scraper.unidades.size() + " units, " + totalCourses + " courses, " + totalDisciplines + " disciplines");
        
        // Export to JSON if requested
        if (exportJson) {
            try {
                exportToJson(scraper.unidades, jsonFileName);
                System.out.println("Data exported to: " + jsonFileName);
            } catch (IOException e) {
                System.err.println("Error exporting to JSON: " + e.getMessage());
            }
        }
        
        // Launch interactive mode if requested
        if (interactiveMode) {
            ConsultasManager consultas = new ConsultasManager(scraper.unidades);
            consultas.startInteractiveMode();
        } else {
            System.out.println("\nTo use interactive query mode, run with: --interactive");
            System.out.println("To set custom limit: --limit=N or just N");
            if (!exportJson) {
                System.out.println("To export JSON: --json or --json=filename.json");
            }
        }
    }
    
    private static void exportToJson(List<Unidade> unidades, String filename) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(new java.util.Date().toString()).append("\",\n");
        json.append("  \"total_units\": ").append(unidades.size()).append(",\n");
        json.append("  \"units\": [\n");
        
        for (int i = 0; i < unidades.size(); i++) {
            Unidade unidade = unidades.get(i);
            json.append("    {\n");
            json.append("      \"name\": \"").append(escapeJson(unidade.nome)).append("\",\n");
            json.append("      \"total_courses\": ").append(unidade.cursos.size()).append(",\n");
            json.append("      \"courses\": [\n");
            
            for (int j = 0; j < unidade.cursos.size(); j++) {
                Curso curso = unidade.cursos.get(j);
                json.append("        {\n");
                json.append("          \"name\": \"").append(escapeJson(curso.nome)).append("\",\n");
                json.append("          \"unit\": \"").append(escapeJson(curso.unidade)).append("\",\n");
                
                if (curso.duracaoIdeal != null) {
                    json.append("          \"duration_ideal\": \"").append(escapeJson(curso.duracaoIdeal)).append("\",\n");
                }
                if (curso.duracaoMin != null) {
                    json.append("          \"duration_min\": \"").append(escapeJson(curso.duracaoMin)).append("\",\n");
                }
                if (curso.duracaoMax != null) {
                    json.append("          \"duration_max\": \"").append(escapeJson(curso.duracaoMax)).append("\",\n");
                }
                
                json.append("          \"mandatory_disciplines\": [\n");
                appendDisciplines(json, curso.obrigatorias);
                json.append("          ],\n");
                
                json.append("          \"elective_disciplines\": [\n");
                appendDisciplines(json, curso.optativasEletivas);
                json.append("          ],\n");
                
                json.append("          \"free_elective_disciplines\": [\n");
                appendDisciplines(json, curso.optativasLivres);
                json.append("          ],\n");
                
                json.append("          \"statistics\": {\n");
                json.append("            \"total_disciplines\": ").append(curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size()).append(",\n");
                json.append("            \"mandatory_count\": ").append(curso.obrigatorias.size()).append(",\n");
                json.append("            \"elective_count\": ").append(curso.optativasEletivas.size()).append(",\n");
                json.append("            \"free_elective_count\": ").append(curso.optativasLivres.size()).append("\n");
                json.append("          }\n");
                
                json.append("        }");
                if (j < unidade.cursos.size() - 1) json.append(",");
                json.append("\n");
            }
            
            json.append("      ]\n");
            json.append("    }");
            if (i < unidades.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}\n");
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(json.toString());
        }
    }
    
    private static void appendDisciplines(StringBuilder json, List<Disciplina> disciplinas) {
        for (int k = 0; k < disciplinas.size(); k++) {
            Disciplina disciplina = disciplinas.get(k);
            json.append("            {\n");
            json.append("              \"code\": \"").append(escapeJson(disciplina.codigo)).append("\",\n");
            json.append("              \"name\": \"").append(escapeJson(disciplina.nome)).append("\",\n");
            json.append("              \"credits_class\": ").append(disciplina.creditosAula).append(",\n");
            json.append("              \"credits_work\": ").append(disciplina.creditosTrabalho).append(",\n");
            json.append("              \"hours\": ").append(disciplina.cargaHoraria).append("\n");
            json.append("            }");
            if (k < disciplinas.size() - 1) json.append(",");
            json.append("\n");
        }
    }
    
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
