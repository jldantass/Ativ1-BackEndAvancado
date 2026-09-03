### ALUNOS

`Joao Lucas Dantas` e `Joao Victor Azevedo`.

## Diagnóstico

Olhando o método `cadastrar` original, dá pra ver que ele faz coisa demais. Em umas 15 linhas
ele valida se o nome veio vazio, checa no banco se o e-mail já existe, define os campos
iniciais do cliente, salva e ainda monta a resposta HTTP. São cinco tipos de trabalho bem
diferentes no mesmo lugar.

---

## O que eu mudei e por quê

### 1. Validação do nome → foi pro DTO

O problema é que a checagem de nome vazio era um `if` no meio do fluxo HTTP. Se amanhã eu
precisar de mais campos obrigatórios, o método vai só crescendo, e essa validação não serve
pra mais nada além desse endpoint.

Então criei o `DadosCadastroCliente` com `@NotBlank` e coloquei `@Valid` no controller.

### 2. Regra do e-mail duplicado → foi pro service

Essa regra estava grudada no `ResponseEntity`. O problema disso é que pra testar ela eu
precisaria subir o Spring inteiro.

Movi pro `ClienteService`, mas sem levar o `ResponseEntity` junto, senão eu só teria mudado
o problema de lugar. O service lança `EmailJaCadastradoException` e quem transforma isso num
400 é o `ApiExceptionHandler`. Assim o service não sabe que existe HTTP.

### 3. `ativo` e `dataCadastro` → foram pro construtor da entidade

Do jeito que estava, quem chamasse o método tinha que lembrar de setar esses dois campos. Se
aparecer um segundo lugar que cria cliente e a pessoa esquecer, salva um cliente quebrado no
banco.

Coloquei no construtor de `Cliente` e tirei os set desses campos. Agora não tem como criar
um cliente sem estado inicial.

### 4. Entidade no `@RequestBody` → virou DTO

O problema é que o contrato da API era a própria tabela. Quem chamasse podia mandar
`{"id": 7, "ativo": false}` e mexer em campo interno, e qualquer mudança de coluna quebraria
quem usa a API.

Separei em dois DTOs: `DadosCadastroCliente` pra entrada e `DadosListagemCliente` pra saída.

---

## Requisitos mínimos do enunciado

| Requisito | Como foi atendido |
|---|---|
| O endpoint deve continuar cadastrando clientes | `POST /clientes` mantido, mesma assinatura de rota |
| E-mail duplicado deve continuar impedindo o cadastro | Regra no service, resposta 400 pelo handler |
| O estado inicial do cliente deve continuar sendo definido | Construtor de `Cliente` define `ativo` e `dataCadastro` |
| Evitar transferir todo o código para uma única classe | Os blocos foram distribuídos em cinco destinos conforme a natureza de cada um |
