# Base de Conhecimento — EDU (Versão Atualizada)

## Dados Utilizados

| Arquivo                     | Formato | Para que serve no Edu?                                                                                        |
| --------------------------- | ------- | ------------------------------------------------------------------------------------------------------------- |
| `historico_atendimento.csv` | CSV     | Permite que o Edu mantenha continuidade no atendimento, entendendo temas já explicados e dúvidas recorrentes. |
| `perfil_investidor.json`    | JSON    | Ajuda o Edu a adaptar explicações ao perfil do usuário.                                                       |
| `produtos_financeiros.json` | JSON    | Lista os produtos que o Edu pode explicar de forma educativa.                                                 |
| `transacoes.csv`            | CSV     | Permite que o Edu analise padrões de gastos e use exemplos reais nas explicações.                             |

---

## Adaptações nos Dados

O produto “Fundo Imobiliário (FII)” substituiu o “Fundo Multimercado”, para garantir que o conjunto de produtos seja coerente com o conhecimento do criador do agente e facilite a validação das respostas.

---

## Estratégia de Integração

### Como os dados são carregados?

Os dados são carregados diretamente via código Python, garantindo que o Edu sempre use a versão mais atual da base de conhecimento:

```python
import pandas as pd
import json

perfil = json.load(open('./data/perfil_investidor.json'))
transacoes = pd.read_csv('./data/transacoes.csv')
historico = pd.read_csv('./data/historico_atendimento.csv')
produtos = json.load(open('./data/produtos_financeiros.json'))

```

### Como os dados são usados no prompt?

O Edu injeta os dados diretamente no prompt, exatamente como você pediu.

Isso garante:

contexto completo

explicações personalizadas

uso dos dados reais do usuário

consistência entre sessões

Além disso:

o Edu não inventa nomes

o Edu não cria saudações artificiais

o Edu pode fornecer cotações reais via AwesomeAPI

o Edu complementa cotações com explicações educativas

o Edu não recomenda investimentos específicos

PRODUTOS DISPONIVEIS PARA ENSINO (data/produtos_financeiros.json):
[
{
"nome": "Tesouro Selic",
"categoria": "renda_fixa",
"risco": "baixo",
"rentabilidade": "100% da Selic",
"aporte_minimo": 30.00,
"indicado_para": "Reserva de emergência e iniciantes"
},
{
"nome": "CDB Liquidez Diária",
"categoria": "renda_fixa",
"risco": "baixo",
"rentabilidade": "102% do CDI",
"aporte_minimo": 100.00,
"indicado_para": "Quem busca segurança com rendimento diário"
},
{
"nome": "LCI/LCA",
"categoria": "renda_fixa",
"risco": "baixo",
"rentabilidade": "95% do CDI",
"aporte_minimo": 1000.00,
"indicado_para": "Quem pode esperar 90 dias (isento de IR)"
},
{
"nome": "Fundo Imobiliário (FII)",
"categoria": "fundo",
"risco": "medio",
"rentabilidade": "Dividend Yield (DY) costuma ficar entre 6% a 12% ao ano",
"aporte_minimo": 100.00,
"indicado_para": "Perfil moderado que busca diversificação e renda recorrente mensal"
},
{
"nome": "Fundo de Ações",
"categoria": "fundo",
"risco": "alto",
"rentabilidade": "Variável",
"aporte_minimo": 100.00,
"indicado_para": "Perfil arrojado com foco no longo prazo"
}
]

```

---

## Exemplo de Contexto Montado

O exemplo de contexto montado abaixo, se baiseia nos dados originais da base de conhecimento, mas os sintetiza deixando apenas as informações mais relevantes, otimizando assim o consumo de tokens. Entretanto, vale lembrar que mais importante do que economizar tokens, é ter todas as informações relevantes disponíveis em seu contexto.

```

DADOS DO USUÁRIO:

- Perfil: Moderado
- Objetivo principal: Construir reserva de emergência
- Reserva atual: R$ 10.000 (meta: R$ 15.000)

RESUMO DE GASTOS MENSAIS:

- Moradia: R$ 1.380
- Alimentação: R$ 570
- Transporte: R$ 295
- Saúde: R$ 188
- Lazer: R$ 55,90
- Total de saídas: R$ 2.488,90

HISTÓRICO DE ATENDIMENTO:

- Já perguntou sobre CDB, Tesouro Selic e metas financeiras
- Última interação: atualização cadastral

PRODUTOS DISPONÍVEIS PARA EXPLICAÇÃO:

- Tesouro Selic (risco baixo)
- CDB Liquidez Diária (risco baixo)
- LCI/LCA (risco baixo)
- Fundo Imobiliário - FII (risco médio)
- Fundo de Ações (risco alto)

REGRAS DO EDU:

- Injeta dados diretamente no prompt
- Não inventa nomes
- Não cria saudações artificiais
- Pode fornecer cotações reais (via API)
- Complementa cotações com explicações educativas
- Não recomenda investimentos específicos
- Explica conceitos de forma simples

```

```
