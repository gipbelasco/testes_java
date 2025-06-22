package scraper;

import models.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.*;

public class Scraper {
    public List<Unidade> unidades = new ArrayList<>();
    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "https://uspdigital.usp.br/jupiterweb/jupCarreira.jsp?codmnu=8275";

    public Scraper() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(15));
    }

    public void start(int limiteUnidades) throws Exception {
        System.out.println("Starting scraper with limit: " + limiteUnidades);
        
        driver.get(baseUrl);
        System.out.println("Navigated to USP page: " + driver.getTitle());
        
        WebElement selectUnidade = wait.until(d -> d.findElement(By.xpath("//select[@id='comboUnidade']")));
        System.out.println("Found units dropdown");

        List<WebElement> opcoesUnidades = new Select(selectUnidade).getOptions();
        System.out.println("Found " + opcoesUnidades.size() + " units available");
        
        // Extract unit names first to avoid stale element issues
        List<String> unitNames = new ArrayList<>();
        for (int i = 1; i < opcoesUnidades.size() && unitNames.size() < limiteUnidades; i++) {
            unitNames.add(opcoesUnidades.get(i).getText());
        }
        
        for (int i = 0; i < unitNames.size(); i++) {
            String nomeUnidade = unitNames.get(i);
            System.out.println("\n=== Processing Unit " + (i+1) + ": " + nomeUnidade + " ===");
            
            Unidade unidade = processUnidade(nomeUnidade);
            unidades.add(unidade);
            
            System.out.println("Completed unit: " + nomeUnidade + " with " + unidade.cursos.size() + " courses");
        }

        System.out.println("\nScraping completed. Total units: " + unidades.size());
        driver.quit();
    }

    private Unidade processUnidade(String nomeUnidade) {
        Unidade unidade = new Unidade(nomeUnidade);
        
        try {
            // Navigate to base page and select the unit
            driver.get(baseUrl);
            WebElement selectUnidade = wait.until(d -> d.findElement(By.xpath("//select[@id='comboUnidade']")));
            
            // Use text-based selection for consistency
            Select unitSelect = new Select(selectUnidade);
            try {
                unitSelect.selectByVisibleText(nomeUnidade);
            } catch (Exception e) {
                // Fallback: try partial match
                List<WebElement> unitOptions = unitSelect.getOptions();
                boolean found = false;
                for (WebElement option : unitOptions) {
                    String optionText = option.getText().trim();
                    if (optionText.isEmpty()) continue;
                    
                    if (optionText.equals(nomeUnidade) || 
                        optionText.equals(nomeUnidade.trim()) ||
                        (nomeUnidade.contains(optionText.split(" - ")[0]) && optionText.length() > 10) ||
                        (optionText.contains(nomeUnidade.split(" - ")[0]) && nomeUnidade.split(" - ")[0].length() > 10)) {
                        option.click();
                        found = true;
                        System.out.println("Selected unit: '" + optionText + "'");
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Could not find unit in processUnidade: " + nomeUnidade);
                }
            }
            
            // Wait for courses to load
            wait.until(d -> d.findElement(By.xpath("//select[@id='comboCurso']")));
            Thread.sleep(1000); // Small delay for dropdown to populate
            
            WebElement selectCurso = driver.findElement(By.xpath("//select[@id='comboCurso']"));
            List<WebElement> opcoesCurso = new Select(selectCurso).getOptions();
            System.out.println("Found " + opcoesCurso.size() + " courses for this unit");
            
            // Extract course names first to avoid stale element issues
            List<String> courseNames = new ArrayList<>();
            for (int k = 1; k < opcoesCurso.size(); k++) { // Start from 1 to skip empty option
                courseNames.add(opcoesCurso.get(k).getText());
            }
            
            // Process each course
            for (int j = 0; j < courseNames.size(); j++) {
                String courseName = courseNames.get(j);
                System.out.println("\n--- Processing Course " + (j+1) + ": " + courseName + " ---");
                
                try {
                    Curso curso = processCourse(courseName, nomeUnidade);
                    if (curso != null) {
                        unidade.cursos.add(curso);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing course " + courseName + ": " + e.getMessage());
                    // Continue with next course
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing unit " + nomeUnidade + ": " + e.getMessage());
        }
        
        return unidade;
    }
    
    private Curso processCourse(String courseName, String unitName) {
        try {
            // Navigate back to base page and re-select unit and course
            driver.get(baseUrl);
            
            // Select unit
            WebElement selectUnidade = wait.until(d -> d.findElement(By.xpath("//select[@id='comboUnidade']")));
            
            // Wait for the dropdown to be properly populated
            wait.until(d -> {
                Select tempSelect = new Select(d.findElement(By.xpath("//select[@id='comboUnidade']")));
                return tempSelect.getOptions().size() > 1; // More than just empty option
            });
            
            Select unitSelect = new Select(selectUnidade);
            try {
                unitSelect.selectByVisibleText(unitName);
            } catch (Exception e) {
                // Fallback: try partial match or trimmed text
                List<WebElement> unitOptions = unitSelect.getOptions();
                boolean found = false;
                
                if (unitOptions.size() <= 1) {
                    // If dropdown is empty, wait a bit more and try to refresh
                    Thread.sleep(2000);
                    driver.navigate().refresh();
                    Thread.sleep(1000);
                    selectUnidade = wait.until(d -> d.findElement(By.xpath("//select[@id='comboUnidade']")));
                    unitSelect = new Select(selectUnidade);
                    unitOptions = unitSelect.getOptions();
                }
                
                for (WebElement option : unitOptions) {
                    String optionText = option.getText().trim();
                    // Skip empty options and try various matching strategies
                    if (optionText.isEmpty()) continue;
                    
                    if (optionText.equals(unitName) || 
                        optionText.equals(unitName.trim()) ||
                        (unitName.contains(optionText.split(" - ")[0]) && optionText.length() > 10) ||  // Match first part, avoid short matches
                        (optionText.contains(unitName.split(" - ")[0]) && unitName.split(" - ")[0].length() > 10)) {
                        option.click();
                        found = true;
                        System.out.println("Selected unit by partial match: '" + optionText + "' for target: '" + unitName + "'");
                        break;
                    }
                }
                if (!found) {
                    // Debug: print all available options
                    System.err.println("Available unit options:");
                    for (WebElement option : unitOptions) {
                        String optText = option.getText().trim();
                        if (!optText.isEmpty()) {
                            System.err.println("  - '" + optText + "'");
                        }
                    }
                    throw new RuntimeException("Could not find unit: " + unitName);
                }
            }
            
            // Wait for courses to load and select course
            WebElement selectCurso = wait.until(d -> d.findElement(By.xpath("//select[@id='comboCurso']")));
            Thread.sleep(1000); // Allow time for dropdown to populate
            
            // Select course by visible text instead of index for more reliability
            Select cursoSelect = new Select(selectCurso);
            try {
                cursoSelect.selectByVisibleText(courseName);
            } catch (Exception e) {
                // Fallback: try partial match or trimmed text
                List<WebElement> courseOptions = cursoSelect.getOptions();
                boolean found = false;
                for (WebElement option : courseOptions) {
                    String optionText = option.getText();
                    // Try exact match, trimmed match, or close match
                    if (optionText.equals(courseName) || 
                        optionText.trim().equals(courseName.trim()) ||
                        optionText.contains(courseName.substring(0, Math.min(courseName.length(), 30)))) {  // First 30 chars match
                        option.click();
                        found = true;
                        System.out.println("Selected course by partial match: '" + optionText + "' for target: '" + courseName + "'");
                        break;
                    }
                }
                if (!found) {
                    // Debug: print all available options
                    System.err.println("Available course options:");
                    for (WebElement option : courseOptions) {
                        System.err.println("  - '" + option.getText() + "'");
                    }
                    throw new RuntimeException("Could not find course: " + courseName);
                }
            }
            
            // Submit the form
            WebElement btn = wait.until(d -> d.findElement(By.id("enviar")));
            btn.click();
            
            // Wait for course page to load and find the Grade Curricular tab
            WebElement targetTab = wait.until(d -> {
                try {
                    return d.findElement(By.id("step4-tab"));
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    // Fallback: try to find by text
                    try {
                        return d.findElement(By.xpath("//a[contains(text(), 'Grade') or contains(text(), 'Curricular')]"));
                    } catch (org.openqa.selenium.NoSuchElementException e2) {
                        return null;
                    }
                }
            });
            
            if (targetTab != null) {
                // Wait for any blocking overlays to disappear before clicking
                wait.until(d -> {
                    try {
                        List<WebElement> overlays = d.findElements(By.cssSelector(".blockUI.blockOverlay"));
                        return overlays.isEmpty() || overlays.stream().noneMatch(WebElement::isDisplayed);
                    } catch (Exception e) {
                        return true; // If we can't find overlays, assume they're gone
                    }
                });
                
                // Additional wait for the tab to be fully clickable
                wait.until(ExpectedConditions.elementToBeClickable(targetTab));
                
                // Try clicking with JavaScript if regular click fails
                try {
                    targetTab.click();
                } catch (ElementClickInterceptedException e) {
                    System.out.println("Regular click intercepted, trying JavaScript click...");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", targetTab);
                }
                
                System.out.println("Clicked on Grade Curricular tab");
                
                // Wait for tab content to load
                Thread.sleep(2000);
                
                // Parse the course data
                Document doc = Jsoup.parse(driver.getPageSource());
                
                return parseCurso(doc, courseName, unitName);
            } else {
                System.err.println("Could not find Grade Curricular tab for course: " + courseName);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error in processCourse for " + courseName + ": " + e.getMessage());
            return null;
        }
    }

    private Curso parseCurso(Document doc, String nomeCurso, String nomeUnidade) {
        Curso curso = new Curso(nomeCurso, nomeUnidade);
        
        System.out.println("Parsing course: " + nomeCurso);
        
        // Extract course duration from the text content
        String pageText = doc.text();
        if (pageText.contains("Duração Ideal:")) {
            try {
                String[] parts = pageText.split("Duração Ideal:");
                if (parts.length > 1) {
                    String afterIdeal = parts[1].split("Mínima:")[0].trim();
                    curso.duracaoIdeal = afterIdeal.split("\\s+")[0] + " " + afterIdeal.split("\\s+")[1]; // e.g., "8 semestres"
                }
            } catch (Exception e) {
                System.out.println("Error parsing duração ideal: " + e.getMessage());
            }
        }
        
        if (pageText.contains("Mínima:")) {
            try {
                String[] parts = pageText.split("Mínima:");
                if (parts.length > 1) {
                    String afterMin = parts[1].split("Máxima:")[0].trim();
                    curso.duracaoMin = afterMin.split("\\s+")[0] + " " + afterMin.split("\\s+")[1];
                }
            } catch (Exception e) {
                System.out.println("Error parsing duração mínima: " + e.getMessage());
            }
        }
        
        if (pageText.contains("Máxima:")) {
            try {
                String[] parts = pageText.split("Máxima:");
                if (parts.length > 1) {
                    String afterMax = parts[1].split("\\s+")[2] + " " + parts[1].split("\\s+")[3]; // Skip first words
                    curso.duracaoMax = afterMax;
                }
            } catch (Exception e) {
                System.out.println("Error parsing duração máxima: " + e.getMessage());
            }
        }
        
        System.out.println("Extracted course durations successfully");

        // Look for discipline tables with specific structure
        Elements allTables = doc.select("table");
        for (int i = 0; i < allTables.size(); i++) {
            Element table = allTables.get(i);
            Elements rows = table.select("tr");
            
            if (rows.size() < 3) continue; // Need at least title, header, and data rows
            
            String firstRowText = rows.get(0).text();
            
            List<Disciplina> targetList = null;
            String categoryName = "";
            
            if (firstRowText.contains("Disciplinas Obrigatórias")) {
                targetList = curso.obrigatorias;
                categoryName = "MANDATORY";
            } else if (firstRowText.contains("Disciplinas Optativas Eletivas")) {
                targetList = curso.optativasEletivas;
                categoryName = "ELECTIVE";
            } else if (firstRowText.contains("Disciplinas Optativas Livres")) {
                targetList = curso.optativasLivres;
                categoryName = "FREE_ELECTIVE";
            }
            
            if (targetList != null) {
                System.out.println("Processing " + categoryName + " disciplines...");
                
                // Check if second row looks like a header
                boolean hasHeader = false;
                if (rows.size() > 1) {
                    String secondRowText = rows.get(1).text().toLowerCase();
                    if (secondRowText.contains("créd") || secondRowText.contains("semestre") || 
                        secondRowText.contains("aula") || secondRowText.contains("trabalho")) {
                        hasHeader = true;
                    }
                }
                
                int startRow = hasHeader ? 2 : 1; // Skip title and potentially header
                
                for (int rowIndex = startRow; rowIndex < rows.size(); rowIndex++) {
                    try {
                        Elements cols = rows.get(rowIndex).select("td");
                        
                        if (cols.size() < 2) {
                            continue; // Skip rows with insufficient columns
                        }
                        
                        String codigo = cols.get(0).text().trim();
                        String nome = cols.get(1).text().trim();
                        
                        if (!codigo.isEmpty() && !nome.isEmpty() && 
                            !codigo.toLowerCase().contains("créd") && 
                            !nome.toLowerCase().contains("créd")) {
                            
                            // Try to extract additional fields if available
                            int creditosAula = 0, creditosTrabalho = 0, cargaHoraria = 0;
                            
                            try {
                                if (cols.size() > 2) creditosAula = parseIntSafely(cols.get(2).text());
                                if (cols.size() > 3) creditosTrabalho = parseIntSafely(cols.get(3).text());
                                if (cols.size() > 4) cargaHoraria = parseIntSafely(cols.get(4).text());
                            } catch (Exception e) {
                                // Silently continue if additional fields can't be parsed
                            }
                            
                            Disciplina disciplina = new Disciplina(codigo, nome);
                            disciplina.creditosAula = creditosAula;
                            disciplina.creditosTrabalho = creditosTrabalho;
                            disciplina.cargaHoraria = cargaHoraria;
                            
                            targetList.add(disciplina);
                        }
                        
                    } catch (Exception e) {
                        // Continue processing other rows if one fails
                        continue;
                    }
                }
                
                System.out.println("Found " + targetList.size() + " " + categoryName.toLowerCase() + " disciplines");
            }
        }
        
        System.out.println("Course parsed successfully - Total disciplines: " + 
                          (curso.obrigatorias.size() + curso.optativasEletivas.size() + curso.optativasLivres.size()));
        
        return curso;
    }
    
    private int parseIntSafely(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}