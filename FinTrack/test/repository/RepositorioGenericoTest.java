// Agrupa esta classe na camada repository do projeto.
package repository;

// Disponibiliza listas cujo conteúdo pode crescer durante a execução.
import java.util.ArrayList;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.Test;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de RepositorioGenerico.
class RepositorioGenericoTest {
    // Cenário de regressão: adiciona lista e remove elementos.
    @Test void adicionaListaERemoveElementos() {
        // Prepara repositorio com os dados ou recursos usados nas próximas operações.
        RepositorioGenerico<String> repositorio = new RepositorioGenerico<>();
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        repositorio.adicionar("Um");
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        repositorio.adicionar("Dois");
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(List.of("Um", "Dois"), repositorio.listar());
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(repositorio.remover("Um"));
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(repositorio.remover("Ausente"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(List.of("Dois"), repositorio.listar());
    }

    // Cenário de regressão: lista nao permite alterar repositorio.
    @Test void listaNaoPermiteAlterarRepositorio() {
        // Prepara repositorio com os dados ou recursos usados nas próximas operações.
        RepositorioGenerico<Integer> repositorio = new RepositorioGenerico<>();
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        repositorio.adicionar(1);
        // Relê os registros e captura o resultado que será apresentado ou verificado.
        List<Integer> copia = repositorio.listar();
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(UnsupportedOperationException.class, () -> copia.add(2));
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        repositorio.adicionar(3);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(List.of(1), copia);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(List.of(1, 3), repositorio.listar());
    }

    // Cenário de regressão: aceita subclasses e copia para supertipo.
    @Test void aceitaSubclassesECopiaParaSupertipo() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita receita = new Receita("Outros", "Item", 1, "01/01/2026");
        // Prepara repositorio com os dados ou recursos usados nas próximas operações.
        RepositorioGenerico<Transacao> repositorio = new RepositorioGenerico<>();
        // Adiciona a coleção para verificar a aceitação de subtipos pelo curinga extends.
        repositorio.adicionarTodos(List.of(receita));
        // Prepara destino com os dados ou recursos usados nas próximas operações.
        List<Object> destino = new ArrayList<>();
        // Copia os elementos para uma coleção de supertipo, exercitando o curinga super.
        repositorio.copiarPara(destino);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(List.of(receita), destino);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(1, RepositorioGenerico.contar(destino));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(List.of(receita), repositorio.filtrar(t -> t.getValor() == 1));
    }

    // Cenário de regressão: rejeita nulos sem importacao parcial.
    @Test void rejeitaNulosSemImportacaoParcial() {
        // Prepara repositorio com os dados ou recursos usados nas próximas operações.
        RepositorioGenerico<String> repositorio = new RepositorioGenerico<>();
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(NullPointerException.class, () -> repositorio.adicionar(null));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(NullPointerException.class, () -> repositorio.adicionarTodos(java.util.Arrays.asList("Item", null)));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(repositorio.listar().isEmpty());
    }
}
