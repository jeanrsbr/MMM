/*
 * TCCMaven
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package MMM.SVM;

import MMM.MISC.LeituraProperties;
import java.util.ArrayList;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.GridSearch;

/**
 * Descrição da classe.
 */
public class ManipuladorParametroSVM {

    private final ArrayList<ParametroSVM> parametroSVM;
    private final int diasConjuntoTeste;
    private final int tamConjuntoIni;
    private final int tamConjuntoFin;

    public ManipuladorParametroSVM() {
        this.diasConjuntoTeste = LeituraProperties.getInstance().leituraPropertiesInteiro("svm.diasconjuntoteste");
        this.parametroSVM = new ArrayList<>();
        this.tamConjuntoIni = LeituraProperties.getInstance().leituraPropertiesInteiro("svm.tamconjuntoini");
        this.tamConjuntoFin = LeituraProperties.getInstance().leituraPropertiesInteiro("svm.tamconjuntofin");

    }

    //Inclui ocorrência de parâmetro
    public void addParametro(ParametroSVM par) {
        parametroSVM.add(par);
    }

    public ArrayList<ParametroSVM> getParametroSVM() {
        return parametroSVM;
    }

    public void populaAnalise() throws ParametroSVMException {
        //Começa em dois para descartar o último dia do conjunto onde não sabemos a cotação de amanhã e
        //não temos como comparar com o valor predito
        for (int i = 2; i < diasConjuntoTeste + 2; i++) {
            populaTamanhoDoConjunto(i);
        }
    }

    private void populaTamanhoDoConjunto(int diaInicial) throws ParametroSVMException {
        //Varre os conjuntos possíveis
        for (int i = tamConjuntoIni; i <= tamConjuntoFin; i = i + 10) {
            populaGridSearchEvaluation(diaInicial, i);
        }
    }

    private void populaGridSearchEvaluation(int diaInicial, int tamanhoDoConjunto) throws ParametroSVMException {
//        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_MAE);
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_RAE);
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_RMSE);
//        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_RRSE);
        populaKernel(diaInicial, tamanhoDoConjunto, GridSearch.EVALUATION_COMBINED);

    }

    private void populaKernel(int diaInicial, int tamanhoDoConjunto, int gridSearchEvaluation) throws
            ParametroSVMException {
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_RBF);
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_SIGMOID);
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_POLYNOMIAL);
        populaTipo(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, LibSVM.KERNELTYPE_LINEAR);
    }

    private void populaTipo(int diaInicial, int tamanhoDoConjunto, int gridSearchEvaluation, int kernel) throws
            ParametroSVMException {
        gravaParametro(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, kernel, LibSVM.SVMTYPE_EPSILON_SVR);

//        //Se não for o Kernel Polynomial (Demora para caralho)
//        if (kernel != LibSVM.KERNELTYPE_POLYNOMIAL) {
//            gravaParametro(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, kernel, LibSVM.SVMTYPE_NU_SVR);
//        }
    }

    private void gravaParametro(int diaInicial, int tamanhoDoConjunto, int gridSearchEvaluation, int kernel, int type)
            throws ParametroSVMException {
        parametroSVM.add(new ParametroSVM(diaInicial, tamanhoDoConjunto, gridSearchEvaluation, kernel, type));
    }

}
