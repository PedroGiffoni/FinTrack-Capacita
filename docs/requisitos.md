# Entrega intermediária

O FinTrack evolui a aplicação de console construída na etapa inicial do curso, mantendo receitas, despesas, categorias e transações mensais.

## Requisitos e implementação

| Requisito | Implementação |
| --- | --- |
| Classes e métodos genéricos | RepositorioGenerico<T>, com adicionar, remover, listar e filtrar |
| Curinga ? | contagem de coleções de qualquer tipo |
| ? extends T | importação de coleções com subclasses e cálculo de saldo |
| ? super T | cópia para coleções de supertipos e predicados de filtro |
| Application e start(Stage) | app.FinApp |
| Tela principal | view/transacoes.fxml e TransacoesController |
| Tela de cadastro e edição | view/transacao-formulario.fxml e TransacaoController |
| Tela de relatório e saldo | view/relatorio.fxml e RelatorioController |
| FXML e controllers | fx:controller e campos @FXML |
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

O esquema mantém os campos pedidos no exemplo do enunciado, com adaptação do valor monetário para centavos e acréscimo de categoria e recorrência. O SQLite usa INTEGER PRIMARY KEY AUTOINCREMENT para gerar os identificadores.

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
