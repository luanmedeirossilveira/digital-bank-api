# Digital Bank API

API REST desenvolvida para simular operações básicas de um banco digital.

Funcionalidades:

- cadastro e consulta de contas
- transferência entre contas
- consulta de movimentações
- notificação após transferência concluída com sucesso

## Stack

- Java 17
- Spring Boot
- Maven
- PostgreSQL
- Swagger / OpenAPI
- JUnit 5 e Mockito

## Como rodar

Pré-requisitos:

- Java 17+
- Maven
- Docker e Docker Compose (ou Docker Desktop)

Subindo o banco:

```bash
docker compose up -d postgres
```

Rodando a aplicação:

```bash
mvn spring-boot:run
```

Swagger UI:

- <http://localhost:8080/swagger-ui.html>

## Testes

```bash
mvn test
```

## Endpoints principais

- `POST /api/v1/accounts`
- `GET /api/v1/accounts`
- `GET /api/v1/accounts/{id}`
- `POST /api/v1/transfers`
- `GET /api/v1/accounts/{id}/movements`

## Decisões de implementação

1. Usei PostgreSQL para ficar mais próximo de um cenário real de transações concorrentes. O banco sobe com Docker Compose para facilitar a execução local.

2. A lógica de negócio ficou concentrada na camada de service. A transferência roda dentro de uma transação e atualiza saldo, transferência e movimentações de forma atômica.

3. Usei DTOs para encapsular os dados de transferência e a resposta da transferência concluída. Dessa forma, o controller fica mais simples e o service fica mais testável.

4. Para evitar inconsistências em transferências concorrentes, as contas envolvidas são carregadas com lock pessimista. A transferência trabalha com três status:

  - `PENDING`: transferência em execução.
  - `COMPLETED`: transferência concluída com sucesso.
  - `FAILED`: transferência concluída com falha.

5. Para consulta das movimentações, o service busca os registros no banco e os ordena por data em ordem crescente. O histórico registra os tipos `CREDIT` e `DEBIT`.

6. A notificação foi mantida simples, por meio de um serviço próprio com implementação stub em log, chamado apenas após a conclusão da transferência. Isso mantém o requisito funcional sem adicionar integração externa fora do escopo do teste.
