/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//            CVParameterSelection ps = new CVParameterSelection();
//            ps.setClassifier(svm);
//            ps.setNumFolds(10);  // using 5-fold CV
//            ps.addCVParameter("G 0 10 100");
//            // build and output best options
//            ps.buildClassifier(train);
//            System.out.println(Utils.joinOptions(ps.getBestClassifierOptions()));
package MMM.SVM;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import MMM.MISC.Log;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.GridSearch;
import weka.core.Instances;
import weka.core.SelectedTag;

/**
 *
 * @author Jean-NoteI5
 */
public class WekaSVM implements Runnable {

    private Instances dataSet;
    private ParametroSVM parametrosSVM;
    private int exception = 0;

    public WekaSVM(String arqARFF, ParametroSVM parametrosSVM) throws WekaSVMException {
        dataSet = buildBase(arqARFF);
        this.parametrosSVM = parametrosSVM;
    }

    //Realiza o teste de performance do modelo construído
    public void perfomanceAnalysis() throws WekaSVMException, ParametroSVMException {

        //Se o SET de treino for maior que o SET disponível
        if ((parametrosSVM.getTamanhoDoConjunto() + parametrosSVM.getDiaInicial()) > dataSet.numInstances()) {
            throw new WekaSVMException("Set de treino muito grande");
        }

        long ini = System.currentTimeMillis();

        Instances train = new Instances(dataSet, dataSet.numInstances() - parametrosSVM.getDiaInicial() - parametrosSVM.
                getTamanhoDoConjunto(), parametrosSVM.getTamanhoDoConjunto());
        Instances test = new Instances(dataSet, dataSet.numInstances() - parametrosSVM.getDiaInicial(), 1);

        train.setClassIndex(train.numAttributes() - 1);
        test.setClassIndex(test.numAttributes() - 1);
        
        //Seta o valor do dia anterior
        parametrosSVM.setRealAnterior(train.instance(train.numInstances() - 1).classValue());
        
        LibSVM svm = buildSVM();
        svm = GridSearch(svm, train);
        constroiClassificador(svm, train);

        long fim = System.currentTimeMillis();

        Log.loga("EVALUATION:" + parametrosSVM.getGridSearchEvaluationAlfa() + " KERNEL:" + parametrosSVM.
                getKernelAlfa() + " TYPE:" + parametrosSVM.getTypeAlfa() + " COST:" + svm.getCost() + " gamma:" +
                svm.getGamma() + " TIME:" + (fim - ini));


        double real = test.instance(0).classValue();
        double predict = 0;
        try {
            predict = svm.classifyInstance(test.instance(0));
        } catch (Exception ex) {
            Logger.getLogger(WekaSVM.class.getName()).log(Level.SEVERE, null, ex);
            exception = 1;
        }

        //Estraga valores
        if (exception == 1){
            predict = 9999;
            real = 0.1;
        }

        //Valor real
        parametrosSVM.setReal(real);
        //Valor predito
        parametrosSVM.setPredict(predict);
        //Percentual de acerto
        parametrosSVM.setPercentualAcerto(real, predict);
        //Diferença em módulo
        parametrosSVM.setDiffMod(real, predict);
        //Atualiza os parâmetros com o custo e o gamma utilizado
        parametrosSVM.setCost(svm.getCost());
        parametrosSVM.setGamma(svm.getGamma());

    }

    private LibSVM buildSVM() throws WekaSVMException {
        try {

            LibSVM svm = new LibSVM();
            svm.setSVMType(new SelectedTag(parametrosSVM.getType(), LibSVM.TAGS_SVMTYPE));
            svm.setCacheSize(1000);
            svm.setCoef0(0.0);
            svm.setCost(1.0);
            svm.setDebug(false);
            svm.setDegree(3);
            svm.setDoNotReplaceMissingValues(true);
            svm.setEps(0.001);
            svm.setGamma(0.0);
            svm.setKernelType(new SelectedTag(parametrosSVM.getKernel(), LibSVM.TAGS_KERNELTYPE));
            svm.setLoss(0.1);
            svm.setNormalize(true);
            svm.setNu(0.5);
            svm.setProbabilityEstimates(true);
            svm.setShrinking(true);

            return svm;
        } catch (Exception ex) {
            exception = 1;
            throw new WekaSVMException("Não foi possível executar o algoritmo de predição");
        }
    }

