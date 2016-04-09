/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.ARFF.PARAMETROS;

import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.AverageDirectionalMovementIndicator;
import eu.verdelhan.ta4j.indicators.trackers.MACDIndicator;
import eu.verdelhan.ta4j.indicators.trackers.ROCIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.WilliamsRIndicator;
import eu.verdelhan.ta4j.indicators.volume.OnBalanceVolumeIndicator;

/**
 *
 * @author Jean-NoteI5
 */
public class Indicadores {

    private InsereParametros parametros;
    private String pais;
    private TimeSeries timeSeries;
    private NomeParametros nomeParametros;

    public static final String MOVING_AVERAGE = "SMA";
    public static final String RELATIVE_STRENGTH_INDICATOR = "RSI";
    public static final String STOCHASTIC_OSCILATOR_KD = "SOKI";
    public static final String ON_BALANCE_VOLUME = "OBV";
    public static final String MOVING_AVERAGE_CONVERGENCE_DIVERGENCE = "MACD";
    public static final String PRICE_VOLUME_TREND = "PVT";
    public static final String WILLIANS_R = "WILLR";
    public static final String PRICE_RATE_OF_CHANGE = "PROC";
    public static final String MOMENTUM = "Momentum";
    public static final String BIAS = "BIAS";
    public static final String AVERAGE_DIRECTIONAL_MOVEMENT_INDICATOR = "ADMI";

    public Indicadores(InsereParametros parametros, TimeSeries timeSeries, NomeParametros nomeParametros) {
        this.parametros = parametros;
        this.timeSeries = timeSeries;
        this.nomeParametros = nomeParametros;
    }

    public InsereParametros getParametros() {
        return parametros;
    }

    public void setPaisBrasil() {
        pais = "Bra";
    }

    public void setPaisEstrangeiro() {
        pais = "Est";
    }

    //Calcula indicadores da série temporal
    public void calculaIndicadoresSerie() throws IndicadoresException, InsereParametrosException,
            NomeParametrosException {

        if (pais.equals("")) {
            throw new IndicadoresException("Não foi informado o código do país para gerar os indicadores");
        }

        int[] periodos;

        if (nomeParametros.verificaExisteParametro(pais, MOVING_AVERAGE) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, MOVING_AVERAGE);
            for (int i = 0; i < periodos.length; i++) {
                calculaSMA(periodos[i]);
            }
        }

