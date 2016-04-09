/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.MISC;

import java.text.NumberFormat;

/**
 *
 * @author Jean-NoteI5
 */
public class EditaValores {

    /*
     * Realiza a formatação do número para evitar que ele seja representado
     * como notação cientifica quando o seu valor for muito grande
     */
    public static String edita2Dec(Double valor) {
        NumberFormat f = NumberFormat.getInstance();
        f.setGroupingUsed(false);
        f.setMaximumFractionDigits(2);
        return f.format(valor.doubleValue()).replaceAll(",", ".");
    }

    public static String edita2DecVirgula(Double valor) {
        NumberFormat f = NumberFormat.getInstance();
        f.setGroupingUsed(false);
        f.setMaximumFractionDigits(2);
        return f.format(valor.doubleValue());
    }

    public static String editaVirgula(Double valor) {
        NumberFormat f = NumberFormat.getInstance();
        f.setGroupingUsed(false);        
        f.setMaximumFractionDigits(10);
        return f.format(valor.doubleValue());
    }
}
