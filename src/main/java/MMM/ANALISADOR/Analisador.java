/*
 * MMM
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package MMM.ANALISADOR;

import MMM.SVM.ManipuladorParametroSVM;
import MMM.SVM.ParametroSVM;
import MMM.SVM.ParametroSVMException;
import MMM.SVM.SVMExecutor;
import MMM.SVM.WekaSVMException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Descrição da classe.
 */
public class Analisador {

    private String nomArqCSV;
    private String nomArqARFF;

    public Resultado analisa() throws AnalisadorException {

        try {
            //Encontra a linha de parâmetros que obteve o melhor resultado
            ParametroSVM parametroSVM = encontraMelhoresParametros(carregaCSV());
            //Executar algoritmo SVM
            SVMExecutor sVMAnalisador = new SVMExecutor(nomArqARFF);
            sVMAnalisador.executaAnalise(parametroSVM);
            //Incluir objeto de resultado
            return new Resultado(nomArqARFF.split(".ARFF")[0], parametroSVM.getRealAnterior(), parametroSVM.getPredict(), 0);
            
            
        } catch (IOException | ParametroSVMException | AnalisadorException | CloneNotSupportedException | WekaSVMException ex) {
            throw new AnalisadorException("Não foi possível realizar a análise do arquivo de resultados");
        }

    }

    //Carrega dados do arquivo CSV
    private ManipuladorParametroSVM carregaCSV() throws FileNotFoundException, IOException, ParametroSVMException {

        ManipuladorParametroSVM manipuladorParametroSVM = new ManipuladorParametroSVM();
        //Abre arquivo CSV
        BufferedReader br = new BufferedReader(new FileReader(nomArqCSV));

        //Descarta a primeira linha
        br.readLine();

        //Varre o arquivo
        while (true) {
            String linha = br.readLine();

            if (linha == null) {
                break;
            }
            //Inclui o parâmetro no array
            manipuladorParametroSVM.addParametro(ParametroSVM.desmontaLinha(linha));
        }
        return manipuladorParametroSVM;

    }

    //Verifica qual a melhor combinação de resultados do algoritmo
    private ParametroSVM encontraMelhoresParametros(ManipuladorParametroSVM manipuladorParametroSVM) throws AnalisadorException, CloneNotSupportedException {

        //Monta HASH MAP para agrupar os resultados dos parâmetros repetidos
        HashMap<Long, Double> resultados = new HashMap<>();

        //Obtém ARRAY de parâmetros montados a partir do CSV
        ArrayList<ParametroSVM> parametros = manipuladorParametroSVM.getParametroSVM();

        //Varre os parâmetro obtidos
        for (int i = 0; i < parametros.size(); i++) {
            //Soma o valor ao conjunto
            Double valor = resultados.get(parametros.get(i).getId());
            valor = valor + parametros.get(i).getDiffMod();
            resultados.put(parametros.get(i).getId(), valor);
        }

        //Identifica qual ocorrência possui o menor valor
        Set<Long> chaves = resultados.keySet();
        Long iD = 0l;
        Double valorMenor = Double.MAX_VALUE;
        //Varre as chaves procurando o menor valor
        for (Long chave : chaves) {
            //Se o resultado é menor que o valorMenor processado
            if (resultados.get(chave) < valorMenor) {
                valorMenor = resultados.get(chave);
                iD = chave;
            }
        }

        ParametroSVM parametroRetorno = null;

        //Varre os parâmetro obtidos
        for (int i = 0; i < parametros.size(); i++) {
            //Se o parâmetro possui o mesmo ID do menor e for o último dia analisado
            if (parametros.get(i).getId() == iD && parametros.get(i).getDiaInicial() == 2) {
                parametroRetorno = parametros.get(i).clone();
            }
        }

        if (parametroRetorno == null) {
            throw new AnalisadorException("Não foi possível encontrar o melhor parâmetro");
        }

        //Indica que processará o primeiro dia da série
        parametroRetorno.setDiaInicial(1);

        return parametroRetorno;
    }

}
