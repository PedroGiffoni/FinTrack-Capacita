package app;

import Controller.FinTracker;
import model.TransacaoMensal;
import utils.Formatador;
import java.util.Scanner;
import model.Receita;
import model.Despesa;

/**
 * Classe principal da aplicação FinTrack.
 *
 * Esta classe é responsável por iniciar o sistema, exibir o menu no console,
 * receber as opções digitadas pelo usuário e chamar os métodos da classe
 * FinTracker, que contém a lógica principal do controle financeiro.
 *
 * O projeto foi desenvolvido em versão console, com foco em:
 * - Programação Orientada a Objetos;
 * - Uso de ArrayList;
 * - Herança;
 * - Polimorfismo;
 * - Tratamento de exceções;
 * - Persistência em arquivo;
 * - Integração com o Edu IA.
 *
 * @author Pedro Giffoni
 */
public class Main {

    public static void main(String[] args) {

        /*
         * Configuração de UTF-8.
         *
         * Essa configuração tenta evitar problemas de acentuação no console,
         * principalmente em palavras como "transação", "descrição" e "opção".
         */
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            System.out.println("Erro ao configurar UTF-8.");
        }

        /*
         * Scanner usado para receber as informações digitadas pelo usuário.
         */
        Scanner input = new Scanner(System.in);

        /*
         * Objeto principal do sistema.
         *
         * A classe FinTracker é responsável por armazenar as transações,
         * listar dados, calcular saldo, gerar relatórios, dashboard
         * e salvar/carregar os dados do arquivo.
         */
        FinTracker finTracker = new FinTracker();

        /*
         * Variável que controla a opção escolhida no menu.
         *
         * Começa com 50 apenas para garantir que o while seja executado.
         * O programa só encerra quando o usuário digita 0.
         */
        int opcao = 50;

