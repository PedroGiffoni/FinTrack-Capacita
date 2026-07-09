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

Além do sistema principal, o projeto conta com o **Edu IA**, um consultor financeiro desenvolvido em Python utilizando a API do Google Gemini.

---

# 🚀 Funcionalidades

## Cadastro de transações

O usuário pode cadastrar:

- Receitas
- Despesas
- Transações Mensais

Cada transação possui:

- ID automático
- Categoria
- Descrição
- Valor
- Tipo
- Data

Caso seja uma transação recorrente, também é informado o dia de vencimento ou recebimento.

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

# 🤖 Edu IA

O projeto possui integração com o **Edu IA**.

O Edu é uma aplicação desenvolvida em Python utilizando:

- Streamlit
- Google Gemini
- API AwesomeAPI (cotações)
- Pandas

Ao selecionar a opção **9** do menu do FinTrack, o Edu é iniciado automaticamente.

O Edu utiliza como base de conhecimento:

- Perfil financeiro
- Histórico de atendimento
- Produtos financeiros
- Transações cadastradas no FinTrack

Dessa forma, a IA consegue responder perguntas considerando os dados reais do usuário.

Exemplos:

> Como posso economizar este mês?

> Quanto estou gastando com alimentação?

> Meu saldo está saudável?

> Como funciona Tesouro Direto?

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

- Google Gemini

## Interface IA

- Streamlit

## Persistência

- CSV

---

# ▶ Como executar

## Executar o FinTrack

Compile:

```bash
javac -encoding UTF-8 -d build\classes src\app\Main.java src\Controller\*.java src\model\*.java src\utils\*.java src\exceptions\*.java
```

Execute:

```bash
java -cp build\classes app.Main
```

---

## Executar o Edu IA

Dentro da pasta:

```
eduIa
```

execute:

```bash
streamlit run src/app.py
```

Ou utilize:

```
rodar_edu.bat
```

---

# 📸 Menu Principal

```text
===== FINTRACK =====

1. Adicionar transação
2. Listar transações
3. Mostrar saldo
4. Remover transação
5. Relatório de receitas
6. Relatório de despesas
7. Relatório por categoria
8. Dashboard financeiro
9. Edu IA
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
- Integração em tempo real com IA
- Importação de extratos bancários
- Exportação para PDF e Excel

---

# 👨‍💻 Autor

### Pedro Giffoni

Fotógrafo publicitário e desenvolvedor em transição para a área de tecnologia, com foco em Java, Inteligência Artificial e Visão Computacional.

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
