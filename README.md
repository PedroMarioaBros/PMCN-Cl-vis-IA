# PMCN Clóvis IA

**PMCN Clóvis IA** é o projeto de uma IA pessoal chamada **Clóvis**, planejada para funcionar prioritariamente no próprio aparelho Android, com foco em privacidade, continuidade de contexto e uso offline.

> **Estado atual:** fase de planejamento e definição técnica. Este repositório está começando agora; ainda não há aplicativo implementado nem compatibilidade/performance confirmadas no aparelho-alvo.

## Objetivos principais

- Executar um modelo de IA local, sem depender de uma API remota para o funcionamento básico.
- Ter uma interface Android familiar, inspirada na organização e nos fluxos do WhatsApp, mas com identidade visual própria.
- Manter conversas independentes, pesquisáveis e persistentes.
- Preservar o histórico original integral das conversas normais; resumos nunca substituem mensagens.
- Recuperar informações antigas por pesquisa e seleção de trechos relevantes antes de responder.
- Oferecer memória exclusiva por conversa, memória compartilhada e chat temporário.
- Ser transparente sobre limitações do modelo, do aparelho e das ferramentas realmente implementadas.

## Aparelho-alvo inicial

Moto G52, Android 13, Snapdragon 680, 4 GB de RAM e 128 GB de armazenamento, conforme informações disponíveis no planejamento. A compatibilidade e o desempenho de qualquer modelo serão validados por testes reais antes de serem declarados como confirmados.

## Princípios que não podem ser quebrados

1. **Não inventar progresso:** só declarar concluído aquilo que foi efetivamente implementado e verificado.
2. **Histórico original é a fonte da verdade:** resumos e memórias são auxiliares.
3. **Privacidade por padrão:** conteúdo permanece local sempre que tecnicamente possível; nenhum envio remoto sem decisão explícita e documentação.
4. **Offline como objetivo central:** baixar modelos/recursos pode exigir internet; o funcionamento básico pretendido deve continuar sem conexão depois da preparação.
5. **Persistência confiável:** mensagens normais devem sobreviver ao fechamento e reinício do app.
6. **Transparência:** não prometer memória perfeita, desempenho ilimitado ou ausência absoluta de limitações.
7. **Desenvolvimento incremental:** cada etapa deve ter critérios de aceite e registro de alterações.

## Documentos

- [Requisitos funcionais](docs/REQUISITOS_FUNCIONAIS.md)
- [Arquitetura técnica proposta](docs/ARQUITETURA_TECNICA.md)
- [Plano de trabalho e critérios de aceite](docs/PLANO_DE_TRABALHO.md)
- [Decisões pendentes](docs/DECISOES_PENDENTES.md)
- [Estudo inicial de IA local no Android](docs/ESTUDO_VIABILIDADE_MODELO_LOCAL.md)

## Primeira meta

Consolidar requisitos e arquitetura; depois, validar a execução de um modelo local no Moto G52 e iniciar um protótipo Android pequeno, sem implementar prematuramente funções ainda não testadas.

## Licença

A licença do projeto ainda não foi definida. Não presumir que o código seja de código aberto enquanto essa decisão estiver pendente.
