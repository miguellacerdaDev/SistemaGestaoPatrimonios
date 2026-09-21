package br.com.patrimonio;

public record Patrimonio(int id, String codigo, String nome, String categoria, boolean emUso, String localAlocado) {
    @Override public String toString() { return codigo + " — " + nome; }
}
