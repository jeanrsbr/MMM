/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.ANALISADOR;

/**
 *
 * @author Jean-NoteI5
 */
public class Resultado {
    
    private final String ativo;
    private final double valorHoje;
    private final double valorPredito;
    private final double diffValores;
    private final double percentualDiffValores;
    private final int tipo; //0 = Máximo / 1 = Mínimo

    public Resultado(String ativo, double valorHoje, double valorPredito, int tipo) {
        this.ativo = ativo;
        this.valorHoje = valorHoje;
        this.valorPredito = valorPredito;
        this.tipo = tipo;
        diffValores = valorPredito - valorHoje;
        percentualDiffValores = (valorPredito / valorHoje) * 100;
    }

    public String getAtivo() {
        return ativo;
    }

    public double getValorHoje() {
        return valorHoje;
    }

    public double getValorPredito() {
        return valorPredito;
    }

    public double getDiffValores() {
        return diffValores;
    }

    public double getPercentualDiffValores() {
        return percentualDiffValores;
    }

    public int getTipo() {
        return tipo;
    }
    
}
