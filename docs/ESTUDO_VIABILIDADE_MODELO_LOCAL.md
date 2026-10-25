# Estudo inicial — IA local no Android

**Data da pesquisa:** 2026-10-25  
**Estado:** pesquisa documental inicial; nenhum modelo foi executado no Moto G52. Portanto, ainda não há confirmação de desempenho ou compatibilidade prática.

## Resumo executivo

Há pelo menos duas rotas técnicas plausíveis para prototipar inferência local no Android:

1. **llama.cpp**: oferece documentação oficial para Android e ABI `arm64-v8a`, além de um exemplo de aplicativo Android. É uma opção forte para testar modelos em formato GGUF e manter controle sobre quantização e parâmetros.
2. **LiteRT-LM (Google AI Edge)**: é a direção recomendada pela documentação atual do Google para novos projetos Android com LLMs no dispositivo. A API antiga MediaPipe LLM Inference está em modo de manutenção; a documentação recomenda migrar para LiteRT-LM.

A escolha não deve ser feita só com base na documentação. Precisamos testar pelo menos uma opção no aparelho real, medir uso de memória e desempenho e confirmar operação offline.

## Evidências documentais

### llama.cpp
- A documentação oficial descreve suporte a Android `arm64-v8a` e fornece um exemplo de interface Android: https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
- As instruções oficiais de build explicam a compilação Android NDK e opções para CPU ARM64: https://github.com/ggml-org/llama.cpp/blob/master/docs/build.md
- A existência de suporte Android não garante que qualquer modelo ou configuração caiba na RAM do Moto G52. Contexto, quantização, modelo e memória de execução afetam o resultado.

### LiteRT-LM / Google AI Edge
- A documentação de LiteRT descreve suporte a Android e exemplos de aplicativos locais: https://developers.google.com/edge/litert
- A documentação de inferência Android informa que MediaPipe LLM Inference está em modo de manutenção e recomenda LiteRT-LM para novos desenvolvimentos: https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference/android
- O aplicativo AI Edge Gallery pode ser usado como uma etapa exploratória para experimentar modelos locais no dispositivo, mas a execução bem-sucedida nele não substitui a integração e os testes do nosso próprio aplicativo.

## Recomendação provisória

1. **Não escolher ainda um modelo definitivo.** Primeiro verificar o aparelho e executar testes exploratórios.
2. Comparar LiteRT-LM e llama.cpp com base em integração Kotlin/Android, formatos de modelo disponíveis, licenças, português, tamanho, memória, velocidade e facilidade de manter offline.
3. Priorizar primeiro a execução em CPU, sem pressupor aceleração de GPU/NPU no Snapdragon 680.
4. Avaliar modelos pequenos e quantizados como candidatos de teste; a faixa de tamanho final deve ser determinada por medições reais.
5. Registrar versão do runtime, modelo exato, quantização, tamanho, parâmetros de contexto e resultados para que o teste seja reproduzível.

## Protocolo mínimo de teste no Moto G52

- Confirmar espaço livre antes de baixar modelos.
- Registrar versão do Android, versão do aplicativo de teste e origem/licença do modelo.
- Executar uma conversa curta em português e uma sequência de perguntas/respostas.
- Medir tempo de carregamento, latência até o primeiro token, velocidade aproximada de geração e memória usada, quando as ferramentas permitirem.
- Observar aquecimento, consumo de bateria, estabilidade e encerramentos pelo Android.
- Ativar modo avião depois da instalação e confirmar que a inferência continua funcionando.
- Repetir com contexto maior, sem presumir que a janela máxima anunciada seja prática no aparelho.
- Documentar falhas e não declarar sucesso se o teste não tiver sido executado.

## Limitações desta pesquisa

- A documentação confirma que as tecnologias oferecem caminhos para Android; não comprova que funcionem com desempenho aceitável neste aparelho específico.
- Não houve instalação, benchmark ou execução de modelo no dispositivo durante esta etapa.
- Nenhuma decisão definitiva de runtime ou modelo foi tomada.