        /*
         * Loop principal do sistema.
         *
         * Enquanto a opção for diferente de 0, o menu continua sendo exibido.
         */
        while (opcao != 0) {
            try {

                /*
                 * Exibição do menu principal.
                 */
                System.out.println("\n===== FINTRACK - SEU CONTROLE FINANCEIRO =====");
                System.out.println("1. Adicionar nova transação");
                System.out.println("2. Listar transações");
                System.out.println("3. Mostrar saldo atual");
                System.out.println("4. Remover transação");
                System.out.println("5. Relatório de receitas");
                System.out.println("6. Relatório de despesas");
                System.out.println("7. Relatório por categoria");
                System.out.println("8. Dashboard financeiro");
                System.out.println("9. Edu - Educação financeira com IA");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");

                /*
                 * Lê a opção do usuário.
                 *
                 * Usei Integer.parseInt porque o Scanner lê inicialmente
                 * como texto. Caso o usuário digite letras, o catch
                 * NumberFormatException tratará o erro.
                 */
                opcao = Integer.parseInt(input.nextLine());

                /*
                 * Estrutura switch responsável por direcionar o usuário
                 * para a funcionalidade escolhida.
                 */
                switch (opcao) {

                    /*
                     * Opção 1:
                     * Cadastro de uma nova transação financeira.
                     */
                    case 1:
                        System.out.println("Alimentação | Transporte | Moradia | Saúde | Educação | Lazer | Salário | Freelance | Investimentos | Outros");
                        System.out.print("Escolha uma categoria: ");
                        String categoria = input.nextLine();

                        /*
                         * Validação da categoria.
                         *
                         * Só permite cadastrar categorias previstas no sistema.
                         */
                        if (!categoria.equalsIgnoreCase("Alimentação") &&
                            !categoria.equalsIgnoreCase("Transporte") &&
                            !categoria.equalsIgnoreCase("Moradia") &&
                            !categoria.equalsIgnoreCase("Saúde") &&
                            !categoria.equalsIgnoreCase("Educação") &&
                            !categoria.equalsIgnoreCase("Lazer") &&
                            !categoria.equalsIgnoreCase("Salário") &&
                            !categoria.equalsIgnoreCase("Freelance") &&
                            !categoria.equalsIgnoreCase("Investimentos") &&
                            !categoria.equalsIgnoreCase("Outros")) {

                            System.out.println("Categoria inválida.");
                            break;
                        }

                        /*
                         * Entrada e validação da descrição.
                         */
                        System.out.print("Descrição da transação: ");
                        String descricao = input.nextLine();

                        if (descricao.trim().isEmpty()) {
                            System.out.println("A descrição não pode ficar vazia.");
                            break;
                        }

                        /*
                         * Entrada e validação do valor.
                         *
                         * O replace permite que o usuário digite valores com vírgula,
                         * por exemplo: 10,50.
                         */
                        System.out.print("Valor: R$ ");
                        double valor = Double.parseDouble(input.nextLine().replace(",", "."));

                        if (valor <= 0) {
                            System.out.println("O valor precisa ser maior que zero.");
                            break;
                        }

                        /*
                         * Entrada e validação do tipo.
                         *
                         * O sistema aceita apenas receita ou despesa.
                         */
                        System.out.print("Tipo receita/despesa: ");
                        String tipo = input.nextLine();

                        if (!tipo.equalsIgnoreCase("receita") && !tipo.equalsIgnoreCase("despesa")) {
                            System.out.println("Tipo inválido. Use receita ou despesa.");
                            break;
                        }

                        /*
                         * Entrada da data da transação.
                         */
                        System.out.print("Data da transação dd/mm/aaaa: ");
                        String data = input.nextLine();

                        /*
                         * Verifica se a transação será mensal.
                         */
                        System.out.print("É uma transação mensal? s/n: ");
                        String mensal = input.nextLine();

                        if (!mensal.equalsIgnoreCase("s") && !mensal.equalsIgnoreCase("n")) {
                            System.out.println("Resposta inválida. Digite apenas s ou n.");
                            break;
                        }

                        /*
                         * Caso a transação seja mensal, cria um objeto TransacaoMensal.
                         *
                         * Aqui existe herança, pois TransacaoMensal herda de Transacao.
                         */
                        if (mensal.equalsIgnoreCase("s")) {
                            System.out.print("Dia de vencimento/recebimento apenas número de 1 a 31: ");
                            int dia = Integer.parseInt(input.nextLine());

                            if (dia < 1 || dia > 31) {
                                System.out.println("Dia inválido. Digite um número entre 1 e 31.");
                                break;
                            }

                            TransacaoMensal transacaoMensal = new TransacaoMensal(descricao, valor, tipo, categoria, data, dia);
                            finTracker.adicionarTransacao(transacaoMensal);

                        } else {

                            /*
                             * Caso não seja mensal, o sistema cria uma Receita ou uma Despesa.
                             *
                             * Aqui também apliquei herança:
                             * - Receita herda de Transacao;
                             * - Despesa herda de Transacao.
                             *
                             * Como ambas podem ser tratadas como transações pelo FinTracker,
                             * também há aplicação de polimorfismo.
                             */
                            if (tipo.equalsIgnoreCase("receita")) {
                                Receita receita = new Receita(categoria, descricao, valor, data);
                                finTracker.adicionarTransacao(receita);
                            } else {
                                Despesa despesa = new Despesa(categoria, descricao, valor, data);
                                finTracker.adicionarTransacao(despesa);
                            }
                        }

                        break;

                    /*
                     * Opção 2:
                     * Lista todas as transações cadastradas.
                     */
                    case 2:
                        finTracker.listarTransacoes();
                        break;

                    /*
                     * Opção 3:
                     * Calcula e exibe o saldo atual.
                     */
                    case 3:
                        double saldo = finTracker.calcularSaldoTotal();
                        System.out.println("Saldo atual: " + Formatador.formatarMoeda(saldo));
                        break;

                    /*
                     * Opção 4:
                     * Remove uma transação a partir do ID informado pelo usuário.
                     */
                    case 4:
                        finTracker.listarTransacoes();

                        System.out.print("Digite o ID da transação que deseja remover: ");
                        int id = Integer.parseInt(input.nextLine());

                        finTracker.removerTransacao(id);
                        break;

                    /*
                     * Opção 5:
                     * Exibe apenas as receitas cadastradas.
                     */
                    case 5:
                        finTracker.listarReceitas();
                        break;

                    /*
                     * Opção 6:
                     * Exibe apenas as despesas cadastradas.
                     */
                    case 6:
                        finTracker.listarDespesas();
                        break;

                    /*
                     * Opção 7:
                     * Gera relatório filtrado por categoria.
                     */
                    case 7:
                        System.out.println("Alimentação | Transporte | Moradia | Saúde | Educação | Lazer | Salário | Freelance | Investimentos | Outros");
                        System.out.print("Digite a categoria desejada: ");

                        String categoriaBusca = input.nextLine();

                        /*
                         * Validação da categoria pesquisada.
                         */
                        if (!categoriaBusca.equalsIgnoreCase("Alimentação") &&
                            !categoriaBusca.equalsIgnoreCase("Transporte") &&
                            !categoriaBusca.equalsIgnoreCase("Moradia") &&
                            !categoriaBusca.equalsIgnoreCase("Saúde") &&
                            !categoriaBusca.equalsIgnoreCase("Educação") &&
                            !categoriaBusca.equalsIgnoreCase("Lazer") &&
                            !categoriaBusca.equalsIgnoreCase("Salário") &&
                            !categoriaBusca.equalsIgnoreCase("Freelance") &&
                            !categoriaBusca.equalsIgnoreCase("Investimentos") &&
                            !categoriaBusca.equalsIgnoreCase("Outros")) {

                            System.out.println("Categoria inválida.");
                            break;
                        }

                        finTracker.relatorioPorCategoria(categoriaBusca);
                        break;

                    /*
                     * Opção 8:
                     * Exibe o dashboard financeiro no console.
                     */
                    case 8:
                        finTracker.exibirDashboard();
                        break;

                    /*
                     * Opção 9:
                     * Abre o Edu IA, aplicação Python/Streamlit integrada ao projeto.
                     */
                    case 9:
                        try {
                            ProcessBuilder pb = new ProcessBuilder(
                                "cmd.exe",
                                "/c",
                                "start",
                                "",
                                "C:\\Users\\Pedro\\Desktop\\FinTrack\\eduIa\\rodar_edu.bat"
                            );

                            pb.start();

                            System.out.println("Abrindo o Edu IA...");
                            System.out.println("Aguarde alguns segundos e acesse: http://localhost:8501");

                        } catch (Exception e) {
                            System.out.println("Erro ao abrir o Edu IA: " + e.getMessage());
                        }

                        break;

                    /*
                     * Opção 0:
                     * Encerra o sistema.
                     */
                    case 0:
                        System.out.println("Encerrando o FinTrack...");
                        break;

                    /*
                     * Caso o usuário digite uma opção fora do menu.
                     */
                    default:
                        System.out.println("Opção inválida. Escolha uma opção de 0 a 9.");
                }

            /*
             * Tratamento para entradas numéricas inválidas.
             *
             * Exemplo:
             * - usuário digita letra no menu;
             * - usuário digita texto onde deveria informar valor;
             * - usuário digita texto no ID de remoção.
             */
            } catch (NumberFormatException e) {
                System.out.println("Erro: digite um número válido.");

            /*
             * Tratamento genérico para qualquer outro erro inesperado.
             */
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }

        /*
         * Fecha o Scanner ao final da execução.
         */
        input.close();
    }
}