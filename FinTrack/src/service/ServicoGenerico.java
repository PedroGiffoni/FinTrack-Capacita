// Agrupa as operações reutilizáveis na camada de serviços.
package service;

// Permite receber coleções de T e de suas subclasses.
import java.util.Collection;
// Representa o resultado ordenado das consultas.
import java.util.List;
// Recebe critérios capazes de avaliar T ou um supertipo.
import java.util.function.Predicate;
// Reutiliza a proteção das listas e a seleção de registros.
import repository.RepositorioGenerico;

// Define operações comuns; a especialização escolhe como acessar a persistência.
public abstract class ServicoGenerico<T> {
    // Obtém os registros atuais; subclasses podem devolver coleções de subtipos de T.
    protected abstract Collection<? extends T> consultar();

    // Exige que cada serviço implemente a gravação de um registro do tipo T.
    public abstract void adicionar(T registro);

    // Exige que a especialização remova o registro identificado na persistência.
    public abstract boolean remover(int id);

    // Devolve uma nova consulta sem expor uma lista que permita inclusão ou exclusão.
    public List<T> listar() {
        // Cria uma coleção para esta consulta, sem manter dados antigos em cache.
        RepositorioGenerico<T> repositorio = new RepositorioGenerico<>();
        // Consulta a fonte definida pela especialização e aceita subclasses de T.
        repositorio.adicionarTodos(consultar());
        // Protege a estrutura da lista; os próprios objetos não são copiados.
        return repositorio.listar();
    }

    // Seleciona registros atuais com um critério de T ou de um supertipo.
    public List<T> filtrar(Predicate<? super T> criterio) {
        // Usa um repositório próprio para esta seleção, isolado das consultas anteriores.
        RepositorioGenerico<T> repositorio = new RepositorioGenerico<>();
        // Reconsulta a persistência antes de aplicar o predicado.
        repositorio.adicionarTodos(consultar());
        // Reutiliza o filtro genérico e devolve os registros aprovados.
        return repositorio.filtrar(criterio);
    }
}
