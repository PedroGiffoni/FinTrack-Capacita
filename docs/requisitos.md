# Entrega intermediária

O FinTrack evolui a aplicação de console construída na etapa inicial do curso, mantendo receitas, despesas, categorias e transações mensais.

## Requisitos e implementação

| Requisito | Implementação |
| --- | --- |
| Classes e métodos genéricos | RepositorioGenerico<T> e ServicoGenerico<T>; TransacaoService especializa o serviço em Transacao |
| Curinga ? | contagem de coleções de qualquer tipo |
| ? extends T | importação de coleções com subclasses e cálculo de saldo |
| ? super T | cópia para coleções de supertipos e predicados de filtro |
| Application e start(Stage) | app.FinApp |
| Tela principal | view/transacoes.fxml e TransacoesController |
| Tela de cadastro e edição | view/transacao-formulario.fxml e TransacaoController |
| Tela de relatório e saldo | view/relatorio.fxml e RelatorioController |
| FXML e controllers | fx:controller e campos @FXML |
| Scene Builder | Quatro telas abertas e revisadas visualmente; procedimento em scene-builder.md |
| Estilização | css/fintrack.css |
| Componentes gráficos | Label, TextField, TextArea, Button, DatePicker, CheckBox, TableView |
| Layout | VBox, HBox, GridPane e BorderPane |
| Eventos | setOnAction nos controllers |
| Banco relacional | SQLite local |
| Conexão JDBC | dao.Conexao e DriverManager |
| CRUD e consultas | TransacaoDAO, PreparedStatement e ResultSet |
| Erros de persistência | PersistenciaException e logs com java.util.logging |
| Transações | commit e rollback na migração |
| Testes | JUnit com @Test, @BeforeEach e @AfterEach |
| Banco para testes | SQLite em memória e arquivo temporário |
| Validações | assertEquals, assertThrows e verificações de inserção, edição, remoção e saldo |

## Organização das responsabilidades

- Os modelos validam os dados da transação.
- O DAO concentra SQL e conversão entre registros e objetos.
- O serviço calcula totais e aplica filtros.
- O repositório genérico oferece operações reutilizáveis sobre coleções.
- Os controllers tratam os eventos das telas.
- FXML define os componentes e CSS define sua aparência.

O esquema representa os dados do exemplo do enunciado com adaptações para SQLite. A coluna monetária muda de nome e unidade; a correspondência está abaixo. Categoria e recorrência são informações adicionais.

## Correspondência do SQL do enunciado

O exemplo da página 8 usa sintaxe de MySQL. A aplicação utiliza SQLite pelo driver JDBC e cria o esquema automaticamente em TransacaoDAO. Esta é uma adaptação documentada, não uma reprodução literal do DDL.

| Exemplo do PDF | Implementação SQLite | Motivo e comportamento |
| --- | --- | --- |
| id INT PRIMARY KEY AUTO_INCREMENT | id INTEGER PRIMARY KEY AUTOINCREMENT | O banco gera o identificador; a sintaxe corresponde ao SQLite |
| descricao VARCHAR(100) | descricao TEXT NOT NULL, com CHECK de texto preenchido | Armazena a descrição; o projeto não impõe o limite de 100 caracteres do exemplo |
| valor DECIMAL(10,2) | valor_centavos INTEGER NOT NULL, com CHECK de valor positivo | R$ 12,34 é gravado como 1234; BigDecimal mantém os cálculos decimais |
| tipo VARCHAR(10) | tipo TEXT NOT NULL, com CHECK para receita/despesa | Restringe o campo aos dois tipos usados pelo modelo |
| data DATE | data TEXT NOT NULL em yyyy-MM-dd | LocalDate valida a entrada; o formato ISO permite ordenar e comparar datas |

O SQLite não requer um servidor nem usuário e senha de conexão. O arquivo do banco fica fora do Git. O enunciado FinTrack não exige autenticação no banco, e os testes de DAO usam SQLite em memória, como solicitado na página 9. A aceitação do esquema adaptado na avaliação depende dos critérios do professor.

## Serviço genérico integrado

ServicoGenerico<T> define adicionar(T), remover(int), listar() e filtrar(Predicate<? super T>). As especializações implementam a persistência por consultar(), adicionar() e remover(). TransacaoService estende ServicoGenerico<Transacao> e utiliza o DAO SQLite.

Listagem e filtros são herdados e usados pelos controllers e pelo Edu. Cada chamada consulta a fonte novamente e utiliza RepositorioGenerico<T> para proteger a estrutura da lista. Os testes exercitam cadastro e remoção pelo contrato genérico, aceitação de predicado de supertipo e consulta de registros gravados diretamente no DAO.

## Verificação manual

1. Cadastrar uma receita e uma despesa e conferir tabela e saldo.
2. Editar descrição, valor, categoria e data.
3. Marcar uma transação como mensal e conferir o dia.
4. Cancelar uma edição e verificar que os dados anteriores foram mantidos.
5. Remover uma transação, primeiro cancelando e depois confirmando.
6. Filtrar transações por descrição e tipo.
7. Abrir o relatório e filtrar por período e categoria.
8. Informar valor inválido, data impossível e período invertido.
9. Fechar e reabrir a aplicação para conferir a persistência.
10. Abrir os FXML no Scene Builder e revisar seus componentes.

Não há geração automática de parcelas, autenticação, API web ou deploy nesta etapa.

## Tela do Edu

A educação financeira integra a interface JavaFX por `edu.fxml`, `EduController` e a folha de estilo comum. O panorama consulta as transações pelo serviço e DAO SQLite. `EducacaoFinanceiraService` envia perguntas à Groq por HTTP, com JSON estruturado e histórico da sessão. Consultas em `Task` mantêm a interface responsiva e permitem cancelamento. A chave permanece na sessão por padrão; no Windows, a opção de lembrar utiliza DPAPI por usuário, fora do banco e do projeto. Testes JUnit verificam contexto, resposta, erros de credencial e a opção de não compartilhar transações.
