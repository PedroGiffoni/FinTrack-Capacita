// Agrupa esta classe na camada repository do projeto.
package repository;

// Disponibiliza listas cujo conteúdo pode crescer durante a execução.
import java.util.ArrayList;
// Disponibiliza operações que aceitam diferentes implementações de coleção.
import java.util.Collection;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza validação de referências obrigatórias.
import java.util.Objects;
// Disponibiliza os critérios utilizados para filtrar coleções genéricas.
import java.util.function.Predicate;

// Coleção reutilizável; T representa o tipo dos registros aceitos pelo repositório.
public class RepositorioGenerico<T> {
    // T limita o tipo dos elementos; final impede trocar a lista, não modificar seu conteúdo.
    private final List<T> registros = new ArrayList<>();

    // Valida o registro e o encaminha ao armazenamento desta camada.
    public void adicionar(T registro) {
        // Rejeita null antes de incluir um registro.
        registros.add(Objects.requireNonNull(registro, "O registro é obrigatório."));
    }

    // Aceita T e suas subclasses; valida o lote antes de modificar a coleção.
    public void adicionarTodos(Collection<? extends T> origem) {
        // Uma origem nula não representa uma coleção válida.
        Objects.requireNonNull(origem, "A origem é obrigatória.");
        // Valida todo o lote antes de adicionar qualquer elemento.
        origem.forEach(Objects::requireNonNull);
        // Inclui o lote validado; extends T permite receber subclasses.
        registros.addAll(origem);
    }

    // Remove o registro solicitado e informa se algo foi encontrado.
    public boolean remover(T registro) {
        // Remove pela igualdade do objeto e informa se ele foi encontrado.
        return registros.remove(registro);
    }

    // Devolve os registros disponíveis sem permitir alteração direta da coleção interna.
    public List<T> listar() {
        // Retorna uma fotografia imutável, sem expor a lista interna.
        return List.copyOf(registros);
    }

    // Seleciona os registros que atendem ao critério recebido.
    public List<T> filtrar(Predicate<? super T> criterio) {
        // Aplica o predicado e cria outra lista com os elementos aprovados.
        return registros.stream().filter(criterio).toList();
    }

    // Aceita uma coleção de T ou de um supertipo, que pode receber todos os registros.
    public void copiarPara(Collection<? super T> destino) {
        // Um destino super T pode receber todos os registros do tipo T.
        destino.addAll(registros);
    }

    // O curinga não exige conhecer o tipo dos elementos para contar a coleção.
    public static int contar(Collection<?> registros) {
        // A contagem não depende do tipo dos elementos; o parâmetro utiliza ?.
        return registros.size();
    }
}
