/*
 * MMM
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package MMM.ANALISADOR;

import MMM.SVM.ManipuladorParSVM;
import MMM.SVM.ParametroSVM;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Descrição da classe.
 */
public class Analisador {

    String nomArq;
    ManipuladorParSVM parametros;
    
    
    
    
    
    
    
    //Lê o arquivo
    //Pegar os resultados
    
    
    //Carregar um array de parâmetro e Array de Resultado
    //Analisar 
    
    
    //Analisar quais parâmetros obtiveram melhor desempenho nos 5 dias
    //Montar um Objeto de ParametrosSVM
    //Chamar a SVM
    //Retornar resultados em um objeto

    public Analisador() {
        
    }
    
    
    //Verifica qual a melhor combinação de resultados do algoritmo
    public int processaMelhor(){
        

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

        //Varre os parâmetro obtidos
        for (int i = 0; i < parametros.size(); i++) {
            //Se o parâmetro possui o mesmo ID do menor e for o último dia analisado
            if (parametros.get(i).getId() == iD && parametros.get(i).getDiaInicial() == 2){
                return i;
            }
        }        
        
        return -1;
        
    }
        
}
