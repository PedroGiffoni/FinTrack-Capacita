<div align="center">

# 💰 FinTrack

### Sistema Inteligente de Controle de Finanças Pessoais

Controle suas receitas e despesas, acompanhe sua evolução financeira e conte com o **Edu IA**, um consultor financeiro inteligente integrado ao sistema.

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)
![POO](https://img.shields.io/badge/POO-Programação%20Orientada%20a%20Objetos-blue?style=for-the-badge)
![Python](https://img.shields.io/badge/Python-3.x-yellow?style=for-the-badge&logo=python)
![Streamlit](https://img.shields.io/badge/Streamlit-App-red?style=for-the-badge&logo=streamlit)
![Gemini](https://img.shields.io/badge/Google-Gemini-blue?style=for-the-badge&logo=google)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-success?style=for-the-badge)

</div>

---

# 📖 Sobre o projeto

O **FinTrack** é um sistema desenvolvido em Java para gerenciamento de finanças pessoais.

Inicialmente criado como uma aplicação em console para prática de Programação Orientada a Objetos, o projeto evoluiu para um sistema mais completo, incorporando conceitos como:

- Programação Orientada a Objetos
- Herança
- Polimorfismo
- Encapsulamento
- Tratamento de Exceções
- Persistência em Arquivos
- Relatórios Financeiros
- Dashboard
- Integração com Inteligência Artificial

Além do sistema principal em Java, o projeto possui o **Edu IA**, um educador financeiro que utiliza as transações cadastradas no FinTrack como contexto para responder perguntas sobre finanças pessoais.

---

# 🚀 Funcionalidades

O FinTrack possui um menu interativo executado pelo terminal.

```text
===== FINTRACK - SEU CONTROLE FINANCEIRO =====

1. Adicionar nova transação
2. Listar transações
3. Mostrar saldo atual
4. Remover transação
5. Relatório de receitas
6. Relatório de despesas
7. Relatório por categoria
8. Dashboard financeiro
9. Edu - Educação financeira com IA
0. Sair
```

---


O usuário pode cadastrar novas movimentações financeiras informando:

- Categoria;
- Descrição;
- Valor;
- Tipo da transação;
- Data;
- Se a transação é mensal;
- Dia de vencimento ou recebimento, quando aplicável.

As transações podem ser classificadas como:

```text
Receita
Despesa
```

Entre as categorias disponíveis estão:

```text
Alimentação
Transporte
Moradia
Saúde
Educação
Lazer
Salário
Freelance
Investimentos
Outros
```

---

## 📋 Listagem de transações

O sistema permite visualizar todas as transações cadastradas.

Cada transação possui informações como:

- ID;
- Descrição;
- Valor;
- Tipo;
- Categoria;
- Data.

As transações mensais também apresentam o dia de vencimento ou recebimento.

---

## 💰 Cálculo do saldo

O saldo é calculado considerando o impacto de cada transação:

```text
Receita → adiciona valor ao saldo
Despesa → subtrai valor do saldo
```

O cálculo é realizado através do comportamento definido na classe `Transacao`.

---

## 🗑️ Remoção de transações

Cada transação possui um ID único.

O usuário pode informar o ID de uma transação para removê-la do sistema.

Após a remoção, o arquivo de dados é atualizado automaticamente.

---

## Relatórios

O sistema possui diversos relatórios:

- Todas as transações
- Apenas receitas
- Apenas despesas
- Relatório por categoria
- Dashboard financeiro

---

## Dashboard

O dashboard apresenta:

- Total de receitas
- Total de despesas
- Saldo atual
- Resumo visual utilizando barras no console

Exemplo:

```text
===== DASHBOARD =====

Receitas:
███████████████

Despesas:
███████

Saldo:
R$ 3.540,00
```

---

# 🤖 Edu IA — Educador Financeiro Inteligente

O **Edu IA** é o módulo de Inteligência Artificial integrado ao FinTrack.

Ele foi desenvolvido em:

- Python;
- Streamlit;
- Groq;
- GPT-OSS 20B;
- Pandas;
- Requests.

O Edu utiliza as informações financeiras cadastradas no FinTrack para fornecer respostas mais contextualizadas sobre finanças pessoais.


Dessa forma, a IA consegue responder perguntas considerando os dados reais do usuário.

Exemplos:

> Como posso economizar este mês?

> Quanto estou gastando com alimentação?

> Meu saldo está saudável?

> Como funciona Tesouro Direto?
>
> ## 🔑 API Key da Groq

A chave da API não fica armazenada no código-fonte do projeto.

Ao abrir o Edu IA, o próprio usuário deve informar sua API Key da Groq na barra lateral da aplicação.

O campo é protegido:

```text
Insira sua API Key da Groq
••••••••••••••••••••
```

Isso permite que cada pessoa utilize sua própria chave para acessar o modelo de Inteligência Artificial.

A aplicação também disponibiliza na barra lateral um link para criação da chave na plataforma Groq.

---

# 💾 Persistência de Dados

O sistema salva automaticamente todas as transações.

Os dados ficam armazenados em:

```
dados/transacoes.csv
```

Sempre que o sistema é iniciado, o arquivo é carregado automaticamente.

Isso permite que nenhuma informação seja perdida após fechar o programa.

---

# 🧠 Conceitos de Programação Utilizados

Durante o desenvolvimento foram aplicados diversos conceitos importantes da linguagem Java.

## Programação Orientada a Objetos

- Classes
- Objetos
- Métodos
- Construtores

---

## Encapsulamento

Todos os atributos são privados e acessados através de getters e setters.

---

## Herança

A classe base:

```
Transacao
```

é utilizada pelas subclasses:

```
Receita
Despesa
TransacaoMensal
```

---

## Polimorfismo

O sistema trabalha com uma única lista:

```java
ArrayList<Transacao>
```

Nela podem ser armazenados objetos de diferentes tipos:

- Receita
- Despesa
- TransacaoMensal

Além disso, o método:

```java
calcularImpactoNoSaldo()
```

é utilizado para calcular automaticamente o impacto de cada tipo de transação.

---

## Tratamento de Exceções

Foram utilizados:

- try
- catch
- NumberFormatException
- Exception
- Exceção personalizada

---

# 📂 Estrutura do Projeto

```
FinTrack

src
│
├── app
│     Main.java
│
├── Controller
│     FinTracker.java
│
├── model
│     Transacao.java
│     Receita.java
│     Despesa.java
│     TransacaoMensal.java
│
├── utils
│     Formatador.java
│     GerenciadorArquivo.java
│
└── exceptions
      EntradaInvalidaException.java
```

---

# 🛠 Tecnologias

## Java

- Java 25

## Python

- Python 3

## IA

- Groq

## Interface IA

- Streamlit

## Persistência

- CSV

---

## 1. Baixe ou clone o projeto

Abra a pasta principal do projeto no computador.

A estrutura deve manter o FinTrack Java e o Edu IA nas posições esperadas pelo código.

---

## 2. Configure a Aplicação pela primeira vez

## ⚙️ Configuração inicial

Na primeira vez que executar o projeto, utilize o arquivo:

```text
configurar_projeto.bat
```

Basta dar dois cliques no arquivo.

O configurador irá automaticamente:

- Verificar se o Java está instalado;
- Verificar se o compilador Java está disponível;
- Verificar se o Python está instalado;
- Criar o ambiente virtual do Edu IA;
- Instalar as dependências presentes no `requirements.txt`.

Após a mensagem de configuração concluída, execute:

```text
rodar_fintrack.bat
```

### Primeira utilização

```text
configurar_projeto.bat
        ↓
rodar_fintrack.bat
```

### Próximas utilizações

```text
rodar_fintrack.bat
```

> O arquivo `configurar_projeto.bat` precisa ser executado novamente apenas se o ambiente virtual for removido ou se for necessário reinstalar as dependências do projeto.

# 🚀 Execução simplificada com `rodar_fintrack.bat`

Depois que o projeto estiver configurado, localize o arquivo:

```text
rodar_fintrack.bat
```

na pasta principal do projeto.

Execute com:

```text
duplo clique
```

ou, pelo terminal:

```powershell
.\rodar_fintrack.bat
```

O arquivo `.bat` executa os comandos necessários para iniciar o FinTrack Java.

Após a inicialização, o menu será exibido no terminal:

```text
===== FINTRACK - SEU CONTROLE FINANCEIRO =====

1. Adicionar nova transação
2. Listar transações
3. Mostrar saldo atual
4. Remover transação
5. Relatório de receitas
6. Relatório de despesas
7. Relatório por categoria
8. Dashboard financeiro
9. Edu - Educação financeira com IA
0. Sair
```

---

# 🔮 Evoluções Futuras

O projeto foi planejado para crescer.

Próximas implementações:

- Interface Web
- API REST com Spring Boot
- Banco de Dados PostgreSQL
- Login de usuários
- Dashboard gráfico
- Deploy em nuvem
- Aplicação mobile
- Importação de extratos bancários
- Exportação para PDF e Excel

---

# 👨‍💻 Autor

### Pedro Giffoni

Desenvolvedor em transição para a área de tecnologia, com foco em Java, Inteligência Artificial e Visão Computacional.

GitHub:

```
https://github.com/PedroGiffoni
```

LinkedIn:

```
https://linkedin.com/in/PedroGiffoni
```

---

# 📄 Licença

Projeto desenvolvido para fins acadêmicos e educacionais.
