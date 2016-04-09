/*
 * TCCMaven
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package tccmaven.SVM;

/**
 * Descrição da classe.
 */
public class ResultadoSVM {

    private double real;
    private double predict;
    private double percentualAcerto;
    private double diffMod;

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

}
