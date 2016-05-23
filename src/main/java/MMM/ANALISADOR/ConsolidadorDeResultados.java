/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.ANALISADOR;

import MMM.ARFF.ARFFConstants;
import MMM.MISC.ClientFTP;
import MMM.MISC.ClienteFTPException;
import MMM.MISC.EditaValores;
import MMM.MISC.LeituraProperties;
import MMM.MISC.Log;
import MMM.SVM.SVMConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Jean-NoteI5
 */
public class ConsolidadorDeResultados {

    private boolean usaFTP;

    public ConsolidadorDeResultados() {
        //Indica que usa FTP
        usaFTP = true;
        //Se nÃ£o usa FTP
        if (LeituraProperties.getInstance().leituraPropertiesInteiro("ftp.ftp") == 0) {
            usaFTP = false;
        }

    }

    //Realiza a consolidaÃ§Ã£o de resultados e sugere qual ativo deve ser comprado ou vendido
    public void sugereCompra() throws ConsolidadorDeResultadosException, AnalisadorException, ClienteFTPException {

        try {

            Log.loga("Inicio da função de sugestão de compra", "COMPRA");
            //Valida os arquivos existentes no diretÃ³rio
            validaArquivos();

            Log.loga("Arquivos estão válidos para importação", "COMPRA");

            //Popula array de resultados
            ArrayList<Resultado> resultados = new ArrayList<>();
            //ObtÃ©m a lista de arquivos do diretÃ³rio ARFF
            String[] listaArquivosARFF = listaArquivos(ARFFConstants.ARFF_FOLDER, ARFFConstants.ARFF_EXT);
            String[] listaArquivosResultado = listaArquivos(SVMConstants.RESULTADO_FOLDER, SVMConstants.RESULTADO_EXT);

            Log.loga("Criando array com resultados preditos", "COMPRA");

            //Varre os arquivos para anÃ¡lise
            for (int i = 0; i < listaArquivosARFF.length; i++) {

                //Se os arquivos nÃ£o tiverem o mesmo nome
                if (!listaArquivosARFF[i].replaceAll(".ARFF", "").equals(listaArquivosResultado[i].replaceAll(".csv", ""))) {
                    throw new ConsolidadorDeResultadosException("A pasta de ARFF e resultados não está igual");
                }
                //Inicia analise de determinado ativo
                Analisador analisador = new Analisador(listaArquivosResultado[i], listaArquivosARFF[i]);
                //Inclui o resultado analisado deste ativo
                resultados.add(analisador.analisa());
            }

            Log.loga("Criando MAP para avaliar os melhores resultados", "COMPRA");
            HashMap<Date, Resultado> melhoresResultados = new HashMap<>();

            //Varre os resultados
            for (int i = 0; i < resultados.size(); i++) {

                //Se preveu que o ativo vai baixar de preÃ§o
                if (resultados.get(i).getPercentualDiffValores() <= 100) {
                    Log.loga("Dia:" + resultados.get(i).getData().toString() + " Diff:" + resultados.get(i).getPercentualDiffValores(), "PREJUÃ?ZO");
                    continue;
                }

                //Se houve um aumento exagerado, presume que o algoritmo loquetiou
                if (resultados.get(i).getPercentualDiffValores() > 150) {
                    Log.loga("Dia:" + resultados.get(i).getData().toString() + " Diff:" + resultados.get(i).getPercentualDiffValores(), "LIXO");
                    continue;
                }

                //Se nÃ£o possui registro para o dia
                if (melhoresResultados.get(resultados.get(i).getData()) == null) {
                    Log.loga("Dia:" + resultados.get(i).getData().toString() + " Ativo:" + resultados.get(i).getAtivo(), "PRIMEIRO");
                    melhoresResultados.put(resultados.get(i).getData(), resultados.get(i));
                    continue;
                }

                //Se o registro atual processado, possui uma diferenÃ§a maior que o registro anterior
                if (resultados.get(i).getPercentualDiffValores() > melhoresResultados.get(resultados.get(i).getData()).getPercentualDiffValores()) {
                    Log.loga("Dia:" + resultados.get(i).getData().toString() + " Diff:" + resultados.get(i).getPercentualDiffValores() + " Ativo:" + resultados.get(i).getAtivo(), "NOVO");
                    Log.loga("Dia:" + melhoresResultados.get(resultados.get(i).getData()).getData().toString() + " Diff:" + melhoresResultados.get(resultados.get(i).getData()).getPercentualDiffValores(), "VELHO");
                    melhoresResultados.put(resultados.get(i).getData(), resultados.get(i));
                }
            }

            criaCSV(melhoresResultados);

//            //Indica ao usuÃ¡rio quais dos ativos devem ser investidos
//            Log.loga("Invista no ativo: " + resultados.get(indAux).getAtivo(), "ANALISE");
//            Log.loga("Lucratividade prevista: " + resultados.get(indAux).getPercentualDiffValores(), "ANALISE");
//            Log.loga("Valor previsto: " + resultados.get(indAux).getDiffValores(), "ANALISE");
//            Log.loga("Valor predito: " + resultados.get(indAux).getValorPredito(), "ANALISE");
//            Log.loga("Valor fechamento: " + resultados.get(indAux).getValorHoje(), "ANALISE");
        } catch (InterruptedException ex) {
            throw new ConsolidadorDeResultadosException("Houve um erro no momento de consolidar os resultados");
        }
    }

