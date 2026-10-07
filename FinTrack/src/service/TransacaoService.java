// Agrupa esta classe na camada service do projeto.
package service;

// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza datas sem horário para cadastro, ordenação e filtros.
import java.time.LocalDate;
// Disponibiliza operações que aceitam diferentes implementações de coleção.
import java.util.Collection;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza associação entre categoria e total financeiro.
import java.util.Map;
// Disponibiliza agrupamento ordenado com comparação configurável.
import java.util.TreeMap;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;

// Reúne consultas e cálculos financeiros usados pela interface e pelo console.
public class TransacaoService extends ServicoGenerico<Transacao> {
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private final TransacaoDAO dao;

    // Construtor que prepara as dependências e o estado inicial desta classe.
    public TransacaoService(TransacaoDAO dao) {
        // Recebe o DAO por construtor, facilitando testes com banco em memória.
        this.dao = dao;
    }

    // Implementa a fonte de dados usada pela listagem e pelos filtros herdados.
    @Override
    protected List<Transacao> consultar() {
        // Relê o SQLite a cada consulta; o serviço genérico organiza o resultado.
        return dao.listar();
    }

    // Valida o registro e o encaminha ao armazenamento desta camada.
    @Override
    public void adicionar(Transacao transacao) {
        // Delega o SQL ao DAO; o serviço coordena a operação de cadastro.
        dao.inserir(transacao);
    }

    // Aplica a alteração ou atualiza a apresentação, conforme a responsabilidade desta classe.
    public void atualizar(Transacao transacao) {
        // Uma edição sem linha afetada precisa ser informada como registro ausente.
        if (!dao.atualizar(transacao)) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException("A transação não foi encontrada. Atualize a lista.");
        }
    }

    // Remove o registro solicitado e informa se algo foi encontrado.
    @Override
    public boolean remover(int id) {
        // Informa se a exclusão realmente encontrou o registro.
        return dao.remover(id);
    }

    // Seleciona os registros que atendem ao critério recebido.
    public List<Transacao> filtrar(String busca, String tipo, String categoria, LocalDate inicio, LocalDate fim) {
        // Só compara limites existentes; null representa um filtro aberto.
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final.");
        }
        // Normaliza a busca para comparação sem distinguir maiúsculas.
        String texto = busca == null ? "" : busca.trim().toLowerCase(java.util.Locale.ROOT);
        // O filtro herdado consulta o banco e exige todos os critérios no mesmo registro.
        return filtrar(t ->
                // Descrição sem filtro aceita todos os registros; contains procura um trecho.
                (texto.isEmpty() || t.getDescricao().toLowerCase(java.util.Locale.ROOT).contains(texto))
                // Tipo nulo permite receitas e despesas.
                && (tipo == null || t.getTipo().equalsIgnoreCase(tipo))
                // Categoria nula não restringe o resultado; a comparação ignora maiúsculas.
                && (categoria == null || t.getCategoria().equalsIgnoreCase(categoria))
                // Inclui a data inicial e rejeita somente datas anteriores.
                && (inicio == null || !t.getDataComoLocalDate().isBefore(inicio))
                // Inclui a data final e rejeita somente datas posteriores.
                && (fim == null || !t.getDataComoLocalDate().isAfter(fim)));
    }

    // Soma impactos positivos e negativos com BigDecimal.
    public static BigDecimal calcularSaldo(Collection<? extends Transacao> transacoes) {
        // Transforma cada registro em impacto positivo ou negativo antes de somar.
        return transacoes.stream().map(Transacao::calcularImpactoDecimal)
                // O valor inicial com escala 2 mantém o resultado de uma coleção vazia em 0,00.
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    // Soma somente as movimentações do tipo solicitado.
    public static BigDecimal calcularTotal(Collection<? extends Transacao> transacoes, String tipo) {
        // Seleciona apenas o tipo financeiro solicitado.
        return transacoes.stream().filter(t -> t.getTipo().equalsIgnoreCase(tipo))
                // Lê os valores como BigDecimal, evitando somas com double.
                .map(Transacao::getValorDecimal).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    // Agrupa despesas e acumula valores sem distinguir maiúsculas nas categorias.
    public static Map<String, BigDecimal> despesasPorCategoria(Collection<? extends Transacao> transacoes) {
        // O comparador une categorias como Moradia e moradia no mesmo grupo.
        Map<String, BigDecimal> totais = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // O agrupamento do relatório considera somente despesas.
        transacoes.stream().filter(t -> t.getTipo().equals("despesa"))
                // merge cria o primeiro total ou soma o novo valor ao grupo existente.
                .forEach(t -> totais.merge(t.getCategoria(), t.getValorDecimal(), BigDecimal::add));
        // Devolve os grupos ordenados para apresentação na tabela e no gráfico.
        return totais;
    }
}
