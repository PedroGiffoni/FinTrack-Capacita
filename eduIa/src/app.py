import os
import json
import pandas as pd
import streamlit as st
from dotenv import load_dotenv
from google import genai
import requests
import re

# 1. Carregar variáveis de ambiente (.env)
load_dotenv()
API_KEY = os.getenv("GEMINI_API_KEY")

if not API_KEY:
    raise ValueError("A variável GEMINI_API_KEY não foi encontrada. Verifique o arquivo .env.")

# 2. Configurar cliente Gemini (NOVA API)
client = genai.Client(api_key=API_KEY)
MODEL_NAME = "gemini-2.5-flash"


# 3. Função para carregar base de conhecimento

def carregar_bases():
    base_dir = os.path.join(os.path.dirname(__file__), "..", "data")

    perfil_path = os.path.join(base_dir, "perfil_investidor.json")

    transacoes_path = os.path.abspath(
        os.path.join(
            os.path.dirname(__file__),
            "..",
            "..",
            "FinTrack",
            "dados",
            "transacoes.csv"
        )
    )

    historico_path = os.path.join(base_dir, "historico_atendimento.csv")
    produtos_path = os.path.join(base_dir, "produtos_financeiros.json")

    perfil = {}
    transacoes = pd.DataFrame()
    historico = pd.DataFrame()
    produtos = {}

    if os.path.exists(perfil_path):
        with open(perfil_path, "r", encoding="utf-8") as f:
            perfil = json.load(f)

    if os.path.exists(transacoes_path):
        transacoes = pd.read_csv(
            transacoes_path,
            sep=";",
            names=[
                "id",
                "descricao",
                "valor",
                "tipo",
                "categoria",
                "data",
                "mensal",
                "dia"
            ],
            encoding="utf-8"
        )
    else:
        st.warning(f"Arquivo de transações do FinTrack não encontrado em: {transacoes_path}")

    if os.path.exists(historico_path):
        historico = pd.read_csv(historico_path, sep=",")

    if os.path.exists(produtos_path):
        with open(produtos_path, "r", encoding="utf-8") as f:
            produtos = json.load(f)

    return perfil, transacoes, historico, produtos

# 4. Função para buscar cotações reais
def buscar_cotacao(moeda):
    urls = {
        "dolar": "https://economia.awesomeapi.com.br/json/last/USD-BRL",
        "dollar": "https://economia.awesomeapi.com.br/json/last/USD-BRL",
        "usd": "https://economia.awesomeapi.com.br/json/last/USD-BRL",
        "euro": "https://economia.awesomeapi.com.br/json/last/EUR-BRL",
        "eur": "https://economia.awesomeapi.com.br/json/last/EUR-BRL",
        "bitcoin": "https://economia.awesomeapi.com.br/json/last/BTC-BRL",
        "btc": "https://economia.awesomeapi.com.br/json/last/BTC-BRL"
    }

    moeda = moeda.lower()

    if moeda not in urls:
        return None

    try:
        response = requests.get(urls[moeda], timeout=5)
        data = response.json()

        if "USDBRL" in data:
            return float(data["USDBRL"]["bid"])
        if "EURBRL" in data:
            return float(data["EURBRL"]["bid"])
        if "BTCBRL" in data:
            return float(data["BTCBRL"]["bid"])

        return None

    except:
        return None

# 5. Função para montar o prompt com contexto
def montar_prompt(pergunta, perfil, transacoes, historico, produtos):
    prompt_sistema = """
Você é o Edu, um educador e consultor financeiro inteligente.

OBJETIVO:
Ensinar finanças pessoais de forma simples e clara.
Você também pode fornecer cotações de moedas, pois recebe esses dados de uma API externa.

REGRAS:
- Nunca invente nomes para o usuário.
- Só use um nome se o usuário disser explicitamente.
- Caso contrário, trate o usuário apenas como "você".
- Não crie saudações artificiais ("Olá, investidor", "Olá, seu educador financeiro", etc.).
- Comece a resposta diretamente com o conteúdo.
- Você PODE fornecer cotações reais quando a API retornar valores.
- Depois da cotação, você pode complementar com explicações educativas.
- Não invente dados que não vieram da API.
- Não recomende investimentos específicos.
- Use linguagem simples, como se explicasse para um amigo.
- Se realmente não souber algo, admita: "Não tenho essa informação, mas posso explicar o conceito".
"""

    contexto = "=== PERFIL DO CLIENTE ===\n"
    contexto += json.dumps(perfil, ensure_ascii=False, indent=2)
    contexto += "\n\n=== PRODUTOS FINANCEIROS (PARA ENSINO) ===\n"
    contexto += json.dumps(produtos, ensure_ascii=False, indent=2)

    if not transacoes.empty:
        contexto += "\n\n=== TRANSACOES DO CLIENTE ===\n"
        contexto += transacoes.to_csv(index=False)

    if not historico.empty:
        contexto += "\n\n=== HISTORICO DE ATENDIMENTO ===\n"
        contexto += historico.to_csv(index=False)

    prompt_usuario = f"""
Pergunta do usuário:
\"\"\"{pergunta}\"\"\"
"""

    return prompt_sistema + "\n\n" + contexto + "\n\n" + prompt_usuario

# 6. Função para chamar o Gemini
def chamar_gemini(prompt):
    try:
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=prompt
        )

        texto = response.text
        texto = re.sub(r"^(Olá|Oi|Saudações)[^a-zA-Z]*", "", texto, flags=re.IGNORECASE)

        return texto.strip()

    except Exception as e:
        erro = str(e)

        if "503" in erro or "UNAVAILABLE" in erro:
            return "O Edu está temporariamente indisponível porque o modelo de IA está com alta demanda. Tente novamente em alguns instantes."

        if "429" in erro:
            return "O limite de uso da API foi atingido no momento. Tente novamente mais tarde."

        if "403" in erro:
            return "Houve um problema com a chave da API Gemini. Verifique se a chave está válida no arquivo .env."

        return "Não consegui consultar a IA agora. Erro: " + erro

# 7. Interface Streamlit
def main():
    st.set_page_config(page_title="Edu - Educador Financeiro (Gemini)", page_icon="💸")
    st.title("💸 Edu - Educador Financeiro Inteligente (versão Gemini)")
    st.write("Agente de IA que ensina finanças pessoais baseado nos seus dados reais e em informações de produtos financeiros.")

    pergunta = st.text_area("Digite sua pergunta:", height=100)

    if st.button("Perguntar para o Edu"):
        if not pergunta.strip():
            st.warning("Digite uma pergunta.")
            return

        palavras_cotacao = ["dólar", "dolar", "dollar", "usd", "euro", "eur", "bitcoin", "btc"]

        cotacao_detectada = None
        for palavra in palavras_cotacao:
            if palavra in pergunta.lower():
                cotacao_detectada = palavra
                break

        resposta_cotacao = None
        if cotacao_detectada:
            resposta_cotacao = buscar_cotacao(cotacao_detectada)

        perfil, transacoes, historico, produtos = carregar_bases()

        prompt = montar_prompt(pergunta, perfil, transacoes, historico, produtos)
        resposta_edu = chamar_gemini(prompt)

        st.markdown("### Resposta do Edu:")

        if resposta_cotacao:
            st.success(f"💱 Cotação atual ({cotacao_detectada.upper()}): **R$ {resposta_cotacao:.2f}**")
        elif cotacao_detectada:
            st.error("Não consegui obter a cotação agora. Tente novamente em instantes.")

        st.write(resposta_edu)

if __name__ == "__main__":
    main()
