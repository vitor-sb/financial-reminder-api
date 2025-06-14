# API de Gestão Financeira

API RESTful para gestão de finanças pessoais, construída com Spring Boot e seguindo os princípios da Arquitetura Hexagonal.

* **Tecnologias**: Java 17, Spring Boot, Spring Security, JWT, JPA/Hibernate, PostgreSQL, Redis, Docker.
* **Pré-requisitos**: JDK 17, Maven 3.x e Docker.

## Como Executar

1.  Clone o repositório.
2.  Crie e configure o arquivo `.env` a partir do `.env.example` na raiz do projeto.
3.  Inicie a infraestrutura com Docker:
    ```bash
    docker-compose up -d
    ```
4. Execute a aplicação:
   1. **Opção 1: Via Terminal (Linux & macOS)**
      1. No terminal, navegue até a raiz do projeto. 
      2. Carregue as variáveis de ambiente do arquivo `.env`: 
      ```bash
      export $(grep -v '^#' .env | xargs)
      ```
      3. Execute a aplicação:
        ```bash
        ./mvnw spring-boot:run
        ```

   2. **Opção 2: Via IntelliJ IDEA (Qualquer SO)**
      1.  Abra o projeto na IDE IntelliJ.
      2.  No canto superior direito, clique em **"Edit Configurations..."**. 
      3. Encontre o campo **"Environment variables"** e clique no primeiro ícone ao final da linha para abrir a janela de seleção de arquivo. 
      4. Selecione o arquivo `.env` configurado anteriormente. 
      5. Clique em **OK** e depois em **Apply** e **OK** para salvar a configuração. 
      6. Agora você pode executar ou depurar a aplicação usando os botões de Play/Debug no topo da IDE.
