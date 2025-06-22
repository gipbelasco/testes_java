package app;

import scraper.Scraper;
import models.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== USP Courses Scraper ===");
        
        // Parse command line arguments
        int limite = 2; // Default limit
        boolean interactiveMode = false;
        
        for (String arg : args) {
            if (arg.equals("--interactive") || arg.equals("-i")) {
                interactiveMode = true;
            } else if (arg.startsWith("--limit=")) {
                limite = Integer.parseInt(arg.substring(8));
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
        
        // Launch interactive mode if requested
        if (interactiveMode) {
            ConsultasManager consultas = new ConsultasManager(scraper.unidades);
            consultas.startInteractiveMode();
        } else {
            System.out.println("\nTo use interactive query mode, run with: --interactive");
            System.out.println("To set custom limit: --limit=N or just N");
        }
    }
}
