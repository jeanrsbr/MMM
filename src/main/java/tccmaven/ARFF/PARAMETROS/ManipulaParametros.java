/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.ARFF.PARAMETROS;

import java.util.ArrayList;

/**
 *
 * @author Jean-NoteI5
 */
public class ManipulaParametros {

    private ArrayList<double[]> manipulaParametros;
    String ativo;

    public ManipulaParametros(ArrayList<double[]> listaParametros, String ativo) {
        this.manipulaParametros = listaParametros;
        this.ativo = ativo;
    }

    public ArrayList<double[]> getListaParametros() {
        return manipulaParametros;
    }

    public String getAtivo() {
        return ativo;
    }

    public int getNumReg() {
        //Retorna a quantidade de registros
        return manipulaParametros.size();
    }


    //Popula variável target
    public void criaTarget(int oco) throws InsereParametrosException {
        //Varre as ocorrências da lista (Não processa a última ocorrência)
        for (int i = 0; i < manipulaParametros.size() - 1; i++) {
            //Obtém o array de parâmetros da ocorrência atual
            double[] par = manipulaParametros.get(i);
            //Atribuir a última ocorrência dos parâmetros os valor do parâmetro na ocorrência posterior
            par[par.length - 1] = manipulaParametros.get(i + 1)[oco];
            //Devolve o valor a lista
            manipulaParametros.set(i, par);
        }
    }

    //Balanceia os registros contidos nos parâmetros
    public void balance() {

        //Varre os registros do ArrayList
        for (int i = 0; i < manipulaParametros.size(); i++) {

            //Obtém o array de parâmetros presente na lista
            double[] par = manipulaParametros.get(i);

            //Varre os parâmetros
            for (int j = 0; j < par.length; j++) {
                //Se estiver com conteúdo inválido pega o conteúdo da ocorrência anterior
                if (par[j] == -9999999999d) {
                    //Se for a primeira ocorrência
                    if (i == 0) {
                        //Apenas zera
                        par[j] = 0d;
                    } else {
                        //Inicializa com o conteúdo da ocorrência anterior
                        par[j] = manipulaParametros.get(i - 1)[j];
                    }
                }
            }
            manipulaParametros.set(i, par);
        }
    }
}
