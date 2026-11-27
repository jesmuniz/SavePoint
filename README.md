# SavePoint

**Organize sua biblioteca. Acompanhe seu progresso. Descubra seu próximo jogo.**

SavePoint é um aplicativo Android desenvolvido em Kotlin com Jetpack Compose, criado para ajudar jogadores a organizar seus jogos, acompanhar seu progresso e gerenciar sua biblioteca em um só lugar.

## Funcionalidades

- **Explorar:** pesquisar jogos e visualizar informações disponíveis por meio de integração com serviços externos.
- **Biblioteca:** organizar os jogos cadastrados e consultar seus detalhes.
- **Backlog:** acompanhar os jogos que o usuário deseja jogar.
- **Perfil:** gerenciar informações do perfil e recursos relacionados à conta Steam.
- **Integração com Steam:** autenticação e consulta à biblioteca de jogos vinculada à conta.
- **Capas de jogos:** consulta de imagens por meio da SteamGridDB.
- **Persistência local:** armazenamento de dados dos jogos utilizando Room.
- **Preferências do usuário:** armazenamento local de preferências com DataStore.

## Tecnologias utilizadas

- **Kotlin** — linguagem principal.
- **Jetpack Compose** — construção das interfaces.
- **Material Design 3** — componentes e padrões visuais.
- **Room** — persistência local de dados.
- **DataStore** — armazenamento de preferências e dados de sessão.
- **Ktor Client** — comunicação com serviços e APIs.
- **Kotlin Coroutines** — operações assíncronas.
- **Coil** — carregamento de imagens.
- **Gradle** — gerenciamento e configuração do projeto.

## Arquitetura e organização

O projeto organiza suas responsabilidades em camadas, separando interface, gerenciamento de estado e acesso aos dados.

```text
app/
└── src/main/java/br/unasp/savepoint/
    ├── data/
    │   ├── dao/
    │   ├── database/
    │   ├── local/
    │   ├── model/
    │   ├── preferences/
    │   ├── remote/
    │   └── repository/
    ├── ui/
    │   └── theme/
    │       ├── navigation/
    │       └── screens/
    ├── viewmodel/
    ├── MainActivity.kt
    └── SavePointApplication.kt
```

### Responsabilidades

- **Data:** reúne entidades, DAOs, banco de dados, fontes de dados locais e remotas e repositórios.
- **UI:** contém as telas, os componentes visuais, o tema e a navegação.
- **ViewModel:** gerencia os dados apresentados pelas telas e intermedeia as operações da interface com os repositórios.

## Requisitos

- Android Studio compatível com a configuração do projeto.
- JDK 25 para a toolchain Gradle configurada no repositório.
- Android SDK 37.
- Dispositivo ou emulador Android com API 26 ou superior.
- Conexão com a internet para os recursos que utilizam serviços externos.

## Como executar

1. Clone este repositório:

   ```bash
   git clone https://github.com/jesmuniz/SavePoint.git
   ```

2. Abra o projeto no Android Studio.

3. Aguarde a sincronização do Gradle.

4. Configure a chave da SteamGridDB no arquivo `local.properties`, na raiz do projeto:

   ```properties
   STEAMGRIDDB_API_KEY=SUA_CHAVE_AQUI
   ```

5. Execute o aplicativo em um dispositivo ou emulador Android.

> **Importante:** não compartilhe sua chave de API nem envie o arquivo `local.properties` ao GitHub. Alguns recursos também dependem da disponibilidade dos serviços externos utilizados pelo aplicativo.

## Capturas de tela

As capturas de tela das principais funcionalidades serão adicionadas nesta seção.

## Contexto acadêmico

Projeto desenvolvido como parte das atividades acadêmicas do curso de Ciência da Computação do Centro Universitário Adventista de São Paulo (UNASP).

## Autoria

Desenvolvido por **Jhessy Muniz**.

---

**SavePoint — Your games, your journey.**
