# PMCN Clóvis IA

**PMCN Clóvis IA** é o projeto de uma IA pessoal chamada **Clóvis**, planejada para funcionar prioritariamente no próprio aparelho Android, com foco em privacidade, continuidade de contexto e uso offline.

> **Estado atual (2026-10-25):** o APK debug da branch `feature/app-teste-local-v0.1.0` compilou com sucesso no GitHub Actions. No teste real no Moto G52, o modelo baixou e o app indicou que estava pronto, mas uma mensagem enviada não produziu resposta visível após mais de dois minutos. A geração local ainda não está validada; este é o bloqueio prioritário. A versão diagnóstica mais recente mostra estado de geração e limite de três minutos.

## Para Cláudio — comece por aqui

O repositório contém o código-fonte Android e os documentos de contexto. Faça primeiro a leitura do [handoff técnico completo](docs/HANDOFF_CLOUD_AI.md), que explica o problema de inferência, os arquivos relevantes, as restrições do produto e o que esperamos da revisão.

Arquivos de entrada:
- [Código principal — MainActivity.kt](app/src/main/java/br/com/pmcn/clovis/MainActivity.kt)
- [Dependências e configuração Android](app/build.gradle.kts)
- [Manifest Android](app/src/main/AndroidManifest.xml)
- [Workflow de compilação do APK](.github/workflows/android-apk.yml)
- [Roteiro de teste no Moto G52](TESTE_NO_MOTO_G52.md)
- [Issue #2 — bloqueio de inferência](https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA/issues/2)

**Importante:** a compilação no GitHub Actions foi concluída, mas a geração de respostas ainda não foi confirmada no aparelho real. Não declarar o bloqueio resolvido sem evidência. Cláudio deve propor correções em uma branch própria ou branch de correção, sem sobrescrever o código de referência diretamente.

## Protótipo 0.1.0

- Aplicativo Android próprio, com tela de conversa do Clóvis.
- Download do modelo dentro do aplicativo: Qwen3-0.6B em formato LiteRT-LM.
- Inferência local via LiteRT-LM, começando pelo backend CPU.
- Uma conversa de teste com histórico salvo localmente.
- A conexão é usada para baixar o modelo. O texto da conversa não é enviado para uma API remota.
- Workflow do GitHub Actions para gerar APK debug e publicá-lo como artefato de teste.

Consulte o [roteiro de teste no Moto G52](TESTE_NO_MOTO_G52.md). Este protótipo é deliberadamente pequeno; não é a aplicação completa planejada.

## Objetivos principais do projeto

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

## Documentos do projeto

- [Handoff técnico para revisão externa](docs/HANDOFF_CLOUD_AI.md)
- [Requisitos funcionais](docs/REQUISITOS_FUNCIONAIS.md)
- [Arquitetura técnica proposta](docs/ARQUITETURA_TECNICA.md)
- [Plano de trabalho e critérios de aceite](docs/PLANO_DE_TRABALHO.md)
- [Decisões pendentes](docs/DECISOES_PENDENTES.md)
- [Estudo inicial de IA local no Android](docs/ESTUDO_VIABILIDADE_MODELO_LOCAL.md)

## Próximos passos

1. **Prioridade imediata:** investigar e corrigir a geração local que não devolveu resposta no Moto G52.
2. Gerar um APK atualizado por GitHub Actions e verificar o artefato antes de distribuir.
3. Repetir o teste no Moto G52 e registrar resposta, latência, erros, aquecimento, estabilidade e teste em modo avião.
4. Só depois ampliar o aplicativo.

## Limitações da versão 0.1.0

Ainda não há múltiplas conversas, busca, exportação, memória compartilhada, chat temporário, recuperação semântica ou integração opcional com Gemini. O histórico/contexto atual é simples e não deve ser considerado solução para conversas longas. O teste real no telefone é obrigatório. O modelo ter sido baixado e o runtime indicar inicialização concluída não comprova que a geração de texto esteja funcionando.

## Licença

A licença do projeto ainda não foi definida. Não presumir que o código seja de código aberto enquanto essa decisão estiver pendente.
