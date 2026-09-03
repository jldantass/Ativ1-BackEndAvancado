# API de Cadastro de Clientes

Exercício 1 — **Refatoração e Separação de Responsabilidades**
Tecnologia para Back-End Avançado · Prof. Jonas Bernardino

API REST em Spring Boot para cadastro de clientes. O objetivo do exercício não era criar
funcionalidade nova, e sim pegar um endpoint que já funcionava e reorganizar as
responsabilidades que estavam todas misturadas dentro do Controller.

---

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA
- Bean Validation
- H2 (banco em memória)
- Maven

---

## Como rodar

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

O H2 roda em memória e o Hibernate cria a tabela sozinho a partir da entidade — não precisa
instalar banco nem rodar script SQL. Os dados somem quando a aplicação para.

Para inspecionar o banco pelo navegador, habilite o console em
`src/main/resources/application.properties`:

```properties
spring.h2.console.enabled=true
```

E acesse `http://localhost:8080/h2-console`.

---

## Endpoint

### `POST /clientes`

**Entrada**

```json
{
  "nome": "Joao Lucas",
  "email": "joao@teste.com"
}
```

**Resposta 200**

```json
{
  "id": 1,
  "nome": "Joao Lucas",
  "email": "joao@teste.com",
  "ativo": true,
  "dataCadastro": "2026-09-03T14:22:10.123"
}
```

**Respostas 400**

| Situação | Onde é barrado |
|---|---|
| `nome` vazio ou ausente | Bean Validation no DTO — não chega no service |
| `email` em formato inválido | Bean Validation no DTO |
| `email` já cadastrado | Regra de negócio no service |

---

## Testando

```bash
# 1) cadastro válido
curl -X POST localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Joao","email":"joao@teste.com"}'

# 2) repetir o comando acima → 400 "Email já cadastrado"

# 3) nome vazio → 400 pela validação
curl -X POST localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"","email":"outro@teste.com"}'
```

---

## Estrutura

```
src/main/java/com/example/api1_backavancado/
├── Api1BackavancadoApplication.java
├── controller/
│   └── ClienteController.java          # fronteira HTTP
├── dto/
│   ├── DadosCadastroCliente.java       # contrato de entrada + validação de formato
│   └── DadosListagemCliente.java       # contrato de saída
├── services/
│   └── ClienteService.java             # caso de uso + regra de negócio
├── entities/
│   └── Cliente.java                    # modelo e estado inicial
├── repository/
│   └── ClienteRepository.java          # acesso a dados
└── exception/
    ├── EmailJaCadastradoException.java # sinalização da violação
    └── ApiExceptionHandler.java        # tradução da exceção para HTTP
```

**Direção das dependências**

```
controller  →  services  →  repository  →  entities
     ↓             ↓                          ↑
    dto  ─────────────────────────────────────┘
```

Ninguém importa "para cima". `entities` não importa nada do projeto e `services` nunca importa
`controller` — se aparecer um import nessa direção, é sinal de que alguma responsabilidade
vazou de camada.

---

## Diagnóstico

Olhando o método `cadastrar` original, dá pra ver que ele faz coisa demais. Em umas 15 linhas
ele valida se o nome veio vazio, checa no banco se o e-mail já existe, define os campos
iniciais do cliente, salva e ainda monta a resposta HTTP. São cinco tipos de trabalho bem
diferentes no mesmo lugar.

Tem outro detalhe que percebi no cabeçalho: o método recebe `@RequestBody Cliente`, ou seja,
a entidade do banco é o que a API aceita como entrada.

---

## O que eu mudei e por quê

### 1. Validação do nome → foi pro DTO

O problema é que a checagem de nome vazio era um `if` no meio do fluxo HTTP. Se amanhã eu
precisar de mais campos obrigatórios, o método vai só crescendo, e essa validação não serve
pra mais nada além desse endpoint.

Então criei o `DadosCadastroCliente` com `@NotBlank` e coloquei `@Valid` no controller. Agora
o Spring barra a requisição errada antes mesmo de chamar o service.

### 2. Regra do e-mail duplicado → foi pro service

Essa regra estava grudada no `ResponseEntity`. O problema disso é que pra testar ela eu
precisaria subir o Spring inteiro, e se um dia o cadastro vier por outro caminho (uma fila,
uma importação de planilha) eu teria que copiar a mesma regra de novo.

Movi pro `ClienteService`, mas sem levar o `ResponseEntity` junto — senão eu só teria mudado
o problema de lugar. O service lança `EmailJaCadastradoException` e quem transforma isso num
400 é o `ApiExceptionHandler`. Assim o service não sabe que existe HTTP.

### 3. `ativo` e `dataCadastro` → foram pro construtor da entidade

Do jeito que estava, quem chamasse o método tinha que lembrar de setar esses dois campos. Se
aparecer um segundo lugar que cria cliente e a pessoa esquecer, salva um cliente quebrado no
banco e o compilador não reclama.

Coloquei no construtor de `Cliente` e tirei os setters desses campos. Agora não tem como criar
um cliente sem estado inicial.

### 4. Entidade no `@RequestBody` → virou DTO

O problema é que o contrato da API era a própria tabela. Quem chamasse podia mandar
`{"id": 7, "ativo": false}` e mexer em campo interno, e qualquer mudança de coluna quebraria
quem usa a API.

Separei em dois DTOs: `DadosCadastroCliente` pra entrada e `DadosListagemCliente` pra saída.

---

## O que eu decidi não fazer

Pensei em criar interface pro service e um mapper separado, mas achei exagero. É um cadastro
com uma regra só — isso ia adicionar mais classes sem resolver problema nenhum. A atividade
pede pra reorganizar de forma proporcional ao problema, então parei aqui.

---

## Requisitos mínimos do enunciado

| Requisito | Como foi atendido |
|---|---|
| O endpoint deve continuar cadastrando clientes | `POST /clientes` mantido, mesma assinatura de rota |
| E-mail duplicado deve continuar impedindo o cadastro | Regra no service, resposta 400 pelo handler |
| O estado inicial do cliente deve continuar sendo definido | Construtor de `Cliente` define `ativo` e `dataCadastro` |
| Evitar transferir todo o código para uma única classe | Os blocos foram distribuídos em cinco destinos conforme a natureza de cada um |
