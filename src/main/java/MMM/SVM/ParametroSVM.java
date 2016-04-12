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
public class ParametroSVM {

    //PARAMETROS INICIAS
    private final int diaInicial; // Dia inicial para treino do conjunto (De trás para frente)
    private final int tamanhoDoConjunto; // Tamanho do conjunto de treino
    private final int gridSearchEvaluation; //Tipo de avaliação do GridSearch
    private final int kernel; //Kernel que será utilizado na SVM
    private final int type; //Tipo da SVM, usando apenas os dois tipos que são direcionados para problemas de regressão

    //PARAMETROS ATRAVÉS DE GRID SEARCH
    private double gamma;
    private double cost;

    //RESULTADOS OBTIDOS ATRAVÉS DE SVM    
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

    //Monta o cabeçalho da classe
    public String montaCabecalho() {
        return "dia_inicial;cost;gamma;tam_treino;evaluation;evaluationAlfa;kernel;kernelAlfa;type;typeAlfa;valor_real;valor_predito;diffMod;perc_acerto";
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
        linha.append(EditaValores.edita2DecVirgula(real));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(predict));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(diffMod));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(percentualAcerto));

        return linha.toString();

    }
    
    //Obtém ID único dos parâmetros da máquina de vetor de suporte
    public long getId(){
        
        long tamGoedel = 2 * tamanhoDoConjunto;
        long gridSearchGoedel = 3 * gridSearchEvaluation;
        long kernelGoedel = 5 * kernel;
        long typeGoedel = 7 * type;
        
        return tamGoedel * gridSearchGoedel * kernelGoedel * typeGoedel;
    } 
    
}
