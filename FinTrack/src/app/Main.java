// Agrupa esta classe na camada app do projeto.
package app;

// Referencia FinTracker, componente da camada controller utilizado neste fluxo.
import controller.FinTracker;
// Referencia EduService, componente da camada service utilizado neste fluxo.
import service.EduService;
// Referencia EntradaInvalidaException, componente da camada exceptions utilizado neste fluxo.
import exceptions.EntradaInvalidaException;
// Disponibiliza datas sem horário para cadastro, ordenação e filtros.
import java.time.LocalDate;
// Disponibiliza o formato usado para analisar e apresentar datas.
import java.time.format.DateTimeFormatter;
// Disponibiliza o tratamento de datas que não podem ser interpretadas.
import java.time.format.DateTimeParseException;
// Disponibiliza a rejeição de datas impossíveis em vez de sua correção automática.
import java.time.format.ResolverStyle;
// Disponibiliza a leitura de entradas da versão de console.
import java.util.Scanner;
// Referencia Despesa, componente da camada model utilizado neste fluxo.
import model.Despesa;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;
// Referencia Formatador, componente da camada utils utilizado neste fluxo.
import utils.Formatador;
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
 * - Persistência em banco de dados;
 * - Integração com o Edu IA.
 *
 * @author Pedro Giffoni
 */
public class Main {

    // Entrada chamada pela JVM; inicia a versão correspondente da aplicação.
    public static void main(String[] args) {

        /*
         * Configuração de UTF-8.
         *
         * Essa configuração tenta evitar problemas de acentuação no console,
         * principalmente em palavras como "transação", "descrição" e "opção".
         */
        try {
            // Configura a saída do console antes de apresentar textos com acentuação.
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            // Configura a saída do console antes de apresentar textos com acentuação.
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (java.io.UnsupportedEncodingException e) {
            // Apresenta esta informação no terminal da versão de console.
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
         * e salvar/carregar os dados do banco.
         */
        FinTracker finTracker = new FinTracker();
        // A opção antiga do console usa o servidor Streamlit, não a tela JavaFX.
        EduService edu = new EduService();

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
            // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
            try {

                /*
                 * Exibição do menu principal.
                 */
                System.out.println("\n===== FINTRACK - SEU CONTROLE FINANCEIRO =====");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("1. Adicionar nova transação");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("2. Listar transações");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("3. Mostrar saldo atual");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("4. Remover transação");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("5. Relatório de receitas");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("6. Relatório de despesas");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("7. Relatório por categoria");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("8. Dashboard financeiro");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("9. Edu - Educação financeira com IA");
                // Apresenta esta informação no terminal da versão de console.
                System.out.println("0. Sair");
                // Apresenta esta informação no terminal da versão de console.
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
                        // Apresenta esta informação no terminal da versão de console.
                        System.out.println("Alimentação | Transporte | Moradia | Saúde | Educação | Lazer | Salário | Freelance | Investimentos | Outros");
                        // Apresenta esta informação no terminal da versão de console.
                        System.out.print("Escolha uma categoria: ");
                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        String categoria = input.nextLine();

                        /*
                         * Validação da categoria.
                         *
                         * Só permite cadastrar categorias previstas no sistema.
                         */
                        if (!categoria.equalsIgnoreCase("Alimentação") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Transporte") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Moradia") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Saúde") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Educação") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Lazer") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Salário") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Freelance") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Investimentos") &&
                            // Esta alternativa também precisa ser diferente para a categoria ser considerada inválida.
                            !categoria.equalsIgnoreCase("Outros")) {

                            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                            throw new EntradaInvalidaException(
                                "Categoria inválida."
                            );
                        }

                        /*
                         * Entrada e validação da descrição.
                         */
                        System.out.print("Descrição da transação: ");
                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        String descricao = input.nextLine();

                        // Valida se a descrição foi preenchida
                        if (descricao.trim().isEmpty()) {
                            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                            throw new EntradaInvalidaException(
                                "A descrição não pode ficar vazia."
                            );
                        }

                        /*
                         * Entrada e validação do valor.
                         *
                         * O replace permite que o usuário digite valores com vírgula,
                         * por exemplo: 10,50.
                         */
                        System.out.print("Valor: R$ ");
                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        double valor = Double.parseDouble(input.nextLine().replace(",", "."));

                        // Valida se o valor é positivo
                        if (valor <= 0) {
                            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                            throw new EntradaInvalidaException(
                                "O valor precisa ser maior que zero."
                            );
                        }

                        /*
                         * Entrada e validação do tipo.
                         *
                         * O sistema aceita apenas receita ou despesa.
                         */
                        System.out.print("Tipo receita/despesa: ");
                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        String tipo = input.nextLine();

