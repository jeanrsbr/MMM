/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.ARFF;

import MMM.ARFF.PARAMETROS.IndicadoresException;
import MMM.ARFF.PARAMETROS.Indicadores;
import MMM.ARFF.PARAMETROS.NomeParametros;
import MMM.ARFF.PARAMETROS.NomeParametrosException;
import MMM.ARFF.PARAMETROS.InsereParametros;
import MMM.ARFF.PARAMETROS.InsereParametrosException;
import MMM.ARFF.PARAMETROS.ValidaParametros;
import MMM.ARFF.PARAMETROS.ManipulaParametros;
import eu.verdelhan.ta4j.TimeSeries;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import MMM.MISC.LeituraProperties;
import MMM.MISC.Log;
import MMM.ARFF.IMPORT.BaixaArquivoException;
import MMM.ARFF.IMPORT.Importador;
import MMM.ARFF.IMPORT.ImportadorException;
import MMM.MISC.ClientFTP;
import MMM.MISC.ClienteFTPException;
import MMM.MISC.EditaValores;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Jean-NoteI5
 */
public class GeraArquivoARFF {

    private final String ativoBrasil;
    private final String ativoEst;
    private final Date dataInicial;
    private final Date dataFinal;

    public GeraArquivoARFF(String ativoBrasil, String ativoUsa, Date dataInicial, Date dataFinal) {
        this.ativoBrasil = ativoBrasil;
        this.ativoEst = ativoUsa;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;

    }

    //Gera o arquivo ARFF
    public String geraArquivo() throws GeraArquivoARFFException, ImportadorException, InsereParametrosException,
            BaixaArquivoException, IndicadoresException, NomeParametrosException, ClienteFTPException {

        //------------------------- IDENTIFICAÇÃO DOS PARAMÊTROS  --------------------
        //Obtém a lista de ativos que devem ser importados
        NomeParametros nomeParametros = new NomeParametros(LeituraProperties.getInstance().
                leituraPropertiesString("ind.indicadores").split(";"));

        //------------------------- INSERÇÃO DOS PARAMETROS --------------------
        //Instância os parâmetros com o primeiro ativo
        InsereParametros insereParametros = new InsereParametros(nomeParametros);

        //Baixa arquivo CSV e Converte arquivo para memória
        Log.loga("Importando o ativo " + ativoBrasil, "INSERÇÃO");
        Importador importador = new Importador(ativoBrasil, dataInicial, dataFinal);
        TimeSeries timeseries = importador.montaTimeSeries();
        Log.loga("Criada série temporal do ativo " + ativoBrasil + " com " + timeseries.getTickCount()
                + " registros", "INSERÇÃO");
        insereParametros.insereSerieTemporalBrasil(timeseries);
        Log.loga("Inseridos parâmetros do ativo " + ativoBrasil + " com " + insereParametros.getNumReg()
                + " registros", "INSERÇÃO");

        //Calcula indicadores
        Log.loga("Serão calculados os indicadores do ativo " + ativoBrasil, "INSERÇÃO");
        //Instância os indicadores referenciando os parâmetros
        Indicadores indicadoresBra = new Indicadores(insereParametros, timeseries, nomeParametros);
        indicadoresBra.setPaisBrasil();
        indicadoresBra.calculaIndicadoresSerie();

        //Se possui ativo de outro país
        if (ativoEst != null) {
            //Baixa arquivo CSV e Converte arquivo para memória
            Log.loga("Importando o ativo " + ativoEst, "INSERÇÃO");
            importador = new Importador(ativoEst, dataInicial, dataFinal);
            timeseries = importador.montaTimeSeries();
            Log.loga("Criada série temporal do ativo " + ativoEst + " com " + timeseries.getTickCount() + " registros", "INSERÇÃO");
            insereParametros.insereSerieTemporalEstrangeiro(timeseries);
            Log.loga("Inseridos parâmetros do ativo " + ativoEst + " com " + insereParametros.getNumReg() + " registros", "INSERÇÃO");

            //Calcula indicadores
            Log.loga("Serão calculados os indicadores do ativo " + ativoEst, "INSERÇÃO");
            //Instância os indicadores referenciando os parâmetros
            Indicadores indicadoresEst = new Indicadores(insereParametros, timeseries, nomeParametros);
            indicadoresEst.setPaisEstrangeiro();
            indicadoresEst.calculaIndicadoresSerie();
        }

        //------------------------- AJUSTE DOS PARAMETROS --------------------
        ArrayList<double[]> lista = insereParametros.getParametros();

        //Cria lista dos parâmetros
        ManipulaParametros manipulaParametros = new ManipulaParametros(lista, ativoBrasil);

        //Inicia ajustes da base de dados
        Log.loga("Iniciando ajuste da base de dados", "AJUSTE");
        //Balanceia os parâmetros (Feriados, dias sem pregão, dias sem movimento)
        manipulaParametros.balance();
        Log.loga("Será inserida a variável alvo", "AJUSTE");
        manipulaParametros.criaTarget(nomeParametros.getOcoTarget());

        //------------------------- VALIDA OS PARAMETROS --------------------
        int valida = LeituraProperties.getInstance().leituraPropertiesInteiro("prop.valida");
        //Se deve validar
        if (valida == 1) {
            Log.loga("Iniciando etapa de validação dos dados", "VALIDAÇÃO");
            ValidaParametros validaParametros = new ValidaParametros(manipulaParametros.getListaParametros(), nomeParametros);
            //Se encontrou inconsistência nas validações
            if (!validaParametros.validaDados()) {
                throw new GeraArquivoARFFException("Foi encontrada inconsistência na geração dos parâmetros");
            }
            Log.loga("Os dados estão validos", "VALIDAÇÃO");
        }
        //Retorna o nome do arquivo gerado
        return geraArquivo(manipulaParametros, nomeParametros);
    }

