# Handoff técnico — PMCN Clóvis IA

**Data:** 2026-10-25  
**Responsável pelo projeto:** Pedro (PMCN)  
**Objetivo deste documento:** permitir que outro agente de IA revise tecnicamente o projeto e proponha uma correção verificável para o bloqueio atual.

## Repositório e ponto de partida

- Repositório público (visibilidade verificada em 2026-10-25): https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA
- Branch de desenvolvimento original: `feature/app-teste-local-v0.1.0`
- Commit da versão diagnóstica: `a085f1d03b5dea74f9b9716bd6bd5e7ad5e694a8`
- Build diagnóstico inicial: https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA/actions/runs/40186710854
- Build mais recente de verificação da organização do repositório: https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA/actions/runs/40188568771 — **sucesso**, artefato gerado. Isso confirma compilação, não funcionamento da inferência no telefone.
- APK anterior (antes do diagnóstico): o usuário instalou no Moto G52 e testou. Não é a versão diagnóstica atual.

**A branch padrão `main` já contém o código-fonte Android, o workflow de build, o roteiro de teste e toda a documentação de contexto.** O revisor pode abrir a página inicial do repositório e navegar pelos arquivos sem trocar de branch. A branch `feature/app-teste-local-v0.1.0` permanece como histórico da implementação original. Links diretos: [MainActivity.kt](https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA/blob/main/app/src/main/java/br/com/pmcn/clovis/MainActivity.kt), [Gradle do app](https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA/blob/main/app/build.gradle.kts), [workflow](https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA/blob/main/.github/workflows/android-apk.yml) e [roteiro de teste](https://github.com/PedroMarioaBros/PMCN-Cl-vis-IA/blob/main/TESTE_NO_MOTO_G52.md). O repositório é público; não compartilhar senha, token pessoal nem segredo de autenticação no prompt.

## Estado observado no aparelho real

Aparelho-alvo: Motorola Moto G52, Android 13, Snapdragon 680, 4 GB de RAM e 128 GB de armazenamento.

1. O usuário instalou o APK inicial.
2. O aplicativo baixou o modelo local e mostrou estado de pronto.
3. O usuário enviou uma mensagem de teste (por exemplo, “tudo bem?”).
4. Não apareceu resposta após mais de dois minutos.
5. Portanto, a geração de texto local **não está validada**. Não concluir que é apenas lentidão do aparelho sem investigar.

A branch `fix/diagnostico-geracao` contém uma tentativa de correção/diagnóstico ainda aguardando compilação: usa a API de callback `MessageCallback` em vez do adaptador Flow interno, fixa LiteRT-LM em `0.18.1`, desativa o thinking na chamada e limita a saída a 512 tokens. Registra tempos/blocos no Logcat com tag `Clovis`, mostra avisos separados das respostas e chama `Conversation.cancelProcess()` quando o timeout cancela a coroutine. Nada disso está validado até o Actions compilar e o APK ser testado no Moto G52.

## Arquivos importantes

- `app/src/main/java/br/com/pmcn/clovis/MainActivity.kt` — interface, download, inicialização do runtime, envio e fluxo de resposta.
- `app/build.gradle.kts` — dependências Android/Kotlin/LiteRT-LM.
- `app/src/main/AndroidManifest.xml` — configuração do app.
- `.github/workflows/android-apk.yml` — compilação e publicação do APK de teste.
- `TESTE_NO_MOTO_G52.md` — roteiro de teste.
- `docs/REQUISITOS_FUNCIONAIS.md` — requisitos do produto.
- `docs/ARQUITETURA_TECNICA.md` — arquitetura planejada.
- `docs/PLANO_DE_TRABALHO.md` — fases e critérios de aceite.
- `docs/DECISOES_PENDENTES.md` — decisões ainda em aberto.
- `docs/ESTUDO_VIABILIDADE_MODELO_LOCAL.md` — pesquisa inicial sobre modelos e runtime.

## Configuração técnica conhecida

- App Android nativo em Kotlin.
- Java 17, Gradle 8.13, Android SDK platform 36/build-tools 36.0.0.
- Kotlin plugin 2.4.0, escolhido para compatibilidade de metadados com LiteRT-LM 0.18.1.
- Modelo candidato: `Qwen3-0.6B.litertlm`, baixado de Hugging Face para armazenamento interno do app.
- Runtime: LiteRT-LM, backend CPU.
- Na branch de diagnóstico, a geração usa `chat.sendMessageAsync(text, MessageCallback, maxOutputToken = 512, thinkingConfig = ThinkingConfig(enableThinking = false))`. Essa troca evita o adaptador Flow pré-compilado e permite registrar explicitamente `onMessage`, `onDone` e `onError`. A dependência LiteRT-LM está fixada em `0.18.1` para builds reproduzíveis.
- Histórico da única conversa de protótipo fica em SharedPreferences como JSON; não é a arquitetura final de persistência.
- O fluxo de conversa pretendido é local. O download do modelo exige internet; o texto das mensagens não deve ser enviado para serviço remoto nesta versão.

## Pedido técnico prioritário

Faça uma revisão de código de verdade, rastreando a API e o comportamento do LiteRT-LM 0.18.1. Determine por que a geração não entrega texto no aparelho mesmo após o modelo carregar. Verifique, entre outros pontos:

1. Confirmar a compilação da sobrecarga de callback `sendMessageAsync` no LiteRT-LM `0.18.1`.
2. Confirmar se os callbacks `onMessage`, `onDone` e `onError` chegam no aparelho e se `Message.toString()` contém os chunks de texto visíveis.
3. Usar a tag Logcat `Clovis` para distinguir ausência de callbacks, blocos somente com canal `thought`, erros nativos e término sem texto.
4. Logs e exceções do runtime nativo, compatibilidade real do arquivo do modelo com a versão da biblioteca, ABI e requisitos de Android.
5. Se o bloqueio está em inicialização, primeira inferência, stream ou atualização da UI.
6. Segurança do ciclo de vida da Activity, cancelamento, fechamento do Engine/Conversation e persistência.
7. Se o timeout atual é realmente eficaz quando o runtime entra em código nativo.

Use documentação oficial e código de exemplo compatível com a versão instalada; não presuma APIs. Não alegue ter executado testes no Moto G52 sem ter acesso físico ao aparelho. Diferencie fatos confirmados, hipóteses e testes ainda necessários.

## Restrições de produto

- Nome/persona: **Clóvis**, assistente pessoal de Pedro; idioma padrão português brasileiro.
- Prioridade: Android local/offline-first; não migrar a conversa básica para API remota.
- Aparelho inicial: Moto G52, Android 13, Snapdragon 680, 4 GB RAM.
- O usuário não quer testar modelos em aplicativos de terceiros. Os testes devem ocorrer no app próprio.
- Não declarar compatibilidade ou desempenho confirmado sem teste real.
- Preservar histórico original integral; resumos não substituem mensagens.
- Objetivos futuros: interface inspirada nos fluxos do WhatsApp com identidade própria, múltiplas conversas nomeadas, pesquisa por conversa e global, memória por conversa ou compartilhada, chat temporário e integração opcional com Gemini API somente em fase posterior, com decisão explícita do usuário.
- Esses recursos futuros **não estão implementados** no protótipo 0.1.0.
- Não sobrescrever o código de referência diretamente. Trabalhar em uma branch de correção e propor alterações revisáveis por pull request.
- Não adicionar chaves API, tokens, senhas ou segredos ao código, prompt, commits ou logs.

## Entrega esperada do agente

1. Diagnóstico com causa provável, evidências e grau de certeza.
2. Lista dos arquivos e alterações propostas.
3. Código completo ou patch/diff aplicável ao repositório, não apenas sugestões genéricas.
4. Instruções para compilar com GitHub Actions e validar o artefato.
5. Casos de teste objetivos, incluindo mensagens curtas em português, resposta vazia, erro de inicialização, timeout e reabertura do app.
6. Declaração explícita do que foi e não foi testado.
7. Não ampliar escopo antes de a geração local funcionar.

## Critério de aceite para encerrar o bloqueio

O GitHub Actions compila a alteração e gera APK; o APK atualizado é instalado no Moto G52; o usuário envia pelo menos três mensagens diferentes e recebe respostas não vazias do modelo local, sem rede remota de inferência; o app continua utilizável depois de fechar e reabrir. O teste de modo avião deve ser feito após o download do modelo. Até isso ocorrer, o bloqueio permanece aberto.
