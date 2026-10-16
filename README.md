# Gestão de Patrimônios de TI

Sistema desktop para organização e controle de patrimônios de Tecnologia da Informação.

Permite cadastrar computadores e equipamentos, organizar patrimônios por blocos e salas, consultar registros, editar informações, excluir itens e agendar manutenções.

## Funcionalidades

- Cadastro de patrimônios de TI.
- Organização por categoria:
  - Computadores
  - Equipamentos
- Cadastro em lote.

  Exemplo: ao informar o código inicial `PC-LEN-001` e quantidade `5`, o sistema gera os códigos de `PC-LEN-001` até `PC-LEN-005`.

- Controle de patrimônio em uso ou disponível.
- Organização de patrimônios por bloco e sala.
- Cadastro, visualização e exclusão de blocos e salas.
- Consulta por código, nome ou local.
- Filtro de patrimônios por bloco.
- Edição de patrimônios cadastrados.
- Exclusão de patrimônios com confirmação.
- Agendamento de manutenções.
- Exclusão de manutenções agendadas.
- Banco de dados local, sem necessidade de login.
- Interface moderna criada com CSS.

## Tecnologias utilizadas

- Java 21
- JavaFX 21.0.5
- Maven
- SQLite
- JDBC SQLite
- CSS
- JPackage
- WiX Toolset

## Banco de dados

O sistema utiliza SQLite, um banco de dados local em arquivo.

Após a instalação, o banco é salvo na pasta de dados do usuário:

```text
%LOCALAPPDATA%\GestaoPatrimonioTI\patrimonio-ti.db
```

Isso permite que o sistema funcione sem servidor, internet ou configuração de usuário e senha.

## Estrutura principal do banco

### Patrimônio

Armazena os computadores e equipamentos cadastrados.

- Código
- Nome
- Categoria
- Status de uso
- Local alocado

### Manutenção

Armazena os agendamentos de manutenção.

- Local
- Tipo de manutenção
- Data agendada
- Observação

### Localização

Armazena os blocos e salas disponíveis no sistema.

- Bloco
- Sala

## Como executar o projeto

### Requisitos

- JDK 21 ou superior
- Maven

### Executar pelo terminal

Na pasta do projeto, execute:

```powershell
mvn javafx:run
```

### Executar pelo NetBeans

1. Abra a pasta do projeto como um projeto Maven.
2. Aguarde o NetBeans carregar as dependências.
3. Clique em **Executar Projeto**.


## Instalação

Baixe o arquivo `.exe` disponível na seção [Releases](../../releases) deste repositório.

Após instalar, o sistema poderá ser aberto pelo atalho da Área de Trabalho ou pelo Menu Iniciar.

## Autor

Miguel Lacerda