    //Gera arquivo ARFF
    private String geraArquivo(ManipulaParametros manipulaParametros, NomeParametros nomeParametros) throws
            GeraArquivoARFFException, InsereParametrosException, ClienteFTPException {

        try {

            File dir = new File(ARFFConstants.ARFF_FOLDER);
            //Se o diretório existe
            if (!dir.exists()) {
                Log.loga("O diretório " + dir.getAbsolutePath() + " para criação do arquivo ARFF não existe", "ARFF");
                throw new GeraArquivoARFFException("Não foi possível gerar o arquivo ARFF");
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

            //Abre o arquivo
            File file = new File(ARFFConstants.ARFF_FOLDER + formatter.format(dataFinal) + "_" + manipulaParametros.getAtivo() + ARFFConstants.ARFF_EXT);
            Log.loga("Arquivo ARFF: " + file.getAbsolutePath(), "ARFF");

            FileOutputStream arquivoGravacao = new FileOutputStream(file);
            OutputStreamWriter strWriter = new OutputStreamWriter(arquivoGravacao);
            BufferedWriter writer = new BufferedWriter(strWriter);

            writer.write("% This is a dataset obtained from the YAHOO FINANCES. Here is the included description:");
            writer.newLine();
            writer.write("%");
            writer.newLine();

            formatter = new SimpleDateFormat("dd/MM/yyyy");

            writer.
                    write(new String("% The data provided are daily stock prices from #INICIO# through #FIM#, for #ATIVO#.").
                            replaceAll("#INICIO#", formatter.format(dataInicial)).
                            replaceAll("#FIM#", formatter.format(dataFinal)).
                            replaceAll("#ATIVO#", manipulaParametros.getAtivo()));
            writer.newLine();
            writer.write("%");
            writer.newLine();
            writer.write("% Source: collection of regression datasets by Jean Felipe Hartz (jeanrsbr@gmail.com) at");
            writer.newLine();
            writer.write("% http://real-chart.finance.yahoo.com ");
            writer.newLine();
            writer.write(new String("% Characteristics: #CASES# cases, #ATTRIB# continuous attributes").
                    replaceAll("#CASES#", Integer.toString(manipulaParametros.getNumReg())).
                    replaceAll("#ATTRIB#", Integer.toString(nomeParametros.getNumPar())));
            writer.newLine();
            writer.newLine();
            writer.write("@relation stock");
            writer.newLine();
            writer.newLine();

            //Imprime o nome dos parâmetros
            for (int i = 0; i < nomeParametros.getNumPar(); i++) {
                writer.write("@attribute " + nomeParametros.getNomeParametros()[i] + " numeric");
                writer.newLine();
            }

            writer.newLine();
            writer.write("@data");
            writer.newLine();

            //Obtém a lista de parâmetros
            ArrayList<double[]> lista = manipulaParametros.getListaParametros();
            //Varre a lista de parâmetros
            for (int i = 0; i < lista.size(); i++) {

                StringBuilder linha = new StringBuilder();
                //Obtém os valores da lista
                double[] valores = lista.get(i);

                for (int j = 0; j < valores.length; j++) {
                    linha.append(EditaValores.edita2Dec(valores[j]));
                    linha.append(",");
                }
                //Remove a última vírgula da literal
                linha.delete(linha.length() - 1, linha.length());

                //Exporta a linha para o arquivo
                writer.write(linha.toString());
                writer.newLine();

            }
            //Fecha o arquivo
            writer.close();

            //Se utiliza FTP
            if (LeituraProperties.getInstance().leituraPropertiesInteiro("ftp.ftp") == 1) {
                ClientFTP clientFTP = new ClientFTP();
                clientFTP.sendFile(file.getAbsolutePath(), ARFFConstants.ARFF_FOLDER);
            }

            return file.getAbsolutePath();

        } catch (IOException | IllegalArgumentException ex) {
            throw new GeraArquivoARFFException("Ocorreu erro no momento de gerar o arquivo ARFF", ex);
        }

    }
}
