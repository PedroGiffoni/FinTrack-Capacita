// Agrupa esta classe na camada controller do projeto.
package controller;

// Disponibiliza listas cujo conteúdo pode crescer durante a execução.
import java.util.ArrayList;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Referencia MigracaoDados, componente da camada service utilizado neste fluxo.
import service.MigracaoDados;
// Referencia TransacaoService, componente da camada service utilizado neste fluxo.
import service.TransacaoService;

/**
 * Classe responsável por controlar as principais regras de negócio
 * do sistema FinTrack.
 *
 * Esta classe funciona como o "gerenciador financeiro" da aplicação.
 * Ela recebe as transações criadas no menu principal, armazena em uma lista,
 * calcula saldos, gera relatórios, exibe o dashboard e aciona a persistência
 * no banco de dados por meio de TransacaoDAO.
 *
 * Principais responsabilidades:
 * - Adicionar transações;
 * - Listar transações;
 * - Remover transações;
 * - Calcular saldo total;
 * - Gerar relatórios de receitas, despesas e categorias;
 * - Exibir dashboard financeiro;
 * - Salvar e carregar dados do banco SQLite.
 *
 * @author Pedro
 */
public class FinTracker implements AutoCloseable {

    /**
     * Lista que armazena todas as transações do sistema.
     *
     * Como Receita, Despesa e TransacaoMensal herdam de Transacao,
     * a lista pode guardar todos esses tipos usando polimorfismo.
     */
    private ArrayList<Transacao> transacoes;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private final TransacaoDAO dao;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private final TransacaoService service;

    /**
     * Construtor da classe.
     *
     * Ao iniciar o FinTracker, os dados são carregados automaticamente
     * do banco de dados por meio de TransacaoDAO.
     */
    public FinTracker() {
        // Mantém uma conexão SQLite para a sessão do console.
        dao = new TransacaoDAO(Conexao.abrir());
        // Reaproveita consultas e cálculos da aplicação gráfica.
        service = new TransacaoService(dao);
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Importa o arquivo antigo apenas quando o marcador ainda não existe.
            MigracaoDados.importar(Conexao.pastaDados().resolve("transacoes.csv"), dao);
            // Guarda no objeto o valor recebido para uso nas próximas operações.
            this.transacoes = new ArrayList<>(service.listar());
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (java.io.IOException | RuntimeException e) {
            // Libera o recurso ou fecha a janela depois de concluir seu uso.
            dao.close();
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalStateException("Não foi possível carregar as transações.", e);
        }
    }

    /**
     * Adiciona uma nova transação ao banco e atualiza a lista.
     *
     * @param transacao objeto do tipo Transacao ou de alguma subclasse,
     * como Receita, Despesa ou TransacaoMensal.
     */
    public void adicionarTransacao(Transacao transacao) {
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        service.adicionar(transacao);
        // Relê os registros e captura o resultado que será apresentado ou verificado.
        transacoes = new ArrayList<>(service.listar());
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("Transação adicionada com sucesso!");
    }

    /**
     * Lista todas as transações cadastradas.
     *
     * Se a lista estiver vazia, exibe uma mensagem informando que não há transações cadastradas.
     *
     */
    public void listarTransacoes() {
        // Uma lista vazia precisa de mensagem própria, em vez de um cabeçalho sem registros.
        if (transacoes.isEmpty()) {
            // Apresenta esta informação no terminal da versão de console.
            System.out.println("Nenhuma transação cadastrada.");
            // Encerra esta operação sem executar as etapas restantes.
            return;
        }

        // Apresenta esta informação no terminal da versão de console.
        System.out.println("\n===== LISTA DE TRANSAÇÕES =====");

        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (Transacao transacao : transacoes) {
            // O polimorfismo permite à subclasse mensal acrescentar o vencimento à apresentação.
            transacao.exibirTransacao();
        }
    }

    /**
     * Remove uma transação a partir do ID informado pelo usuário.
     *
     * Após remover, carrega a lista atualizada do banco.
     *
     * @param id ID da transação que será removida.
     */
    public void removerTransacao(int id) {
        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (Transacao transacao : transacoes) {
            // Procura o registro pelo identificador persistido, e não pela posição na lista.
            if (transacao.getId() == id) {
                // Delega a exclusão ao DAO pelo serviço compartilhado.
                service.remover(id);
                // Relê os registros e captura o resultado que será apresentado ou verificado.
                transacoes = new ArrayList<>(service.listar());
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("Transação removida com sucesso!");
                // Encerra esta operação sem executar as etapas restantes.
                return;
            }
        }

        // Apresenta esta informação no terminal da versão de console.
        System.out.println("ID não encontrado.");
    }

