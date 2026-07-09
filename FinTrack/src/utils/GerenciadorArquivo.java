package utils;

import java.io.*;
import java.util.ArrayList;
import model.Transacao;
import model.TransacaoMensal;

/**
 * Classe responsável pela persistência dos dados do FinTrack.
 *
 * Ela salva e carrega as transações em um arquivo CSV, permitindo que
 * os dados continuem disponíveis mesmo após o encerramento do programa.
 *
 * @author Pedro Giffoni
 */
public class GerenciadorArquivo {

    /**
     * Caminho do arquivo onde as transações serão salvas.
     */
    private static final String CAMINHO_ARQUIVO = "dados/transacoes.csv";

    /**
     * Salva todas as transações no arquivo CSV.
     *
     * O método percorre a lista de transações e grava cada uma em uma linha
     * do arquivo, separando os dados por ponto e vírgula.
     *
     * @param transacoes Lista de transações que será salva no arquivo.
     */
    public static void salvar(ArrayList<Transacao> transacoes) {
        try {
            File pasta = new File("dados");

            // Cria a pasta dados caso ela ainda não exista.
            if (!pasta.exists()) {
                pasta.mkdir();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(CAMINHO_ARQUIVO));

            for (Transacao t : transacoes) {
                boolean mensal = t instanceof TransacaoMensal;
                int dia = 0;

                // Se for uma transação mensal, salva também o dia de vencimento.
                if (mensal) {
                    TransacaoMensal tm = (TransacaoMensal) t;
                    dia = tm.getDiaVencimento();
                }

                writer.write(
                    t.getId() + ";" +
                    t.getDescricao() + ";" +
                    t.getValor() + ";" +
                    t.getTipo() + ";" +
                    t.getCategoria() + ";" +
                    t.getData() + ";" +
                    mensal + ";" +
                    dia
                );

                writer.newLine();
            }

            writer.close();

        } catch (IOException e) {
            System.out.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    /**
     * Carrega as transações salvas no arquivo CSV.
     *
     * O método lê cada linha do arquivo, separa os dados e recria os objetos
     * Transacao ou TransacaoMensal.
     *
     * @return Lista de transações carregadas do arquivo.
     */
    public static ArrayList<Transacao> carregar() {
        ArrayList<Transacao> transacoes = new ArrayList<>();

        try {
            File arquivo = new File(CAMINHO_ARQUIVO);

            // Se o arquivo ainda não existir, retorna uma lista vazia.
            if (!arquivo.exists()) {
                return transacoes;
            }

            BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_ARQUIVO));

            String linha;

            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";");

                int id = Integer.parseInt(partes[0]);
                String descricao = partes[1];
                double valor = Double.parseDouble(partes[2]);
                String tipo = partes[3];
                String categoria = partes[4];
                String data = partes[5];
                boolean mensal = Boolean.parseBoolean(partes[6]);
                int dia = Integer.parseInt(partes[7]);

                if (mensal) {
                    transacoes.add(new TransacaoMensal(id, descricao, valor, tipo, categoria, data, dia));
                } else {
                    transacoes.add(new Transacao(id, descricao, valor, tipo, categoria, data));
                }
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("Erro ao carregar arquivo: " + e.getMessage());
        }

        return transacoes;
    }
}