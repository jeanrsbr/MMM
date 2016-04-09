/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.MISC;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 *
 * @author Jean-NoteI5
 */
public class Log {

    private static final int LOG = Integer.parseInt(LeituraProperties.getInstance().leituraProperties("prop.log"));
    private static PrintStream defOut;
    private static PrintStream defErr;

    public static void iniBuf() throws FileNotFoundException {
//        defOut = System.out;
//        defErr = System.err;

        defOut = new PrintStream(System.out);
        defErr = new PrintStream(System.err);

        System.setOut(new PrintStream("c:/temp/output_weka_out.txt"));
        System.setErr(new PrintStream("c:/temp/output_weka_err.txt"));

    }

    public static void loga(String mensagem) {
        if (LOG == 1) {
            defOut.println(mensagem);
        }
    }

    public static void loga(String mensagem, String titulo) {
        if (LOG == 1) {
            defOut.println("[" + String.format("%12s", titulo) + "]:" + mensagem);
        }
    }

    public static PrintStream getDefOut() {
        return defOut;
    }

    public static PrintStream getDefErr() {
        return defErr;
    }


}
