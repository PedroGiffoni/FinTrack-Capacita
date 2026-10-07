# Roteiro de apresentação da entrega intermediária

Este roteiro acompanha os requisitos FinTrack das páginas 7 a 9 do enunciado. Use a branch Fintrack-V2 e dados fictícios para a demonstração.

## 1. Preparar o ambiente

Abra a raiz do projeto no VS Code. Configure JDK 17 ou superior e Maven conforme o README. Para demonstrar sem alterar o banco pessoal, execute:

```powershell
.\scripts\executar.ps1 -Acao compilar
```

No terminal, inicie usando outra pasta de dados. O script de execução seleciona o JDK instalado; a propriedade abaixo é transmitida à JVM pelo ambiente e restaurada ao fechar o aplicativo:

```powershell
$opcoesAnteriores = $env:JAVA_TOOL_OPTIONS
try {
    $env:JAVA_TOOL_OPTIONS = "$opcoesAnteriores -Dfintrack.dados=tmp/apresentacao/dados"
    .\rodar_fintrack.bat
} finally {
    $env:JAVA_TOOL_OPTIONS = $opcoesAnteriores
}
```

A pasta tmp é ignorada pelo Git. Na primeira execução, a base de demonstração começa vazia; nas seguintes, conserva os registros dessa apresentação.

## 2. Mostrar as telas e a persistência

1. Cadastre uma receita de R$ 1.000,00 e uma despesa de R$ 200,00 no mesmo período. Confira o saldo de R$ 800,00.
2. Mostre data, descrição, tipo e valor na tabela principal.
3. Edite a despesa para R$ 250,00 e confira o saldo de R$ 750,00.
4. Filtre pela descrição e pelo tipo; limpe os filtros.
5. Abra o relatório, selecione o período e confira os totais e as categorias.
6. Feche e reabra com o mesmo comando para demonstrar a persistência no SQLite.
7. Remova a despesa após confirmar e confira o saldo de R$ 1.000,00.
8. Tente cadastrar valor negativo para mostrar a validação.

## 3. Explicar Generics no código

Abra RepositorioGenerico.java e ServicoGenerico.java. Mostre T, Collection<?>, Collection<? extends T> e Predicate<? super T>/Collection<? super T>. Explique que T é definido na utilização da classe.

Abra TransacaoService.java e mostre a especialização ServicoGenerico<Transacao>, a consulta pelo DAO e o filtro financeiro que usa o método herdado. A listagem e os filtros usados pelas telas passam por essa abstração. O serviço genérico não guarda uma cópia permanente dos dados.

## 4. Mostrar FXML, controllers e Scene Builder

Abra os quatro arquivos de FinTrack/resources/view no Scene Builder, seguindo o [guia do editor](scene-builder.md). Mostre a hierarquia de layouts, fx:controller, fx:id e a folha de estilo relativa. Use Preview para conferir a composição.

As telas foram definidas em FXML e revisadas visualmente no editor. Para demonstrar uma edição visual, altere temporariamente o espaçamento de um HBox no Inspector, observe o resultado e use Undo antes de salvar. Preserve os comentários, identificadores e caminhos.

No código, mostre @FXML e setOnAction nos controllers. Explique que os saldos, registros e mensagens são carregados durante a execução, por isso a prévia do editor não apresenta todos os dados.

## 5. Mostrar JDBC e explicar o esquema

Abra Conexao.java: DriverManager seleciona o driver pela URL JDBC. Abra TransacaoDAO.java: mostre CREATE TABLE, INSERT, SELECT, UPDATE e DELETE, os parâmetros de PreparedStatement e a leitura do ResultSet.

Apresente a [correspondência do SQL](requisitos.md#correspondência-do-sql-do-enunciado). O esquema foi adaptado para SQLite: dinheiro em centavos, data ISO e autoincremento na sintaxe desse banco. Explique a ausência de usuário e senha de servidor e o fechamento de recursos com try-with-resources.

Mostre try/catch e logs para erros SQL. Na migração, commit e rollback demonstram o requisito opcional de transações.

## 6. Executar e explicar JUnit

Com o aplicativo fechado e o JDK configurado, execute:

```powershell
mvn '-Dmaven.repo.local=.m2/repository' '-Dfintrack.testes.interface=true' verify
```

Mostre TransacaoTest, RepositorioGenericoTest e TransacaoDAOTest, exigidos pelo PDF. No DAO, identifique jdbc:sqlite::memory:, @BeforeEach, @AfterEach, @Test, assertEquals e assertThrows. Em TransacaoServiceTest, mostre o contrato genérico, saldo e filtros. Os testes usam dados fictícios e não alteram o banco pessoal.

Alguns testes provocam erros SQL intencionalmente para conferir rollback. Avalie o resultado pelo resumo de testes e BUILD SUCCESS. A integração do servidor Python legado é opcional e depende da propriedade fintrack.testes.edu; não é necessária para demonstrar o FinTrack Java.

## 7. Apresentar o Edu como recurso adicional

Mostre o panorama vindo do mesmo SQLite, o botão Confirmar chave e o consentimento para compartilhar transações. Para uma chamada real, configure a chave pela interface e use somente dados fictícios. O CRUD, relatório e testes do curso funcionam sem credencial Groq. A chave não faz parte do código nem do banco.

## Conferência antes da entrega

- Compartilhe o link da branch Fintrack-V2, que contém a versão intermediária.
- Confira os comandos de execução e testes na máquina usada para apresentar.
- Tenha as telas abertas no Scene Builder e os arquivos indicados disponíveis no editor.
- Apresente as adaptações do esquema SQL como decisões do projeto; a avaliação segue os critérios do professor.
