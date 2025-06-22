package models;

public class Disciplina {
    public String codigo, nome;
    public int creditosAula, creditosTrabalho, cargaHoraria, cargaEstagio, cargaPCC, cargaATPA;

    public Disciplina(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}