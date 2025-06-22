package scraper;

import models.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
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

    public Scraper() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
    }

    public void start(int limiteUnidades) throws Exception {
        driver.get("https://uspdigital.usp.br/jupiterweb/jupCarreira.jsp?codmnu=8275");
        WebElement selectUnidade = wait.until(d -> d.findElement(By.xpath("//select[@id='comboUnidade']")));

        List<WebElement> opcoesUnidades = new Select(selectUnidade).getOptions();
        for (int i = 1; i <= limiteUnidades && i < opcoesUnidades.size(); i++) {
            new Select(selectUnidade).selectByIndex(i);
            Thread.sleep(1000);
            String nomeUnidade = opcoesUnidades.get(i).getText();
            Unidade unidade = new Unidade(nomeUnidade);

            WebElement selectCurso = driver.findElement(By.xpath("//select[@id='comboCurso']"));
            List<WebElement> opcoesCurso = new Select(selectCurso).getOptions();

            for (int j = 1; j < opcoesCurso.size(); j++) {
                new Select(selectCurso).selectByIndex(j);
                WebElement btn = driver.findElement(By.id("enviar"));
                btn.click(); 
                wait.until(d -> d.findElement((By.id("step4-tab")))).click();

                Thread.sleep(1000);
                Document doc = Jsoup.parse(driver.getPageSource());
                Curso curso = parseCurso(doc, opcoesCurso.get(j).getText(), nomeUnidade);
                unidade.cursos.add(curso);

                driver.navigate().back();
                driver.navigate().back();
            }

            unidades.add(unidade);
        }

        driver.quit();
    }

    private Curso parseCurso(Document doc, String nomeCurso, String nomeUnidade) {
        Curso curso = new Curso(nomeCurso, nomeUnidade);

        Element infoTable = doc.selectFirst("table:has(caption:contains(Dados do Curso))");
        if (infoTable != null) {
            Elements rows = infoTable.select("tr");
            for (Element row : rows) {
                if (row.text().contains("Duração Ideal"))
                    curso.duracaoIdeal = row.select("td").get(1).text();
                if (row.text().contains("Duração Mínima"))
                    curso.duracaoMin = row.select("td").get(1).text();
                if (row.text().contains("Duração Máxima"))
                    curso.duracaoMax = row.select("td").get(1).text();
            }
        }

        Elements tables = doc.select("table");
        for (Element table : tables) {
            String caption = table.selectFirst("caption") != null ? table.selectFirst("caption").text() : "";
            List<Disciplina> lista = null;
            if (caption.contains("Obrigatórias")) lista = curso.obrigatorias;
            if (caption.contains("Optativas Livres")) lista = curso.optativasLivres;
            if (caption.contains("Optativas Eletivas")) lista = curso.optativasEletivas;

            if (lista != null) {
                Elements rows = table.select("tr");
                for (int i = 1; i < rows.size(); i++) {
                    Elements cols = rows.get(i).select("td");
                    String cod = cols.get(0).text();
                    String nome = cols.get(1).text();
                    lista.add(new Disciplina(cod, nome));
                }
            }
        }

        return curso;
    }
}