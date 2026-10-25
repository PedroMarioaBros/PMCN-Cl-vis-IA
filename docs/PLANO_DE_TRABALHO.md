# Plano de trabalho — fases e critérios de aceite

**Regra:** uma fase só pode ser marcada como concluída após executar os testes correspondentes e registrar resultados. Documentação não equivale a aplicativo implementado.

## Fase 0 — Documentação
- [x] Criar README e consolidar requisitos, arquitetura, plano e decisões pendentes.
- [ ] Rever as decisões pendentes.

Aceite: documentação no repositório e pendências explicitadas.

## Fase 1 — Viabilidade de IA local
- [x] Pesquisa documental inicial: llama.cpp para Android ARM64 e LiteRT-LM como rota atual recomendada pelo Google.
- [ ] Testar runtime e modelo no Moto G52; pesquisa documental não comprova desempenho no aparelho.
- Comparar runtimes e modelos compatíveis com Android/ARM64, licença, português, RAM e armazenamento.
- Instalar e executar modelo local no Moto G52.
- Medir inicialização, latência, velocidade, memória, aquecimento, bateria e espaço ocupado.
- Confirmar funcionamento offline após baixar os recursos.

Aceite: teste real reproduzível. Não declarar compatibilidade antes de executar no aparelho.

## Fase 2 — Esqueleto Android
- Definir estrutura e build reproduzível.
- Criar navegação, lista de conversas, tela de chat e configurações.
- Aplicar identidade própria inspirada em fluxos de referência.
- Separar adaptador de inferência da interface.

Aceite: APK instalável e navegação básica testada. Simulações devem ser identificadas como simulações.

## Fase 3 — Persistência
- Criar, renomear, listar e excluir conversas com confirmação.
- Persistir mensagens e manter histórico após reinício.
- Testar encerramento durante gravação e integridade.
- Implementar exportação TXT e JSON/JSONL.

Aceite: testes de persistência e exportação aprovados.

## Fase 4 — IA local integrada
- Integrar runtime escolhido.
- Tratar carregamento, geração, falha e cancelamento quando suportado.
- Testar offline, memória, aquecimento e sessões longas.

Aceite: conversa real com modelo local e limitações documentadas.

## Fase 5 — Pesquisa
- Busca dentro da conversa e global.
- Mostrar trecho, conversa, autor e data/hora; ordenar do mais recente ao mais antigo.
- Abrir a mensagem original e destacar ocorrência.
- Implementar pesquisa por data e avaliar FTS5.

Aceite: testes com mensagens antigas, termos repetidos, acentos e várias conversas.

## Fase 6 — Recuperação de contexto
- Recuperar trechos antigos sem eliminar mensagens.
- Manter resumos como dados derivados.
- Referenciar data/mensagem quando útil.
- Admitir quando a informação não for encontrada.

Aceite: testes verificáveis com fatos antigos e referências às mensagens recuperadas.

## Fase 7 — Memória e chat temporário
- Escolha entre memória exclusiva e compartilhada ao criar conversa normal.
- Implementar isolamento temporário, timeout e confirmação de descarte.
- Permitir revisar, corrigir e excluir memória compartilhada.

Aceite: comprovar isolamento entre chats e ausência de conteúdo temporário nos dados persistentes.

## Fase 8 — Qualidade e distribuição
- Testar acessibilidade, ciclo de vida, falhas e desempenho.
- Validar backup/restauração.
- Definir licença, instruções de instalação e limitações conhecidas.

Aceite: checklist executado e artefato de release realmente produzido e verificado.

## Registro de execução
| Data | Fase | Ação | Resultado verificado | Próximo passo |
|---|---|---|---|---|
| 2026-10-25 | 0 | Criada documentação inicial no GitHub | README e documentos de planejamento gravados; app ainda não implementado | Rever decisões pendentes |
| 2026-10-25 | 1 | Pesquisa documental inicial sobre runtimes locais | Documentação oficial indica caminhos via llama.cpp Android e LiteRT-LM; nenhum teste no Moto G52 foi executado | Escolher teste exploratório e medir desempenho real |
