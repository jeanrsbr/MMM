/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.ARFF.PARAMETROS;

import java.text.NumberFormat;

/**
 *
 * @author Jean-NoteI5
 */
public class NomeParametros {

    private String[] nomeParametros;
    private String[] nomeParametrosOriginal;

    public NomeParametros(String[] nomeParametros) {
        nomeParametrosOriginal = nomeParametros.clone();
        this.nomeParametros = nomeParametros.clone();
        //Ajusta o último parâmetro para possuir o nome fixo de TARGET
        this.nomeParametros[nomeParametros.length - 1] = "Target";
    }

    //Retorna os parâmetros de apenas um país
    public String[] getNomeParametros() {
        return nomeParametros;
    }

    public int verificaExisteParametro(String pais, String nomeParametro) {

        String nomeTemp = montaNomeParametro(pais, nomeParametro);

        int num = 0;

        //Verifica se existe o parâmetro indicado
        for (int i = 0; i < nomeParametros.length; i++) {
            if (nomeParametros[i].contains(nomeTemp)) {
                num++;
            }
        }
        return num;
    }

    public int[] getPeriodoParametro(String pais, String nomeParametro) throws NomeParametrosException {

        int[] periodo = new int[verificaExisteParametro(pais, nomeParametro)];
        int indice = 0;
        String nomeTemp = montaNomeParametro(pais, nomeParametro);

        //Verifica se existe o parâmetro indicado
        for (int i = 0; i < nomeParametros.length; i++) {
            if (nomeParametros[i].contains(nomeTemp)) {
                periodo[indice] = extraiPeriodoParametro(i);
                indice++;
            }
        }
        return periodo;
    }

    private int extraiPeriodoParametro(int oco) throws NomeParametrosException {
        String[] decomposto = nomeParametros[oco].split("_");

        if (decomposto.length < 3) {
            throw new NomeParametrosException("Foi tentado extrair o período de um parâmetro sem período");
        }

        //Extrai o valor inteiro do período
        return Integer.parseInt(decomposto[2]);
    }

    //Retorna a ocorrência do parâmetro Target
    public int getOcoTarget() throws NomeParametrosException {
        return getOcoParametro(nomeParametrosOriginal[nomeParametros.length - 1]);
    }

    //Retorna o número de parâmetros
    public int getNumPar() {
        //Retorna a quantidade de parâmetros adicionados
        return nomeParametros.length;
    }

    //Retorna a ocorrência que o parâmetro deve ser inserido
    public int getOcoParametro(String nomeParametro) throws NomeParametrosException {

        for (int i = 0; i < nomeParametros.length; i++) {

            if (nomeParametros[i].equals(nomeParametro)) {
                return i;
            }
        }
        throw new NomeParametrosException("Não foi encontrada a ocorrência do parâmetro indicado");
    }

    public boolean verificaParametroExiste(String nomeParametro) {
        for (int i = 0; i < nomeParametros.length; i++) {

            if (nomeParametros[i].equals(nomeParametro)) {
                return true;
            }
        }

        return false;
    }

    //Monta a literal de nome do parâmetro
    public String montaNomeParametro(String pais, String parametro, int periodo) {
        NumberFormat f = NumberFormat.getInstance();
        f.setMaximumIntegerDigits(2);
        f.setMinimumIntegerDigits(2);
        return pais + "_" + parametro + "_" + f.format(periodo);
    }

    //Monta a literal de nome do parâmetro
    public String montaNomeParametro(String pais, String parametro) {
        return pais + "_" + parametro;
    }
}