    private void constroiClassificador(LibSVM svm, Instances train) throws WekaSVMException {

        try {
            svm.buildClassifier(train);
        } catch (Exception ex) {
            exception = 1;
            Log.loga("Excecão Classifier", "EXCECÃO");
        }

    }

    /*

     -d degree : set degree in kernel function (default 3)
     -g gamma : set gamma in kernel function (default 1/num_features)
     -r coef0 : set coef0 in kernel function (default 0)
     -c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)
     -n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
     -p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)
     -m cachesize : set cache memory size in MB (default 100)
     -e epsilon : set tolerance of termination criterion (default 0.001)
     -h shrinking: whether to use the shrinking heuristics, 0 or 1 (default 1)
     -b probability_estimates: whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
     -wi weight: set the parameter C of class i to weight*C, for C-SVC (default 1)

     */
    private LibSVM GridSearch(LibSVM svm, Instances train) throws WekaSVMException {

        GridSearch gridSearch = new GridSearch();
        gridSearch.setDebug(false);
        gridSearch.setClassifier(svm);
        gridSearch.setEvaluation(new SelectedTag(parametrosSVM.getGridSearchEvaluation(), GridSearch.TAGS_EVALUATION));

        //evalaute C 12^-5, 2^-4,..,2^2.
        gridSearch.setXProperty("classifier.cost");
        gridSearch.setXMin(-10);
        gridSearch.setXMax(20);
        gridSearch.setXStep(1);
        gridSearch.setXBase(2);
        gridSearch.setXExpression("pow(BASE,I)");

        //Kernel Linear não possui o parâmetro gama
        if (parametrosSVM.getKernel() == LibSVM.KERNELTYPE_LINEAR) {

            // evaluate gamma s 2^-5, 2^-4,..,2^2.
            gridSearch.setYProperty("classifier.gamma");
            gridSearch.setYMin(1);
            gridSearch.setYMax(2);
            gridSearch.setYStep(1);
            gridSearch.setYBase(2);
            gridSearch.setYExpression("pow(BASE,I)");



        } else {
            // evaluate gamma s 2^-5, 2^-4,..,2^2.
            gridSearch.setYProperty("classifier.gamma");
            gridSearch.setYMin(-15);
            gridSearch.setYMax(10);
            gridSearch.setYStep(1);
            gridSearch.setYBase(2);
            gridSearch.setYExpression("pow(BASE,I)");
        }

        try {
            gridSearch.buildClassifier(train);
        } catch (Exception ex) {
            exception = 1;
            Log.loga("Excessão no Grid Search");
            ex.printStackTrace(Log.getDefOut());
            Log.loga(ex.getMessage());
        }

        LibSVM bestClassifier = (LibSVM) gridSearch.getBestClassifier();

        return bestClassifier;

    }

    //Monta a base de dados
    private Instances buildBase(String arqARFF) throws WekaSVMException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(arqARFF));
            Instances train = new Instances(reader);
            //Define o atributo de classe (Atributo que será predito)
            train.setClassIndex(train.numAttributes() - 1);
            reader.close();
            return train;
        } catch (IOException ex) {
            exception = 1;
            throw new WekaSVMException("Não foi possível executar o algoritmo de predição");
        }
    }

    public Instances getDataSet() {
        return dataSet;
    }

    @Override
    public void run() {
        try {
            perfomanceAnalysis();

        } catch (WekaSVMException ex) {
            exception = 1;
            Logger.getLogger(WekaSVM.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParametroSVMException ex) {
            exception = 1;
            Logger.getLogger(WekaSVM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
