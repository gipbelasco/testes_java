package models;

import java.util.*;

public class Curso {
    public String nome;
    public String unidade;
    public String duracaoIdeal, duracaoMin, duracaoMax;
    public List<Disciplina> obrigatorias = new ArrayList<>();
    public List<Disciplina> optativasLivres = new ArrayList<>();
    public List<Disciplina> optativasEletivas = new ArrayList<>();

    public Curso(String nome, String unidade) {
        this.nome = nome;
        this.unidade = unidade;
    }

    @Override
    public String toString() {
        return nome + " (" + unidade + ")";
    }
}