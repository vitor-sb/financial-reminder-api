# API de Gestão Financeira

API RESTful para gestão de finanças pessoais, construída com Spring Boot e seguindo os princípios da Arquitetura Hexagonal.

* **Tecnologias**: Java 17, Spring Boot, Spring Security, JWT, JPA/Hibernate, PostgreSQL, Redis, Docker.
* **Pré-requisitos**: JDK 17, Maven 3.x e Docker.

## Como Executar

1.  Clone o repositório.
2.  Crie e configure o arquivo `.env` a partir do `.env.example` na raiz do projeto.
3.  Inicie a infraestrutura:
    ```bash
    docker-compose up -d
    ```
4.  Execute a aplicação:
    ```bash
    mvn spring-boot:run
    ```