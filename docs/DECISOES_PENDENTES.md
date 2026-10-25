# Decisões pendentes — PMCN Clóvis IA

Este documento separa propostas de decisões efetivamente confirmadas.

## Prioridade alta

### 1. Política da memória compartilhada
**Pendente de confirmação explícita.** Proposta para avaliação:
- Guardar automaticamente fatos gerais e preferências úteis não sensíveis, quando a função estiver habilitada.
- Pedir autorização antes de guardar informações pessoais, delicadas ou sensíveis.
- Permitir consultar, corrigir e apagar memórias.
- Nunca alimentar memória compartilhada a partir de chats temporários.

A proposta não é decisão final até ser confirmada.

### 2. Runtime e modelo local
**Pendente de teste real.** Avaliar compatibilidade Android/ARM64, desempenho no Moto G52, RAM, tamanho, português, licença e operação offline. Não escolher apenas com base em contagem de parâmetros ou alegações promocionais.

### 3. Armazenamento do histórico
**Pendente de protótipo.** Comparar SQLite transacional, JSONL append-only ou arquitetura híbrida. Avaliar integridade, busca, migrações, exportação e recuperação após falha. Em qualquer opção, o histórico original integral é obrigatório.

### 4. Proteção de dados locais
**Pendente de desenho técnico.** Definir criptografia em repouso, proteção por dispositivo/biometria quando viável, backup, restauração e comportamento após desinstalação. Não prometer proteção não implementada e testada.

## Prioridade média

### 5. Comportamento do chat temporário
Definir timeout, comportamento ao alternar de app ou bloquear a tela, reação ao encerramento do processo pelo Android, confirmação de descarte e eventual conversão explícita para conversa normal.

Requisitos já definidos: não aparece na lista normal, não alimenta memória compartilhada e não entra no histórico persistente.

### 6. Referência visual
A interface deve seguir fluxos reconhecíveis do WhatsApp Android, mas com identidade própria. Confirmar a versão de referência e, se necessário, usar capturas de tela fornecidas pelo usuário para validar espaçamento e comportamento. A documentação pública não dá acesso ao código privado do WhatsApp.

### 7. Licença
Definir se o código permanecerá privado/proprietário ou terá licença de publicação. Até então, não presumir licença de código aberto.

## Premissas já estabelecidas
- Nome do sistema/aplicativo: PMCN Clóvis IA; assistente: Clóvis.
- Plataforma inicial: Android; aparelho-alvo: Moto G52.
- Operação local/offline é objetivo central.
- Conversas normais preservam integralmente todas as mensagens.
- Cada conversa tem contexto independente, salvo compartilhamento habilitado.
- Ao criar conversa normal, oferecer memória exclusiva ou compartilhada.
- Chat temporário não aparece na lista normal e não alimenta memória compartilhada.
- Pesquisa global e por conversa deve encontrar mensagens antigas originais.
- Resumos e índices nunca substituem o histórico.
- Nenhum recurso é declarado concluído sem implementação e verificação.