    private void criaCSV(HashMap<Date, Resultado> melhoresResultados) throws ConsolidadorDeResultadosException {
        try {
            //Abre o arquivo CSV de predicoes
            File file = new File("predicoes/predicoes.csv");
            FileOutputStream arquivoGravacao = new FileOutputStream(file);
            OutputStreamWriter strWriter = new OutputStreamWriter(arquivoGravacao);
            BufferedWriter predicoes = new BufferedWriter(strWriter);

            //CabeÃ§alho
            predicoes.write("data;prioridade;ativo;stopGain;StopLoss");
            predicoes.newLine();

            Log.loga("Iniciando exportação do arquivo CSV de predições", "PREDICOES");

            //Cria arquivo CSV com os melhores resultados
            Set<Date> chaves = melhoresResultados.keySet();

            //Varre os dados com os melhores resultados
            for (Date chave : chaves) {

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                StringBuilder linha = new StringBuilder();
                //A predição foi feita no dia para descobrir o valor Máximo do dia seguinte, assim, a predição sempre se refere ao dia seguinte
                linha.append(formatter.format(proxDiaUtil(chave)));
                linha.append(";");
                linha.append("1");
                linha.append(";");
                linha.append(melhoresResultados.get(chave).getAtivo());
                linha.append(";");
                linha.append(EditaValores.edita2DecVirgula(melhoresResultados.get(chave).getStopGain()));
                linha.append(";");
                linha.append(EditaValores.edita2DecVirgula(melhoresResultados.get(chave).getStopLoss()));

                predicoes.write(linha.toString());
                predicoes.newLine();

            }

            predicoes.flush();
            predicoes.close();

        } catch (IOException ex) {
            throw new ConsolidadorDeResultadosException("Houve um problema no momento de gerar o arquivo CSV com as predições");
        }

    }

    //Retorna o próximo dia útil
    private Date proxDiaUtil(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //Incrementa o dia
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        //Se for sábado ou Domingo
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1 || calendar.get(Calendar.DAY_OF_WEEK) == 7) {
            //Incrementa o dia
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        //Se for sábado ou Domingo
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1 || calendar.get(Calendar.DAY_OF_WEEK) == 7) {
            //Incrementa o dia
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTime();
    }

    //Retorna a lista de arquivos existentes no diretÃ³rio
    private String[] listaArquivos(String name, String ext) {
        File list = new File(name);

        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(ext);
            }
        };
        return list.list(filenameFilter);

    }

    private void validaArquivos() throws ClienteFTPException, InterruptedException, ConsolidadorDeResultadosException {

        //Se deve obter os arquivos do FTP
        if (usaFTP) {
            baixaArquivosFTP();
        }

        //Verifica se possui todos os arquivos ARFF necessÃ¡rios
        if (listaArquivos(ARFFConstants.ARFF_FOLDER, ARFFConstants.ARFF_EXT).length != listaArquivos(SVMConstants.RESULTADO_FOLDER, SVMConstants.RESULTADO_EXT).length) {

            Log.loga("A quantidade de arquivos ARFF é " + listaArquivos(ARFFConstants.ARFF_FOLDER, ARFFConstants.ARFF_EXT).length, "PREDICOES");
            Log.loga("A quantidade de arquivos Resultado é " + listaArquivos(SVMConstants.RESULTADO_FOLDER, SVMConstants.RESULTADO_EXT).length, "PREDICOES");

            throw new ConsolidadorDeResultadosException("A quantidade de arquivos ARFF não é igual a quantidade de Resultados");
        }

    }

    //Baixa arquivos do FTP
    private void baixaArquivosFTP() throws ClienteFTPException, InterruptedException {

        //Baixa o arquivo ARFF
        ClientFTP arff = new ClientFTP();
        //Baixa os arquivos ARFF
        arff.receiveFile(ARFFConstants.ARFF_FOLDER);
        //Baixa o arquivo CSV
        ClientFTP resultado = new ClientFTP();
        resultado.receiveFile(SVMConstants.RESULTADO_FOLDER);
    }

}
