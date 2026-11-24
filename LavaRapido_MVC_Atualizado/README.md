# Sistema de Controle de Clientes e Agenda de Serviços para Lava-Rápido

Projeto acadêmico desenvolvido em Java Swing com arquitetura MVC.

## Conteúdo do repositório
- `src/`: código-fonte Java (Model, View, Controller e Service).
- `test/`: testes automatizados da camada de negócio.
- `docs/`: documentação Word, diagramas UML e capturas das telas.
- `nbproject/` e `build.xml`: configuração do projeto Apache NetBeans.
- `compilar.sh`, `executar.sh` e `executar-testes.sh`: scripts auxiliares.

## Como abrir no NetBeans
1. Extraia o arquivo ZIP.
2. Abra o Apache NetBeans.
3. Selecione **File > Open Project**.
4. Escolha a pasta `LavaRapido_MVC_Atualizado` (a pasta que contém `build.xml` e `nbproject`).
5. Execute a classe `lavarapido.Main`.

## Como compilar e testar
Com JDK 17 ou superior, na pasta do projeto:

```bash
bash compilar.sh
bash executar-testes.sh
bash executar.sh
```

No Windows, os comandos podem ser executados pelo terminal com Bash instalado. Também é possível abrir e executar o projeto diretamente pelo NetBeans.

## Regras importantes da agenda
O lava-rápido possui um único box. Cada agendamento ocupa o intervalo entre seu início e o término calculado pela duração do serviço. O sistema impede sobreposição entre agendamentos ativos, mas permite que um atendimento comece exatamente quando o anterior termina. O preço e a duração aplicáveis são registrados no agendamento.

## Publicação no GitHub
Extraia o ZIP e envie o conteúdo da pasta `LavaRapido_MVC_Atualizado` para um repositório GitHub. Não é necessário enviar o ZIP como arquivo dentro do repositório: o ideal é publicar as pastas e arquivos do projeto, mantendo esta estrutura.
