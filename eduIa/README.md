📘 README 

## — EDU: Educador Financeiro Inteligente

💡 Sobre o Projeto

O Edu é um agente de IA focado em educação financeira, criado para explicar conceitos de forma simples, personalizada e usando dados reais do usuário como exemplos.
Ele também consulta cotações reais (dólar, euro, bitcoin etc.) através da AwesomeAPI.

O objetivo do Edu não é recomendar investimentos, mas ensinar, contextualizar e ajudar o usuário a entender melhor suas próprias finanças.

## 🚀 Tecnologias Utilizadas
Python 3.10+

Streamlit (interface)

Google Gemini 2.5 Flash (LLM)

AwesomeAPI (cotações em tempo real)

Pandas (manipulação de dados)

dotenv (variáveis de ambiente)

JSON/CSV (base de conhecimento)

## 🧠 Como o Edu Funciona
O Edu recebe:

Perfil do investidor

Histórico de atendimento

Transações financeiras

Lista de produtos financeiros

Esses dados são carregados automaticamente da pasta data/ e injetados no prompt do modelo.

Quando o usuário faz uma pergunta, o Edu:

Analisa a intenção

Verifica se há cotação envolvida

Consulta a AwesomeAPI (se necessário)

Monta um prompt completo com todos os dados

Gera uma resposta clara, simples e personalizada

## 🔐 Regras de Segurança e Comportamento
O Edu foi projetado para ser seguro e evitar alucinações:

Não inventa nomes

Não cria saudações artificiais

Não inventa dados que não vieram da API ou da base

Não recomenda investimentos

Admite quando não sabe algo

Explica conceitos de forma simples

Usa cotações reais quando disponíveis

Se a API cair, responde de forma amigável (tratamento de erro 429/503)

## 📂 Estrutura do Projeto

'''
📦 edu-financeiro
┣ 📂 data
┃ ┣ perfil_investidor.json
┃ ┣ transacoes.csv
┃ ┣ historico_atendimento.csv
┃ ┗ produtos_financeiros.json
┣ 📂 src
┃ ┗ app.py
┣ 01-documentacao-agente.md
┣ 02-base-conhecimento.md
┣ 03-prompts.md
┣ 04-metricas.md
┣ 05-pitch.md
┗ README.md
'''
## ▶️ Como Rodar o Projeto

1. Instale as dependências

'''
pip install -r requirements.txt
''' 2. Configure sua chave da API Gemini
Crie um arquivo .env na raiz:

'''
GEMINI_API_KEY=coloque_sua_chave_aqui
''' 3. Execute o Streamlit
'''
streamlit run src/app.py
'''

## 💸 Funcionalidades Principais
✔ Explicação de conceitos financeiros
Ex.: CDI, Selic, FII, renda fixa, risco, câmbio.

✔ Análise personalizada
Usa seus próprios gastos e perfil para contextualizar.

✔ Cotações em tempo real
Dólar, euro, bitcoin etc.

✔ Tratamento de erros
Se a API do Gemini estiver indisponível, o Edu responde:

“O serviço está temporariamente indisponível, mas posso te ajudar com outra dúvida.”

✔ Linguagem simples e direta
Sem jargões, sem enrolação.

## 🧪 Avaliação e Métricas
O projeto inclui um arquivo 04-metricas.md com:

Métricas de assertividade, segurança e coerência

Cenários de teste

Formulário de feedback

Espaço para registrar resultados

## 🎤 Pitch do Projeto
O arquivo 05-pitch.md contém:

Estrutura do pitch de 3 minutos

Roteiro completo

Sugestões de demo

Checklist final

## 🛠 Tratamento de Erros do Gemini
O app inclui proteção contra:

429 (quota excedida)

503 (modelo indisponível)

Timeouts

Falhas de rede

Quando isso acontece, o Edu responde de forma amigável e continua funcionando.

## 📜 Licença
Este projeto é de uso educacional e pode ser adaptado livremente.

## 🙋‍♂️ Autor
Projeto desenvolvido por Pedro Giffoni, como parte do laboratório da DIO — BIA do Futuro.
