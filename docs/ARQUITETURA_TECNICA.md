# Arquitetura técnica proposta

**Estado:** proposta inicial. Nenhuma tecnologia ou funcionalidade descrita aqui está marcada como implementada antes de testes.

## Princípios
1. O histórico persistido é a fonte de verdade.
2. Interface, armazenamento, pesquisa, memória e inferência são módulos separados.
3. Resumos e índices podem ser reconstruídos a partir das mensagens originais.
4. O funcionamento básico deve ser offline depois de instalar o modelo e os recursos necessários.
5. Chat temporário usa caminho de dados separado para evitar persistência acidental.
6. A interface não deve depender de um runtime específico de IA.

## Componentes
- **Interface Android:** lista de conversas, tela de chat, pesquisa, configurações e ciclo de vida.
- **Gerenciador de conversas:** cria, renomeia, lista e exclui conversas; mantém modo de memória e isolamento.
- **Repositório de mensagens:** grava mensagens com IDs, autor, conteúdo, data/hora, estado e anexos. Deve tolerar encerramento inesperado.
- **Pesquisa local:** índice derivado, possivelmente SQLite FTS5; permite pesquisa global e por conversa e navegação à mensagem original.
- **Recuperador de contexto:** busca mensagens relevantes e prepara trechos para o modelo dentro do limite real de contexto.
- **Gerenciador de memória:** separa memória da conversa, memória compartilhada e sessão temporária.
- **Adaptador de inferência:** abstrai o runtime local, carregamento, geração, erros e cancelamento quando suportado.
- **Exportação/backup:** TXT legível e JSON/JSONL estruturado, com validação de integridade.

## Armazenamento a avaliar
Alternativas ainda pendentes de protótipo:
- SQLite transacional como armazenamento principal, com exportação JSON/JSONL.
- JSONL append-only como registro principal, com índice separado.
- Solução híbrida com sincronização e recuperação claramente definidas.

Em qualquer opção, o histórico integral é obrigatório. Não apagar mensagens antigas automaticamente. Testar falhas durante gravação, migrações, exportação e restauração.

## Organização conceitual
- conversas normais e seus dados persistentes;
- índices de pesquisa reconstruíveis;
- memória pessoal compartilhada;
- configurações e versões de esquema;
- backups e exportações iniciados pelo usuário;
- sessão temporária volátil, excluída de backups e índices persistentes.

Preferir armazenamento interno privado do app para dados privados. Este esquema é conceitual; não descreve diretórios já implementados.

## Fluxo de recuperação de contexto
1. Receber pergunta.
2. Determinar o escopo permitido: conversa atual e, se habilitada, memória compartilhada.
3. Pesquisar histórico e fontes locais autorizadas.
4. Selecionar trechos relevantes com data e referência à mensagem.
5. Montar o contexto dentro do limite do modelo.
6. Gerar resposta local.
7. Persistir a resposta em conversa normal.
8. Atualizar índices e memórias derivadas sem alterar o histórico original.

A recuperação não garante interpretação perfeita. Se os resultados forem insuficientes, o assistente deve admitir a incerteza.

## Privacidade temporária
- Não incluir chats temporários em histórico normal, busca global, memória compartilhada ou backups automáticos.
- Limpar dados temporários controlados pelo app ao encerrar.
- Confirmar antes de descartar a sessão.
- Definir e testar timeout e comportamento quando o Android encerra o processo.
- Não prometer apagar cópias externas ao controle do app.

## Validação de modelo
O runtime e o modelo ainda não foram escolhidos. Comparar compatibilidade Android/ARM64, licença, qualidade em português, RAM, tamanho, velocidade, bateria, aquecimento e funcionamento offline. Testar no Moto G52 antes de declarar compatibilidade.