/*
 * MMM
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package MMM.ANALISADOR;

import MMM.SVM.ManipuladorParametroSVM;
import MMM.SVM.ParametroSVM;
import eu.verdelhan.ta4j.Tick;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.joda.time.DateTime;

/**
 * Descrição da classe.
 */
public class Analisador {

    String nomArqCSV;
    ManipuladorParametroSVM parametros;
    
    
    
    
    
    
    
    //Lê o arquivo
    //Pegar os resultados
    
    
    //Carregar um array de parâmetro e Array de Resultado
    //Analisar 
    
    
    //Analisar quais parâmetros obtiveram melhor desempenho nos 5 dias
    //Montar um Objeto de ParametrosSVM
    //Chamar a SVM
    //Retornar resultados em um objeto

    
    
    public void analisa(){
        
        
        
    }
    
    
    //Carrega dados do arquivo CSV
    private ManipuladorParametroSVM carregaCSV() {
        
        
//        try {
//        //Abre arquivo CSV
//        BufferedReader br = new BufferedReader(new FileReader(nomArqCSV));
//        
//           //Descarta a primeira linha
//            br.readLine();
//
//            //Varre o arquivo
//            while (true) {
//                String linha = br.readLine();
//
//                if (linha == null) {
//                    break;
//                }
//                
//                parametros.ParametroSVM.desmontaLinha(linha);
//            }
//        
//        } catch (Exception ex){
//            throw AnalisadorException("Não foi possível importar o arquivo CSV para análise");
//        }
//        
        
    return null;
    }
    
    
    //Verifica qual a melhor combinação de resultados do algoritmo
    private ParametroSVM encontraMelhoresParametros() throws AnalisadorException, CloneNotSupportedException{
        

        //Monta HASH MAP para agrupar os resultados dos parâmetros repetidos
        HashMap<Long, Double> resultados = new HashMap<>();
        
        //Obtém ARRAY de parâmetros montados a partir do CSV
        ArrayList<ParametroSVM> parametros = this.parametros.getParametroSVM();
        
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
        for (Long chave : chaves){
            //Se o resultado é menor que o valorMenor processado
            if (resultados.get(chave) < valorMenor){
                valorMenor = resultados.get(chave);
                iD = chave;                
            }            
        }

        ParametroSVM parametroRetorno = null;
        
        //Varre os parâmetro obtidos
        for (int i = 0; i < parametros.size(); i++) {
            //Se o parâmetro possui o mesmo ID do menor e for o último dia analisado
            if (parametros.get(i).getId() == iD && parametros.get(i).getDiaInicial() == 2){
                parametroRetorno = parametros.get(i).clone();
            }
        }        
        
        if (parametroRetorno == null){
            throw new AnalisadorException("Não foi possível encontrar o melhor parâmetro");
        }
        
        //Indica que processará o primeiro dia da série
        parametroRetorno.setDiaInicial(1);        
        
        return parametroRetorno;       
    }
        
}
