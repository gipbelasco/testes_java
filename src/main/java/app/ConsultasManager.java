package app;

import models.*;
import java.util.*;

/**
 * ConsultasManager fornece capacidades de consulta interativa para dados de cursos USP processados.
 * Permite aos usuários buscar e analisar informações de cursos e disciplinas.
 */
public class ConsultasManager {
    private List<Unidade> unidades;
    private Scanner scanner;

    public ConsultasManager(List<Unidade> unidades) {
        this.unidades = unidades;
        this.scanner = new Scanner(System.in);
    }

    public void startInteractiveMode() {
        System.out.println("\n=== Sistema de Consultas de Cursos USP ===");
        System.out.println("Comandos disponíveis:");
        System.out.println("1. list-units - Mostrar todas as unidades acadêmicas");
        System.out.println("2. list-courses [unidade] - Mostrar cursos em uma unidade");
        System.out.println("3. search-course [nome] - Buscar cursos por nome");
        System.out.println("4. search-discipline [nome] - Buscar disciplinas por nome ou código");
        System.out.println("5. course-details [curso] - Mostrar informações detalhadas sobre um curso");
        System.out.println("6. statistics - Mostrar estatísticas gerais");
        System.out.println("7. export [arquivo] - Exportar dados para CSV");
        System.out.println("8. exit - Sair do sistema");
        System.out.println();

        while (true) {
            System.out.print("Consulta> ");
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
                        System.out.println("Saindo do sistema de consultas. Até logo!");
                        return;
                    default:
                        System.out.println("Comando desconhecido: " + command);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Erro ao executar comando: " + e.getMessage());
            }
            
            System.out.println();
        }
    }

    private void listUnits() {
        System.out.println("Unidades Acadêmicas (" + unidades.size() + " total):");
        for (int i = 0; i < unidades.size(); i++) {
            Unidade unidade = unidades.get(i);
            System.out.printf("%d. %s (%d cursos)\n", 
                i + 1, unidade.nome, unidade.cursos.size());
        }
    }

    private void listCourses(String unitName) {
        if (unitName.isEmpty()) {
            System.out.println("Todos os cursos:");
            for (Unidade unidade : unidades) {
                System.out.println("\n" + unidade.nome + ":");
                for (Curso curso : unidade.cursos) {
                    int totalDisciplines = curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size();
                    System.out.printf("  - %s (%d disciplinas)\n", curso.nome, totalDisciplines);
                }
            }
        } else {
            Unidade foundUnit = findUnitByName(unitName);
            if (foundUnit != null) {
                System.out.println("Cursos em " + foundUnit.nome + ":");
                for (Curso curso : foundUnit.cursos) {
                    int totalDisciplines = curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size();
                    System.out.printf("  - %s (%d disciplinas)\n", curso.nome, totalDisciplines);
                }
            } else {
                System.out.println("Unidade não encontrada: " + unitName);
            }
        }
    }

    private void searchCourses(String searchTerm) {
        if (searchTerm.isEmpty()) {
            System.out.println("Por favor, forneça um termo de busca.");
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
            System.out.println("Nenhum curso encontrado correspondente a: " + searchTerm);
        } else {
            System.out.println("Encontrado(s) " + matches.size() + " curso(s) correspondente(s) a '" + searchTerm + "':");
            for (Curso curso : matches) {
                int totalDisciplines = curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size();
                System.out.printf("  - %s (%s) - %d disciplinas\n", 
                    curso.nome, curso.unidade, totalDisciplines);
            }
        }
    }

    private void searchDisciplines(String searchTerm) {
        if (searchTerm.isEmpty()) {
            System.out.println("Por favor, forneça um termo de busca.");
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
            System.out.println("Nenhuma disciplina encontrada correspondente a: " + searchTerm);
        } else {
            System.out.println("Disciplinas encontradas correspondentes a '" + searchTerm + "':");
            for (Map.Entry<String, List<String>> entry : disciplineMap.entrySet()) {
                System.out.printf("  - %s\n", entry.getKey());
                System.out.printf("    Encontrada em: %s\n", String.join(", ", entry.getValue()));
            }
        }
    }

    private void showCourseDetails(String courseName) {
        if (courseName.isEmpty()) {
            System.out.println("Por favor, forneça o nome de um curso.");
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
            System.out.println("Curso não encontrado: " + courseName);
            return;
        }

        System.out.println("=== Detalhes do Curso ===");
        System.out.println("Nome: " + foundCourse.nome);
        System.out.println("Unidade: " + foundCourse.unidade);
        System.out.println("Duração Ideal: " + (foundCourse.duracaoIdeal != null ? foundCourse.duracaoIdeal : "N/A"));
        System.out.println("Duração Mínima: " + (foundCourse.duracaoMin != null ? foundCourse.duracaoMin : "N/A"));
        System.out.println("Duração Máxima: " + (foundCourse.duracaoMax != null ? foundCourse.duracaoMax : "N/A"));
        
        System.out.println("\nDisciplinas Obrigatórias (" + foundCourse.obrigatorias.size() + "):");
        for (Disciplina d : foundCourse.obrigatorias) {
            System.out.printf("  - %s - %s\n", d.codigo, d.nome);
        }
        
        System.out.println("\nDisciplinas Optativas Eletivas (" + foundCourse.optativasEletivas.size() + "):");
        for (Disciplina d : foundCourse.optativasEletivas) {
            System.out.printf("  - %s - %s\n", d.codigo, d.nome);
        }
        
        System.out.println("\nDisciplinas Optativas Livres (" + foundCourse.optativasLivres.size() + "):");
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

        System.out.println("=== Estatísticas de Cursos USP ===");
        System.out.println("Unidades Acadêmicas: " + unidades.size());
        System.out.println("Total de Cursos: " + totalCourses);
        System.out.println("Total de Disciplinas: " + (totalMandatory + totalElectives + totalFreeElectives));
        System.out.println("  - Obrigatórias: " + totalMandatory);
        System.out.println("  - Eletivas: " + totalElectives);
        System.out.println("  - Optativas Livres: " + totalFreeElectives);
        
        if (totalCourses > 0) {
            System.out.printf("Média de Cursos por Unidade: %.1f\n", (double) totalCourses / unidades.size());
            System.out.printf("Média de Disciplinas por Curso: %.1f\n", 
                (double) (totalMandatory + totalElectives + totalFreeElectives) / totalCourses);
        }
    }

    private void exportData(String filename) {
        if (filename.isEmpty()) {
            filename = "exportacao_cursos_usp.csv";
        }
        if (!filename.endsWith(".csv")) {
            filename += ".csv";
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println("Unidade,Curso,Tipo_Disciplina,Codigo,Nome,Creditos_Aula,Creditos_Trabalho,Horas");
            
            for (Unidade unidade : unidades) {
                for (Curso curso : unidade.cursos) {
                    exportDisciplines(writer, unidade.nome, curso.nome, "Obrigatoria", curso.obrigatorias);
                    exportDisciplines(writer, unidade.nome, curso.nome, "Eletiva", curso.optativasEletivas);
                    exportDisciplines(writer, unidade.nome, curso.nome, "Optativa_Livre", curso.optativasLivres);
                }
            }
            
            System.out.println("Dados exportados para: " + filename);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Erro ao criar arquivo de exportação: " + e.getMessage());
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