                        // Aceita receita ou despesa, independentemente de maiúsculas.
                        if (!tipo.equalsIgnoreCase("receita") && !tipo.equalsIgnoreCase("despesa")) {
                        // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                        throw new EntradaInvalidaException(
                            "Digite apenas (receita) ou (despesa)."
                        );
                        }

                        /*
                        * Entrada e validação da data da transação.
                        *
                        * A data é inicialmente recebida como uma String no formato
                        * dd/MM/aaaa.
                        */
                        System.out.print("Data da transação dd/mm/aaaa: ");
                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        String data = input.nextLine();

                        /*
                        * Cria o formato esperado para a data.
                        *
                        * O padrão utilizado é:
                        * dd   = dia com dois dígitos;
                        * MM   = mês com dois dígitos;
                        * uuuu = ano com quatro dígitos.
                        *
                        * O ResolverStyle.STRICT faz uma validação rigorosa da data,
                        * impedindo que datas inexistentes sejam aceitas,
                        * como 31/02/2026 ou 32/06/2026.
                        */
                        DateTimeFormatter formatadorData = DateTimeFormatter
                                // Define dia, mês e ano no mesmo padrão apresentado ao usuário.
                                .ofPattern("dd/MM/uuuu")
                                // Rejeita datas inexistentes em vez de ajustar o dia para outro mês.
                                .withResolverStyle(ResolverStyle.STRICT);

                        /*
                        * Tenta converter o texto digitado pelo usuário em uma data válida.
                        *
                        * O método LocalDate.parse verifica tanto o formato informado
                        * quanto a existência real da data.
                        */
                        try {
                            // Confirma que a data é válida antes de construir a transação.
                            LocalDate.parse(data, formatadorData);

                        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
                        } catch (DateTimeParseException e) {

                            /*
                            * Caso a data esteja em um formato incorreto ou não exista,
                            * é lançada a exceção personalizada do sistema.
                            *
                            * Essa exceção será capturada pelo catch de
                            * EntradaInvalidaException no loop principal.
                            */
                            throw new EntradaInvalidaException(
                                "Digite uma data real no formato dd/mm/aaaa."
                            );
                        }

                        /*
                         * Verifica se a transação será mensal.
                         */
                        System.out.print("É uma transação mensal? s/n: ");
                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        String mensal = input.nextLine();

                        // Restringe a resposta de recorrência às alternativas s e n.
                        if (!mensal.equalsIgnoreCase("s") && !mensal.equalsIgnoreCase("n")) {
                            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                            throw new EntradaInvalidaException(
                                "Digite apenas s ou n."
                            );
                        }

                        /*
                         * Caso a transação seja mensal, cria um objeto TransacaoMensal.
                         *
                         * Aqui existe herança, pois TransacaoMensal herda de Transacao.
                         */
                        if (mensal.equalsIgnoreCase("s")) {
                            // Apresenta esta informação no terminal da versão de console.
                            System.out.print("Dia de vencimento/recebimento apenas número de 1 a 31: ");
                            // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                            int dia = Integer.parseInt(input.nextLine());
                            // valida se o dia esta entre 1 e 31
                            if (dia < 1 || dia > 31) {
                                // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                                throw new EntradaInvalidaException(
                                    "Digite um número entre 1 e 31."
                                );
                            }

                            // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                            TransacaoMensal transacaoMensal = new TransacaoMensal(descricao, valor, tipo, categoria, data, dia);
                            // Grava a nova subclasse pelo serviço financeiro e recarrega a lista do console.
                            finTracker.adicionarTransacao(transacaoMensal);

                        // Executa a alternativa quando a condição anterior não foi atendida.
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
                                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                                Receita receita = new Receita(categoria, descricao, valor, data);
                                // Grava a nova subclasse pelo serviço financeiro e recarrega a lista do console.
                                finTracker.adicionarTransacao(receita);
                            // Executa a alternativa quando a condição anterior não foi atendida.
                            } else {
                                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                                Despesa despesa = new Despesa(categoria, descricao, valor, data);
                                // Grava a nova subclasse pelo serviço financeiro e recarrega a lista do console.
                                finTracker.adicionarTransacao(despesa);
                            }
                        }

                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;

                    /*
                     * Opção 2:
                     * Lista todas as transações cadastradas.
                     */
                    case 2:
                        // Apresenta os registros disponíveis no console.
                        finTracker.listarTransacoes();
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;


                    /*
                     * Opção 3:
                     * Calcula e exibe o saldo atual.
                     */
                    case 3:
                        // Converte o saldo calculado pelo serviço para a assinatura mantida no console.
                        double saldo = finTracker.calcularSaldoTotal();
                        // Apresenta esta informação no terminal da versão de console.
                        System.out.println("Saldo atual: " + Formatador.formatarMoeda(saldo));
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;


                    /*
                     * Opção 4:
                     * Remove uma transação a partir do ID informado pelo usuário.
                     */
                    case 4:
                        // Apresenta os registros disponíveis no console.
                        finTracker.listarTransacoes();

                        // Apresenta esta informação no terminal da versão de console.
                        System.out.print("Digite o ID da transação que deseja remover: ");
                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        int id = Integer.parseInt(input.nextLine());

                        // Remove pelo ID solicitado e atualiza os dados apresentados.
                        finTracker.removerTransacao(id);
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;


                    /*
                     * Opção 5:
                     * Exibe apenas as receitas cadastradas.
                     */
                    case 5:
                        // Apresenta apenas as entradas financeiras e o total correspondente.
                        finTracker.listarReceitas();
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;


                    /*
                     * Opção 6:
                     * Exibe apenas as despesas cadastradas.
                     */
                    case 6:
                        // Apresenta apenas as saídas financeiras e o total correspondente.
                        finTracker.listarDespesas();
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;


                    /*
                     * Opção 7:
                     * Gera relatório filtrado por categoria.
                     */
                    case 7:
                        // Apresenta esta informação no terminal da versão de console.
                        System.out.println("Alimentação | Transporte | Moradia | Saúde | Educação | Lazer | Salário | Freelance | Investimentos | Outros");
                        // Apresenta esta informação no terminal da versão de console.
                        System.out.print("Digite a categoria desejada: ");

                        // Lê a linha inteira do terminal antes de validar ou converter a entrada.
                        String categoriaBusca = input.nextLine();


                        /*
                         * Validação da categoria pesquisada.
                         */
                        if (!categoriaBusca.equalsIgnoreCase("Alimentação") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Transporte") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Moradia") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Saúde") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Educação") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Lazer") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Salário") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Freelance") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Investimentos") &&
                            // A condição continua exigindo que nenhuma categoria permitida tenha sido informada.
                            !categoriaBusca.equalsIgnoreCase("Outros")) {

                            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                            throw new EntradaInvalidaException(
                                "A categoria informada não existe."
                            );
                        }

                        // Filtra a apresentação antiga pela categoria validada.
                        finTracker.relatorioPorCategoria(categoriaBusca);
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;

                    /*
                     * Opção 8:
                     * Exibe o dashboard financeiro no console.
                     */
                    case 8:
                        // Mostra o resumo textual e suas barras proporcionais.
                        finTracker.exibirDashboard();
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;

                    /*
                     * Opção 9:
                     * Abre o Edu IA, aplicação Python/Streamlit integrada ao projeto.
                     */
                    case 9:
                        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
                        try {
                            // Aguarda a versão Streamlit ficar disponível e obtém seu endereço local.
                            java.net.URI endereco = edu.iniciar();
                            // Abre o módulo anterior no navegador do sistema.
                            java.awt.Desktop.getDesktop().browse(endereco);
                            // Apresenta esta informação no terminal da versão de console.
                            System.out.println("Edu aberto em " + endereco);
                        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
                        } catch (Exception e) {
                            // Apresenta esta informação no terminal da versão de console.
                            System.out.println(
                                "\n####################---####################"
                                + "\nErro ao abrir o Edu IA: " + e.getMessage()
                                + "\n####################---####################"
                            );
                        }

                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;

                    /*
                     * Opção 0:
                     * Encerra o sistema.
                     */
                    case 0:
                        // Apresenta esta informação no terminal da versão de console.
                        System.out.println(
                            "\n####################---####################"
                            + "\nEncerrando o FinTrack..."
                            + "\n####################---####################"
                        );
                        // Encerra este caso para não executar a alternativa seguinte do switch.
                        break;

                    /*
                     * Caso o usuário digite uma opção fora do menu.
                     */
                    default:
                        // Apresenta esta informação no terminal da versão de console.
                        System.out.println(
                            "\n####################---####################"
                            + "\nOpção inválida. Escolha uma opção de 0 a 9."
                            + "\n####################---####################"
                        );
                }
                /*
             * Tratamento para excessao personalizada.
             *
             */
            } catch (EntradaInvalidaException e) {
                // Apresenta esta informação no terminal da versão de console.
                System.out.println(
                    "\n####################---####################"
                    + "\nEntrada inválida: " + e.getMessage()
                    + "\n####################---####################"
                );
            /*
             * Tratamento para entradas numéricas inválidas.
             *
             * Exemplo:
             * - usuário digita letra no menu;
             * - usuário digita texto onde deveria informar valor;
             * - usuário digita texto no ID de remoção.
             */
            } catch (NumberFormatException e) {
                // Apresenta esta informação no terminal da versão de console.
                System.out.println(
                    "\n####################---####################"
                    + "\nErro: digite um número válido."
                    + "\n####################---####################"
                );

            /*
             * Tratamento genérico para qualquer outro erro inesperado.
             */
            } catch (Exception e) {
                // Apresenta esta informação no terminal da versão de console.
                System.out.println(
                    "\n####################---####################"
                    + "\nErro inesperado: " + e.getMessage()
                    + "\n####################---####################"
                );
            }
        }

        /*
         * Fecha o Scanner ao final da execução.
         */
        input.close();
        // Libera o recurso ou fecha a janela depois de concluir seu uso.
        edu.close();
        // Libera o recurso ou fecha a janela depois de concluir seu uso.
        finTracker.close();
    }
}
