/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.SVM;

import java.util.ArrayList;
import tccmaven.MISC.LeituraProperties;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.GridSearch;

/**
 *
 * @author Jean-NoteI5
 */
public class ManipuladorParametroSVM {

    private final ArrayList<ParametroSVM> parametroSVM;
    private final int diasConjuntoTeste;
    private final int tamConjuntoIni;
    private final int tamConjuntoFin;

    public ManipuladorParametroSVM() {
        this.diasConjuntoTeste = Integer.parseInt(LeituraProperties.getInstance().
                leituraProperties("svm.diasconjuntoteste"));
        this.parametroSVM = new ArrayList<>();
        this.tamConjuntoIni = Integer.parseInt(LeituraProperties.getInstance().leituraProperties("svm.tamconjuntoini"));
        this.tamConjuntoFin = Integer.parseInt(LeituraProperties.getInstance().leituraProperties("svm.tamconjuntofin"));
    }

    public ArrayList<ParametroSVM> getParametroSVM() {
        return parametroSVM;
    }

    public void populaAnalise() {
        //Começa em dois para descartar o último dia do conjunto onde não sabemos a cotação de amanhã e
        //não temos como comparar com o valor predito
        for (int i = 2; i < diasConjuntoTeste + 2; i++) {
            populaTamanhoDoConjunto(i);
        }
    }

    private void populaTamanhoDoConjunto(int diaInicial) {
        //Varre os conjuntos possíveis
        for (int i = tamConjuntoIni; i <= tamConjuntoFin; i = i + 10) {
            populaGridSearchEvaluation(diaInicial, i);
        }
    }

    private void populaGridSearchEvaluation(int diaInicial, int tamanhoDoConjunto) {
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_MAE);
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_RAE);
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_RMSE);
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_RRSE);
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_COMBINED);

    }

    private void populaKernel(int diaInicial, int tamanhoDoConjunto, int gridSearchEvaluation) {
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_RBF);
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_SIGMOID);
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_POLYNOMIAL);
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_LINEAR);
    }

    private void populaTipo(int diaInicial, int tamanhoDoConjunto, int gridSearchEvaluation, int kernel) {
        gravaParametro(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, kernel, LibSVM.SVMTYPE_EPSILON_SVR);

//        //Se não for o Kernel Polynomial (Demora para caralho)
//        if (kernel != LibSVM.KERNELTYPE_POLYNOMIAL) {
//            gravaParametro(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, kernel, LibSVM.SVMTYPE_NU_SVR);
//        }

    }

    private void gravaParametro(int diaInicial, int tamanhoDoConjunto, int gridSearchEvaluation, int kernel, int type) {
        parametroSVM.add(new ParametroSVM(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, kernel, type));
    }
}