    /**
     * Calcula o saldo total do usuário.
     *
     * Soma os impactos das transações usando valores decimais.
     *
     * Receitas retornam valor positivo.
     * Despesas retornam valor negativo.
     *
     * @return saldo total considerando receitas e despesas.
     */
    public double calcularSaldoTotal() {
        // Calcula com BigDecimal e converte apenas para a assinatura antiga de console.
        return TransacaoService.calcularSaldo(transacoes).doubleValue();
    }

    /**
     * Lista somente as transações do tipo receita.
     *
     * Também calcula e exibe o total de receitas encontradas.
     */
    public void listarReceitas() {
        // Acumulador da apresentação antiga; os totais da interface atual usam BigDecimal.
        double totalReceitas = 0;
        // Distingue ausência de receitas de uma lista que contém somente outros tipos.
        boolean encontrouReceita = false;

        // Apresenta esta informação no terminal da versão de console.
        System.out.println("\n===== RELATÓRIO DE RECEITAS =====");

        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (Transacao transacao : transacoes) {
            // Seleciona as entradas para a apresentação ou acumulação de receitas.
            if (transacao.getTipo().equalsIgnoreCase("receita")) {
                // O polimorfismo permite à subclasse mensal acrescentar o vencimento à apresentação.
                transacao.exibirTransacao();
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("------------------------------");
                // Acumula o valor das entradas no relatório legado.
                totalReceitas += transacao.getValor();
                // Registra que o relatório deve apresentar um total, em vez de ausência de dados.
                encontrouReceita = true;
            }
        }

        // Apresenta ausência de dados quando nenhum registro era uma receita.
        if (!encontrouReceita) {
            // Apresenta esta informação no terminal da versão de console.
            System.out.println("Nenhuma receita cadastrada.");
        // Executa a alternativa quando a condição anterior não foi atendida.
        } else {
            // Apresenta esta informação no terminal da versão de console.
            System.out.printf("Total de receitas: R$ %.2f%n", totalReceitas);
        }
    }

    /**
     * Lista somente as transações do tipo despesa.
     *
     * Também calcula e exibe o total de despesas encontradas.
     */
    public void listarDespesas() {
        // Acumulador compatível com a versão inicial de console.
        double totalDespesas = 0;
        // Distingue ausência de despesas antes de apresentar o total.
        boolean encontrouDespesa = false;

        // Apresenta esta informação no terminal da versão de console.
        System.out.println("\n===== RELATÓRIO DE DESPESAS =====");

        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (Transacao transacao : transacoes) {
            // Seleciona as saídas para a apresentação ou acumulação de despesas.
            if (transacao.getTipo().equalsIgnoreCase("despesa")) {
                // O polimorfismo permite à subclasse mensal acrescentar o vencimento à apresentação.
                transacao.exibirTransacao();
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("------------------------------");
                // Acumula o valor das saídas no relatório legado.
                totalDespesas += transacao.getValor();
                // Registra a presença de pelo menos uma despesa.
                encontrouDespesa = true;
            }
        }

        // Apresenta ausência de dados quando nenhum registro era uma despesa.
        if (!encontrouDespesa) {
            // Apresenta esta informação no terminal da versão de console.
            System.out.println("Nenhuma despesa cadastrada.");
        // Executa a alternativa quando a condição anterior não foi atendida.
        } else {
            // Apresenta esta informação no terminal da versão de console.
            System.out.printf("Total de despesas: R$ %.2f%n", totalDespesas);
        }
    }

