// Agrupa esta classe na camada service do projeto.
package service;

// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza a seleção explícita de UTF-8.
import java.nio.charset.StandardCharsets;
// Disponibiliza leitura, gravação e criação de arquivos e diretórios.
import java.nio.file.Files;
// Disponibiliza caminhos sem depender de separadores montados manualmente.
import java.nio.file.Path;
// Disponibiliza listas cujo conteúdo pode crescer durante a execução.
import java.util.ArrayList;
// Disponibiliza detecção de identificadores repetidos durante a importação.
import java.util.HashSet;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza coleções sem repetição.
import java.util.Set;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;

// Importação única da base antiga; somente entrega o lote ao DAO depois de validar todos os registros.
public final class MigracaoDados {
    // O nome fixo identifica esta migração na tabela migracoes.
    private static final String NOME = "transacoes_csv";

    // Construtor que prepara as dependências e o estado inicial desta classe.
    private MigracaoDados() { }

    // Entrada chamada pela JVM; inicia a versão correspondente da aplicação.
    public static void main(String[] args) throws IOException {
        // Fecha a conexão automaticamente depois da migração independente.
        try (TransacaoDAO dao = new TransacaoDAO(Conexao.abrir())) {
            // Procura o CSV antigo na mesma pasta que contém o banco.
            importar(Conexao.pastaDados().resolve("transacoes.csv"), dao);
            // Mostra a quantidade persistida, permitindo conferir a importação pelo terminal.
            System.out.println("Banco de dados pronto: " + dao.listar().size() + " transação(ões).");
        }
    }

    // Executa a etapa de migração respeitando o marcador de conclusão.
    public static void importar(Path arquivo, TransacaoDAO dao) throws IOException {
        // Evita ler o CSV depois que o marcador já foi salvo.
        if (dao.migracaoConcluida(NOME)) {
            // Encerra esta operação sem executar as etapas restantes.
            return;
        }
        // Valida em memória antes de iniciar qualquer gravação do lote.
        List<Transacao> transacoes = new ArrayList<>();
        // Detecta IDs repetidos dentro do próprio arquivo antes de acessar o banco.
        Set<Integer> ids = new HashSet<>();
        // Um arquivo ausente representa um lote vazio, que também conclui a migração.
        List<String> linhas = Files.exists(arquivo)
                // Lê em UTF-8 para preservar acentos da base antiga.
                ? Files.readAllLines(arquivo, StandardCharsets.UTF_8) : List.of();
        // O índice permite informar ao usuário a linha inválida do arquivo.
        for (int indice = 0; indice < linhas.size(); indice++) {
            // Seleciona o registro atual do lote.
            String linha = linhas.get(indice);
            // Ignora linhas vazias sem tentar construir uma transação.
            if (linha.isBlank()) {
                // Pula o registro atual e segue para o próximo elemento do laço.
                continue;
            }
            // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
            try {
                // O limite -1 preserva campos vazios no fim da linha, necessários para validar o formato.
                String[] campos = linha.split(";", -1);
                // O formato antigo possui exatamente oito campos separados por ponto e vírgula.
                if (campos.length != 8) {
                    // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                    throw new IllegalArgumentException("Quantidade de campos inválida.");
                }
                // Converte o identificador textual antes de verificar validade e duplicidade.
                int id = Integer.parseInt(campos[0]);
                // Um ID deve ser positivo; add retorna false quando já havia o mesmo ID no conjunto.
                if (id <= 0 || !ids.add(id)) {
                    // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                    throw new IllegalArgumentException("Identificador inválido ou repetido.");
                }
                // O modelo fará a validação financeira depois da conversão inicial.
                double valor = Double.parseDouble(campos[2]);
                // Boolean.parseBoolean não rejeita textos desconhecidos; a validação precisa ocorrer antes.
                if (!campos[6].equalsIgnoreCase("true") && !campos[6].equalsIgnoreCase("false")) {
                    // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                    throw new IllegalArgumentException("Indicador mensal inválido.");
                }
                // Converte o dia antigo para o atributo da eventual transação mensal.
                int dia = Integer.parseInt(campos[7]);
                // Escolhe a classe conforme o indicador de recorrência.
                Transacao transacao = Boolean.parseBoolean(campos[6])
                        // O construtor mensal valida o dia e os atributos comuns do modelo.
                        ? new TransacaoMensal(id, campos[1], valor, campos[3], campos[4], campos[5], dia)
                        // O registro comum preserva seu identificador sem acrescentar vencimento.
                        : new Transacao(id, campos[1], valor, campos[3], campos[4], campos[5]);
                // Adiciona à lista somente quando todas as validações do registro passaram.
                transacoes.add(transacao);
            // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
            } catch (IllegalArgumentException e) {
                // O índice começa em zero, mas a mensagem usa a numeração de linhas do usuário.
                throw new IOException("Registro inválido na linha " + (indice + 1) + " do arquivo antigo.", e);
            }
        }
        // Só entrega o lote ao DAO depois de validar todas as linhas; o DAO controla commit e rollback.
        dao.importar(NOME, transacoes);
    }
}
