// Main.java
package app;

import scraper.Scraper;
import models.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int limite = args.length > 0 ? Integer.parseInt(args[0]) : 2;

        Scraper scraper = new Scraper();
        scraper.start(limite);

        for (Unidade unidade : scraper.unidades) {
            System.out.println(unidade);
            for (Curso curso : unidade.cursos) {
                System.out.println(" - " + curso);
                System.out.println("   Disciplinas obrigatórias: " + curso.obrigatorias.size());
                System.out.println("   Eletivas: " + curso.optativasEletivas.size());
            }
        }
    }
}