    /**
     * Gera um relatório filtrado por categoria.
     *
     * O método percorre todas as transações e exibe apenas aquelas
     * cuja categoria corresponde à categoria informada pelo usuário.
     *
     * No final, mostra o saldo da categoria:
     * - Receitas somam;
     * - Despesas subtraem.
     *
     *
     */
    public void relatorioPorCategoria(String categoria) {
        // Acumulador compatível com o relatório de categoria da versão de console.
        double saldoCategoria = 0;
        // Registra se pelo menos uma movimentação correspondeu à categoria.
        boolean encontrouCategoria = false;

        // Apresenta esta informação no terminal da versão de console.
        System.out.println("\n===== RELATÓRIO POR CATEGORIA =====");
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("Categoria: " + categoria);

        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (Transacao transacao : transacoes) {
            // Compara o nome da categoria sem distinguir maiúsculas.
            if (transacao.getCategoria().equalsIgnoreCase(categoria)) {
                // O polimorfismo permite à subclasse mensal acrescentar o vencimento à apresentação.
                transacao.exibirTransacao();
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("------------------------------");

                // Seleciona as entradas para a apresentação ou acumulação de receitas.
                if (transacao.getTipo().equalsIgnoreCase("receita")) {
                    // Receitas aumentam o saldo do grupo selecionado.
                    saldoCategoria += transacao.getValor();
                // Executa a alternativa quando a condição anterior não foi atendida.
                } else if (transacao.getTipo().equalsIgnoreCase("despesa")) {
                    // Despesas reduzem o saldo do grupo selecionado.
                    saldoCategoria -= transacao.getValor();
                }

                // Informa que a categoria possui registros para apresentar.
                encontrouCategoria = true;
            }
        }

        // Diferencia uma categoria sem registros de um saldo existente igual a zero.
        if (!encontrouCategoria) {
            // Apresenta esta informação no terminal da versão de console.
            System.out.println("Nenhuma transação encontrada nessa categoria.");
        // Executa a alternativa quando a condição anterior não foi atendida.
        } else {
            // Apresenta esta informação no terminal da versão de console.
            System.out.printf("Saldo da categoria: R$ %.2f%n", saldoCategoria);
        }
    }

    /**
     * Exibe um dashboard financeiro simples no console.
     *
     * O dashboard apresenta:
     * - Total de receitas;
     * - Total de despesas;
     * - Saldo atual;
     * - Barras visuais representando receitas e despesas.
     */
    public void exibirDashboard() {
        // Acumulador da apresentação antiga; os totais da interface atual usam BigDecimal.
        double totalReceitas = 0;
        // Acumulador compatível com a versão inicial de console.
        double totalDespesas = 0;

        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (Transacao transacao : transacoes) {
            // Seleciona as entradas para a apresentação ou acumulação de receitas.
            if (transacao.getTipo().equalsIgnoreCase("receita")) {
                // Acumula o valor das entradas no relatório legado.
                totalReceitas += transacao.getValor();
            // Executa a alternativa quando a condição anterior não foi atendida.
            } else if (transacao.getTipo().equalsIgnoreCase("despesa")) {
                // Acumula o valor das saídas no relatório legado.
                totalDespesas += transacao.getValor();
            }
        }

        // O resumo textual calcula a diferença entre os dois acumuladores legados.
        double saldo = totalReceitas - totalDespesas;

        // Apresenta esta informação no terminal da versão de console.
        System.out.println("\n===== DASHBOARD FINANCEIRO =====");
        // Apresenta esta informação no terminal da versão de console.
        System.out.printf("Total de receitas: R$ %.2f%n", totalReceitas);
        // Apresenta esta informação no terminal da versão de console.
        System.out.printf("Total de despesas: R$ %.2f%n", totalDespesas);
        // Apresenta esta informação no terminal da versão de console.
        System.out.printf("Saldo atual:       R$ %.2f%n", saldo);

        // Apresenta esta informação no terminal da versão de console.
        System.out.println("\nResumo visual:");

        // Apresenta esta informação no terminal da versão de console.
        System.out.print("Receitas: ");
        // A barra de entradas mostra um bloco a cada R$ 100 até o limite visual.
        imprimirBarra(totalReceitas);

        // Apresenta esta informação no terminal da versão de console.
        System.out.print("Despesas: ");
        // A barra de saídas aplica a mesma escala visual para permitir comparação.
        imprimirBarra(totalDespesas);
    }

    /** Fecha a conexão SQLite usada pela versão de console. */
    @Override
    // Implementa AutoCloseable para liberar o recurso no try-with-resources.
    public void close() {
        // Libera o recurso ou fecha a janela depois de concluir seu uso.
        dao.close();
    }

    /**
     * Imprime uma barra visual proporcional ao valor informado.
     *
     * A cada R$ 100,00 é impresso um bloco.
     * O limite máximo é de 30 blocos para evitar barras muito grandes.
     *
     * @param valor usado para calcular o tamanho da barra.
     */
    private void imprimirBarra(double valor) {
        // Converte o valor em blocos inteiros, descartando a fração visual abaixo de R$ 100.
        int quantidade = (int) (valor / 100);

        // Evita uma barra grande demais para a largura do terminal.
        if (quantidade > 30) {
            // Aplica o limite máximo de blocos apenas à visualização.
            quantidade = 30;
        }

        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (int i = 0; i < quantidade; i++) {
            // Apresenta esta informação no terminal da versão de console.
            System.out.print("█");
        }

        // Apresenta esta informação no terminal da versão de console.
        System.out.printf(" R$ %.2f%n", valor);
    }
}
