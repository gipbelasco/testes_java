package models;

import java.util.*;

public class Unidade {
    public String nome;
    public List<Curso> cursos = new ArrayList<>();

    public Unidade(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome + " com " + cursos.size() + " cursos.";
    }
}