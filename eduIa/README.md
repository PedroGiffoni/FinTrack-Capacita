# Edu — módulo legado opcional

O Edu integrado ao FinTrack é implementado em **Java**, dentro da interface JavaFX, e não depende de Python ou Streamlit. Consulte o [README principal](../README.md) e o [guia do código Java](../docs/guia-codigo.md).

Esta pasta preserva a versão anterior do Edu para estudo e execução independente. As respostas são obtidas pela Groq e as transações são consultadas no mesmo SQLite do FinTrack, em modo somente leitura.

## Tecnologias

Python 3.10 ou superior, Streamlit, Groq, Pandas, sqlite3 e Requests. As dependências estão em requirements.txt e são separadas das dependências Maven. A AwesomeAPI fornece as cotações da versão Streamlit.

## Execução no Windows

Na raiz do repositório:

```powershell
.\configurar_edu.bat
.\eduIa\rodar_edu.bat
```

O primeiro comando cria eduIa/.venv e instala as dependências. O segundo inicia o Streamlit no endereço local do computador.

Cadastre transações no FinTrack antes de consultar seu panorama. O banco padrão é FinTrack/dados/fintrack.db. A variável FINTRACK_BANCO permite indicar outro arquivo SQLite, incluindo bancos temporários de teste.

## Chave e contexto

Informe a chave Groq no campo da interface. O link de criação abre o painel de chaves da Groq. Esta versão usa a credencial na sessão e não implementa o cofre DPAPI do Edu nativo Java.

O código consulta novamente o banco ao preparar o contexto de uma pergunta. Os arquivos opcionais de perfil, produtos e histórico de atendimento complementam a versão antiga; eles não substituem o SQLite como origem das transações.

As perguntas e o contexto utilizado são enviados à Groq. As cotações da versão Streamlit são independentes dos relatórios locais do aplicativo Java.

## Testes

Após configurar o ambiente, execute na raiz:

```powershell
.\eduIa\.venv\Scripts\python.exe -m unittest discover -s eduIa/test -v
```

Os testes usam banco temporário e cliente Groq simulado, sem chave real. O teste Java de inicialização e encerramento do módulo legado pode ser habilitado separadamente:

```powershell
mvn '-Dmaven.repo.local=.m2/repository' '-Dfintrack.testes.edu=true' '-Dtest=EduIntegrationTest' test
```

Para o trabalho do curso, a referência principal é a aplicação JavaFX e suas classes Java.
