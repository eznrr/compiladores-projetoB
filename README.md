# Projeto B - Compiladores LPS1

Este projeto implementa um pequeno compilador para a linguagem LPS1, conforme o enunciado do TCD da disciplina de Compiladores.

A linguagem LPS1 possui comandos simples para leitura, impressao, atribuicao, operacoes aritmeticas, decisao, repeticao e bloco composto. O compilador le um programa em LPS1 e gera codigo equivalente em C na saida padrao.

## Arquivos do projeto

- `ProjetoA.java`: implementacao do item (a), com analise lexica, analise sintatica e geracao direta de codigo C.
- `ProjetoB.java`: implementacao do item (b), com analise lexica, analise sintatica, construcao da ASA e geracao de codigo C pelos metodos dos nos da ASA.
- `exemplo1.lps1`: primeiro exemplo do enunciado.
- `exemplo2.lps1`: segundo exemplo do enunciado.
- `saida-exemplo1.c`: codigo C gerado para o primeiro exemplo.
- `saida-exemplo2.c`: codigo C gerado para o segundo exemplo.

## Como compilar e executar

Para compilar os dois programas Java:

```bash
javac ProjetoA.java ProjetoB.java
```

Para executar o Projeto A:

```bash
java ProjetoA exemplo1.lps1
java ProjetoA exemplo2.lps1
```

Para executar o Projeto B:

```bash
java ProjetoB exemplo1.lps1
java ProjetoB exemplo2.lps1
```

Os programas imprimem o codigo C gerado diretamente na saida padrao.

## Ideia geral do compilador

O compilador recebe um codigo escrito em LPS1 e traduz esse codigo para C.

Exemplo em LPS1:

```text
+ i i 1
```

Codigo C gerado:

```c
i = i + 1;
```

Outro exemplo em LPS1:

```text
P a
```

Codigo C gerado:

```c
printf("%d\n", a);
```

A linguagem LPS1 usa comandos prefixados. Isso significa que o operador ou comando aparece antes dos operandos. Por exemplo:

```text
* a p i
```

Esse comando significa:

```c
a = p * i;
```

## Analisador lexico

Nos dois programas existe uma classe chamada `Lexer`.

O analisador lexico e responsavel por quebrar o texto de entrada em tokens. Token e uma unidade da linguagem, como uma palavra-chave, operador, variavel, numero ou simbolo.

Por exemplo, no trecho:

```text
W i # n
```

o lexico reconhece:

```text
W     token WHILE
i     token VARIABLE
#     token NEQ
n     token VARIABLE
```

Os tipos de token aparecem no `enum TokenType`:

```java
ASSIGN, GET, ADD, SUB, MULT, DIV, MOD, PRINT, IF, WHILE,
LBRACE, RBRACE, LT, NEQ, VARIABLE, NUMBER, EOF
```

O analisador lexico reconhece:

- `=` como atribuicao ou igualdade em comparacao.
- `G` como comando de leitura.
- `P` como comando de impressao.
- `I` como comando condicional.
- `W` como comando de repeticao.
- `{` e `}` como inicio e fim de bloco.
- letras minusculas como variaveis.
- numeros de um digito como valores numericos.

Se aparecer um simbolo invalido, o programa gera uma mensagem de erro indicando a linha e a coluna.

## Analisador sintatico

Nos dois programas existe uma classe chamada `Parser`.

O analisador sintatico verifica se os tokens aparecem na ordem correta, de acordo com a gramatica da linguagem LPS1.

Por exemplo, um comando de atribuicao deve ter o formato:

```text
= variavel valor
```

Entao este comando e valido:

```text
= i 0
```

Mas este comando e invalido:

```text
= 0 i
```

Isso acontece porque, depois do simbolo `=`, o parser espera encontrar uma variavel.

Cada tipo de comando possui um metodo proprio no parser:

```java
parseAssign()
parseGet()
parseBinary()
parsePrint()
parseIf()
parseWhile()
parseComposite()
```

O metodo `parseCommand()` olha o token atual e decide qual metodo chamar.

Por exemplo:

```java
case GET: parseGet(); break;
case PRINT: parsePrint(); break;
case WHILE: parseWhile(); break;
```

Assim, se o token atual for `G`, o parser chama o metodo de leitura. Se for `P`, chama o metodo de impressao. Se for `W`, chama o metodo do comando `while`.

## Funcionamento do Projeto A

O arquivo `ProjetoA.java` corresponde ao item (a) do trabalho.

Nessa versao, o analisador sintatico gera o codigo C diretamente enquanto reconhece os comandos da linguagem.

Por exemplo, quando o parser encontra:

```text
G n
```

o metodo `parseGet()` gera:

```c
{
    gets(str);
    sscanf(str, "%d", &n);
}
```

Quando o parser encontra:

