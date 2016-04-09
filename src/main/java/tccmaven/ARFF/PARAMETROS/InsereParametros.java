/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.ARFF.PARAMETROS;

import eu.verdelhan.ta4j.TimeSeries;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

/**
 *
 * @author Jean-NoteI5
 */
public class InsereParametros {

    //Lista de parâmetros a serem exportados
    NomeParametros nomeParametros;
    //Parâmetros montados a partir das séries temporais HASHMAP
    private TreeMap<Date, double[]> parametros;

    //Ativo ao qual se referem os parâmetros
    public InsereParametros(NomeParametros nomeParametros) {
        this.nomeParametros = nomeParametros;
        parametros = new TreeMap<>();
    }

    public ArrayList getParametros() {
        return new ArrayList<>(parametros.values());
    }
    
    //Número de registros dos parâmetros
    public int getNumReg(){
        return parametros.size();
    }

    public void insereSerieTemporalBrasil(TimeSeries timeSeries) throws InsereParametrosException, NomeParametrosException {
        insereSerieTemporal(timeSeries, "Bra");
    }

    public void insereSerieTemporalEstrangeiro(TimeSeries timeSeries) throws InsereParametrosException, NomeParametrosException {
        insereSerieTemporal(timeSeries, "Est");
    }

    //Insere série temporal do ativo
    private void insereSerieTemporal(TimeSeries timeSeries, String pais) throws InsereParametrosException, NomeParametrosException {

        //Varre a série temporal obtida
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            //Obtém a data da ocorrência
            Date data = timeSeries.getTick(i).getEndTime().toDate();
            //Adiciona o preço de fechamento
            insereValor(data, timeSeries.getTick(i).getClosePrice().toDouble(), pais, "ClosePrice");
            //Adiciona o preço de abertura
            insereValor(data, timeSeries.getTick(i).getOpenPrice().toDouble(), pais, "OpenPrice");
            //Adiciona o maior preços
            insereValor(data, timeSeries.getTick(i).getMaxPrice().toDouble(), pais, "HighPrice");
            //Adiciona o menor preço
            insereValor(data, timeSeries.getTick(i).getMinPrice().toDouble(), pais, "LowPrice");
            //Adiciona o volume
            insereValor(data, timeSeries.getTick(i).getVolume().toDouble(), pais, "Volume");
        }

    }

    public void insereValor(Date data, double valIndicador, String pais, String nomeParametro) throws NomeParametrosException {
        insereValor(data, valIndicador, nomeParametros.getOcoParametro(nomeParametros.montaNomeParametro(pais, nomeParametro)));

    }

    public void insereValor(Date data, double valIndicador, String pais, String nomeParametro, int periodo) throws NomeParametrosException {
        insereValor(data, valIndicador, nomeParametros.getOcoParametro(nomeParametros.montaNomeParametro(pais, nomeParametro, periodo)));
    }

    //Insere um valor do ativo
    private void insereValor(Date data, double valIndicador, int oco) {

        //Obtém os valores da data processada
        double[] valores = parametros.get(data);

        //Se ainda não possui parâmetros associados a data
        if (valores == null) {
            valores = new double[nomeParametros.getNumPar()];

            //TODO: GAMBIARRA
            for (int i = 0; i < valores.length; i++) {
                valores[i] = -9999999999d;
            }
        }

        //Se o parâmetro possui valor inválido
        if (Double.isNaN(valIndicador)) {
            valIndicador = 0d;
        }
        //Atualiza o valor da ocorrência
        valores[oco] = valIndicador;
        //Devolve a ocorrência para o array
        parametros.put(data, valores);
    }
}
