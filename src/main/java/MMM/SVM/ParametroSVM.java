/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.SVM;

import MMM.MISC.EditaValores;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.GridSearch;

/**
 *
 * @author Jean-NoteI5
 */
public class ParametroSVM implements Cloneable {

    //PARAMETROS INICIAS
    private int diaInicial; // Dia inicial para treino do conjunto (De trás para frente)
    private final int tamanhoDoConjunto; // Tamanho do conjunto de treino
    private final int gridSearchEvaluation; //Tipo de avaliação do GridSearch
    private final int kernel; //Kernel que será utilizado na SVM
    private final int type; //Tipo da SVM, usando apenas os dois tipos que são direcionados para problemas de regressão

    //PARAMETROS ATRAVÉS DE GRID SEARCH
    private double gamma;
    private double cost;

    //RESULTADOS OBTIDOS ATRAVÉS DE SVM
    private double realAnterior; //Valor do dia anterior, para comparar no analisador
    private double real;
    private double predict;
    private double diffMod;
    private double percentualAcerto;

    public ParametroSVM(int diaInicial, int tamanhoDoConjunto, int gridSearchEvaluation, int kernel, int type) throws ParametroSVMException {
        this.diaInicial = diaInicial;
        this.tamanhoDoConjunto = tamanhoDoConjunto;
        this.gridSearchEvaluation = gridSearchEvaluation;
        this.kernel = kernel;
        this.type = type;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getDiaInicial() {
        return diaInicial;
    }

    public int getTamanhoDoConjunto() {
        return tamanhoDoConjunto;
    }

    public int getGridSearchEvaluation() {
        return gridSearchEvaluation;
    }

    public int getKernel() {
        return kernel;
    }

    public int getType() {
        return type;
    }

    public void setDiaInicial(int diaInicial) {
        this.diaInicial = diaInicial;
    }




    public String getGridSearchEvaluationAlfa() throws ParametroSVMException {

        switch (gridSearchEvaluation) {
            case GridSearch.EVALUATION_COMBINED:
                return "COMBINED";
            case GridSearch.EVALUATION_MAE:
                return "MAE";
            case GridSearch.EVALUATION_RAE:
                return "RAE";
            case GridSearch.EVALUATION_RMSE:
                return "RMSE";
            case GridSearch.EVALUATION_RRSE:
                return "RRSE";
            default:
                throw new ParametroSVMException("GridSearch com opção não reconhecida");
        }

    }

    public String getKernelAlfa() throws ParametroSVMException {

        switch (kernel) {
            case LibSVM.KERNELTYPE_RBF:
                return "RBF";
            case LibSVM.KERNELTYPE_POLYNOMIAL:
                return "Polynomial";
            case LibSVM.KERNELTYPE_SIGMOID:
                return "Sigmoid";
            case LibSVM.KERNELTYPE_LINEAR:
                return "Linear";
            default:
                throw new ParametroSVMException("Kernel com opção não reconhecida");
        }

    }

    public String getTypeAlfa() throws ParametroSVMException {
        switch (type) {
            case LibSVM.SVMTYPE_NU_SVR:
                return "NU_SVR";
            case LibSVM.SVMTYPE_EPSILON_SVR:
                return "EPSILON_SVR";
            default:
                throw new ParametroSVMException("Não encontrei o tipo da SVM");
        }
    }

    public double getReal() {
        return real;
    }

    public void setReal(double real) {
        this.real = real;
    }

    public double getPredict() {
        return predict;
    }

    public void setPredict(double predict) {
        this.predict = predict;
    }

    public double getPercentualAcerto() {
        return percentualAcerto;
    }

    public void setPercentualAcerto(double real, double predict) {
        percentualAcerto = (predict * 100) / real;
    }

    public double getDiffMod() {
        return diffMod;
    }

    public void setDiffMod(double real, double predict) {
        //Diferença em módulo
        diffMod = real - predict;
        if (diffMod < 0) {
            diffMod = diffMod * -1;
        }
    }

    public double getRealAnterior() {
        return realAnterior;
    }

    public void setRealAnterior(double realAnterior) {
        this.realAnterior = realAnterior;
    }

    //Monta o cabeçalho da classe
    public String montaCabecalho() {
        return "ativo;dia_inicial;cost;gamma;tam_treino;evaluation;evaluationAlfa;kernel;kernelAlfa;type;typeAlfa;valor_real_anterior;valor_real;valor_predito;diffMod;perc_acerto";
    }

    public String montaLinha() throws ParametroSVMException {
        StringBuilder linha = new StringBuilder();

        linha.append(diaInicial);
        linha.append(";");
        linha.append(EditaValores.editaVirgula(cost));
        linha.append(";");
        linha.append(EditaValores.editaVirgula(gamma));
        linha.append(";");
        linha.append(tamanhoDoConjunto);
        linha.append(";");
        linha.append(gridSearchEvaluation);
        linha.append(";");
        linha.append(getGridSearchEvaluationAlfa());
        linha.append(";");
        linha.append(kernel);
        linha.append(";");
        linha.append(getKernelAlfa());
        linha.append(";");
        linha.append(type);
        linha.append(";");
        linha.append(getTypeAlfa());
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(realAnterior));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(real));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(predict));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(diffMod));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(percentualAcerto));

        return linha.toString();

    }

    //Criar parametroSVM através da linha do CSV
    public static ParametroSVM desmontaLinha(String linha) throws ParametroSVMException{

        //Obtém a linha desmontada
        String[] linhaDesmontada = linha.split(";");
        int diaInicialCSV = Integer.parseInt(linhaDesmontada[0]);
        double costCSV = Double.parseDouble(linhaDesmontada[1].replaceAll(",", "."));
        double gammaCSV = Double.parseDouble(linhaDesmontada[2].replaceAll(",", "."));
        int tamanhoDoConjuntoCSV = Integer.parseInt(linhaDesmontada[3]);
        int gridSearchEvaluationCSV = Integer.parseInt(linhaDesmontada[4]);
        //linha.append(getGridSearchEvaluationAlfa());
        int kernelCSV = Integer.parseInt(linhaDesmontada[6]);
        //linha.append(getKernelAlfa());
        int typeCSV = Integer.parseInt(linhaDesmontada[8]);
        //linha.append(getTypeAlfa());
        double realAnteriorCSV = Double.parseDouble(linhaDesmontada[10].replaceAll(",", "."));
        double realCSV = Double.parseDouble(linhaDesmontada[11].replaceAll(",", "."));
        double predictCSV = Double.parseDouble(linhaDesmontada[12].replaceAll(",", "."));
        double diffModCSV = Double.parseDouble(linhaDesmontada[13].replaceAll(",", "."));
        double percentualAcertoCSV = Double.parseDouble(linhaDesmontada[14].replaceAll(",", "."));

        //Monta o parâmetro de acordo com o CSV
        ParametroSVM parametroSVM = new ParametroSVM(diaInicialCSV, tamanhoDoConjuntoCSV, gridSearchEvaluationCSV, kernelCSV, typeCSV);
        parametroSVM.setCost(costCSV);
        parametroSVM.setGamma(gammaCSV);
        parametroSVM.setRealAnterior(realAnteriorCSV);
        parametroSVM.setReal(realCSV);
        parametroSVM.setPredict(predictCSV);
        parametroSVM.setPercentualAcerto(realCSV, predictCSV);
        parametroSVM.setDiffMod(realCSV, predictCSV);

        return parametroSVM;
    }

    //Obtém ID único dos parâmetros da máquina de vetor de suporte
    public long getId() {

        long tamGoedel = 2 * tamanhoDoConjunto;
        long gridSearchGoedel = 3 * gridSearchEvaluation;
        long kernelGoedel = 5 * kernel;
        long typeGoedel = 7 * type;

        return tamGoedel * gridSearchGoedel * kernelGoedel * typeGoedel;
    }

    public ParametroSVM clone() throws CloneNotSupportedException {
        return (ParametroSVM) super.clone();
    }
}