        if (nomeParametros.verificaExisteParametro(pais, RELATIVE_STRENGTH_INDICATOR) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, RELATIVE_STRENGTH_INDICATOR);
            for (int i = 0; i < periodos.length; i++) {
                calculaIFR(periodos[i]);
            }
        }

        if (nomeParametros.verificaExisteParametro(pais, STOCHASTIC_OSCILATOR_KD) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, STOCHASTIC_OSCILATOR_KD);

            //Verifica se houve inconsistência
            if (periodos.length > 1){
                //O Parâmetro SOKI serve de referência para o SODI e o SODI não tem período
                throw new IndicadoresException("Parâmetro SOKI não pode conter mais do que um período");
            }

            for (int i = 0; i < periodos.length; i++) {
                calculaStochasticOscilatorKD(periodos[i]);
            }
        }

        if (nomeParametros.verificaExisteParametro(pais, ON_BALANCE_VOLUME) > 0) {
            calculaOnBalanceVolume();
        }

        if (nomeParametros.verificaExisteParametro(pais, MOVING_AVERAGE_CONVERGENCE_DIVERGENCE) > 0) {
            calculaMACD(12, 26);
        }

        if (nomeParametros.verificaExisteParametro(pais, PRICE_VOLUME_TREND) > 0) {
            calculaPVT();
        }

        if (nomeParametros.verificaExisteParametro(pais, WILLIANS_R) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, WILLIANS_R);
            for (int i = 0; i < periodos.length; i++) {
                calculaWillianR(periodos[i]);
            }
        }

        if (nomeParametros.verificaExisteParametro(pais, PRICE_RATE_OF_CHANGE) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, PRICE_RATE_OF_CHANGE);
            for (int i = 0; i < periodos.length; i++) {
                calculaROC(periodos[i]);
            }
        }

        if (nomeParametros.verificaExisteParametro(pais, MOMENTUM) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, MOMENTUM);
            for (int i = 0; i < periodos.length; i++) {
                calculaMomentum(periodos[i]);
            }
        }

        if (nomeParametros.verificaExisteParametro(pais, BIAS) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, BIAS);
            for (int i = 0; i < periodos.length; i++) {
                calculaBIAS(periodos[i]);
            }
        }

        if (nomeParametros.verificaExisteParametro(pais, AVERAGE_DIRECTIONAL_MOVEMENT_INDICATOR) > 0) {
            periodos = nomeParametros.getPeriodoParametro(pais, AVERAGE_DIRECTIONAL_MOVEMENT_INDICATOR);
            for (int i = 0; i < periodos.length; i++) {
                calculaADMI(periodos[i]);
            }
        }

    }

    //Cálcula média móvel para 5 períodos
    private void calculaSMA(int periodo) throws InsereParametrosException, NomeParametrosException {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        SMAIndicator smaIndicator = new SMAIndicator(closePrice, periodo);

        //Varre os indicadores obtidos
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), smaIndicator.getValue(i).toDouble(), pais, MOVING_AVERAGE, periodo);
        }
    }

    //Calcula o índice de força relativa
    private void calculaIFR(int periodo) throws InsereParametrosException, NomeParametrosException {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, periodo);
        //Varre os indicadores obtidos
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), rsiIndicator.getValue(i).toDouble(), pais, RELATIVE_STRENGTH_INDICATOR, periodo);
        }

    }

    //Calcula o índice Stochastic Oscilator K & D
    private void calculaStochasticOscilatorKD(int periodo) throws InsereParametrosException, NomeParametrosException {

        StochasticOscillatorKIndicator soki = new StochasticOscillatorKIndicator(timeSeries, periodo);
        StochasticOscillatorDIndicator sodi = new StochasticOscillatorDIndicator(soki);

        //Varre os indicadores obtidos
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), soki.getValue(i).toDouble(), pais, STOCHASTIC_OSCILATOR_KD, periodo);
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), sodi.getValue(i).toDouble(), pais, "SODI");
        }

    }

    //Calcula o índice On Balance Volume
    private void calculaOnBalanceVolume() throws InsereParametrosException, NomeParametrosException {

        OnBalanceVolumeIndicator onBalanceVolume = new OnBalanceVolumeIndicator(timeSeries);
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), onBalanceVolume.getValue(i).toDouble(), pais, ON_BALANCE_VOLUME);
        }
    }

    //Calcula o índice MACD
    private void calculaMACD(int periodoCurto, int periodoLongo) throws InsereParametrosException,
            NomeParametrosException {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        MACDIndicator mACD = new MACDIndicator(closePrice, periodoCurto, periodoLongo);
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), mACD.getValue(i).toDouble(), pais, MOVING_AVERAGE_CONVERGENCE_DIVERGENCE);
        }
    }

    //Calcula o índice Willians %R
    private void calculaWillianR(int periodo) throws InsereParametrosException, NomeParametrosException {
        WilliamsRIndicator williamsRIndicator = new WilliamsRIndicator(timeSeries, periodo);
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.insereValor(timeSeries.getTick(i).getEndTime().toDate(), williamsRIndicator.getValue(i).
                    toDouble(), pais, WILLIANS_R, periodo);
        }
    }

    //Calcula o índice Momentum
    private void calculaMomentum(int periodo) throws InsereParametrosException, NomeParametrosException {

        //Varre a série temporal
        for (int i = 0; i < timeSeries.getTickCount(); i++) {

            //Se estiver no inicio da série temporal
            if (periodo > i) {
                parametros.insereValor(timeSeries.getTick(i).getEndTime().toDate(), 0d, pais, MOMENTUM, periodo);
                continue;
            }
            //CLOSE n - CLOSE n-p
            Double momentum = timeSeries.getTick(i).getClosePrice().toDouble() - timeSeries.getTick(i - periodo).
                    getClosePrice().toDouble();
            parametros.insereValor(timeSeries.getTick(i).getEndTime().toDate(), momentum, pais, MOMENTUM, periodo);
        }

    }

    //Calcula o índice Price Rate Of Change
    private void calculaROC(int periodo) throws InsereParametrosException, NomeParametrosException {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        ROCIndicator rOCIndicator = new ROCIndicator(closePrice, periodo);

        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), rOCIndicator.getValue(i).toDouble(), pais, PRICE_RATE_OF_CHANGE, periodo);
        }
    }

    //Calcula o índice Price Volume Trend
    private void calculaPVT() throws InsereParametrosException, NomeParametrosException {

        Double pVTAnterior = 0d;
        //Varre a série temporal
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            //Se estiver na primeira ocorrência
            if (i == 0) {
                parametros.insereValor(timeSeries.getTick(i).getEndTime().toDate(), 0d, pais, PRICE_VOLUME_TREND);
                continue;
            }
            //Preço de fechamento atual
            Double closePrice = timeSeries.getTick(i).getClosePrice().toDouble();
            //Preço de fechamento anterior
            Double closePriceAnt = timeSeries.getTick(i - 1).getClosePrice().toDouble();
            //Volume atual
            Double volume = timeSeries.getTick(i).getVolume().toDouble();
            //VPTprev + Volume x ((CloseN - CloseN-1) / CloseN-1)
            Double pVT = pVTAnterior + volume * ((closePrice - closePriceAnt) / closePriceAnt);

            parametros.insereValor(timeSeries.getTick(i).getEndTime().toDate(), pVT, pais, PRICE_VOLUME_TREND);
            //Atualiza pVTAnterior
            pVTAnterior = pVT;
        }
    }

    //Calcula o índice BIAS
    private void calculaBIAS(int periodo) throws InsereParametrosException, NomeParametrosException {

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);
        SMAIndicator sMAIndicator = new SMAIndicator(closePriceIndicator, periodo);

        //Varre a série temporal
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            Double closePrice = timeSeries.getTick(i).getClosePrice().toDouble();
            Double sMA = sMAIndicator.getValue(i).toDouble();
            Double bIAS = ((closePrice - sMA) / sMA) * 100;
            parametros.insereValor(timeSeries.getTick(i).getEndTime().toDate(), bIAS, pais, BIAS, periodo);
        }
    }

    private void calculaADMI(int periodo) throws InsereParametrosException, NomeParametrosException {

        AverageDirectionalMovementIndicator admi = new AverageDirectionalMovementIndicator(timeSeries, periodo);
        //Varre a série temporal
        for (int i = 0; i < timeSeries.getTickCount(); i++) {
            parametros.
                    insereValor(timeSeries.getTick(i).getEndTime().toDate(), admi.getValue(i).toDouble(), pais, AVERAGE_DIRECTIONAL_MOVEMENT_INDICATOR, periodo);
        }

    }
}