```text
* a p i
```

o metodo `parseBinary("*")` gera:

```c
a = p * i;
```

A geracao de codigo e feita pelo metodo `emit()`, que adiciona uma linha ao codigo C e controla a indentacao.

Portanto, o fluxo do Projeto A e:

```text
ler token -> validar sintaxe -> gerar C diretamente
```

Essa abordagem atende ao item (a), pois a geracao de codigo esta misturada com as instrucoes do analisador sintatico.

## Funcionamento do Projeto B

O arquivo `ProjetoB.java` corresponde ao item (b) do trabalho.

Nessa versao, o parser nao gera codigo C diretamente. Primeiro, ele constroi uma ASA, ou seja, uma Arvore Sintatica Abstrata.

Cada comando da linguagem vira um objeto que representa um no da arvore.

Por exemplo:

```text
P a
```

vira um objeto do tipo:

```java
PrintNode
```

E:

```text
W i < n { ... }
```

vira um objeto do tipo:

```java
WhileNode
```

As principais classes de nos da ASA sao:

```java
ProgramNode
AssignNode
GetNode
BinaryNode
PrintNode
IfNode
WhileNode
CompositeNode
ComparisonNode
ValueNode
```

Depois que a ASA e montada, cada no sabe gerar seu proprio codigo C por meio do metodo `generate()`.

Por exemplo, o `PrintNode` gera:

```java
out.line("printf(\"%d\\n\", " + value.toC() + ");");
```

O `WhileNode` gera a estrutura:

```c
while ( condicao ) {
    comandos
}
```

Portanto, o fluxo do Projeto B e:

```text
ler token -> validar sintaxe -> montar ASA -> percorrer ASA -> gerar C
```

Essa abordagem atende ao item (b), pois a geracao de codigo C fica nos metodos dos nos da ASA.

## Diferenca entre Projeto A e Projeto B

A principal diferenca esta no momento em que o codigo C e gerado.

No `ProjetoA.java`, o codigo C e gerado durante a analise sintatica. Cada metodo do parser reconhece um comando da linguagem e ja imprime o trecho correspondente em C.

No `ProjetoB.java`, o parser primeiro monta uma arvore sintatica abstrata. Depois, essa arvore e percorrida, e cada no gera sua parte do codigo C.

Uma forma simples de explicar ao professor:

```text
No Projeto A, eu faco a geracao de codigo durante a analise sintatica.
Cada metodo do parser reconhece um comando da linguagem e ja gera o trecho C correspondente.

No Projeto B, eu primeiro construo uma arvore sintatica abstrata com nos para
atribuicao, leitura, impressao, if, while e bloco composto. Depois, cada no da
arvore possui um metodo de geracao de codigo C.
```

## Exemplos de traducao

Comando de leitura:

```text
G n
```

Codigo C:

```c
{
    gets(str);
    sscanf(str, "%d", &n);
}
```

Comando de atribuicao:

```text
= i 0
```

Codigo C:

```c
i = 0;
```

Comando de soma:

```text
+ i i 1
```

Codigo C:

```c
i = i + 1;
```

Comando de impressao:

```text
P a
```

Codigo C:

```c
printf("%d\n", a);
```

Comando `while`:

```text
W i # n {
* a p i
P a
+ i i 1
}
```

Codigo C:

```c
while ( i != n ) {
    a = p * i;
    printf("%d\n", a);
    i = i + 1;
}
```

## Sobre o segundo exemplo

O segundo exemplo do enunciado e:

```text
G n
= i 2
% a n i
W i < n {
I a = 0 = i n
+ i i 1
% a n i
}
I a = 0 P 0
I a # 0 P 1
```

Esse programa testa se o numero lido em `n` e primo.

Ele inicia `i` com 2:

```text
= i 2
```

Depois calcula o resto da divisao de `n` por `i`:

```text
% a n i
```

Isso gera:

```c
a = n % i;
```

Enquanto `i < n`, ele verifica se encontrou algum divisor.

Este comando:

```text
I a = 0 = i n
```

significa que, se o resto for zero, entao `n` e divisivel por `i`. Nesse caso, o programa coloca `i = n` para encerrar o laco.

No final:

```text
I a = 0 P 0
I a # 0 P 1
```

Ou seja:

- imprime `0` quando o numero e primo.
- imprime `1` quando o numero e composto.

Uma observacao importante: o compilador nao faz conferencia semantica. Ele apenas traduz o programa LPS1 para C, conforme solicitado no enunciado.

## Mensagens de erro

Quando ha erro lexico ou sintatico, o compilador mostra uma mensagem indicando a linha e a coluna do problema.

Exemplo de mensagem:

```text
Erro na linha 3, coluna 5: valor esperado perto de '}'
```

Isso ajuda a localizar exatamente onde o codigo LPS1 esta incorreto.
