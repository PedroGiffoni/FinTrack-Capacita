package Controller;

import java.util.ArrayList;
import model.Transacao;
import utils.GerenciadorArquivo;

/**
 * Classe responsável por controlar as principais regras de negócio
 * do sistema FinTrack.
 *
 * Esta classe funciona como o "gerenciador financeiro" da aplicação.
 * Ela recebe as transações criadas no menu principal, armazena em uma lista,
 * calcula saldos, gera relatórios, exibe o dashboard e aciona a persistência
 * em arquivo por meio da classe GerenciadorArquivo.
 *
 * Principais responsabilidades:
 * - Adicionar transações;
 * - Listar transações;
 * - Remover transações;
 * - Calcular saldo total;
 * - Gerar relatórios de receitas, despesas e categorias;
 * - Exibir dashboard financeiro;
 * - Salvar e carregar dados do arquivo CSV.
 *
 * @author Pedro
 */
public class FinTracker {

    /**
     * Lista que armazena todas as transações do sistema.
     *
     * Como Receita, Despesa e TransacaoMensal herdam de Transacao,
     * a lista pode guardar todos esses tipos usando polimorfismo.
     */
    private ArrayList<Transacao> transacoes;

    /**
     * Construtor da classe.
     *
     * Ao iniciar o FinTracker, os dados são carregados automaticamente
     * do arquivo CSV por meio do GerenciadorArquivo.
     */
    public FinTracker() {
        this.transacoes = GerenciadorArquivo.carregar();
    }

    /**
     * Adiciona uma nova transação à lista e salva os dados no arquivo.
     *
     * @param transacao objeto do tipo Transacao ou de alguma subclasse,
     * como Receita, Despesa ou TransacaoMensal.
     */
    public void adicionarTransacao(Transacao transacao) {
        transacoes.add(transacao);
        GerenciadorArquivo.salvar(transacoes);
        System.out.println("Transação adicionada com sucesso!");
    }

    /**
     * Lista todas as transações cadastradas.
     *
     * Se a lista estiver vazia, exibe uma mensagem informando que não há transações cadastradas.
     * 
     */
    public void listarTransacoes() {
        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma transação cadastrada.");
            return;
        }

        System.out.println("\n===== LISTA DE TRANSAÇÕES =====");

        for (Transacao transacao : transacoes) {
            transacao.exibirTransacao();
        }
    }

    /**
     * Remove uma transação a partir do ID informado pelo usuário.
     *
     * Após remover, salva novamente a lista atualizada no arquivo CSV.
     *
     * @param id ID da transação que será removida.
     */
    public void removerTransacao(int id) {
        for (Transacao transacao : transacoes) {
            if (transacao.getId() == id) {
                transacoes.remove(transacao);
                GerenciadorArquivo.salvar(transacoes);
                System.out.println("Transação removida com sucesso!");
                return;
            }
        }

        System.out.println("ID não encontrado.");
    }

    /**
     * Calcula o saldo total do usuário.
     *
     * Aqui usaei polimorfismo, pois cada objeto Transacao pode calcular
     * seu impacto no saldo por meio do método calcularImpactoNoSaldo().
     *
     * Receitas retornam valor positivo.
     * Despesas retornam valor negativo.
     *
     * @return saldo total considerando receitas e despesas.
     */
    public double calcularSaldoTotal() {
        double saldo = 0;

        for (Transacao transacao : transacoes) {
            saldo += transacao.calcularImpactoNoSaldo();
        }

        return saldo;
    }

    /**
     * Lista somente as transações do tipo receita.
     *
     * Também calcula e exibe o total de receitas encontradas.
     */
    public void listarReceitas() {
        double totalReceitas = 0;
        boolean encontrouReceita = false;

        System.out.println("\n===== RELATÓRIO DE RECEITAS =====");

        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("receita")) {
                transacao.exibirTransacao();
                System.out.println("------------------------------");
                totalReceitas += transacao.getValor();
                encontrouReceita = true;
            }
        }

        if (!encontrouReceita) {
            System.out.println("Nenhuma receita cadastrada.");
        } else {
            System.out.printf("Total de receitas: R$ %.2f%n", totalReceitas);
        }
    }

    /**
     * Lista somente as transações do tipo despesa.
     *
     * Também calcula e exibe o total de despesas encontradas.
     */
    public void listarDespesas() {
        double totalDespesas = 0;
        boolean encontrouDespesa = false;

        System.out.println("\n===== RELATÓRIO DE DESPESAS =====");

        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("despesa")) {
                transacao.exibirTransacao();
                System.out.println("------------------------------");
                totalDespesas += transacao.getValor();
                encontrouDespesa = true;
            }
        }

        if (!encontrouDespesa) {
            System.out.println("Nenhuma despesa cadastrada.");
        } else {
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
     * @param categoria categoria que será pesquisada.
     */
    public void relatorioPorCategoria(String categoria) {
        double saldoCategoria = 0;
        boolean encontrouCategoria = false;

        System.out.println("\n===== RELATÓRIO POR CATEGORIA =====");
        System.out.println("Categoria: " + categoria);

        for (Transacao transacao : transacoes) {
            if (transacao.getCategoria().equalsIgnoreCase(categoria)) {
                transacao.exibirTransacao();
                System.out.println("------------------------------");

                if (transacao.getTipo().equalsIgnoreCase("receita")) {
                    saldoCategoria += transacao.getValor();
                } else if (transacao.getTipo().equalsIgnoreCase("despesa")) {
                    saldoCategoria -= transacao.getValor();
                }

                encontrouCategoria = true;
            }
        }

        if (!encontrouCategoria) {
            System.out.println("Nenhuma transação encontrada nessa categoria.");
        } else {
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
        double totalReceitas = 0;
        double totalDespesas = 0;

        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("receita")) {
                totalReceitas += transacao.getValor();
            } else if (transacao.getTipo().equalsIgnoreCase("despesa")) {
                totalDespesas += transacao.getValor();
            }
        }

        double saldo = totalReceitas - totalDespesas;

        System.out.println("\n===== DASHBOARD FINANCEIRO =====");
        System.out.printf("Total de receitas: R$ %.2f%n", totalReceitas);
        System.out.printf("Total de despesas: R$ %.2f%n", totalDespesas);
        System.out.printf("Saldo atual:       R$ %.2f%n", saldo);

        System.out.println("\nResumo visual:");

        System.out.print("Receitas: ");
        imprimirBarra(totalReceitas);

        System.out.print("Despesas: ");
        imprimirBarra(totalDespesas);
    }

    /**
     * Imprime uma barra visual proporcional ao valor informado.
     *
     * A cada R$ 100,00 é impresso um bloco.
     * O limite máximo é de 30 blocos para evitar barras muito grandes.
     *
     * @param valor valor usado para calcular o tamanho da barra.
     */
    private void imprimirBarra(double valor) {
        int quantidade = (int) (valor / 100);

        if (quantidade > 30) {
            quantidade = 30;
        }

        for (int i = 0; i < quantidade; i++) {
            System.out.print("█");
        }

        System.out.printf(" R$ %.2f%n", valor);
    }
}