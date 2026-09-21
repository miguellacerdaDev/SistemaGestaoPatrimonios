# Gestão de Patrimônios de TI

Aplicação local para uma única pessoa, sem tela de login. Os dados ficam no arquivo `patrimonio-ti.db`, criado automaticamente ao abrir o programa.

## O que já está incluído

- Cadastro e edição de patrimônios.
- Organização em **Computadores** e **Equipamentos**.
- Código único, nome, status de uso e local de alocação.
- Consulta por código, nome ou local, dentro de cada categoria.
- Agendamento de manutenção com local, tipo, data e observação.

## Como executar

1. Instale o JDK 21 ou superior e o Maven.
2. Abra um terminal nesta pasta.
3. Execute ao clicar em cima do arquivo com botao direito, run maven, e escreva "javafx:run".



Para este projeto, a melhor opção é **SQLite**: é um banco em arquivo, não exige servidor, usuário ou senha. Você pode pedir assim:

> “Quero usar SQLite no meu sistema JavaFX de gestão de patrimônios. Crie um banco local com uma tabela `patrimonio` contendo código único, nome, categoria (Computador ou Equipamento), status de uso e local de alocação; e uma tabela `manutencao` com local, tipo, data agendada e observação. Relacione as manutenções aos patrimônios quando necessário.”

Se o sistema futuramente for usado por várias pessoas ao mesmo tempo, peça PostgreSQL em vez de SQLite. Nesse caso você precisará informar onde o servidor ficará hospedado e criar uma conta de acesso para a aplicação.
