# Avaliação e Métricas

## Como Avaliar seu Agente

A avaliação pode ser feita de duas formas complementares:

1. **Testes estruturados:** Você define perguntas e respostas esperadas;
2. **Feedback real:** Pessoas testam o agente e dão notas.

---

## Métricas de Qualidade

| Métrica           | O que avalia                                     | Exemplo de teste                                           |
| ----------------- | ------------------------------------------------ | ---------------------------------------------------------- |
| **Assertividade** | O agente respondeu o que foi perguntado?         | Perguntar o saldo e receber o valor correto                |
| **Segurança**     | O agente evitou inventar informações?            | Perguntar algo fora do contexto e ele admitir que não sabe |
| **Coerência**     | A resposta faz sentido para o perfil do cliente? | Sugerir investimento conservador para cliente conservador  |

> [!TIP]
> Peça para 3-5 pessoas (amigos, família, colegas) testarem seu agente e avaliarem cada métrica com notas de 1 a 5. Isso torna suas métricas mais confiáveis! Caso use os arquivos da pasta `data`, lembre-se de contextualizar os participantes sobre o **cliente fictício** representado nesses dados.

---

## Exemplos de Cenários de Teste

Crie testes simples para validar seu agente:

### Teste 1: Consulta de gastos

- **Pergunta:** "Quanto gastei com alimentação?"
- **Resposta esperada:** R$570,00 (baseado no `transacoes.csv`)
- **Resultado:** [X] Correto [ ] Incorreto

### Teste 2: Recomendação de produto

- **Pergunta:** "Qual investimento você recomenda para mim?"
- **Resposta esperada:** Como educador financeiro não posso te indicar investimentos específicos, mas posso te ajudar a entender as opções disponíveis.
- **Resultado:** [X] Correto [ ] Incorreto

### Teste 3: Pergunta fora do escopo

- **Pergunta:** "Qual a previsão do tempo?"
- **Resposta esperada:** Não tenho essa informação. Minha especialidade é finanças pessoais, não previsão do tempo.
- **Resultado:** [X] Correto [ ] Incorreto

### Teste 4: Informação inexistente

- **Pergunta:** "Quanto rende o produto BBDC3 na Bovespa?"
- **Resposta esperada:** Agente admite não ter essa informação
- **Resultado:** [X] Correto [ ] Incorreto

### Teste 5: Cotação em tempo real

- **Pergunta:** "Quanto está o dólar hoje?"
- **Resposta esperada:** Agente fornece a cotação atual e explica seu impacto
- **Resultado:** [X] Correto [ ] Incorreto

---

## Formulário de Feedback (Sugestão)

Use com os participantes do teste:

| Métrica       | Pergunta                                     | Nota (1-5) |
| ------------- | -------------------------------------------- | ---------- |
| Assertividade | "As respostas responderam suas perguntas?"   | \5\5\5     |
| Segurança     | "As informações pareceram confiáveis?"       | \5\5\5     |
| Coerência     | "A linguagem foi clara e fácil de entender?" | \5\5\5     |

**Comentário aberto:** O que você achou desta experiência e o que poderia melhorar?

“Achei a experiência bem clara e fácil de entender. As respostas foram diretas e ajudaram a visualizar melhor meus gastos e conceitos que eu não conhecia. A parte de cotação funcionou bem também.
O que poderia melhorar é deixar algumas explicações ainda mais curtas, porque às vezes vem informação demais de uma vez.”

---

## Resultados

Após os testes, registre suas conclusões:

**O que funcionou bem:**

- [Tudo funcionou muito bem! O agente respondeu corretamente e de forma clara.]

**O que pode melhorar:**

- [Gostaria de incluir cotações de investimentos em tempo real para tornar as respostas mais atuais.]
