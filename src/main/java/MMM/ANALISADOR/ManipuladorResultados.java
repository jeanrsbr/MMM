/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.ANALISADOR;

import java.util.ArrayList;

/**
 *
 * @author Jean-NoteI5
 */
public class ManipuladorResultados {
    
    ArrayList<Resultado> resultados;

    public ManipuladorResultados() {
        
        resultados = new ArrayList<>();
    }
    
    //Adiciona resultado
    public void addResultado(Resultado resultado){
        resultados.add(resultado);
    }
}
