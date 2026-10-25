# Requisitos funcionais — PMCN Clóvis IA

Estado: definição inicial; o aplicativo ainda não foi implementado.

## Identidade
- Aplicativo: PMCN Clóvis IA.
- Assistente: Clóvis, IA pessoal do Pedro.
- Idioma inicial: português brasileiro.
- Objetivo: funcionar localmente no Android e continuar útil offline após a instalação do modelo.

## Interface
- Organização inspirada na experiência do WhatsApp para Android, com identidade visual própria.
- Lista de conversas, busca global, criação de conversa e configurações.
- Em cada conversa: cabeçalho, mensagens, campo de texto, envio e estados de processamento.
- Criar, renomear e excluir conversas.

## Tipos de conversa
- Memória exclusiva: contexto e histórico da conversa ficam isolados.
- Memória compartilhada: pode consultar e registrar dados na memória pessoal, conforme regras configuradas.
- Chat temporário: não aparece na lista normal, não alimenta memória compartilhada e não é incluído no histórico persistente nem na busca global. Ao encerrar, descartar os dados temporários controlados pelo app com confirmação.
- A política exata de gravação automática na memória compartilhada permanece pendente de confirmação.

## Histórico integral
- Preservar todas as mensagens do usuário e do assistente em conversas normais.
- O histórico deve sobreviver ao fechamento e reinício do app.
- Mensagens antigas não podem ser removidas para economizar contexto.
- Resumos e índices são auxiliares e nunca substituem o histórico original.
- Preservar autor, ordem, data/hora, identificadores, conteúdo e estado.
- Planejar exportação TXT e JSON/JSONL, backup e restauração local.

## Recuperação de contexto antigo
- A janela de contexto do modelo é limitada; não se promete enviar todo o histórico ao modelo a cada pergunta.
- Combinar histórico integral, resumos derivados, pesquisa textual e recuperação de trechos relevantes.
- Quando possível, responder com referência à data ou mensagem original.
- Se não encontrar a informação, Clóvis deve admitir isso em vez de inventar lembranças.

## Pesquisa
- Dentro da conversa: buscar no histórico completo, listar ocorrências, navegar entre elas e abrir/destacar a mensagem original.
- Pesquisa global: procurar em todas as conversas normais e mostrar conversa, trecho, autor e data/hora, do mais recente ao mais antigo.
- Incluir pesquisa por data.
- Avaliar SQLite FTS5 para indexação local; busca semântica é uma evolução futura.

## Privacidade e aparelho-alvo
- Aparelho inicial informado: Moto G52, Android 13, Snapdragon 680, 4 GB de RAM e 128 GB.
- Compatibilidade e desempenho só serão confirmados após testes reais.
- Priorizar armazenamento local e não enviar conteúdo a serviços remotos sem decisão explícita.
- Não registrar conteúdo de chats temporários em logs.
- Não prometer apagamento de cópias externas ao controle do aplicativo.

## Critérios gerais
- Não misturar contextos entre conversas.
- Não perder mensagens após reiniciar o app.
- Busca deve encontrar mensagens antigas fora de resumos.
- Não declarar funcionalidades concluídas sem implementação e teste.
