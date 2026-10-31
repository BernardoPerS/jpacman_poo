# Mapeamento De Colisão e Gates

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>Segue uma feature que pretendo implementar em um projeto de jogo pacman. Além da árvore de diretórios com os arquivos que contêm a atual implementação. Eu pretendo implementar interruptores e gates, daí estou pensando se os gates são implementados como objetos como as pallets(moedinhas) ou como murinhos, mas ainda não analisei o que faz mais sentido. Eu gostaria que você me ajudasse a mapear quais arquivos parecem ser mais importantes para que procurar os pontos a serem modificados: Arquivos que regem colisão, arquivos que definem os objetos, arquivos que definem a construção do mapa na hora de jogar. (Arquivos que provavelmente terei de mexer para implementar essas modificações)</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/001-Feature1.pdf">Feature1.pdf</a></em></strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/002-Pasted text.txt">Pasted text.txt</a></em></strong></p></td></tr></tbody></table>

Pelo enunciado da atividade e pela árvore de diretórios do JPacman, eu começaria investigando principalmente três áreas: **representação do tabuleiro e das entidades, regras de colisão e carregamento do mapa**. Elas provavelmente concentram a maior parte das modificações necessárias para implementar interruptores e portões.

Há também uma distinção importante na sua dúvida inicial: **um portão pode ser representado visualmente como uma parede quando fechado, mas isso não significa que deva ser implementado como uma parede comum**. O portão tem um comportamento próprio: pode abrir, fechar, aguardar a saída de entidades e bloquear novas entradas enquanto está com fechamento pendente.

Vou mapear os arquivos a partir da estrutura que você forneceu. Como você compartilhou a árvore de diretórios, mas não o conteúdo dos arquivos Java, as responsabilidades abaixo são inferidas pelos nomes, pela organização do projeto e pelas regras da atividade. Para confirmar exatamente onde cada mudança deve ocorrer, seria necessário examinar o código-fonte.

## 1. Os arquivos mais importantes

<icon name="git-branch" size="xl"/>

**`board/Square.java` e `board/Unit.java`**

<badge color="warning">Prioridade alta</badge>

Representação do tabuleiro e ocupação das posições.

São os primeiros arquivos que eu examinaria para entender como uma posição do labirinto comporta o chão, os objetos e os personagens. Isso ajudará a decidir como representar um portão sem confundir sua ocupação com a entidade que está sobre ele.

---

<icon name="shield-alert" size="xl"/>

**`level/CollisionMap.java` e `level/CollisionInteractionMap.java`**

<badge color="warning">Prioridade alta</badge>

Regras de colisão e interação.

São candidatos centrais para descobrir como o jogo decide se um movimento é permitido e o que acontece quando entidades se encontram. É especialmente importante descobrir se a colisão com paredes é tratada como bloqueio de movimento ou como interação entre objetos.

---

<icon name="map" size="xl"/>

**`level/MapParser.java` e `level/LevelFactory.java`**

<badge color="warning">Prioridade alta</badge>

Leitura do mapa e construção da fase.

Provavelmente são os principais pontos para reconhecer os novos símbolos (`1`, `2`, `a` e `b`), criar os objetos correspondentes e validar se os dois circuitos estão completos antes de iniciar a partida.

---

<icon name="gamepad-2" size="xl"/>

**`level/Level.java`, `level/Player.java` e `npc/ghost/Ghost.java`**

Movimentação, estado da partida e comportamento das entidades.

Esses arquivos merecem atenção para entender quando o jogador efetivamente entra em uma posição, como os fantasmas se movimentam e em que momento um portão ocupado pode concluir seu fechamento.

---

<icon name="image" size="xl"/>

**`board/BoardFactory.java`, `level/Pellet.java` e `sprite/PacManSprites.java`**

Criação dos elementos e representação visual.

Ajudam a entender como paredes, chão e pellets são instanciados, como os sprites são associados aos objetos e como introduzir uma aparência para os portões e interruptores.

## 2. Como eu organizaria a investigação por responsabilidade

### A. Colisão e movimentação

| Arquivo | O que procurar |
|---|---|
| `level/CollisionMap.java` | Como o jogo decide quais colisões são permitidas ou bloqueadas. |
| `level/CollisionInteractionMap.java` | Como as interações entre tipos de entidades são registradas e executadas. |
| `level/DefaultPlayerInteractionMap.java` | Como o jogador interage com pellets e outros elementos. |
| `level/PlayerCollisions.java` | Como são tratadas as colisões envolvendo o jogador, inclusive com fantasmas. |
| `level/Level.java` | Em que ponto o movimento é realizado e quando as regras de colisão são consultadas. |
| `npc/ghost/Ghost.java` | Como os fantasmas tentam se mover e reagem a movimentos bloqueados. |
| `npc/ghost/Navigation.java` | Como os fantasmas escolhem suas rotas e evitam caminhos inválidos. |

**Por que isso importa?** Um portão não pode ser tratado apenas como um desenho que desaparece ou aparece. Tanto o jogador quanto os fantasmas precisam respeitar seu estado real.

Além disso, o fechamento pendente exige uma regra mais sutil do que simplesmente bloquear todas as colisões: a entidade que já ocupa o portão pode sair, mas uma nova entidade não pode entrar. Quando o último ocupante sair, o portão deve fechar antes que outra entidade entre.

### B. Objetos do tabuleiro e entidades

Aqui está o ponto mais relevante para sua decisão entre implementar os portões como objetos ou como paredes.

| Arquivo | O que investigar |
|---|---|
| `board/Square.java` | Como uma posição armazena ocupantes e elementos do tabuleiro. |
| `board/Unit.java` | Qual é a classe-base dos objetos que ocupam posições e como eles se movimentam. |
| `board/Board.java` | Como o tabuleiro mantém e consulta as posições. |
| `board/BoardFactory.java` | Como são construídas paredes, chão e posições do mapa. |
| `level/Pellet.java` | Como um objeto coletável é representado e como se comporta. |
| `level/Player.java` | Como o jogador é implementado e como seu movimento é realizado. |
| `npc/ghost/Ghost.java` | Como os fantasmas ocupam posições e se movimentam. |
| `sprite/PacManSprites.java` | Como os sprites existentes são disponibilizados às entidades. |

#### Minha recomendação inicial: um objeto próprio para o portão

Eu evitaria implementar o portão como uma parede comum ou como uma moeda modificada. Criaria uma classe específica, por exemplo, `Gate.java`, com estado próprio.

O motivo é que o portão possui mais estados e regras do que uma parede:

*Illustration unavailable: simple pixel art gray brick wall tile game asset*

**Parede comum**

<badge>Bloqueio permanente</badge>

Não depende de interruptores nem de ocupantes para mudar de estado.

---

*Illustration unavailable: simple pixel art floor tile with open metal gate bars top down 2D game asset*

**Portão aberto**

<badge color="success">Passagem permitida</badge>

Jogador e fantasmas podem entrar e sair.

---

*Illustration unavailable: simple pixel art closed metal gate tile top down 2D maze game asset*

**Portão fechado**

<badge color="danger">Passagem bloqueada</badge>

Nenhuma entidade pode entrar.

---

*Illustration unavailable: simple pixel art metal gate tile with yellow warning stripe top down 2D maze game asset*

**Fechamento pendente**

<badge color="warning">Saída permitida</badge>

Os ocupantes podem sair, mas novas entradas são bloqueadas até a posição ficar vazia.

As imagens são referências conceituais de aparência, não os sprites existentes no seu projeto.

Esses estados são exigidos pelo enunciado, que também especifica que a abertura deve cancelar qualquer fechamento pendente. <FileCite ref_id="turn0file0" line_range_start={27} line_range_end={46}/>

Uma estrutura inicial possível seria:

```java
public class Gate {
    private final int circuitId;
    private boolean open;
    private boolean closingPending;

    // Comportamento de abertura e fechamento
    // Verificação de entrada e saída
}
```

Isso é apenas um esboço conceitual: antes de implementá-lo, precisamos descobrir se `Gate` deve herdar de `Unit`, ser armazenado diretamente em `Square` ou participar de outra abstração já existente.

Eu também consideraria uma classe `Switch` para os interruptores e uma classe `Circuit` para coordenar os portões associados a cada identificador. Dessa forma, o interruptor não precisaria conhecer individualmente todos os portões do mapa.

Um detalhe importante: a classe `Pellet.java` pode ser útil como referência de implementação, mas o interruptor não deve herdar automaticamente o comportamento de uma moeda. Ele permanece no mapa, não concede pontos e só é acionado quando o jogador entra na posição.

## 3. Construção do mapa durante a partida

O caminho de investigação mais provável é este:

Relative throughput: 100%
**`resources/board.txt`**

Arquivo que descreve o labirinto por caracteres.

<icon name="arrow-down" size="lg" color="secondary"/>
Relative throughput: 100%
**`MapParser.java`**

Interpreta os símbolos e identifica os elementos do mapa.

<icon name="arrow-down" size="lg" color="secondary"/>
Relative throughput: 100%
**`LevelFactory.java` e `BoardFactory.java`**

Constroem as posições, entidades e elementos da fase.

<icon name="arrow-down" size="lg" color="secondary"/>
Relative throughput: 100%
**`Level.java` e `Board.java`**

Mantêm o estado do jogo e o tabuleiro em execução.

Essa sequência é uma hipótese de arquitetura baseada nos nomes das classes; a chamada exata entre elas precisa ser confirmada no código.

### O que procurar em cada arquivo

- **`src/main/resources/board.txt`**: o mapa original e os símbolos utilizados atualmente.
- **`MapParser.java`**: o tratamento de `#`, espaço, `.`, `P` e `G`. Aqui devem entrar os novos símbolos `1`, `2`, `a` e `b`, preservando os mapas antigos.
- **`LevelFactory.java`**: como o mapa interpretado se transforma em uma fase jogável, incluindo o posicionamento do jogador e dos fantasmas.
- **`BoardFactory.java`**: como são criadas as posições de chão e parede.
- **`Level.java`**: como o estado da fase é inicializado e reinicializado.

O enunciado também exige que mapas com circuitos incompletos ou símbolos desconhecidos impeçam o início da partida, com uma mensagem identificando o problema. Por isso, eu daria atenção especial à validação após o parsing, quando já for possível verificar se cada circuito tem pelo menos um portão e um interruptor. <FileCite ref_id="turn0file0" line_range_start={64} line_range_end={78}/>

## 4. Interruptores, circuitos e estado dos portões

Além das classes que já existem, provavelmente será necessário introduzir novas responsabilidades.

| Componente proposto | Responsabilidade |
|---|---|
| `Switch.java` | Representar um interruptor fixo e identificar seu circuito. |
| `Gate.java` | Controlar abertura, fechamento e fechamento pendente. |
| `Circuit.java` | Alternar o comando do circuito e propagá-lo aos portões associados. |
| `MapParser.java` | Reconhecer os símbolos dos novos elementos. |
| `LevelFactory.java` | Montar os circuitos e validar sua composição. |

Não é obrigatório criar exatamente essas quatro classes. Por exemplo, o controle dos circuitos poderia ficar em uma classe gerenciadora de elementos do mapa. A vantagem de separar as responsabilidades é evitar que `Level.java` acumule toda a lógica de interruptores, portões e validação.

Também é importante distinguir duas coisas:

- **Comando do circuito:** determina se os portões devem abrir ou fechar.
- **Estado físico de cada portão:** pode ser aberto, fechado ou aguardando a saída dos ocupantes.

Dois portões do mesmo circuito podem receber o mesmo comando de fechamento e terminar em estados físicos diferentes, conforme o enunciado. <FileCite ref_id="turn0file0" line_range_start={12} line_range_end={23}/> <FileCite ref_id="turn0file0" line_range_start={36} line_range_end={46}/>

## 5. Interface gráfica e sprites

Estes arquivos provavelmente serão modificados depois que a lógica estiver funcionando:

| Arquivo | Possível alteração |
|---|---|
| `sprite/PacManSprites.java` | Disponibilizar sprites para interruptores e portões. |
| `sprite/SpriteStore.java` | Verificar como imagens são carregadas. |
| `sprite/ImageSprite.java` | Entender como uma imagem é representada. |
| `ui/BoardPanel.java` | Verificar como o tabuleiro e seus elementos são desenhados. |
| `ui/PacManUI.java` | Investigar se é necessário ajustar a interface geral ou a legenda. |

O enunciado sugere reutilizar `key.png` para o interruptor, `floor.png` como base do portão aberto e `wall.png` para o fechado. Também exige que o circuito e o estado do portão sejam identificáveis, inclusive quando o fechamento estiver pendente. <FileCite ref_id="turn0file0" line_range_start={82} line_range_end={96}/>

## 6. Testes que merecem ser examinados desde o início

Você tem testes em `src/default-test` e `src/test`. Eu começaria por estes:

- `level/LevelTest.java`: comportamento geral da fase.
- `board/SquareTest.java`: ocupação e comportamento das posições.
- `board/BoardTest.java`: funcionamento do tabuleiro.
- `board/BoardFactoryTest.java`: construção de paredes e posições.
- `npc/ghost/NavigationTest.java`: navegação dos fantasmas.

Depois, criaria testes específicos para os portões e interruptores. Os casos mais importantes são:

1. Alternar um circuito sem alterar o outro.
2. Acionar um interruptor somente quando o jogador entra nele.
3. Impedir a entrada de jogadores e fantasmas em portões fechados.
4. Permitir a saída dos ocupantes durante o fechamento pendente.
5. Fechar o portão assim que o último ocupante sair.
6. Cancelar o fechamento pendente quando o circuito voltar a ordenar abertura.
7. Preservar o estado durante a pausa e reinicializar tudo em uma nova partida.
8. Garantir que mapas antigos continuem funcionando.

Esses casos refletem os comportamentos explicitamente previstos na atividade. <FileCite ref_id="turn0file0" line_range_start={100} line_range_end={117}/>

---

## 7. Minha ordem recomendada de leitura

Para evitar começar a modificar código antes de entender a arquitetura, eu seguiria esta ordem:

{@body const tasks = [
{title:"Entender a representação do tabuleiro",files:"Square.java, Unit.java, Board.java"},
{title:"Entender as regras de colisão",files:"CollisionMap.java, CollisionInteractionMap.java, PlayerCollisions.java"},
{title:"Entender a leitura e criação do mapa",files:"MapParser.java, LevelFactory.java, BoardFactory.java, board.txt"},
{title:"Entender a movimentação",files:"Level.java, Player.java, Ghost.java, Navigation.java"},
{title:"Entender a representação visual",files:"Pellet.java, PacManSprites.java, SpriteStore.java, BoardPanel.java"},
{title:"Revisar e ampliar os testes",files:"LevelTest.java, SquareTest.java, NavigationTest.java"}
]}
{@body const [checked,setChecked] = DIL.useState([])}

**Checklist de investigação** |
{checked.length} de {tasks.length}

{#each tasks as task,i}
<checkbox key={task.title} checked={checked.includes(i)} onChange={v=>setChecked(old=>v?[...old,i]:old.filter(n=>n!==i))} lineThrough={false}>

**{i+1}. {task.title}**

{task.files}

</checkbox>
{/each}

**Minha sugestão para o próximo passo:** compartilhe o código de `Square.java`, `Unit.java`, `CollisionMap.java`, `CollisionInteractionMap.java`, `MapParser.java` e `LevelFactory.java`. Com esses seis arquivos, já podemos sair das hipóteses baseadas na árvore e montar um mapa concreto das chamadas, das responsabilidades e dos pontos de extensão da implementação atual. A partir daí, fica muito mais fácil decidir se o portão deve ser um ocupante, uma propriedade da posição ou um elemento de outra camada do tabuleiro, sem precisar refatorar o projeto inteiro antecipadamente.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>O MapParser constroi o nível com base na entrada de texto passada, é preciso modificar as funções de modo a incrementar a criação de interruptores e gates usando &quot;1&quot;, &quot;2&quot;, &quot;a&quot; e &quot;b&quot;.</strong></p>&#10;<p style="margin:0 0 8px"><strong>Eu criei arquivos Gate.java e Switch.java com classes que herdam essa unit semelhante ao arquivo Pellet que passei, por enquanto não vou querer que eles façam algo, quero antes colocar eles visualmente no mapa e depois começarei a modificar o funcionamento deles, como colisões/ativação. </strong></p>&#10;<p style="margin:0 0 8px"><strong>Como você diria que é a melhor abordagem e quais arquivos tenho de mexer para primeiro colocar eles visualmente em &quot;campo&quot;.</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/003-MapParser.java">MapParser.java</a></em></strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/004-Pellet.java">Pellet.java</a></em></strong></p></td></tr></tbody></table>

Acho que a melhor abordagem é exatamente essa: **primeiro fazer os interruptores e portões aparecerem no mapa, sem implementar ainda suas regras de funcionamento**. Assim, você separa a integração dos novos elementos com a arquitetura do JPacman da lógica mais complexa de colisão, acionamento e fechamento pendente.

Examinei o `MapParser.java` e o `Pellet.java` que você enviou. O ponto central está no método `addSquare(...)`, do `MapParser`: ele já decide o que criar para cada caractere do mapa. Atualmente, os caracteres não reconhecidos caem no `default` e provocam uma `PacmanConfigurationException`. <FileCite ref_id="turn1file0" line_range_start={109} line_range_end={136}/>

## 1. O caminho que eu seguiria

<icon name="check-circle" size="xl" color="success"/>

**Etapa 1 — Representar os elementos**

Criar `Gate` e `Switch` como subclasses de `Unit`, com um `Sprite` próprio, seguindo o padrão de `Pellet`.

---

<icon name="circle" size="xl" color="secondary"/>

**Etapa 2 — Criar os elementos no parser**

Fazer `MapParser` reconhecer `a`, `b`, `1` e `2` e posicionar as novas unidades no tabuleiro.

---

<icon name="circle" size="xl" color="secondary"/>

**Etapa 3 — Disponibilizar os sprites**

Ajustar `PacManSprites` e, se necessário, `SpriteStore` para carregar os recursos visuais adequados.

---

<icon name="circle" size="xl" color="secondary"/>

**Etapa 4 — Testar a representação**

Criar um mapa pequeno com os quatro símbolos e verificar se cada elemento aparece na posição correta.

Nesta fase, não é necessário alterar `CollisionMap`, `CollisionInteractionMap`, `PlayerCollisions` ou `Navigation`. O objetivo é apenas que os novos elementos sejam construídos e desenhados.

## 2. Quais arquivos você precisa modificar?

Eu dividiria os arquivos em três grupos, priorizando as mudanças mínimas.

### Grupo A — Necessários para colocar os elementos no mapa

| Arquivo | Modificação |
|---|---|
| `level/Gate.java` | Garantir que a classe tenha um `Sprite` e implemente `getSprite`. |
| `level/Switch.java` | Fazer o mesmo para o interruptor. |
| `level/MapParser.java` | Adicionar os quatro caracteres ao `switch` de `addSquare`. |
| `level/LevelFactory.java` | Adicionar métodos de criação para os novos elementos, caso você queira manter o padrão de fábricas já utilizado no projeto. |
| `sprite/PacManSprites.java` | Expor os sprites necessários para os novos objetos. |

### Grupo B — Talvez necessários

| Arquivo | Quando modificar |
|---|---|
| `sprite/SpriteStore.java` | Se os recursos que você pretende utilizar ainda não estiverem carregados ou disponíveis. |
| `board/BoardFactory.java` | Se você decidir criar um tipo especial de posição para os portões. Para a primeira versão, eu evitaria isso. |
| `ui/BoardPanel.java` | Se os elementos forem criados corretamente, mas não forem desenhados como esperado. |

### Grupo C — Deixe para depois

- `CollisionMap.java`
- `CollisionInteractionMap.java`
- `PlayerCollisions.java`
- `Navigation.java`
- `Player.java`
- `Ghost.java`

Esses arquivos entram na implementação das regras de funcionamento, não na simples representação visual.

## 3. Como modificar o `MapParser.java`

O método atual segue este padrão para uma pellet:

```java
case '.':
    Square pelletSquare = boardCreator.createGround;
    grid[x][y] = pelletSquare;
    levelCreator.createPellet.occupy(pelletSquare);
    break;
```

Observe a sequência:

1. Cria um quadrado de chão.
2. Registra esse quadrado na grade.
3. Cria uma unidade e a coloca no quadrado.

Para os interruptores e portões, eu usaria a mesma abordagem. Por exemplo, **a estrutura conceitual** dos novos casos seria:

```java
case '1':
    // Criar um quadrado de chão.
    // Registrar o quadrado na grade.
    // Criar o interruptor do circuito 1.
    // Colocar o interruptor no quadrado.
    break;

case '2':
    // Criar o interruptor do circuito 2.
    break;

case 'a':
    // Criar o portão do circuito 1.
    break;

case 'b':
    // Criar o portão do circuito 2.
    break;
```

Eu manteria os quatro casos explícitos inicialmente, pois isso deixa clara a correspondência entre os caracteres e os circuitos. Posteriormente, se fizer sentido, você pode eliminar a duplicação.

### Por que usar `createGround`?

Porque, nesta primeira etapa, cada símbolo representa um elemento especial colocado em uma posição transitável do mapa. O elemento visual é a unidade; o chão continua sendo a base da posição.

Isso também mantém uma diferença importante:

- `#`: cria uma parede usando `boardCreator.createWall`.
- `a` ou `b`: cria um chão e posiciona uma unidade `Gate` nele.

**Eu não transformaria os portões em paredes nesta etapa.** Além de serem estados visuais diferentes de uma parede comum, eles precisarão de comportamento próprio quando você começar a implementar as colisões.

Uma ressalva: precisamos confirmar a implementação de `Square` e de seu mecanismo de ocupação para garantir que as novas unidades possam coexistir corretamente com jogador e fantasmas.

## 4. Como organizar `Gate.java` e `Switch.java`

O seu `Pellet.java` é uma boa referência. Ele estende `Unit`, guarda um `Sprite` e retorna esse sprite em `getSprite`. <FileCite ref_id="turn1file1" line_range_start={11} line_range_end={43}/>

Eu seguiria esse mesmo princípio, sem adicionar comportamento ainda.

A diferença é que cada classe precisa identificar seu circuito. Por exemplo, o construtor de `Gate` pode receber um identificador inteiro e um sprite. O mesmo vale para `Switch`.

A estrutura conceitual seria:

```java
public class Gate extends Unit {
    private final int circuitId;
    private final Sprite image;

    public Gate(int circuitId, Sprite image) {
        this.circuitId = circuitId;
        this.image = image;
    }

    @Override
    public Sprite getSprite {
        return image;
    }
}
```

Isso é um exemplo ilustrativo, não uma implementação pronta para substituir seu arquivo: você ainda pode querer expor um getter para `circuitId` ou adotar uma enumeração para representar os circuitos.

O importante, por enquanto, é que o construtor receba os dados necessários para representar o objeto e que `getSprite` devolva a imagem correta.

Para `Switch`, a estrutura seria equivalente.

## 5. Como escolher os sprites

O enunciado da atividade sugere recursos que já existem no projeto:

<grid columns={3} gap={3}>
  <grid-item>

*Illustration unavailable: Pac-Man key sprite pixel art transparent background*

    **Interruptor**

`key.png`

  </grid-item>
  <grid-item>

*Illustration unavailable: Pac-Man floor tile pixel art game sprite*

    **Portão aberto**

`floor.png` como base

  </grid-item>
  <grid-item>

*Illustration unavailable: Pac-Man wall tile pixel art game sprite*

    **Portão fechado**

`wall.png` como base

  </grid-item>
</grid>

Referências ilustrativas. Os recursos efetivamente utilizados são os arquivos existentes em `src/main/resources/sprite`.

Para a primeira versão, eu faria o seguinte:

- `Switch`: utilizar `key.png`.
- `Gate`: escolher uma imagem provisória e fixa para cada portão.
- Diferenciar os circuitos 1 e 2 por um marcador visual, se for fácil adicionar essa identificação.
- Não implementar ainda a troca de sprite ao abrir ou fechar o portão.

Uma decisão que vale tomar desde já: **o sprite de um portão não precisa determinar sua lógica de colisão**. Quando você implementar os estados, poderá trocar sua aparência sem precisar alterar a estrutura do mapa.

## 6. E o `LevelFactory.java`?

Esse é o único ponto em que eu gostaria de verificar o código antes de recomendar uma alteração exata.

O `MapParser` já utiliza `levelCreator.createPellet` e `levelCreator.createGhost`. Isso sugere que o projeto concentra a criação desses objetos no `LevelFactory`.

Se essa fábrica já recebe ou tem acesso a `PacManSprites`, a solução mais consistente seria adicionar métodos como:

```java
createGate(int circuitId)
createSwitch(int circuitId)
```

Os nomes são sugestões, não métodos existentes.

Assim, o parser continua responsável por interpretar o mapa, enquanto a fábrica se responsabiliza por criar as unidades com seus sprites. Isso evita espalhar no parser detalhes de construção de objetos.

Se a implementação atual do `LevelFactory` não seguir esse padrão para sprites, podemos adaptar a solução à estrutura existente, sem introduzir uma abstração desnecessária.

## 7. Como saber se a primeira etapa funcionou?

Monte temporariamente um mapa de teste que contenha os quatro caracteres, por exemplo:

```text
###########
#P..1..a..#
#...2..b.G#
###########
```

Esse mapa é apenas ilustrativo; ele pode precisar ser adaptado às regras de construção e inicialização do seu projeto.

Depois, execute o jogo e verifique:

- [ ] O mapa é carregado sem erro.
- [ ] `1` e `2` produzem interruptores visíveis.
- [ ] `a` e `b` produzem portões visíveis.
- [ ] Cada elemento aparece na posição correspondente ao caractere.
- [ ] Os elementos não são contabilizados como pellets.
- [ ] O mapa original continua funcionando.
- [ ] A posição inicial do jogador e dos fantasmas permanece correta.

Nesta fase, não é necessário que os portões bloqueiem a passagem nem que os interruptores acionem circuitos.

**Próximo passo recomendado:** envie também o conteúdo de `LevelFactory.java` e `PacManSprites.java`. Com esses arquivos, consigo indicar exatamente como conectar os novos construtores ao mecanismo existente de sprites e fábricas e quais linhas do `MapParser.java` devem ser modificadas, sem antecipar a lógica de colisão.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>Certo, consegui colocar visualmente os objetos, agora só falta averiguar como estabelecer a física de colisão do Gate e como implementar a dinâmica do Switch de ativar e desativar o gate seguindo as restrições passadas. entretanto estou cogitando implementar o gate como um tipo wall modificado.</strong></p>&#10;<p style="margin:0 0 8px"><strong>Mas me explique também o funcionamento das colisões, eu achei meio confuso esse arquivo CollisionInteractionMap.java.</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/005-CollisionInteractionMap.java">CollisionInteractionMap.java</a></em></strong></p></td></tr></tbody></table>

Acho que sua ideia de implementar o `Gate` como um tipo de parede modificada merece ser considerada, mas há uma distinção importante: **uma coisa é impedir o movimento; outra é detectar uma colisão e decidir o que acontece nela**. No JPacman, essas responsabilidades estão relacionadas, mas não são necessariamente resolvidas pela mesma classe.

Vou explicar primeiro o `CollisionInteractionMap.java` que você enviou e, depois, relacionar seu funcionamento às regras dos gates e switches.

## 1. O que é o `CollisionInteractionMap`?

Pense nele como uma **tabela de regras de interação entre tipos de objetos**. Ele não é, por si só, o sistema que movimenta Pac-Man ou os fantasmas. Sua responsabilidade é descobrir qual regra corresponde a dois objetos que colidiram e executá-la.

Por exemplo, imagine que o jogo tenha estas regras:

| Objeto que colide | Objeto atingido | Regra possível |
|---|---|---|
| Pac-Man | Fantasma | Pac-Man perde uma vida |
| Pac-Man | Pellet | Pellet é consumido |
| Fantasma | Pac-Man | Aplica-se a regra correspondente à colisão |
| Pac-Man | Gate | Depende da regra definida para o gate |

Esses exemplos são ilustrativos: o comportamento concreto depende dos handlers registrados em outras partes do projeto.

### Como as regras são armazenadas?

No arquivo, o campo central é:

```java
private final Map<Class<? extends Unit>,
    Map<Class<? extends Unit>, CollisionHandler<?, ?>>> handlers;
```

Essa estrutura pode parecer complicada, mas representa um mapa dentro de outro mapa.

- O mapa externo identifica a classe do objeto que colide.
- O mapa interno identifica a classe do objeto atingido.
- O valor é um `CollisionHandler`, isto é, uma função que trata aquela combinação.

Conceitualmente, seria algo assim:

```text
handlers
 ├── Player
 │    ├── Ghost  → regra de colisão
 │    ├── Pellet → regra de consumo
 │    └── Gate   → regra do gate, se registrada
 │
 └── Ghost
      └── Player → regra de colisão
```

O arquivo não define todas essas regras. Ele oferece a estrutura para registrá-las e encontrá-las.

## 2. Como o `onCollision` funciona?

O método `onCollision` registra uma regra. Há duas versões dele no arquivo.

A primeira:

```java
public <C1 extends Unit, C2 extends Unit> void onCollision(
    Class<C1> collider,
    Class<C2> collidee,
    CollisionHandler<C1, C2> handler) {

    onCollision(collider, collidee, true, handler);
}
```

Ela encaminha a chamada para a segunda versão, passando `true` para indicar que a regra deve ser simétrica.

A segunda versão faz o registro:

```java
addHandler(collider, collidee, handler);

if (symetric) {
    addHandler(collidee, collider,
        new InverseCollisionHandler<>(handler));
}
```

Vamos traduzir isso para uma situação concreta.

```java
collisionMap.onCollision(Player.class, Pellet.class, handler);
```

Isso registra a regra `Player → Pellet` e, por padrão, também a regra inversa `Pellet → Player`.

A regra inversa não precisa duplicar o comportamento: a classe `InverseCollisionHandler` simplesmente inverte a ordem dos argumentos ao chamar o handler original.

**Atenção:** simetria significa que a regra é encontrada nos dois sentidos. Não significa que a colisão seja executada duas vezes.

Se você passar `false`, somente a combinação na ordem especificada será registrada. Isso é particularmente relevante para o seu switch: você quer que o jogador o ative, mas que os fantasmas não o ativem.

## 3. Como o `collide` encontra a regra?

Este é o método que faz a busca:

```java
public <C1 extends Unit, C2 extends Unit> void collide(
    C1 collider, C2 collidee) {

    Class<? extends Unit> colliderKey =
        getMostSpecificClass(handlers, collider.getClass);

    if (colliderKey == null) {
        return;
    }

    Map<Class<? extends Unit>, CollisionHandler<?, ?>> map =
        handlers.get(colliderKey);

    Class<? extends Unit> collideeKey =
        getMostSpecificClass(map, collidee.getClass);

    if (collideeKey == null) {
        return;
    }

    CollisionHandler<C1, C2> collisionHandler =
        (CollisionHandler<C1, C2>) map.get(collideeKey);

    if (collisionHandler == null) {
        return;
    }

    collisionHandler.handleCollision(collider, collidee);
}
```

O fluxo é:

**1. Dois objetos colidem**

Por exemplo, um `Player` e um `Switch`.

<icon name="arrow-down" size="lg" />

**2. Procura o tipo do primeiro objeto**

Busca uma regra para a classe concreta ou alguma superclasse aplicável.

<icon name="arrow-down" size="lg" />

**3. Procura o tipo do segundo objeto**

Usa as regras registradas para o primeiro tipo encontrado.

<icon name="arrow-down" size="lg" />

**4. Executa o handler**

Se não houver uma regra compatível, retorna sem fazer nada.

### Por que existe `getMostSpecificClass`?

Imagine que `SpecialGhost` estenda `Ghost`, que por sua vez estenda `Unit`.

Se houver uma regra para `Ghost`, ela também poderá ser aplicada a um objeto `SpecialGhost`, desde que não exista uma regra mais específica aplicável que seja encontrada antes.

O método `getInheritance` constrói a lista de classes ancestrais e interfaces. `getMostSpecificClass` percorre essa lista e devolve a primeira classe que possui uma regra registrada.

Isso permite definir regras gerais sem precisar registrar uma para cada subclasse.

---

## 4. E como isso se relaciona com o movimento e as paredes?

Aqui está a distinção mais importante para sua implementação.

**O `CollisionInteractionMap` trata interações entre objetos `Unit`; ele não define, por si só, se uma casa do tabuleiro é transitável.**

No JPacman, você deve distinguir:

- **`Square` (casa):** representa uma posição do tabuleiro; a estrutura do tabuleiro determina quais casas existem e como elas se conectam.
- **`Unit` (unidade):** representa objetos que ocupam casas, como Pac-Man, fantasmas e pellets.
- **`CollisionInteractionMap`:** associa pares de unidades a handlers de colisão.
- **Lógica de movimento:** determina se uma unidade pode sair da casa atual e entrar na próxima.

O detalhe de como o movimento consulta as casas e dispara as colisões precisa ser confirmado nas classes de movimento e tabuleiro do seu projeto. O arquivo enviado não mostra essa parte.

### Gate como parede ou como unidade?

*Illustration unavailable: pixel art retro game stone wall tile 16 bit*

**Opção A — Gate como uma casa especial, semelhante a uma parede**

A passagem pode ser bloqueada diretamente pela lógica do tabuleiro. Isso combina com a ideia de que um gate fechado é fisicamente intransitável.

A dificuldade é que você precisa implementar a mudança entre aberto e fechado na própria estrutura que controla a passagem, além de tratar o caso de um gate ocupado que não pode fechar imediatamente.

---

*Illustration unavailable: retro pixel art glowing sci fi energy gate floor tile*

**Opção B — Gate como `Unit` sobre uma casa de chão**

O gate fica representado como um objeto com estado próprio — circuito, aberto/fechado e fechamento pendente. Isso facilita controlar seu estado e sua aparência.

Porém, ser uma `Unit` não impede automaticamente a entrada de Pac-Man ou dos fantasmas. Você precisa garantir que a lógica de movimento consulte o estado do gate antes de permitir a entrada.

Minha recomendação inicial seria **manter o `Gate` como uma `Unit` com estado próprio**, pelo menos enquanto você está construindo a dinâmica dos circuitos. Assim, não precisa transformar toda a estrutura do tabuleiro em algo dinâmico logo de início.

A alternativa de uma parede modificada também pode funcionar, mas depende de como as casas e o movimento foram implementados. Não recomendo simplesmente herdar de uma classe de parede sem verificar essa arquitetura.

## 5. Como implementar o `Switch` e o `Gate`?

Pelas regras que você descreveu da atividade, eu separaria o trabalho em três responsabilidades.

### A. O estado do circuito

Você precisa representar, para cada circuito (`1` e `2`), se o comando está ligado ou desligado.

Por exemplo, conceitualmente:

```text
Circuito 1
 ├── Switch 1
 ├── Gate a
 └── estado: ligado/desligado

Circuito 2
 ├── Switch 2
 ├── Gate b
 └── estado: ligado/desligado
```

O switch altera o estado do circuito correspondente; os gates desse circuito reagem a essa alteração.

Não é necessário que cada gate implemente sua própria lógica de alternância. Uma opção mais organizada é ter um objeto que represente o circuito e mantenha referências aos seus gates.

### B. A colisão com o switch

O handler deve verificar se o objeto que chegou ao switch é o jogador. A regra pode ser registrada de forma assimétrica, caso a API usada no restante do projeto permita isso.

Por exemplo, como esboço da intenção:

```java
collisionMap.onCollision(
    Player.class,
    Switch.class,
    false,
    (player, circuitSwitch) -> circuitSwitch.activate
);
```

Esse código é ilustrativo: a assinatura e o acesso ao `collisionMap` devem seguir as classes reais do seu projeto.

O ponto essencial é que **a colisão só deve ativar o switch quando o jogador o aciona**. Não basta registrar uma regra simétrica entre `Unit` e `Switch`, pois isso poderia permitir que um fantasma acionasse o circuito.

Você também precisará verificar se o handler é chamado a cada movimento ou somente em situações específicas. Caso o jogador permaneça na mesma casa ou volte a colidir com o switch, a regra de ativação deve corresponder ao comportamento exigido pela atividade.

### C. O fechamento seguro do gate

Este é o caso que mais exige cuidado. O gate tem pelo menos três estados relevantes:

<icon name="door-open" size="xl" />

**Aberto**

A passagem permite a entrada e a saída.

---

<icon name="door-closed" size="xl" />

**Fechado**

Nenhuma unidade pode entrar na casa do gate.

---

<icon name="clock" size="xl" />

**Fechamento pendente**

Se o circuito manda fechar enquanto há unidades dentro, elas podem sair, mas novas unidades não podem entrar. Quando a última unidade sair, o gate passa a fechado.

Além disso, se o circuito voltar a mandar abrir enquanto o fechamento estiver pendente, o gate precisa cancelar esse fechamento.

Por isso, o estado não deve ser representado apenas por um booleano `isOpen`. Você pode usar um enum, por exemplo:

```java
enum GateState {
    OPEN,
    CLOSED,
    CLOSING
}
```

A seguir, o método que muda o estado do gate deve consultar quem está ocupando a casa. Se houver ocupantes, ele passa para `CLOSING`; se não houver, pode fechar imediatamente. A regra que permite a saída dos ocupantes existentes, mas impede novas entradas, precisa ser implementada na lógica de movimento — não apenas no handler de colisão.

---

## 6. Qual arquivo você deve investigar agora?

Para avançar sem implementar a lógica no lugar errado, eu examinaria estes arquivos, nesta ordem:

1. `Square.java` — como as casas armazenam e movimentam unidades.
2. `Unit.java` — como uma unidade se move entre casas.
3. `Level.java` — se centraliza o movimento e o disparo de colisões.
4. `CollisionMap.java` — a interface que o mapa de colisões implementa.
5. `DefaultPlayerInteractionMap.java` e `PlayerCollisions.java` — como as regras concretas de colisão são registradas e executadas.
6. `BoardFactory.java` — como paredes e casas de chão são construídas.

**O próximo passo que eu sugiro é olhar `Square.java`, `Unit.java` e `Level.java` juntos.** Com esses três, conseguiremos localizar o ponto exato em que o movimento pode ser bloqueado, e então escolher se o gate deve funcionar como uma parede dinâmica ou como uma unidade com um mecanismo próprio de passagem.

Isso também permitirá implementar a regra mais delicada da atividade — deixar os ocupantes saírem de um gate em fechamento pendente, sem permitir novas entradas — sem introduzir efeitos colaterais nas colisões normais do jogo.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>Eu modifiquei o gate para ser um square, para ter colisão como parede e sua colisão ficará ativada enquanto estiver no estado fechado, agora preciso implementar a dinâmica dos switchs interferirem na ativação e desativação dos gates certo? </strong></p>&#10;<p style="margin:0 0 8px"><strong>Eu tive uma ideia de criar uma espécie de classe &quot;observer&quot; ou algo do tipo que recebe sinal do switch e por trás dos panos notifica os gates que sabera que são da mesma classe para alterarem seus estados caso seja possível.</strong></p>&#10;<p style="margin:0 0 8px"><strong>Quero que você diga quais arquivos eu devo modificar para implementar isso e como eu posso fazer isso dar certo?  Estou um pouco perdido ainda em como fazer essa comunicação respeitando o restante da arquitetura.</strong></p></td></tr></tbody></table>

```json
{"api_tool_source":"files/read","results":[{"name":"Pasted text.txt","mime_type":"text/plain","size_bytes":13774,"library_file_id":"libfile_51807354a9d0819184195a20d2b0cd50","file_id":"file_0000000033a8820e922b61e7339b0912","api_tool_source":"files/read"}]}
```

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>Eu modifiquei o gate para ser um square, para ter colisão como parede e sua colisão ficará ativada enquanto estiver no estado fechado, agora preciso implementar a dinâmica dos switchs interferirem na ativação e desativação dos gates certo? </strong></p>&#10;<p style="margin:0 0 8px"><strong>Eu tive uma ideia de criar uma espécie de classe &quot;observer&quot; ou algo do tipo que recebe sinal do switch e por trás dos panos notifica os gates que sabera que são da mesma classe para alterarem seus estados caso seja possível.</strong></p>&#10;<p style="margin:0 0 8px"><strong>Quero que você diga quais arquivos eu devo modificar para implementar isso e como eu posso fazer isso dar certo?  Estou um pouco perdido ainda em como fazer essa comunicação respeitando o restante da arquitetura.</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/006-DefaultPlayerInteractionMap(1).java">DefaultPlayerInteractionMap(1).java</a></em></strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/007-Gate(1).java">Gate(1).java</a></em></strong></p></td></tr></tbody></table>

Sim. Agora que você decidiu representar o `Gate` como um `Square`, faz sentido implementar a comunicação entre os switches e os gates do mesmo circuito. Sua ideia de uma classe intermediária que recebe o sinal do switch e notifica os gates é boa — e tem bastante relação com o padrão de projeto **Observer**.

Mas eu faria uma pequena adaptação: em vez de um observer genérico, criaria uma classe que representa o **circuito**, responsável por manter o estado do circuito e comunicar o novo comando aos gates associados a ele.

Isso deixa a arquitetura mais clara: o `Switch` não precisa conhecer cada gate individualmente, e cada `Gate` não precisa procurar outros gates pelo mapa.

## 1. Arquitetura que eu recomendo

Relative throughput: 100%
**`Switch`**

O jogador entra na casa do interruptor.

<icon name="arrow-down" size="lg" />
Relative throughput: 100%
**`Circuit` — nova classe**

Alterna o comando do circuito e notifica os gates associados.

<icon name="arrow-down" size="lg" />
<grid columns={2} gap={2}>
<grid-item>

**`Gate a`**

Circuito 1

Atualiza seu estado físico.

</grid-item>
<grid-item>

**`Gate b`**

Circuito 2

Não muda quando o circuito 1 é acionado.

</grid-item>
</grid>

O desenho é conceitual: cada circuito deve notificar **todos os seus próprios gates**, e não apenas um gate. Os circuitos 1 e 2 são independentes.

Uma distinção importante: o circuito representa o **comando desejado** (abrir ou fechar); o gate representa a **situação física atual**. Isso é essencial para implementar o fechamento pendente exigido pela atividade.

## 2. Primeiro, corrija a estrutura atual de `Gate.java`

O arquivo que você enviou ainda declara:

```java
public class Gate extends Unit
```

e importa `Square`, mas não o estende. Se você já mudou isso no seu código local, o anexo pode estar desatualizado; de qualquer forma, há alguns problemas importantes no conteúdo enviado. <FileCite ref_id="turn5file1" />

- `state` é `final`, então não pode ser alterado depois da construção.
- `int state=0` não é um parâmetro válido em Java; Java não permite parâmetros com valores padrão.
- `isAccessibleTo` deve retornar um `boolean`, mas o código retorna `state`, que é `int`.
- `getSprite` usa uma condição booleana, mas `state` é `int`.
- `setOpen(boolean open)` ignora o parâmetro e sempre tenta definir o estado como `1`.

Se o `Gate` agora for realmente um `Square`, a implementação deve seguir o contrato de `Square` e a forma como `BoardFactory` constrói paredes e chão. Não basta trocar `extends Unit` por `extends Square` sem conferir os métodos abstratos e a lógica de movimentação.

Eu separaria os estados em duas dimensões:

```java
enum GateState {
    OPEN,
    CLOSED,
    CLOSING
}
```

- `OPEN`: aberto, entrada e saída permitidas.
- `CLOSED`: fechado, entrada bloqueada.
- `CLOSING`: fechamento pendente, saída permitida apenas para ocupantes atuais.

O circuito terá um booleano ou enum próprio indicando o comando desejado. Assim, um gate pode receber o comando de fechar e permanecer temporariamente em `CLOSING`.

## 3. Quais arquivos modificar?

Eu dividiria a implementação em quatro partes: circuito, switch, gate e integração com o mapa.

| Arquivo | Responsabilidade |
|---|---|
| `Circuit.java` — novo | Mantém o comando do circuito e notifica seus gates. |
| `Gate.java` | Armazena o estado físico do gate e decide quando pode abrir ou fechar. |
| `Switch.java` | Identifica seu circuito e solicita a alternância quando acionado pelo jogador. |
| `MapParser.java` | Cria os circuitos e associa cada switch e gate ao circuito correto. |
| `DefaultPlayerInteractionMap.java` | Registra a interação entre jogador e switch. |
| `Square.java` e `BoardFactory.java` | Verifica como a acessibilidade das casas é determinada e como implementar uma parede dinâmica. |
| `Unit.java` e `Level.java` | Verifica o fluxo de movimento e o momento em que uma unidade sai ou entra em uma casa. |
| `LevelFactory.java` | Integra a criação dos objetos, se as factories forem responsáveis por construí-los. |
| `PointCalculator.java` e sua implementação | Só precisam mudar se você realmente quiser pontuar ao acionar switches; isso não é necessário para a dinâmica do circuito. |

Não recomendo alterar todos de uma vez. Primeiro faça a comunicação entre circuito, switch e gates funcionar; depois implemente o fechamento pendente na mecânica de movimento.

## 4. Como eu implementaria a classe `Circuit`?

A classe `Circuit` seria responsável por manter a lista de gates que pertencem a um mesmo circuito.

Por exemplo, o circuito 1 tem os gates `a1`, `a2` e o switch `1`. Quando o jogador aciona esse switch, o circuito alterna seu comando e notifica `a1` e `a2`. O circuito 2 não recebe nenhuma notificação.

Um esqueleto inicial seria:

```java
package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.List;

public class Circuit {

    private boolean open;
    private final List<Gate> gates = new ArrayList<>;

    public Circuit {
        this.open = false; // todos os gates começam fechados
    }

    public void addGate(Gate gate) {
        gates.add(gate);
        gate.receiveCommand(open);
    }

    public void toggle {
        open = !open;

        for (Gate gate : gates) {
            gate.receiveCommand(open);
        }
    }

    public boolean isOpen {
        return open;
    }
}
```

Esse é um **esboço para a camada de comunicação**, não a implementação final completa. Ele pressupõe que `Gate` tenha um método `receiveCommand(boolean open)`, que ainda precisará ser criado.

Observe também que o `Circuit` não precisa procurar os gates no tabuleiro: eles se registram nele durante a criação do mapa.

### Por que isso é melhor do que um observer genérico?

Porque o circuito já representa o conceito da atividade: um conjunto de gates e switches que compartilham um identificador.

Você poderia implementar uma interface `Observer` e uma interface `Observable`, mas isso adicionaria abstrações que talvez não tragam benefício para apenas dois circuitos. A lista de gates e o método `toggle` já resolvem a comunicação de maneira simples.

## 5. Como o `Switch` deve se comunicar com o circuito?

O switch deve guardar uma referência ao circuito ao qual pertence. Em vez de procurar gates ou modificar seus estados diretamente, ele apenas solicita a alternância.

Por exemplo:

```java
public class Switch extends Unit {

    private final int group;
    private final Circuit circuit;

    public Switch(int group, Circuit circuit) {
        this.group = group;
        this.circuit = circuit;
    }

    public int getGroup {
        return group;
    }

    public void changeState {
        circuit.toggle;
    }
}
```

O construtor acima é ilustrativo: preserve os parâmetros e os métodos de sprite que sua classe atual já utiliza.

A comunicação fica assim:

```text
Player
  |
  | entra na posição do switch
  v
DefaultPlayerInteractionMap
  |
  | switch.changeState
  v
Circuit.toggle
  |
  | receiveCommand(...)
  v
Todos os gates daquele circuito
```

No `DefaultPlayerInteractionMap.java` que você enviou, já existe uma regra para `Player` e `Switch` que chama `switch.changeState`. Isso significa que você provavelmente não precisa criar outra regra de colisão: precisa fazer `changeState` comunicar-se corretamente com o circuito. <FileCite ref_id="turn5file0" />

Um detalhe importante: a regra atual também chama `pointCalculator.collidedWithASwitch(player, switch)`. Confira se esse método existe e se você realmente precisa pontuar. A atividade diz que os portões não devem conceder pontos; portanto, a pontuação por acionar um switch não deve ser adicionada sem necessidade.

## 6. Como associar cada gate e switch ao circuito no `MapParser.java`?

Esse é o ponto de integração mais importante.

Seu parser já reconhece os símbolos do mapa e cria os objetos correspondentes. Agora ele precisa garantir que todos os símbolos `1` e `a` compartilhem uma instância de circuito, enquanto `2` e `b` compartilham outra.

O resultado conceitual deve ser:

```text
MapParser
 ├── Circuit 1
 │    ├── Switch 1
 │    ├── Gate a
 │    └── Gate a
 │
 └── Circuit 2
      ├── Switch 2
      └── Gate b
```

Uma forma de fazer isso é manter um mapa de circuitos dentro do processo de parsing:

```java
Map<Integer, Circuit> circuits = new HashMap<>;

circuits.put(1, new Circuit);
circuits.put(2, new Circuit);
```

Ao encontrar um símbolo de gate, o parser cria o gate e o registra no circuito correspondente:

```java
Circuit circuit = circuits.get(1);
Gate gate = /* criar gate associado ao circuito */;
circuit.addGate(gate);
```

Ao encontrar um switch:

```java
Circuit circuit = circuits.get(1);
Switch circuitSwitch = /* criar switch associado ao circuito */;
```

Esses trechos mostram a ideia, não as chamadas definitivas das suas factories.

**Importante:** crie os circuitos uma única vez por mapa, não uma vez para cada símbolo. Caso contrário, dois gates visualmente pertencentes ao mesmo circuito acabariam recebendo comandos de objetos `Circuit` diferentes.

Também é importante validar o mapa depois do parsing: cada circuito usado precisa ter pelo menos um switch e um gate. Os mapas antigos, sem esses símbolos, devem continuar funcionando.

## 7. A parte mais delicada: `Gate` como `Square`

Antes de avançar para o fechamento pendente, verifique uma coisa: a arquitetura do JPacman pode ter métodos que esperam uma casa normal ou uma parede criada por `BoardFactory`. Seu `Gate` precisa respeitar o contrato dessas classes.

Eu evitaria colocar a lista de gates e a lógica dos circuitos dentro de `Square.java`. A classe `Square` deve continuar responsável por representar a casa e sua acessibilidade; o `Circuit` cuida da coordenação, e o `Gate` cuida de seu próprio estado.

Há, porém, uma questão que precisamos resolver olhando o código real: **se o gate é um `Square`, como o jogo identifica as unidades que já estão sobre ele e permite que saiam enquanto bloqueia novas entradas?**

Apenas mudar `isAccessibleTo` não garante o comportamento exigido. Dependendo do fluxo de movimento, um gate em fechamento pendente precisa reconhecer os ocupantes atuais e liberar sua saída, mas negar a entrada de qualquer unidade nova.

Eu implementaria isso em uma segunda etapa, após a comunicação entre os circuitos funcionar.

## 8. Ordem de implementação e testes

Sugiro seguir esta sequência:

1. **Criar `Circuit.java`.** Teste que `toggle` alterna o comando e notifica todos os gates registrados.

2. **Adaptar `Gate.java`.** Corrija os tipos de estado e implemente o recebimento do comando, sem ainda tentar resolver todos os casos de ocupação.

3. **Adaptar `Switch.java`.** Faça o switch manter uma referência ao circuito e solicitar a alternância.

4. **Integrar no `MapParser.java`.** Garanta que cada símbolo seja associado à instância correta de circuito e valide circuitos incompletos.

5. **Verificar a movimentação.** Confirme que o gate fechado bloqueia jogador e fantasmas e que o aberto permite a passagem.

6. **Implementar o fechamento pendente.** Teste o gate ocupado, a saída dos ocupantes, o bloqueio de novas entradas e o cancelamento do fechamento quando chegar um novo comando de abertura.

Minha recomendação é começar pelos três primeiros arquivos e pelo parser. Para definir a implementação correta da física, preciso ver a versão atual de `Square.java`, `BoardFactory.java`, `Unit.java` e `Level.java`, porque é neles que podemos determinar o ponto correto para bloquear a entrada e detectar a saída dos ocupantes.

Se você anexar esses quatro arquivos, consigo indicar exatamente onde encaixar essa lógica, respeitando a arquitetura existente em vez de propor uma implementação que depois entre em conflito com o movimento do jogo.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>como eu coloco valores padrão se não posso passar eles nos parâmetros?</strong></p></td></tr></tbody></table>

Em Java, você não pode definir valores padrão diretamente nos parâmetros de um construtor ou método, como faria em algumas outras linguagens. Mas existem algumas alternativas.

## 1. Inicializar o atributo na declaração

Você define o valor padrão diretamente no atributo da classe:

```java
public class Gate {
    private int state = 0;
    private int group;

    public Gate(int group, Sprite openSprite, Sprite closeSprite) {
        this.group = group;
    }
}
```

Assim, `state` começa com `0` mesmo sem ser passado no construtor.

**Atenção:** se o atributo for `final`, ele poderá ser inicializado apenas uma vez — na declaração ou no construtor.

## 2. Criar construtores sobrecarregados

Se você quiser permitir que o chamador escolha entre usar o valor padrão ou informar um valor específico, pode criar dois construtores:

```java
public class Gate {
    private final int group;
    private int state;

    public Gate(int group, Sprite openSprite, Sprite closeSprite) {
        this(group, 0, openSprite, closeSprite);
    }

    public Gate(int group, int state, Sprite openSprite, Sprite closeSprite) {
        this.group = group;
        this.state = state;
    }
}
```

Agora você pode criar uma `Gate` de duas maneiras:

```java
// Usa state = 0
Gate gate1 = new Gate(1, openSprite, closeSprite);

// Define state explicitamente
Gate gate2 = new Gate(2, 1, openSprite, closeSprite);
```

A expressão `this(group, 0, openSprite, closeSprite)` chama o outro construtor da mesma classe. Essa chamada deve ser a primeira instrução do construtor.

## 3. Qual eu usaria no seu caso?

Para a sua classe `Gate`, eu usaria a **segunda opção** se você precisa definir o estado inicial em alguns casos, mas quer que ele seja `0` por padrão nos demais.

Uma observação importante: se `state` representa se o portão está aberto, fechado ou aguardando para fechar, talvez seja melhor usar um `enum` em vez de um `int`:

```java
public enum GateState {
    OPEN,
    CLOSED,
    CLOSING
}
```

Isso deixa o código mais legível e evita valores numéricos inválidos.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>fui tentar testar com as implementações atuais depois de algumas modificações, como modificar a criação do gate, ajustando as classes, quero ver como está a colisão e se ainda está tudo inteiro, mas estão dando os seguintes erros:</strong></p>&#10;<p style="margin:0 0 8px"><strong>./gradlew run</strong></p>&#10;<blockquote>&#10;<p style="margin:0 0 8px"><strong>Task :compileJava FAILED<br>/mnt/HD/programacao/software/poo/entrega_bernardo_pereira_silva/projeto/jpacman/src/main/java/nl/tudelft/jpacman/level/MapParser.java:143: error: cannot find symbol<br>                grid[x][y] = boardCreator.createGate(1);<br>                                         ^<br>  symbol:   method createGate(int)<br>  location: variable boardCreator of type BoardFactory<br>/mnt/HD/programacao/software/poo/entrega_bernardo_pereira_silva/projeto/jpacman/src/main/java/nl/tudelft/jpacman/level/MapParser.java:146: error: cannot find symbol<br>                grid[x][y] = boardCreator.createGate(2);<br>                                         ^<br>  symbol:   method createGate(int)<br>  location: variable boardCreator of type BoardFactory<br>Note: /mnt/HD/programacao/software/poo/entrega_bernardo_pereira_silva/projeto/jpacman/src/main/java/nl/tudelft/jpacman/points/PointCalculatorLoader.java uses or overrides a deprecated API.<br>Note: Recompile with -Xlint:deprecation for details.<br>2 errors</strong></p>&#10;</blockquote>&#10;<p style="margin:0 0 8px"><strong>FAILURE: Build failed with an exception.</strong></p>&#10;<ul>&#10;<li><p style="margin:0 0 8px"><strong>What went wrong:<br>Execution failed for task &#39;:compileJava&#39;.</strong></p>&#10;<blockquote>&#10;<p style="margin:0 0 8px"><strong>Compilation failed; see the compiler error output for details.</strong></p>&#10;</blockquote>&#10;</li>&#10;<li><p style="margin:0 0 8px"><strong>Try:<br>Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.</strong></p>&#10;</li>&#10;<li><p style="margin:0 0 8px"><strong>Get more help at <a href="https://help.gradle.org">https://help.gradle.org</a></strong></p>&#10;</li>&#10;</ul>&#10;<p style="margin:0 0 8px"><strong>Deprecated Gradle features were used in this build, making it incompatible with Gradle 6.0.<br>Use &#39;--warning-mode all&#39; to show the individual deprecation warnings.<br>See <a href="https://docs.gradle.org/5.3/userguide/command_line_interface.html#sec:command_line_warnings">https://docs.gradle.org/5.3/userguide/command_line_interface.html#sec:command_line_warnings</a></strong></p>&#10;<p style="margin:0 0 8px"><strong>BUILD FAILED in 0s<br>1 actionable task: 1 executed<br>(base) bpereiras@AutobotJr:/mnt/HD/programacao/software/poo/entrega_bernardo_pereira_silva/projeto/jpacman$ </strong></p>&#10;<p style="margin:0 0 8px"><strong>não entendi bem oq pode ser</strong></p></td></tr></tbody></table>

Esse erro é relativamente simples de resolver! O compilador está dizendo que o método `createGate(int)` que você está tentando chamar no `MapParser.java` **não existe na classe `BoardFactory`** (ou não está declarado com essa assinatura).

Vamos por partes.

## 1. Onde está o problema?

O erro aponta para estas linhas do seu `MapParser.java`:

```java
grid[x][y] = boardCreator.createGate(1);
grid[x][y] = boardCreator.createGate(2);
```

O objeto `boardCreator` é do tipo `BoardFactory`, e o compilador não encontra um método chamado `createGate` que receba um `int`.

Provavelmente, você modificou o construtor de `Gate`, mas ainda não criou — ou não atualizou — o método responsável por instanciá-lo em `BoardFactory`.

## 2. Como resolver?

Abra o arquivo `BoardFactory.java`. É nele que você pode centralizar a criação dos elementos do tabuleiro, incluindo os portões.

Se sua classe `Gate` recebe o circuito e os sprites de abertura e fechamento, por exemplo, você poderia criar um método assim:

```java
public Gate createGate(int group) {
    return new Gate(
        group,
        openGateSprite,
        closedGateSprite
    );
}
```

**Atenção:** `openGateSprite` e `closedGateSprite` são nomes ilustrativos. Você precisa usar os sprites que realmente existem no seu projeto. Além disso, esse exemplo pressupõe que seu construtor de `Gate` recebe esses três argumentos.

Se o construtor atual também recebe um objeto `Circuit`, será necessário passá-lo ao criar o portão. A assinatura exata depende de como você implementou suas classes.

## 3. Por que o compilador não resolve isso sozinho?

Porque modificar o construtor de uma classe não cria automaticamente um método de fábrica em outra classe.

São duas operações diferentes:

- `new Gate(...)`: instancia diretamente um portão.
- `boardCreator.createGate(...)`: chama um método que precisa estar declarado em `BoardFactory` e que, internamente, pode instanciar o portão.

## 4. Como eu sugiro prosseguir

Como você está testando a colisão e quer garantir que as modificações não quebraram o restante do JPacman, eu evitaria fazer uma alteração no escuro.

Envie o conteúdo atual destes arquivos:

- `BoardFactory.java`
- `Gate.java`
- `MapParser.java`
- `Square.java`

Você pode anexá-los aqui:

<AskUserFiles kind="file" locale_override="pt-BR" />

Com eles, consigo verificar a assinatura real do construtor, como os sprites são obtidos, como o portão é inserido no tabuleiro e como a colisão é determinada. Assim, podemos corrigir o erro de compilação sem introduzir um segundo problema na lógica de colisão.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>na verdade eu coloquei o gate no levelFactory, seria mais adequado colocar ele no boardFactory? ou isso quebraria alguma estrutura da arquitetura?&#x20;</strong></p>&#10;<p style="margin:0 0 8px"><strong>segue os dois códigos abaixo:</strong></p>&#10;<p style="margin:0 0 8px"><strong>package nl.tudelft.jpacman.level;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import java.util.List;<br>import java.util.Map;<br>import java.util.Optional;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.board.Board;<br>import nl.tudelft.jpacman.board.Direction;<br>import nl.tudelft.jpacman.board.Square;<br>import nl.tudelft.jpacman.npc.Ghost;<br>import nl.tudelft.jpacman.npc.ghost.GhostColor;<br>import nl.tudelft.jpacman.npc.ghost.GhostFactory;<br>import nl.tudelft.jpacman.points.PointCalculator;<br>import nl.tudelft.jpacman.sprite.PacManSprites;<br>import nl.tudelft.jpacman.sprite.Sprite;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Factory that creates levels and units.<br>&#x20;*<br>&#x20;* @author Jeroen Roosen&#x20;<br>&#x20;*/<br>public class LevelFactory {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private static final <em>int</em> GHOSTS = 4;<br>&#x20;   private static final <em>int</em> BLINKY = 0;<br>&#x20;   private static final <em>int</em> INKY = 1;<br>&#x20;   private static final <em>int</em> PINKY = 2;<br>&#x20;   private static final <em>int</em> CLYDE = 3;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The default value of a pellet.<br>&#x20;    */<br>&#x20;   private static final <em>int</em> PELLET_VALUE = 10;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The sprite store that provides sprites for units.<br>&#x20;    */<br>&#x20;   private final <em>PacManSprites</em> sprites;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Used to cycle through the various ghost types.<br>&#x20;    */<br>&#x20;   private <em>int</em> ghostIndex;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The factory providing ghosts.<br>&#x20;    */<br>&#x20;   private final <em>GhostFactory</em> ghostFact;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The way to calculate points upon collisions.<br>&#x20;    */<br>&#x20;   private final <em>PointCalculator</em> pointCalculator;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new level factory.<br>&#x20;    *<br>&#x20;    * @param <em>spriteStore</em><br>&#x20;    *            The sprite store providing the sprites for units.<br>&#x20;    * @param <em>ghostFactory</em><br>&#x20;    *            The factory providing ghosts.<br>&#x20;    * @param <em>pointCalculator</em><br>&#x20;    *            The algorithm to calculate the points.<br>&#x20;    */<br>&#x20;   public LevelFactory(<em>PacManSprites</em> <em>spriteStore</em>,<br><em>GhostFactory</em> <em>ghostFactory</em>,<br><em>PointCalculator</em> <em>pointCalculator</em>) {<br><em>this</em>.sprites = spriteStore;<br><em>this</em>.ghostIndex = -1;<br><em>this</em>.ghostFact = ghostFactory;<br><em>this</em>.pointCalculator = pointCalculator;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new level from the provided data.<br>&#x20;    *<br>&#x20;    * @param <em>board</em><br>&#x20;    *            The board with all ghosts and pellets occupying their squares.<br>&#x20;    * @param <em>ghosts</em><br>&#x20;    *            A list of all ghosts on the board.<br>&#x20;    * @param <em>startPositions</em><br>&#x20;    *            A list of squares from which players may start the game.<br>&#x20;    * @return A new level for the board.<br>&#x20;    */<br>&#x20;   public <em>Level</em> createLevel(<em>Board</em> <em>board</em>, <em>List</em>&lt;Ghost&gt; <em>ghosts</em>, <em>List</em>&lt;Square&gt; <em>startPositions</em>) {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       // We&#39;ll adopt the simple collision map for now.<br><em>CollisionMap</em> collisionMap = <strong>new</strong> PlayerCollisions(pointCalculator);</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       return <strong>new</strong> Level(board, ghosts, startPositions, collisionMap);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new ghost.<br>&#x20;    *<br>&#x20;    * @return The new ghost.<br>&#x20;    */<br><em>Ghost</em> createGhost {<br>&#x20;       ghostIndex++;<br>&#x20;       ghostIndex %= GHOSTS;<br>&#x20;       switch (ghostIndex) {<br>&#x20;           case BLINKY:<br>&#x20;               return ghostFact.createBlinky;<br>&#x20;           case INKY:<br>&#x20;               return ghostFact.createInky;<br>&#x20;           case PINKY:<br>&#x20;               return ghostFact.createPinky;<br>&#x20;           case CLYDE:<br>&#x20;               return ghostFact.createClyde;<br>&#x20;           default:<br>&#x20;               return <strong>new</strong> RandomGhost(sprites.getGhostSprite(GhostColor.RED));<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new pellet.<br>&#x20;    *<br>&#x20;    * @return The new pellet.<br>&#x20;    */<br>&#x20;   public <em>Pellet</em> createPellet {<br>&#x20;       return <strong>new</strong> Pellet(PELLET_VALUE, sprites.getPelletSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new gate.<br>&#x20;    *<br>&#x20;    * @return The new gate.<br>&#x20;    */<br>&#x20;   public <em>Gate</em> createGate(<em>int</em> <em>group</em>) {<br>&#x20;       return <strong>new</strong> Gate(group, sprites.getGateSprite, sprites.getGroundSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new switch.<br>&#x20;    *<br>&#x20;    * @return The new switch.<br>&#x20;    */<br>&#x20;   public <em>Switch</em> createSwitch(<em>int</em> <em>group</em>) {<br>&#x20;       return <strong>new</strong> Switch(group, sprites.getSwitchSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Implementation of an NPC that wanders around randomly.<br>&#x20;    *<br>&#x20;    * @author Jeroen Roosen<br>&#x20;    */<br>&#x20;   private static final class RandomGhost extends <em>Ghost</em> {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * The suggested delay between moves.<br>&#x20;        */<br>&#x20;       private static final <em>long</em> DELAY = 175L;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * Creates a new random ghost.<br>&#x20;        *<br>&#x20;        * @param <em>ghostSprite</em><br>&#x20;        *            The sprite for the ghost.<br>&#x20;        */<br>&#x20;       RandomGhost(<em>Map</em>&lt;Direction, Sprite&gt; <em>ghostSprite</em>) {<br><em>super</em>(ghostSprite, (<em>int</em>) DELAY, 0);<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       @<em>Override</em><br>&#x20;       public <em>Optional</em>&lt;Direction&gt; nextAiMove {<br>&#x20;           return Optional.empty;<br>&#x20;       }<br>&#x20;   }<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>esse é o level, agoar abaixo o board</strong></p>&#10;<p style="margin:0 0 8px"><strong>package nl.tudelft.jpacman.board;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.sprite.PacManSprites;<br>import nl.tudelft.jpacman.sprite.Sprite;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* A factory that creates {@link Board} objects from 2-dimensional arrays of<br>&#x20;* {@link Square}s.<br>&#x20;*<br>&#x20;* @author Jeroen Roosen&#x20;<br>&#x20;*/<br>public class BoardFactory {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The sprite store providing the sprites for the background.<br>&#x20;    */<br>&#x20;   private final <em>PacManSprites</em> sprites;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new BoardFactory that will create a board with the provided<br>&#x20;    * background sprites.<br>&#x20;    *<br>&#x20;    * @param <em>spriteStore</em><br>&#x20;    *            The sprite store providing the sprites for the background.<br>&#x20;    */<br>&#x20;   public BoardFactory(<em>PacManSprites</em> <em>spriteStore</em>) {<br><em>this</em>.sprites = spriteStore;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new board from a grid of cells and connects it.<br>&#x20;    *<br>&#x20;    * @param <em>grid</em><br>&#x20;    *            The square grid of cells, in which grid[x][y] corresponds to<br>&#x20;    *            the square at position x,y.<br>&#x20;    * @return A new board, wrapping a grid of connected cells.<br>&#x20;    */<br>&#x20;   public <em>Board</em> createBoard(<em>Square</em>[][] <em>grid</em>) {<br>&#x20;       assert grid != null;</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Board</em> board = <strong>new</strong> Board(grid);</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>int</em> width = board.getWidth;<br><em>int</em> height = board.getHeight;<br>&#x20;       for (<em>int</em> x = 0; x &lt; width; x++) {<br>&#x20;           for (<em>int</em> y = 0; y &lt; height; y++) {<br><em>Square</em> square = grid[x][y];<br>&#x20;               for (<em>Direction</em> dir : Direction.values) {<br><em>int</em> dirX = (width + x + dir.getDeltaX) % width;<br><em>int</em> dirY = (height + y + dir.getDeltaY) % height;<br><em>Square</em> neighbour = grid[dirX][dirY];<br>&#x20;                   square.link(neighbour, dir);<br>&#x20;               }<br>&#x20;           }<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       return board;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new square that can be occupied by any unit.<br>&#x20;    *<br>&#x20;    * @return A new square that can be occupied by any unit.<br>&#x20;    */<br>&#x20;   public <em>Square</em> createGround {<br>&#x20;       return <strong>new</strong> Ground(sprites.getGroundSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new square that cannot be occupied by any unit.<br>&#x20;    *<br>&#x20;    * @return A new square that cannot be occupied by any unit.<br>&#x20;    */<br>&#x20;   public <em>Square</em> createWall {<br>&#x20;       return <strong>new</strong> Wall(sprites.getWallSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * A wall is a square that is inaccessible to anyone.<br>&#x20;    *<br>&#x20;    * @author Jeroen Roosen<br>&#x20;    */<br>&#x20;   private static final class Wall extends <em>Square</em> {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * The background for this square.<br>&#x20;        */<br>&#x20;       private final <em>Sprite</em> background;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * Creates a new wall square.<br>&#x20;        *<br>&#x20;        * @param <em>sprite</em><br>&#x20;        *            The background for the square.<br>&#x20;        */<br>&#x20;       Wall(<em>Sprite</em> <em>sprite</em>) {<br><em>this</em>.background = sprite;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       @<em>Override</em><br>&#x20;       public <em>boolean</em> isAccessibleTo(<em>Unit</em> <em>unit</em>) {<br>&#x20;           return false;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       @<em>Override</em><br>&#x20;       public <em>Sprite</em> getSprite {<br>&#x20;           return background;<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * A ground square is a square that is accessible to anyone.<br>&#x20;    *<br>&#x20;    * @author Jeroen Roosen<br>&#x20;    */<br>&#x20;   private static final class Ground extends <em>Square</em> {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * The background for this square.<br>&#x20;        */<br>&#x20;       private final <em>Sprite</em> background;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * Creates a new ground square.<br>&#x20;        *<br>&#x20;        * @param <em>sprite</em><br>&#x20;        *            The background for the square.<br>&#x20;        */<br>&#x20;       Ground(<em>Sprite</em> <em>sprite</em>) {<br><em>this</em>.background = sprite;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       @<em>Override</em><br>&#x20;       public <em>boolean</em> isAccessibleTo(<em>Unit</em> <em>unit</em>) {<br>&#x20;           return true;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       @<em>Override</em><br>&#x20;       public <em>Sprite</em> getSprite {<br>&#x20;           return background;<br>&#x20;       }<br>&#x20;   }<br>}</strong></p></td></tr></tbody></table>

Pelo código que você enviou, **eu manteria a criação de `Gate` e `Switch` no `LevelFactory` por enquanto**. Isso não é um erro de arquitetura: depende de como você está modelando esses elementos e de quais responsabilidades quer atribuir a cada fábrica.

O problema que causou o erro de compilação é mais simples: o `MapParser` chama `boardCreator.createGate(...)`, mas o método que você implementou está em `LevelFactory`.

Vamos entender a diferença e escolher uma solução coerente com a arquitetura do JPacman.

## 1. Qual é a responsabilidade de cada fábrica?

<icon name="grid-2x2" size="xl" />

**`BoardFactory`**

Cria a estrutura física do tabuleiro: `Board`, `Square`, chão e paredes. No código que você mostrou, `Ground` e `Wall` são implementações internas de `Square`.

---

<icon name="gamepad-2" size="xl" />

**`LevelFactory`**

Cria elementos relacionados ao nível e ao jogo, como fantasmas, pellets e, na sua implementação, portões e interruptores. Também monta o `Level` com o tabuleiro, os fantasmas, as posições iniciais e o mapa de colisões.

Essa separação é uma convenção de projeto, não uma regra absoluta da linguagem Java. Um portão pode ser visto tanto como parte do tabuleiro quanto como um elemento do jogo.

No seu caso, como o `Gate` tem circuito, estado aberto/fechado e interação com `Switch`, mantê-lo no `LevelFactory` é perfeitamente defensável.

## 2. Por que o erro acontece?

No seu `LevelFactory`, você declarou:

```java
public Gate createGate(int group) {
    return new Gate(
        group,
        sprites.getGateSprite,
        sprites.getGroundSprite
    );
}
```

Já o `MapParser` tenta chamar:

```java
boardCreator.createGate(1);
```

Mas `boardCreator` é um `BoardFactory`, e essa classe não possui esse método.

Você tem duas alternativas.

### Alternativa A — Usar o `LevelFactory` no `MapParser` (recomendação inicial)

Se o `MapParser` já recebe uma referência a `LevelFactory`, utilize-a para criar o portão:

```java
grid[x][y] = levelCreator.createGate(1);
```

E faça o mesmo para o outro circuito:

```java
grid[x][y] = levelCreator.createGate(2);
```

Isso evita duplicar a lógica de criação em `BoardFactory`.

**Importante:** isso pressupõe que `levelCreator` seja o nome do atributo existente no seu `MapParser`. Pelo código que você compartilhou anteriormente, ele já era usado para criar o nível. Se a referência não estiver disponível nesse ponto, será necessário ajustar o construtor ou a injeção de dependências do parser.

### Alternativa B — Mover a criação para `BoardFactory`

Você poderia criar um método `createGate` em `BoardFactory`. Entretanto, isso exigiria decidir onde ficam os sprites e como o portão recebe sua dependência do circuito.

Além disso, como seu `Gate` tem lógica de estado e precisa se comunicar com um circuito, mover toda a criação para `BoardFactory` pode aumentar as responsabilidades dessa classe. Eu não faria essa mudança apenas para resolver o erro de compilação.

---

## 3. Há um detalhe importante: o tipo de `Gate`

Você comentou anteriormente que modificou `Gate` para que a colisão funcionasse como a de uma parede. Porém, no código de `BoardFactory`, o tabuleiro é composto por objetos `Square`, e o método `createBoard` recebe um `Square[][]`.

Isso significa que esta atribuição:

```java
grid[x][y] = levelCreator.createGate(1);
```

só compila se `Gate` for compatível com `Square` — ou seja, se `Gate` estender `Square` ou uma classe que estenda `Square`.

Se `Gate` estender `Unit`, como na versão que você havia compartilhado anteriormente, essa atribuição não vai compilar. Nesse caso, será preciso decidir entre:

- Fazer `Gate` ser um `Square` que controla a acessibilidade e o sprite; ou
- Representar o portão como uma `Unit` ocupando um `Square`, com outra estratégia para bloquear a passagem.

Como você quer que o portão funcione como uma parede que pode abrir e fechar, a primeira abordagem parece mais alinhada ao seu modelo atual de tabuleiro. Ainda assim, precisamos conferir como `Square`, `Unit` e `Level` tratam a ocupação para não prejudicar a movimentação dos fantasmas e do jogador.

## 4. E como isso se encaixa no circuito?

Eu manteria a criação dos elementos em `LevelFactory`, mas separaria a responsabilidade de controlar os circuitos em uma classe própria, por exemplo, `Circuit`.

A arquitetura poderia ficar assim:

Relative throughput: 100%
**`MapParser`**

Interpreta os símbolos do mapa e escolhe o circuito correto

<icon name="arrow-down" color="secondary" size="lg" />
Relative throughput: 100%
**`LevelFactory`**

Cria os portões e interruptores associados ao circuito

<icon name="arrow-down" color="secondary" size="lg" />
Relative throughput: 100%
**`Circuit`**

Mantém o comando aberto/fechado e notifica os portões associados

<icon name="arrow-down" color="secondary" size="lg" />
Relative throughput: 100%
**`Gate`**

Atualiza seu estado físico e determina se o quadrado permite passagem

Assim, o `MapParser` não precisa conhecer os detalhes internos do portão, e o `Circuit` não precisa conhecer as regras de pontuação ou a lógica de colisão do jogador.

## Minha recomendação

1. **Por enquanto, mantenha `createGate` e `createSwitch` no `LevelFactory`.**
2. Corrija as chamadas do `MapParser` para usar `levelCreator`, caso essa referência esteja disponível.
3. Confirme que `Gate` é compatível com `Square`, já que ele é inserido diretamente na matriz do tabuleiro.
4. Depois, teste a colisão antes de implementar o comportamento de fechamento pendente.

O quarto ponto é importante: fazer o portão bloquear a passagem quando fechado é apenas parte do problema. Para permitir que uma unidade saia de um portão ocupado sem permitir que outra entre, precisamos verificar exatamente como o JPacman consulta `isAccessibleTo` e como `Unit` se movimenta.

Se você me mostrar a versão **atual** do seu `Gate.java`, do `Switch.java` e do trecho do `MapParser` que cria esses elementos, consigo indicar a menor alteração necessária para corrigir a arquitetura sem refazer o que você já implementou.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>Eu fiz agora com que o gate tenha colisão, é preciso mexer agora na lógica de fazer o switch ativar, desativar e criarmos o estado de closing, que tem uma lógica específica como foi apresentada antes. Quais arquivos são necessários mexer para tal?</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/008-Gate(2).java">Gate(2).java</a></em></strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/009-Switch.java">Switch.java</a></em></strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Attachment: <a href="files/010-MapParser(1).java">MapParser(1).java</a></em></strong></p></td></tr></tbody></table>

Agora que vi suas implementações atuais, podemos definir os arquivos com mais precisão. Você já implementou boa parte da estrutura: o `Gate` tem os três estados (`CLOSED`, `OPEN` e `CLOSING`), e o `Switch` já possui uma lógica inicial para propagar a mudança aos portões.

**Minha recomendação é não reescrever tudo.** Vamos aproveitar o que você já fez e corrigir a organização da comunicação entre os objetos, além de implementar o tratamento da saída de ocupantes do portão.

## 1. Arquivos que precisam ser modificados

| Arquivo | Responsabilidade na implementação |
|---|---|
| `Gate.java` | Gerenciar os estados e a lógica de fechamento pendente. |
| `Switch.java` | Acionar o circuito e propagar a mudança de comando. |
| `MapParser.java` | Criar e associar os switches e gates de cada circuito. |
| `LevelFactory.java` | Criar os objetos e, possivelmente, centralizar os circuitos. |
| `DefaultPlayerInteractionMap.java` | Garantir que somente o jogador ative switches. |
| `Square.java` / `Unit.java` | Verificar como a ocupação e a movimentação funcionam para permitir a saída de um gate em `CLOSING`. |

Também recomendo criar uma nova classe:

- `Circuit.java`: centralizar o estado do circuito e a comunicação com todos os seus portões e interruptores.

Essa última mudança é uma melhoria arquitetural que evita colocar toda a responsabilidade de gerenciamento do circuito dentro de `Switch`.

## 2. O que já está funcionando e o que falta?

Analisando seus três arquivos:

### `Gate.java` — a estrutura dos estados já existe

Você já implementou:

- `CLOSED`: impede novas entradas.
- `OPEN`: permite entradas.
- `CLOSING`: impede novas entradas, mas aguarda os ocupantes saírem.
- `remove(Unit occupant)`: fecha o portão quando o último ocupante sai. <FileCite ref_id="turn7file0" line_range_start={105} line_range_end={144} />

Essa é uma boa base. O comportamento de `CLOSING` está bem encaminhado.

Um detalhe importante: precisamos conferir se o método `Square.remove` é realmente chamado quando uma unidade sai do quadrado. Isso é essencial para que o fechamento pendente funcione.

### `Switch.java` — a propagação ainda está incompleta

Você já tem uma implementação de `changeState` que:

1. Alterna o comando entre aberto e fechado.
2. Atualiza o estado dos outros switches registrados.
3. Envia o novo comando aos gates registrados. <FileCite ref_id="turn7file1" line_range_start={113} line_range_end={137} />

O problema é que cada switch mantém suas próprias listas:

```java
private final List<Gate> gates = new ArrayList<>;
private final List<Switch> circuitSwitches = new ArrayList<>;
```

Isso significa que, para funcionar, essas listas precisam ser preenchidas corretamente e compartilhadas entre todos os objetos do mesmo circuito.

### `MapParser.java` — aqui está uma das principais alterações necessárias

Atualmente, quando encontra um switch, você faz:

```java
levelCreator.createSwitch(1).occupy(switchSquare1);
```

E, para o portão:

```java
grid[x][y] = levelCreator.createGate(1);
```

O problema é que você não guarda as referências dos switches nem dos gates criados. Consequentemente, não consegue associá-los posteriormente. <FileCite ref_id="turn7file2" line_range_start={132} line_range_end={146} />

Precisamos mudar isso para registrar os objetos por circuito.

---

## 3. Arquitetura que recomendo

Eu faria uma pequena evolução na sua implementação:

**MapParser**

Identifica os símbolos e registra os objetos

<icon name="arrow-down" color="secondary" />

**Circuit 1 / Circuit 2**

Cada circuito conhece seus switches e gates

**Switches**

**Gates**

<icon name="arrow-down" color="secondary" />
Relative throughput: 100%
**Gate**

Recebe o comando e gerencia seu estado físico individualmente

O fluxo seria:

1. O `MapParser` encontra um switch do grupo 1.
2. Cria o switch e o registra no `Circuit 1`.
3. Encontra um gate do grupo 1.
4. Cria o gate e o registra no mesmo circuito.
5. Quando o jogador colide com qualquer switch do grupo 1, o circuito alterna seu comando.
6. Todos os gates do grupo 1 recebem esse comando.
7. Cada gate decide individualmente se pode fechar imediatamente ou se precisa entrar em `CLOSING`.

Isso também resolve um detalhe importante: dois switches do mesmo circuito precisam compartilhar o mesmo estado de comando.

## 4. O que não precisamos modificar agora?

- `BoardFactory.java`: não precisa mudar, pois já consegue montar o tabuleiro com objetos `Square`.
- `CollisionInteractionMap.java`: provavelmente não precisa mudar, pois já oferece o mecanismo de associação entre tipos de objetos.
- `Player.java` e `Ghost.java`: inicialmente, não precisamos alterar suas implementações.

O `DefaultPlayerInteractionMap.java` precisa apenas ser conferido para garantir que a colisão entre `Player` e `Switch` chama `changeState`. Como a interação é registrada especificamente para esses dois tipos, os fantasmas não devem ativar os switches.

---

## 5. Ordem de implementação sugerida

Eu dividiria o trabalho em quatro etapas:

1. **Criar `Circuit.java`**

Centralizar o estado do circuito, os switches e os gates associados.

2. **Modificar `MapParser.java` e `Switch.java`**

Registrar os objetos nos circuitos e fazer o switch delegar a ativação ao circuito.

3. **Revisar `Gate.java` e a movimentação**

Garantir que `CLOSING` permita a saída de ocupantes e que o portão feche quando ficar vazio.

4. **Testar as colisões**

Testar jogador, fantasmas, múltiplos switches, múltiplos gates e fechamento pendente.

**Minha sugestão:** antes de começarmos a modificar o código, envie também as versões atuais de `Square.java`, `Unit.java`, `Level.java` e `DefaultPlayerInteractionMap.java`.

Com esses arquivos, conseguimos verificar o mecanismo de movimentação e ocupação do JPacman. Isso é especialmente importante porque não queremos implementar o `CLOSING` de uma maneira que bloqueie acidentalmente os próprios ocupantes dentro do portão.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>crie o modelo desse circuit.java com base nos arquivos de gate e switch.java</strong></p>&#10;<p style="margin:0 0 8px"><strong>package nl.tudelft.jpacman.level;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.board.Square;<br>import nl.tudelft.jpacman.board.Unit;<br>import nl.tudelft.jpacman.sprite.<em>Sprite</em>;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* A gate, a wall that can be activated and deactivated with a switch.<br>&#x20;*<br>&#x20;* @author Bernardo Pereira Silva<br>&#x20;*/</strong></p>&#10;<p style="margin:0 0 8px"><strong>public class Gate extends Square {</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* The group of this gate.<br>&#x20;*/<br>private final <em>int</em> group;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* The state of the gate&#39;s group.<br>&#x20;*/<br>private <em>int</em> state = 0;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* The opened-state sprite of this object.<br>&#x20;*/<br>private final <em>Sprite</em> open_sprite;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* The closed-state sprite of this object.<br>&#x20;*/<br>private final <em>Sprite</em> close_sprite;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Creates a new gate.<br>&#x20;* @param <em>group</em> The group value of this gate.<br>&#x20;* @param <em>sprite</em> The sprite of this gate.<br>&#x20;*/<br>public Gate(<em>int</em> <em>group</em>, <em>Sprite</em> <em>close_sprite</em>, <em>Sprite</em> <em>open_sprite</em>) {<br><em>this</em>.group = <em>group</em>;<br><em>this</em>.open_sprite = <em>open_sprite</em>;<br><em>this</em>.close_sprite = <em>close_sprite</em>;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Returns the group of this gate.<br>&#x20;* @return The group value of this gate.<br>&#x20;*/<br>public <em>int</em> getGroup {<br>&#x20;   return group;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Returns the state of this gate.<br>&#x20;* @return The state value of this gate.<br>&#x20;*/<br>public <em>int</em> getState {<br>&#x20;   return state;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Changes the state of the gate.<br>&#x20;*/<br>public <em>void</em> setState(<em>int</em> <em>state</em>) {<br><em>this</em>.state = <em>state</em>;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>@Override<br>public <em>boolean</em> isAccessibleTo(Unit <em>unit</em>) {<br>&#x20;   // Se estiver aberto, permite passagem. Se fechado, funciona como parede.<br>&#x20;   return (state == 1) ? true : false;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>@Override<br>public <em>Sprite</em> getSprite {<br>&#x20;   return (state == 1) ? open_sprite : close_sprite;<br>}<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>package nl.tudelft.jpacman.level;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.board.Unit;<br>import nl.tudelft.jpacman.sprite.<em>Sprite</em>;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* A switch, a wall that can be activated and deactivated with a switch.<br>&#x20;*<br>&#x20;* @author Bernardo Pereira Silva<br>&#x20;*/</strong></p>&#10;<p style="margin:0 0 8px"><strong>public class Switch extends Unit {</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* The sprite of this unit.<br>&#x20;*/<br>private final <em>Sprite</em> image;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* The group of this switch.<br>&#x20;*/<br>private final <em>int</em> group;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* The state of the switch&#39;s group.<br>&#x20;*/<br>private <em>int</em> state = 0;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Creates a new switch.<br>&#x20;* @param <em>group</em> The group value of this switch.<br>&#x20;* @param <em>sprite</em> The sprite of this switch.<br>&#x20;*/<br>public Switch(<em>int</em> <em>group</em>, <em>Sprite</em> <em>sprite</em>) {<br><em>this</em>.image = <em>sprite</em>;<br><em>this</em>.group = <em>group</em>;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Returns the group of this switch.<br>&#x20;* @return The group value of this switch.<br>&#x20;*/<br>public <em>int</em> getGroup {<br>&#x20;   return group;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Returns the state of this switch.<br>&#x20;* @return The state value of this swithc.<br>&#x20;*/<br>public <em>int</em> getState {<br>&#x20;   return state;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>public <em>void</em> changeState {<br><em>this</em>.state = (state == 1) ? 0 : 1;<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>@Override<br>public <em>Sprite</em> getSprite {<br>&#x20;   return image;<br>}<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>Quero que você implemente a estrutura que una os dois e gerencia completamente a relação entre os dois objetos, alem de dizer quais outros serão impactados pela criação desse&#x20;</strong></p></td></tr></tbody></table>

Eu implementaria `Circuit.java` como a classe responsável por **unificar os switches e gates de um mesmo grupo e controlar o estado compartilhado entre eles**.

A principal mudança de arquitetura será esta: atualmente, tanto `Gate` quanto `Switch` possuem um atributo `state`. Isso pode gerar inconsistências, porque o estado do circuito e o estado físico de cada portão não são exatamente a mesma coisa.

Por exemplo, quando o circuito recebe um comando para fechar:
- O circuito passa a ter estado `CLOSED`.
- Um portão desocupado fecha imediatamente.
- Um portão ocupado entra em `CLOSING`.
- Todos os switches do circuito passam a refletir o comando `CLOSED`.

Portanto, **o circuito controla o comando; cada gate controla seu próprio estado físico**.

## 1. Nova classe `Circuit.java`

Crie o arquivo em `nl.tudelft.jpacman.level`:

<CodeBlock language="java" editable>
package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a circuit that connects switches and gates.
 * A switch toggles the circuit command, which is propagated
 * to all gates belonging to the circuit.
 *
 * @author Bernardo Pereira Silva
 */
public class Circuit {

    /**
     * Circuit identifier.
     */
    private final int group;

    /**
     * Current command state of the circuit.
     */
    private int state = Gate.CLOSED;

    /**
     * Gates controlled by this circuit.
     */
    private final List<Gate> gates = new ArrayList<>;

    /**
     * Switches associated with this circuit.
     */
    private final List<Switch> switches = new ArrayList<>;

    /**
     * Creates a new circuit.
     *
     * @param group The circuit identifier.
     */
    public Circuit(int group) {
        this.group = group;
    }

    /**
     * Returns the circuit identifier.
     *
     * @return The group identifier.
     */
    public int getGroup {
        return group;
    }

    /**
     * Returns the current command state.
     *
     * @return OPEN or CLOSED.
     */
    public int getState {
        return state;
    }

    /**
     * Registers a gate in this circuit.
     *
     * @param gate The gate to register.
     */
    public void addGate(Gate gate) {
        if (gate != null && gate.getGroup == group && !gates.contains(gate)) {
            gates.add(gate);
            gate.setState(state);
        }
    }

    /**
     * Registers a switch in this circuit.
     *
     * @param sw The switch to register.
     */
    public void addSwitch(Switch sw) {
        if (sw != null && sw.getGroup == group && !switches.contains(sw)) {
            switches.add(sw);
            sw.setCircuit(this);
        }
    }

    /**
     * Toggles the circuit command and notifies all its gates.
     */
    public void toggle {
        state = (state == Gate.OPEN) ? Gate.CLOSED : Gate.OPEN;

        for (Gate gate : gates) {
            gate.setState(state);
        }
    }

    /**
     * Returns an unmodifiable list of gates.
     *
     * @return Gates belonging to this circuit.
     */
    public List<Gate> getGates {
        return Collections.unmodifiableList(gates);
    }

    /**
     * Returns an unmodifiable list of switches.
     *
     * @return Switches belonging to this circuit.
     */
    public List<Switch> getSwitches {
        return Collections.unmodifiableList(switches);
    }
}
</CodeBlock>

Repare que o `Circuit` não precisa atualizar individualmente o estado dos switches. Cada switch guarda uma referência ao circuito e consulta seu estado quando necessário.

Isso evita manter duas cópias da mesma informação.

## 2. Alteração necessária em `Switch.java`

O switch não precisa mais armazenar seu próprio estado nem manter listas de gates. Ele só precisa conhecer o circuito ao qual pertence.

Substitua sua implementação por:

<CodeBlock language="java" editable>
package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A switch that toggles its associated circuit.
 *
 * @author Bernardo Pereira Silva
 */
public class Switch extends Unit {

    private final Sprite image;
    private final int group;
    private Circuit circuit;

    /**
     * Creates a new switch.
     *
     * @param group The circuit identifier.
     * @param sprite The switch sprite.
     */
    public Switch(int group, Sprite sprite) {
        this.group = group;
        this.image = sprite;
    }

    public int getGroup {
        return group;
    }

    /**
     * Associates this switch with a circuit.
     *
     * @param circuit The circuit this switch belongs to.
     */
    void setCircuit(Circuit circuit) {
        if (circuit == null || circuit.getGroup != group) {
            throw new IllegalArgumentException("Invalid circuit for switch.");
        }
        this.circuit = circuit;
    }

    /**
     * Returns the current command state of the circuit.
     */
    public int getState {
        if (circuit == null) {
            return Gate.CLOSED;
        }
        return circuit.getState;
    }

    /**
     * Activates the associated circuit.
     */
    public void changeState {
        if (circuit == null) {
            throw new IllegalStateException(
                "Switch has not been associated with a circuit."
            );
        }
        circuit.toggle;
    }

    @Override
    public Sprite getSprite {
        return image;
    }
}
</CodeBlock>

Agora, quando o jogador colidir com um switch, `changeState` simplesmente chama `circuit.toggle`. O switch não precisa saber quantos portões existem nem como eles funcionam.

## 3. Alteração necessária em `Gate.java`

Aqui precisamos preservar uma distinção importante:

- `OPEN` e `CLOSED` são comandos recebidos do circuito.
- `CLOSING` é um estado físico temporário do portão.

Eu manteria os três estados como constantes inteiras, como você já começou a fazer.

<CodeBlock language="java" editable>
package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A gate that can be opened and closed by a circuit.
 *
 * @author Bernardo Pereira Silva
 */
public class Gate extends Square {

    public static final int CLOSED = 0;
    public static final int OPEN = 1;
    public static final int CLOSING = 2;

    private final int group;
    private int state = CLOSED;

    private final Sprite openSprite;
    private final Sprite closeSprite;

    public Gate(int group, Sprite closeSprite, Sprite openSprite) {
        this.group = group;
        this.closeSprite = closeSprite;
        this.openSprite = openSprite;
    }

    public int getGroup {
        return group;
    }

    public int getState {
        return state;
    }

    /**
     * Receives a command from the circuit.
     *
     * @param newState OPEN or CLOSED.
     */
    public void setState(int newState) {
        if (newState == OPEN) {
            state = OPEN;
        } else if (newState == CLOSED) {
            if (getOccupants.isEmpty) {
                state = CLOSED;
            } else {
                state = CLOSING;
            }
        } else {
            throw new IllegalArgumentException("Invalid gate command.");
        }
    }

    /**
     * Called when a unit leaves this square.
     * A pending gate closes when its last occupant leaves.
     */
    @Override
    protected void remove(Unit occupant) {
        super.remove(occupant);

        if (state == CLOSING && getOccupants.isEmpty) {
            state = CLOSED;
        }
    }

    @Override
    public boolean isAccessibleTo(Unit unit) {
        return state == OPEN;
    }

    @Override
    public Sprite getSprite {
        return state == OPEN ? openSprite : closeSprite;
    }
}
</CodeBlock>

**Como fica o comportamento:**

| Situação | Estado do gate |
|---|---|
| Circuito começa fechado e gate está vazio | `CLOSED` |
| Circuito abre | `OPEN` |
| Circuito fecha e gate está vazio | `CLOSED` |
| Circuito fecha e gate está ocupado | `CLOSING` |
| Último ocupante sai de `CLOSING` | `CLOSED` |
| Circuito abre enquanto gate está em `CLOSING` | `OPEN` |

Observe que `CLOSING` não é enviado pelo circuito. É uma decisão individual do gate.

**Uma ressalva:** esse código pressupõe que `Square` oferece `getOccupants` e que sua remoção de unidades passa pelo método protegido `remove(Unit)`, como na versão anterior do seu `Gate.java`. Precisamos confirmar isso em `Square.java` antes de considerar o comportamento de fechamento pendente concluído.

## 4. Alteração em `MapParser.java`

Este é o ponto que conecta os objetos criados.

Precisamos garantir que todos os objetos do mesmo grupo recebam a mesma instância de `Circuit`.

Por exemplo, todos os símbolos `a` e `1` devem compartilhar o circuito 1.

### Primeiro: crie os circuitos durante o parsing

Adicione os imports:

```java
import java.util.HashMap;
import java.util.Map;
```

Dentro de `parseMap(char[][] map)`, antes de `makeGrid`, crie:

```java
Map<Integer, Circuit> circuits = new HashMap<>;

circuits.put(1, new Circuit(1));
circuits.put(2, new Circuit(2));
```

Depois, passe essa coleção para `makeGrid`:

```java
makeGrid(map, width, height, grid, ghosts, startPositions, circuits);
```

Altere a assinatura de `makeGrid`:

```java
private void makeGrid(char[][] map, int width, int height,
                      Square[][] grid, List<Ghost> ghosts,
                      List<Square> startPositions,
                      Map<Integer, Circuit> circuits) {
```

E passe `circuits` na chamada de `addSquare`:

```java
addSquare(grid, ghosts, startPositions, x, y, c, circuits);
```

Altere também a assinatura de `addSquare`:

```java
protected void addSquare(Square[][] grid, List<Ghost> ghosts,
                         List<Square> startPositions,
                         int x, int y, char c,
                         Map<Integer, Circuit> circuits) {
```

### Segundo: modifique os casos dos switches e gates

Substitua os quatro casos atuais por:

```java
case '1':
case '2':
    int switchGroup = (c == '1') ? 1 : 2;

    Square switchSquare = boardCreator.createGround;
    grid[x][y] = switchSquare;

    Switch sw = levelCreator.createSwitch(switchGroup);
    circuits.get(switchGroup).addSwitch(sw);

    sw.occupy(switchSquare);
    break;

case 'a':
case 'b':
    int gateGroup = (c == 'a') ? 1 : 2;

    Gate gate = levelCreator.createGate(gateGroup);
    circuits.get(gateGroup).addGate(gate);

    grid[x][y] = gate;
    break;
```

Agora, quando o parser encontrar um switch ou gate, ele o registra no circuito correspondente.

O `Circuit` também aplica o estado atual ao gate quando ele é registrado.

---

## 5. Quais outros arquivos serão impactados?

| Arquivo | Alteração |
|---|---|
| `Circuit.java` | Novo arquivo. Gerencia os objetos e o comando do circuito. |
| `Gate.java` | Mantém os estados físicos e o fechamento pendente. |
| `Switch.java` | Passa a delegar a ativação ao circuito. |
| `MapParser.java` | Cria os circuitos e registra os objetos. |
| `LevelFactory.java` | Pode permanecer como está, desde que seus métodos `createGate` e `createSwitch` estejam funcionando. |
| `DefaultPlayerInteractionMap.java` | Deve chamar `switch.changeState` na colisão com o jogador. |
| `Square.java` | Precisamos conferir os métodos de ocupação e remoção. |
| `Unit.java` | Precisamos conferir como uma unidade sai de um quadrado e entra em outro. |

## 6. Uma observação sobre a validação do mapa

Ainda falta uma etapa importante: verificar se cada circuito possui pelo menos um switch e um gate.

Por exemplo, se o mapa tiver um gate `a`, mas nenhum switch `1`, o circuito está incompleto.

Podemos fazer essa validação depois que o `MapParser` terminar de construir o tabuleiro, antes de retornar o `Level`.

---

## 7. Como ficará a comunicação

**Jogador colide com Switch 1**

<icon name="arrow-down" color="secondary" />

**Switch.changeState**

Delegação para Circuit 1

<icon name="arrow-down" color="secondary" />

**Circuit 1.toggle**

Alterna o comando e notifica seus gates

<icon name="arrow-down" color="secondary" />
<grid columns={2} gap={2}>
<grid-item>

**Gate A**
<badge color="success">OPEN</badge>

Abre imediatamente

</grid-item>
<grid-item>

**Gate B**
<badge color="warning">CLOSING</badge>

Aguarda ocupantes saírem

</grid-item>
</grid>

A vantagem dessa estrutura é que o `Circuit` não precisa conhecer os detalhes físicos de cada portão. Ele apenas envia o comando. Cada gate decide como reagir.

**Minha sugestão para o próximo passo:** antes de testar tudo, vamos conferir `Square.java` e `Unit.java`. São justamente as classes que vão determinar se a regra de `CLOSING` funciona corretamente, especialmente quando um fantasma ou o jogador estiver ocupando o portão no momento em que ele receber o comando de fechamento.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>The method Gate.remove(Unit) does not override the inherited method from Square since it is private to a different package</strong></p>&#10;<p style="margin:0 0 8px"><strong>apareceu esse erro no gate!</strong></p>&#10;<p style="margin:0 0 8px"><strong>e não entendo essa implementação</strong></p>&#10;<p style="margin:0 0 8px"><strong>case &#39;1&#39;:<br>case &#39;2&#39;:<br>    int switchGroup = (c == &#39;1&#39;) ? 1 : 2;</strong></p>&#10;<pre style="margin:0 0 8px;white-space:pre-wrap"><code>Square switchSquare = boardCreator.createGround;&#10;grid[x][y] = switchSquare;&#10;&#10;Switch sw = levelCreator.createSwitch(switchGroup);&#10;circuits.get(switchGroup).addSwitch(sw);&#10;&#10;sw.occupy(switchSquare);&#10;break;&#10;</code></pre>&#10;<p style="margin:0 0 8px"><strong>case &#39;a&#39;:<br>case &#39;b&#39;:<br>    int gateGroup = (c == &#39;a&#39;) ? 1 : 2;</strong></p>&#10;<pre style="margin:0 0 8px;white-space:pre-wrap"><code>Gate gate = levelCreator.createGate(gateGroup);&#10;circuits.get(gateGroup).addGate(gate);&#10;&#10;grid[x][y] = gate;&#10;break;&#10;</code></pre></td></tr></tbody></table>

Esse erro acontece porque o método `remove(Unit)` em `Square` tem acesso `private`. Em Java, uma subclasse **não pode sobrescrever um método privado** de sua classe pai. O método nem sequer é visível para `Gate`.

Vamos separar os dois problemas.

## 1. Corrigindo o erro em `Gate`

Na implementação anterior, eu sugeri algo parecido com:

```java
@Override
protected void remove(Unit occupant) {
    super.remove(occupant);

    if (state == CLOSING && getOccupants.isEmpty) {
        state = CLOSED;
    }
}
```

Isso não funciona porque `Square.remove(Unit)` é privado.

**Minha recomendação:** por enquanto, remova esse método de `Gate`. Não tente sobrescrevê-lo.

Mas há uma consequência importante: ainda não teremos o comportamento de fechamento pendente (`CLOSING`) funcionando automaticamente. Para implementá-lo corretamente, precisamos entender como o projeto controla a saída de uma unidade de uma casa.

Se você compartilhar `Square.java` e `Unit.java`, consigo indicar onde implementar isso sem quebrar a arquitetura existente.

## 2. Entendendo o trecho do `MapParser`

Esse código está tratando os símbolos do mapa e transformando cada um em um objeto do jogo.

Vamos por partes.

### Interruptores: `'1'` e `'2'`

```java
case '1':
case '2':
    int switchGroup = (c == '1') ? 1 : 2;

    Square switchSquare = boardCreator.createGround;
    grid[x][y] = switchSquare;

    Switch sw = levelCreator.createSwitch(switchGroup);
    circuits.get(switchGroup).addSwitch(sw);

    sw.occupy(switchSquare);
    break;
```

Imagine que o mapa tenha este trecho:

```text
#######
# 1   #
#######
```

Quando o parser encontra o caractere `'1'`, ele precisa criar duas coisas:

- Um chão (`Square`), que será a posição física do mapa.
- Um interruptor (`Switch`), que ficará sobre esse chão.

O código faz exatamente isso:

**1. Identifica o circuito:**

```java
int switchGroup = (c == '1') ? 1 : 2;
```

Isso é um operador ternário. Equivale a:

```java
int switchGroup;

if (c == '1') {
    switchGroup = 1;
} else {
    switchGroup = 2;
}
```

**2. Cria o chão e coloca na matriz:**

```java
Square switchSquare = boardCreator.createGround;
grid[x][y] = switchSquare;
```

A matriz `grid` representa o tabuleiro. Portanto, naquela posição haverá um chão.

**3. Cria o interruptor e o registra no circuito:**

```java
Switch sw = levelCreator.createSwitch(switchGroup);
circuits.get(switchGroup).addSwitch(sw);
```

Aqui está a parte mais importante da arquitetura:

- `createSwitch` cria o objeto.
- `circuits.get(switchGroup)` encontra o circuito correspondente.
- `addSwitch` registra o interruptor nesse circuito.

Assim, o circuito passa a conhecer seus interruptores.

**4. Coloca o interruptor sobre o chão:**

```java
sw.occupy(switchSquare);
```

Isso estabelece a relação espacial entre o `Switch` e o `Square`.

### Portões: `'a'` e `'b'`

```java
case 'a':
case 'b':
    int gateGroup = (c == 'a') ? 1 : 2;

    Gate gate = levelCreator.createGate(gateGroup);
    circuits.get(gateGroup).addGate(gate);

    grid[x][y] = gate;
    break;
```

O raciocínio é parecido, mas existe uma diferença importante: como `Gate` herda de `Square`, ele próprio ocupa uma posição na matriz.

Por isso, não precisamos criar um chão separado nem chamar `occupy`.

- `'a'` cria um portão do circuito 1.
- `'b'` cria um portão do circuito 2.
- `addGate` registra o portão no circuito.
- `grid[x][y] = gate` coloca o portão no tabuleiro.

## 3. Como as classes se relacionam

A ideia é que `Circuit` seja responsável por gerenciar seus portões e interruptores:

Relative throughput: 65%
**Circuit 1**

Gerencia o estado e os objetos associados

<icon name="arrow-down-up" color="secondary" size="lg"/>
<grid columns={2} gap={2}>
<grid-item>

**Switch 1**

Aciona o circuito

</grid-item>
<grid-item>

**Gate a**

Recebe mudanças de estado

</grid-item>
</grid>

---

Relative throughput: 65%
**Circuit 2**

Gerencia o estado e os objetos associados

<icon name="arrow-down-up" color="secondary" size="lg"/>
<grid columns={2} gap={2}>
<grid-item>

**Switch 2**

Aciona o circuito

</grid-item>
<grid-item>

**Gate b**

Recebe mudanças de estado

</grid-item>
</grid>

O fluxo fica assim:

1. `MapParser` encontra os símbolos.
2. `LevelFactory` cria os objetos.
3. `Circuit` registra os objetos correspondentes.
4. Quando o jogador colide com um `Switch`, ele solicita ao `Circuit` que altere seu estado.
5. O `Circuit` atualiza todos os seus `Gate`s.

---

**Para resolvermos a parte do `CLOSING` corretamente, envie `Square.java` e `Unit.java`.** Quero verificar como o JPacman implementa `occupy`, `leaveSquare` e a remoção de ocupantes antes de propor uma solução. Assim evitamos implementar uma lógica de colisão paralela à que o jogo já possui.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>The method Gate.remove(Unit) does not override the inherited method from Square since it is private to a different package </strong></p>&#10;<p style="margin:0 0 8px"><strong> Como me livro desse erro?</strong></p></td></tr></tbody></table>

Para se livrar desse erro, **remova o método `remove(Unit)` da classe `Gate` e também a anotação `@Override` associada a ele**.

O problema é que `Square` possui um método `remove(Unit)` com acesso `private`. Portanto, `Gate`, mesmo herdando de `Square`, não consegue sobrescrevê-lo.

Remova este trecho de `Gate.java`:

```java
@Override
protected void remove(Unit occupant) {
    super.remove(occupant);

    if (state == CLOSING && getOccupants.isEmpty) {
        state = CLOSED;
    }
}
```

**Atenção:** isso elimina o erro de compilação, mas também deixa de implementar o fechamento automático do portão quando seu último ocupante sair.

Se você quiser manter esse comportamento, precisamos implementar a lógica em outro ponto: provavelmente no método `leaveSquare` de `Unit`, ou em algum mecanismo de atualização da ocupação de `Square`.

Por enquanto, remova o método para voltar a compilar. Não basta trocar `protected` por `public`: o método privado da classe pai continua não podendo ser sobrescrito.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>como altero o método em square para manter a implementação funcional?</strong></p></td></tr></tbody></table>

Você pode alterar o método em `Square` de `private` para `protected` (ou `public`). Assim, `Gate` poderá sobrescrevê-lo usando `@Override`.

A alteração seria:

**Em `Square.java`, procure algo semelhante a:**

```java
private void remove(Unit unit) {
    // implementação atual
}
```

Altere somente o modificador de acesso:

```java
protected void remove(Unit unit) {
    // mantenha a implementação original
}
```

**Em `Gate.java`,** você poderá manter a sobrescrita:

```java
@Override
protected void remove(Unit occupant) {
    super.remove(occupant);

    if (state == CLOSING && getOccupants.isEmpty) {
        state = CLOSED;
    }
}
```

O motivo é que `Gate` está em outro pacote (`level`), enquanto `Square` está em `board`. O modificador `protected` permite que uma subclasse acesse o método mesmo estando em outro pacote.

**Importante:** mantenha o corpo original de `remove` em `Square`. Altere apenas `private` para `protected`.

Depois, em `Gate`, o `super.remove(occupant)` poderá chamar a implementação original de `Square`, e o código verificará se o portão ficou vazio para concluir o fechamento.

Se aparecer outro erro, envie o método `remove` completo de `Square.java` e o trecho atualizado de `Gate.java`. Assim podemos verificar se a visibilidade de `getOccupants` também permite essa implementação.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>Fiz mais algumas modificações para acomodar os outros métodos à nova classe circuits e consegui fazer o feature 1 funcionar, mas o player ainda não colide mesmo tendo regra para colisão.</strong></p>&#10;<p style="margin:0 0 8px"><strong>segue o package nl.tudelft.jpacman.level;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import java.util.<em>List</em>;<br>import java.util.<em>Map</em>;<br>import java.util.Optional;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.board.Board;<br>import nl.tudelft.jpacman.board.<em>Direction</em>;<br>import nl.tudelft.jpacman.board.Square;<br>import nl.tudelft.jpacman.npc.Ghost;<br>import nl.tudelft.jpacman.npc.ghost.<em>GhostColor</em>;<br>import nl.tudelft.jpacman.npc.ghost.GhostFactory;<br>import nl.tudelft.jpacman.points.<em>PointCalculator</em>;<br>import nl.tudelft.jpacman.sprite.PacManSprites;<br>import nl.tudelft.jpacman.sprite.<em>Sprite</em>;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* Factory that creates levels and units.<br>&#x20;*<br>&#x20;* @author Jeroen Roosen&#x20;<br>&#x20;*/<br>public class LevelFactory {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private static final <em>int</em> GHOSTS = 4;<br>&#x20;   private static final <em>int</em> BLINKY = 0;<br>&#x20;   private static final <em>int</em> INKY = 1;<br>&#x20;   private static final <em>int</em> PINKY = 2;<br>&#x20;   private static final <em>int</em> CLYDE = 3;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The default value of a pellet.<br>&#x20;    */<br>&#x20;   private static final <em>int</em> PELLET_VALUE = 10;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The sprite store that provides sprites for units.<br>&#x20;    */<br>&#x20;   private final PacManSprites sprites;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Used to cycle through the various ghost types.<br>&#x20;    */<br>&#x20;   private <em>int</em> ghostIndex;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The factory providing ghosts.<br>&#x20;    */<br>&#x20;   private final GhostFactory ghostFact;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The way to calculate points upon collisions.<br>&#x20;    */<br>&#x20;   private final <em>PointCalculator</em> pointCalculator;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new level factory.<br>&#x20;    *<br>&#x20;    * @param <em>spriteStore</em><br>&#x20;    *            The sprite store providing the sprites for units.<br>&#x20;    * @param <em>ghostFactory</em><br>&#x20;    *            The factory providing ghosts.<br>&#x20;    * @param <em>pointCalculator</em><br>&#x20;    *            The algorithm to calculate the points.<br>&#x20;    */<br>&#x20;   public LevelFactory(PacManSprites <em>spriteStore</em>,<br>&#x20;                       GhostFactory <em>ghostFactory</em>,<br><em>PointCalculator</em> <em>pointCalculator</em>) {<br><em>this</em>.sprites = <em>spriteStore</em>;<br><em>this</em>.ghostIndex = -1;<br><em>this</em>.ghostFact = <em>ghostFactory</em>;<br><em>this</em>.pointCalculator = <em>pointCalculator</em>;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new level from the provided data.<br>&#x20;    *<br>&#x20;    * @param <em>board</em><br>&#x20;    *            The board with all ghosts and pellets occupying their squares.<br>&#x20;    * @param <em>ghosts</em><br>&#x20;    *            A list of all ghosts on the board.<br>&#x20;    * @param <em>startPositions</em><br>&#x20;    *            A list of squares from which players may start the game.<br>&#x20;    * @return A new level for the board.<br>&#x20;    */<br>&#x20;   public Level createLevel(Board <em>board</em>, <em>List</em>&lt;Ghost&gt; <em>ghosts</em>, <em>List</em>&lt;Square&gt; <em>startPositions</em>, <em>List</em>&lt;Circuit&gt; <em>circuits</em>) {<br><em>CollisionMap</em> collisionMap = <strong>new</strong> PlayerCollisions(pointCalculator);<br>&#x20;       return <strong>new</strong> Level(<em>board</em>, <em>ghosts</em>, <em>startPositions</em>, collisionMap, <em>circuits</em>);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new ghost.<br>&#x20;    *<br>&#x20;    * @return The new ghost.<br>&#x20;    */<br>&#x20;   Ghost createGhost {<br>&#x20;       ghostIndex++;<br>&#x20;       ghostIndex %= GHOSTS;<br>&#x20;       switch (ghostIndex) {<br>&#x20;           case BLINKY:<br>&#x20;               return ghostFact.createBlinky;<br>&#x20;           case INKY:<br>&#x20;               return ghostFact.createInky;<br>&#x20;           case PINKY:<br>&#x20;               return ghostFact.createPinky;<br>&#x20;           case CLYDE:<br>&#x20;               return ghostFact.createClyde;<br>&#x20;           default:<br>&#x20;               return <strong>new</strong> RandomGhost(sprites.getGhostSprite(<em>GhostColor</em>.RED));<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new pellet.<br>&#x20;    *<br>&#x20;    * @return The new pellet.<br>&#x20;    */<br>&#x20;   public Pellet createPellet {<br>&#x20;       return <strong>new</strong> Pellet(PELLET_VALUE, sprites.getPelletSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new gate.<br>&#x20;    *<br>&#x20;    * @return The new gate.<br>&#x20;    */<br>&#x20;   public Gate createGate(<em>int</em> <em>group</em>) {<br>&#x20;       return <strong>new</strong> Gate(<em>group</em>, sprites.getGateSprite, sprites.getGroundSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new switch.<br>&#x20;    *<br>&#x20;    * @return The new switch.<br>&#x20;    */<br>&#x20;   public Switch createSwitch(<em>int</em> <em>group</em>) {<br>&#x20;       return <strong>new</strong> Switch(<em>group</em>, sprites.getSwitchSprite);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Implementation of an NPC that wanders around randomly.<br>&#x20;    *<br>&#x20;    * @author Jeroen Roosen<br>&#x20;    */<br>&#x20;   private static final class RandomGhost extends Ghost {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * The suggested delay between moves.<br>&#x20;        */<br>&#x20;       private static final <em>long</em> DELAY = 175L;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * Creates a new random ghost.<br>&#x20;        *<br>&#x20;        * @param <em>ghostSprite</em><br>&#x20;        *            The sprite for the ghost.<br>&#x20;        */<br>&#x20;       RandomGhost(<em>Map</em>&lt;*Direction*, *Sprite*&gt; <em>ghostSprite</em>) {<br><em>super</em>(<em>ghostSprite</em>, (<em>int</em>) DELAY, 0);<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       @Override<br>&#x20;       public Optional&lt;*Direction*&gt; nextAiMove {<br>&#x20;           return Optional.empty;<br>&#x20;       }<br>&#x20;   }<br>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>e o package nl.tudelft.jpacman.level;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.board.Unit;<br>import nl.tudelft.jpacman.npc.Ghost;<br>import nl.tudelft.jpacman.points.<em>PointCalculator</em>;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* A simple implementation of a collision map for the JPacman player.<br>&#x20;* &lt;p&gt;<br>&#x20;* It uses a number of instanceof checks to implement the multiple dispatch for the&#x20;<br>&#x20;* collisionmap. For more realistic collision maps, this approach will not scale,<br>&#x20;* and the recommended approach is to use a {@link CollisionInteractionMap}.<br>&#x20;*<br>&#x20;* @author Arie van Deursen, 2014<br>&#x20;*<br>&#x20;*/</strong></p>&#10;<p style="margin:0 0 8px"><strong>public class PlayerCollisions implements <em>CollisionMap</em> {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>PointCalculator</em> pointCalculator;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Create a simple player-based collision map, informing the<br>&#x20;    * point calculator about points to be added.<br>&#x20;    *<br>&#x20;    * @param <em>pointCalculator</em><br>&#x20;    *             Strategy for calculating points.<br>&#x20;    */<br>&#x20;   public PlayerCollisions(<em>PointCalculator</em> <em>pointCalculator</em>) {<br><em>this</em>.pointCalculator = <em>pointCalculator</em>;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   @Override<br>&#x20;   public <em>void</em> collide(Unit <em>mover</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>mover</em> instanceof Player) {<br>&#x20;           playerColliding((Player) <em>mover</em>, <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;       else if (<em>mover</em> instanceof Ghost) {<br>&#x20;           ghostColliding((Ghost) <em>mover</em>, <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;       else if (<em>mover</em> instanceof Pellet) {<br>&#x20;           pelletColliding((Pellet) <em>mover</em>, <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>void</em> playerColliding(Player <em>player</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>collidedOn</em> instanceof Ghost) {<br>&#x20;           playerVersusGhost(<em>player</em>, (Ghost) <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;       if (<em>collidedOn</em> instanceof Pellet) {<br>&#x20;           playerVersusPellet(<em>player</em>, (Pellet) <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>void</em> ghostColliding(Ghost <em>ghost</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>collidedOn</em> instanceof Player) {<br>&#x20;           playerVersusGhost((Player) <em>collidedOn</em>, <em>ghost</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>void</em> pelletColliding(Pellet <em>pellet</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>collidedOn</em> instanceof Player) {<br>&#x20;           playerVersusPellet((Player) <em>collidedOn</em>, <em>pellet</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Actual case of player bumping into ghost or vice versa.<br>&#x20;    *<br>&#x20;    * @param <em>player</em><br>&#x20;    *          The player involved in the collision.<br>&#x20;    * @param <em>ghost</em><br>&#x20;    *          The ghost involved in the collision.<br>&#x20;    */<br>&#x20;   public <em>void</em> playerVersusGhost(Player <em>player</em>, Ghost <em>ghost</em>) {<br>&#x20;       pointCalculator.collidedWithAGhost(<em>player</em>, <em>ghost</em>);<br><em>player</em>.setAlive(false);<br><em>player</em>.setKiller(<em>ghost</em>);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Actual case of player consuming a pellet.<br>&#x20;    *<br>&#x20;    * @param <em>player</em><br>&#x20;    *           The player involved in the collision.<br>&#x20;    * @param <em>pellet</em><br>&#x20;    *           The pellet involved in the collision.<br>&#x20;    */<br>&#x20;   public <em>void</em> playerVersusPellet(Player <em>player</em>, Pellet <em>pellet</em>) {<br>&#x20;       pointCalculator.consumedAPellet(<em>player</em>, <em>pellet</em>);<br><em>pellet</em>.leaveSquare;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>e ainda</strong></p>&#10;<p style="margin:0 0 8px"><strong>package nl.tudelft.jpacman.level;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import java.util.ArrayList;<br>import java.util.HashMap;<br>import java.util.<em>List</em>;<br>import java.util.<em>Map</em>;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.board.Unit;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* A map of possible collisions and their handlers.<br>&#x20;*<br>&#x20;* @author Michael de Jong<br>&#x20;* @author Jeroen Roosen&#x20;<br>&#x20;*/<br>public class CollisionInteractionMap implements <em>CollisionMap</em> {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * The collection of collision handlers.<br>&#x20;    */<br>&#x20;   private final <em>Map</em>&lt;Class&lt;*?* extends Unit&gt;,<br><em>Map</em>&lt;Class&lt;*?* extends Unit&gt;, <em>CollisionHandler</em>&lt;*?*, *?*&gt;&gt;&gt; handlers;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Creates a new, empty collision map.<br>&#x20;    */<br>&#x20;   public CollisionInteractionMap {<br><em>this</em>.handlers = <strong>new</strong> HashMap&lt;&gt;;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Adds a two-way collision interaction to this collection, i.e. the<br>&#x20;    * collision handler will be used for both C1 versus C2 and C2 versus C1.<br>&#x20;    *<br>&#x20;    * @param <em>&lt;C1&gt;</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>&lt;C2&gt;</em><br>&#x20;    *            The collidee (unit that was moved into) type.<br>&#x20;    *<br>&#x20;    * @param <em>collider</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>collidee</em><br>&#x20;    *            The collidee type.<br>&#x20;    * @param <em>handler</em><br>&#x20;    *            The handler that handles the collision.<br>&#x20;    */<br>&#x20;   public &lt;*C1* extends Unit, *C2* extends Unit&gt; <em>void</em> onCollision(<br>&#x20;       Class&lt;*C1*&gt; <em>collider</em>, Class&lt;*C2*&gt; <em>collidee</em>, <em>CollisionHandler</em>&lt;*C1*, *C2*&gt; <em>handler</em>) {<br>&#x20;       onCollision(<em>collider</em>, <em>collidee</em>, true, <em>handler</em>);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Adds a collision interaction to this collection.<br>&#x20;    *<br>&#x20;    * @param <em>&lt;C1&gt;</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>&lt;C2&gt;</em><br>&#x20;    *            The collidee (unit that was moved into) type.<br>&#x20;    *<br>&#x20;    * @param <em>collider</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>collidee</em><br>&#x20;    *            The collidee type.<br>&#x20;    * @param <em>symetric</em><br>&#x20;    *            &lt;code&gt;true&lt;/code&gt; if this collision is used for both<br>&#x20;    *            C1 against C2 and vice versa;<br>&#x20;    *            &lt;code&gt;false&lt;/code&gt; if only for C1 against C2.<br>&#x20;    * @param <em>handler</em><br>&#x20;    *            The handler that handles the collision.<br>&#x20;    */<br>&#x20;   public &lt;*C1* extends Unit, *C2* extends Unit&gt; <em>void</em> onCollision(<br>&#x20;       Class&lt;*C1*&gt; <em>collider</em>, Class&lt;*C2*&gt; <em>collidee</em>, <em>boolean</em> <em>symetric</em>,<br><em>CollisionHandler</em>&lt;*C1*, *C2*&gt; <em>handler</em>) {<br>&#x20;       addHandler(<em>collider</em>, <em>collidee</em>, <em>handler</em>);<br>&#x20;       if (<em>symetric</em>) {<br>&#x20;           addHandler(<em>collidee</em>, <em>collider</em>, <strong>new</strong> InverseCollisionHandler&lt;&gt;(<em>handler</em>));<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Adds the collision interaction..<br>&#x20;    *<br>&#x20;    * @param <em>collider</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>collidee</em><br>&#x20;    *            The collidee type.<br>&#x20;    * @param <em>handler</em><br>&#x20;    *            The handler that handles the collision.<br>&#x20;    */<br>&#x20;   private <em>void</em> addHandler(Class&lt;*?* extends Unit&gt; <em>collider</em>,<br>&#x20;                           Class&lt;*?* extends Unit&gt; <em>collidee</em>, <em>CollisionHandler</em>&lt;*?*, *?*&gt; <em>handler</em>) {<br>&#x20;       if (!handlers.containsKey(<em>collider</em>)) {<br>&#x20;           handlers.put(<em>collider</em>, <strong>new</strong> HashMap&lt;&gt;);<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Map</em>&lt;Class&lt;*?* extends Unit&gt;, <em>CollisionHandler</em>&lt;*?*, *?*&gt;&gt; map = handlers.get(<em>collider</em>);<br>&#x20;       map.put(<em>collidee</em>, <em>handler</em>);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Handles the collision between two colliding parties, if a suitable<br>&#x20;    * collision handler is listed.<br>&#x20;    *<br>&#x20;    * @param <em>&lt;C1&gt;</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>&lt;C2&gt;</em><br>&#x20;    *            The collidee (unit that was moved into) type.<br>&#x20;    *<br>&#x20;    * @param <em>collider</em><br>&#x20;    *            The collider.<br>&#x20;    * @param <em>collidee</em><br>&#x20;    *            The collidee.<br>&#x20;    */<br>&#x20;   @SuppressWarnings(&quot;unchecked&quot;)<br>&#x20;   @Override<br>&#x20;   public &lt;*C1* extends Unit, *C2* extends Unit&gt; <em>void</em> collide(<em>C1</em> <em>collider</em>,<br><em>C2</em> <em>collidee</em>) {<br>&#x20;       Class&lt;*?* extends Unit&gt; colliderKey = getMostSpecificClass(handlers, <em>collider</em>.getClass);<br>&#x20;       if (colliderKey == null) {<br>&#x20;           return;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>Map</em>&lt;Class&lt;*?* extends Unit&gt;, <em>CollisionHandler</em>&lt;*?*, *?*&gt;&gt; map = handlers.get(colliderKey);<br>&#x20;       Class&lt;*?* extends Unit&gt; collideeKey = getMostSpecificClass(map, <em>collidee</em>.getClass);<br>&#x20;       if (collideeKey == null) {<br>&#x20;           return;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>CollisionHandler</em>&lt;*C1*, *C2*&gt; collisionHandler = (<em>CollisionHandler</em>&lt;*C1*, *C2*&gt;) map.get(collideeKey);<br>&#x20;       if (collisionHandler == null) {<br>&#x20;           return;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       collisionHandler.handleCollision(<em>collider</em>, <em>collidee</em>);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Figures out the most specific class that is listed in the map. I.e. if A<br>&#x20;    * extends B and B is listed while requesting A, then B will be returned.<br>&#x20;    *<br>&#x20;    * @param <em>map</em><br>&#x20;    *            The map with the key collection to find a matching class in.<br>&#x20;    * @param <em>key</em><br>&#x20;    *            The class to search the most suitable key for.<br>&#x20;    * @return The most specific class from the key collection.<br>&#x20;    */<br>&#x20;   private Class&lt;*?* extends Unit&gt; getMostSpecificClass(<br><em>Map</em>&lt;Class&lt;*?* extends Unit&gt;, <em>?</em>&gt; <em>map</em>, Class&lt;*?* extends Unit&gt; <em>key</em>) {<br><em>List</em>&lt;Class&lt;*?* extends Unit&gt;&gt; collideeInheritance = getInheritance(<em>key</em>);<br>&#x20;       for (Class&lt;*?* extends Unit&gt; pointer : collideeInheritance) {<br>&#x20;           if (<em>map</em>.containsKey(pointer)) {<br>&#x20;               return pointer;<br>&#x20;           }<br>&#x20;       }<br>&#x20;       return null;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Returns a list of all classes and interfaces the class inherits.<br>&#x20;    *<br>&#x20;    * @param <em>clazz</em><br>&#x20;    *            The class to create a list of super classes and interfaces<br>&#x20;    *            for.<br>&#x20;    * @return A list of all classes and interfaces the class inherits.<br>&#x20;    */<br>&#x20;   @SuppressWarnings(&quot;unchecked&quot;)<br>&#x20;   private <em>List</em>&lt;Class&lt;*?* extends Unit&gt;&gt; getInheritance(<br>&#x20;       Class&lt;*?* extends Unit&gt; <em>clazz</em>) {<br><em>List</em>&lt;Class&lt;*?* extends Unit&gt;&gt; found = <strong>new</strong> ArrayList&lt;&gt;;<br>&#x20;       found.add(<em>clazz</em>);</strong></p>&#10;<p style="margin:0 0 8px"><strong><em>int</em> index = 0;<br>&#x20;       while (found.size &gt; index) {<br>&#x20;           Class&lt;*?*&gt; current = found.get(index);<br>&#x20;           Class&lt;*?*&gt; superClass = current.getSuperclass;<br>&#x20;           if (superClass != null &amp;&amp; Unit.class.isAssignableFrom(superClass)) {<br>&#x20;               found.add((Class&lt;? extends Unit&gt;) superClass);<br>&#x20;           }<br>&#x20;           for (Class&lt;*?*&gt; classInterface : current.getInterfaces) {<br>&#x20;               if (Unit.class.isAssignableFrom(classInterface)) {<br>&#x20;                   found.add((Class&lt;? extends Unit&gt;) classInterface);<br>&#x20;               }<br>&#x20;           }<br>&#x20;           index++;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       return found;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Handles the collision between two colliding parties.<br>&#x20;    *<br>&#x20;    * @author Michael de Jong<br>&#x20;    *<br>&#x20;    * @param <em>&lt;C1&gt;</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>&lt;C2&gt;</em><br>&#x20;    *            The collidee type.<br>&#x20;    */<br>&#x20;   public interface <em>CollisionHandler</em>&lt;*C1* extends Unit, *C2* extends Unit&gt; {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * Handles the collision between two colliding parties.<br>&#x20;        *<br>&#x20;        * @param <em>collider</em><br>&#x20;        *            The collider.<br>&#x20;        * @param <em>collidee</em><br>&#x20;        *            The collidee.<br>&#x20;        */<br><em>void</em> handleCollision(<em>C1</em> <em>collider</em>, <em>C2</em> <em>collidee</em>);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * An symmetrical copy of a collision hander.<br>&#x20;    *<br>&#x20;    * @author Michael de Jong<br>&#x20;    *<br>&#x20;    * @param <em>&lt;C1&gt;</em><br>&#x20;    *            The collider type.<br>&#x20;    * @param <em>&lt;C2&gt;</em><br>&#x20;    *            The collidee type.<br>&#x20;    */<br>&#x20;   private static class InverseCollisionHandler&lt;*C1* extends Unit, *C2* extends Unit&gt;<br>&#x20;       implements <em>CollisionHandler</em>&lt;*C1*, *C2*&gt; {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * The handler of this collision.<br>&#x20;        */<br>&#x20;       private final <em>CollisionHandler</em>&lt;*C2*, *C1*&gt; handler;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * Creates a new collision handler.<br>&#x20;        *<br>&#x20;        * @param <em>handler</em><br>&#x20;        *            The symmetric handler for this collision.<br>&#x20;        */<br>&#x20;       InverseCollisionHandler(<em>CollisionHandler</em>&lt;*C2*, *C1*&gt; <em>handler</em>) {<br><em>this</em>.handler = <em>handler</em>;<br>&#x20;       }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;       /**<br>&#x20;        * Handles this collision by flipping the collider and collidee, making<br>&#x20;        * it compatible with the initial collision.<br>&#x20;        */<br>&#x20;       @Override<br>&#x20;       public <em>void</em> handleCollision(<em>C1</em> <em>collider</em>, <em>C2</em> <em>collidee</em>) {<br>&#x20;           handler.handleCollision(<em>collidee</em>, <em>collider</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>o que deveria fazer?</strong></p></td></tr></tbody></table>

Olhando os códigos, encontrei um problema bastante provável: **sua classe `PlayerCollisions` não possui nenhuma regra para colisão entre `Player` e `Switch`**.

Você implementou as regras para `Ghost` e `Pellet`, mas não para `Switch`. Como o `LevelFactory.createLevel` está instanciando `PlayerCollisions`, é nessa classe que precisamos adicionar o tratamento.

Além disso, há um detalhe importante: o sistema de colisões do JPacman diferencia quem está se movendo (`mover`) de quem está sendo atingido (`collidedOn`). Vamos acompanhar esse fluxo.

## 1. Adicione a colisão com Switch

Na classe `PlayerCollisions`, dentro de `playerColliding`, adicione:

```java
if (collidedOn instanceof Switch) {
    playerVersusSwitch(player, (Switch) collidedOn);
}
```

Depois, implemente o método:

```java
public void playerVersusSwitch(Player player, Switch sw) {
    sw.changeState;
}
```

Sua classe `playerColliding` ficará assim:

```java
private void playerColliding(Player player, Unit collidedOn) {
    if (collidedOn instanceof Ghost) {
        playerVersusGhost(player, (Ghost) collidedOn);
    }

    if (collidedOn instanceof Pellet) {
        playerVersusPellet(player, (Pellet) collidedOn);
    }

    if (collidedOn instanceof Switch) {
        playerVersusSwitch(player, (Switch) collidedOn);
    }
}
```

Isso permite que o jogador ative o interruptor ao colidir com ele.

Não precisa adicionar uma regra para `Switch` em `ghostColliding` ou `pelletColliding`. O jogador já tem um método próprio para tratar suas colisões.

## 2. Por que isso deve funcionar?

O fluxo esperado é:

1. O jogador tenta se mover para uma casa.
2. O `Level` verifica quais unidades estão ocupando essa casa.
3. Se encontrar um `Switch`, chama `PlayerCollisions.collide(player, switch)`.
4. `collide` identifica que o objeto que se moveu é um `Player`.
5. `playerColliding` identifica o `Switch` e chama `playerVersusSwitch`.
6. `sw.changeState` aciona o circuito associado.

Como seu `LevelFactory` já instancia `PlayerCollisions`, não precisa alterar essa parte:

```java
public Level createLevel(...) {
    CollisionMap collisionMap = new PlayerCollisions(pointCalculator);
    return new Level(board, ghosts, startPositions, collisionMap, circuits);
}
```

## 3. E se ainda não funcionar?

Se, mesmo adicionando a regra, o interruptor não for acionado, eu investigaria estes três pontos:

- **`Switch.java`:** o interruptor está realmente sendo associado ao `Circuit`? O método `changeState` precisa chamar o circuito.
- **`MapParser.java`:** você está chamando `sw.occupy(switchSquare)`? Isso é essencial para que o interruptor seja registrado como ocupante da casa.
- **`Level.java`:** o método responsável pela movimentação está chamando `collisionMap.collide` para os ocupantes da casa de destino?

Uma observação: se você está falando da colisão com o **Gate**, o mecanismo é diferente. Como `Gate extends Square`, ele não participa diretamente do `PlayerCollisions`, que trabalha com objetos `Unit`. Nesse caso, quem controla a passagem é `Gate.isAccessibleTo`.

**Minha sugestão:** adicione primeiro a regra de `Switch` e teste. Se continuar sem funcionar, envie `Level.java`, `Square.java` e `Unit.java`. Com esses três arquivos conseguimos rastrear exatamente o caminho da movimentação e descobrir onde a colisão está deixando de acontecer.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>package nl.tudelft.jpacman.level;</strong></p>&#10;<p style="margin:0 0 8px"><strong>import nl.tudelft.jpacman.board.Unit;<br>import nl.tudelft.jpacman.npc.Ghost;<br>import nl.tudelft.jpacman.points.<em>PointCalculator</em>;</strong></p>&#10;<p style="margin:0 0 8px"><strong>/**<br>&#x20;* A simple implementation of a collision map for the JPacman player.<br>&#x20;* &lt;p&gt;<br>&#x20;* It uses a number of instanceof checks to implement the multiple dispatch for the&#x20;<br>&#x20;* collisionmap. For more realistic collision maps, this approach will not scale,<br>&#x20;* and the recommended approach is to use a {@link CollisionInteractionMap}.<br>&#x20;*<br>&#x20;* @author Arie van Deursen, 2014<br>&#x20;*<br>&#x20;*/</strong></p>&#10;<p style="margin:0 0 8px"><strong>public class PlayerCollisions implements <em>CollisionMap</em> {</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>PointCalculator</em> pointCalculator;</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Create a simple player-based collision map, informing the<br>&#x20;    * point calculator about points to be added.<br>&#x20;    *<br>&#x20;    * @param <em>pointCalculator</em><br>&#x20;    *             Strategy for calculating points.<br>&#x20;    */<br>&#x20;   public PlayerCollisions(<em>PointCalculator</em> <em>pointCalculator</em>) {<br><em>this</em>.pointCalculator = <em>pointCalculator</em>;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   @Override<br>&#x20;   public <em>void</em> collide(Unit <em>mover</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>mover</em> instanceof Player) {<br>&#x20;           playerColliding((Player) <em>mover</em>, <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;       else if (<em>mover</em> instanceof Ghost) {<br>&#x20;           ghostColliding((Ghost) <em>mover</em>, <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;       else if (<em>mover</em> instanceof Pellet) {<br>&#x20;           pelletColliding((Pellet) <em>mover</em>, <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>void</em> playerColliding(Player <em>player</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>collidedOn</em> instanceof Ghost) {<br>&#x20;           playerVersusGhost(<em>player</em>, (Ghost) <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;       if (<em>collidedOn</em> instanceof Pellet) {<br>&#x20;           playerVersusPellet(<em>player</em>, (Pellet) <em>collidedOn</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>void</em> ghostColliding(Ghost <em>ghost</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>collidedOn</em> instanceof Player) {<br>&#x20;           playerVersusGhost((Player) <em>collidedOn</em>, <em>ghost</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   private <em>void</em> pelletColliding(Pellet <em>pellet</em>, Unit <em>collidedOn</em>) {<br>&#x20;       if (<em>collidedOn</em> instanceof Player) {<br>&#x20;           playerVersusPellet((Player) <em>collidedOn</em>, <em>pellet</em>);<br>&#x20;       }<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Actual case of player bumping into ghost or vice versa.<br>&#x20;    *<br>&#x20;    * @param <em>player</em><br>&#x20;    *          The player involved in the collision.<br>&#x20;    * @param <em>ghost</em><br>&#x20;    *          The ghost involved in the collision.<br>&#x20;    */<br>&#x20;   public <em>void</em> playerVersusGhost(Player <em>player</em>, Ghost <em>ghost</em>) {<br>&#x20;       pointCalculator.collidedWithAGhost(<em>player</em>, <em>ghost</em>);<br><em>player</em>.setAlive(false);<br><em>player</em>.setKiller(<em>ghost</em>);<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>&#x20;   /**<br>&#x20;    * Actual case of player consuming a pellet.<br>&#x20;    *<br>&#x20;    * @param <em>player</em><br>&#x20;    *           The player involved in the collision.<br>&#x20;    * @param <em>pellet</em><br>&#x20;    *           The pellet involved in the collision.<br>&#x20;    */<br>&#x20;   public <em>void</em> playerVersusPellet(Player <em>player</em>, Pellet <em>pellet</em>) {<br>&#x20;       pointCalculator.consumedAPellet(<em>player</em>, <em>pellet</em>);<br><em>pellet</em>.leaveSquare;<br>&#x20;   }</strong></p>&#10;<p style="margin:0 0 8px"><strong>}</strong></p>&#10;<p style="margin:0 0 8px"><strong>coloque o código modificado direto no arquivo, me devolva o arquivo por completo com as modificações</strong></p></td></tr></tbody></table>

Aqui está o arquivo `PlayerCollisions.java` completo, com a regra de colisão entre `Player` e `Switch` adicionada.

Também incluí o método `playerVersusSwitch`, responsável por chamar `changeState` no interruptor.

<CodeBlock language="java" editable>
package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.points.PointCalculator;

/**
 * A simple implementation of a collision map for the JPacman player.
 * <p>
 * It uses a number of instanceof checks to implement the multiple dispatch for the
 * collisionmap. For more realistic collision maps, this approach will not scale,
 * and the recommended approach is to use a {@link CollisionInteractionMap}.
 *
 * @author Arie van Deursen, 2014
 */
public class PlayerCollisions implements CollisionMap {

    private PointCalculator pointCalculator;

    /**
     * Create a simple player-based collision map, informing the
     * point calculator about points to be added.
     *
     * @param pointCalculator
     *             Strategy for calculating points.
     */
    public PlayerCollisions(PointCalculator pointCalculator) {
        this.pointCalculator = pointCalculator;
    }

    @Override
    public void collide(Unit mover, Unit collidedOn) {
        if (mover instanceof Player) {
            playerColliding((Player) mover, collidedOn);
        }
        else if (mover instanceof Ghost) {
            ghostColliding((Ghost) mover, collidedOn);
        }
        else if (mover instanceof Pellet) {
            pelletColliding((Pellet) mover, collidedOn);
        }
    }

    private void playerColliding(Player player, Unit collidedOn) {
        if (collidedOn instanceof Ghost) {
            playerVersusGhost(player, (Ghost) collidedOn);
        }
        if (collidedOn instanceof Pellet) {
            playerVersusPellet(player, (Pellet) collidedOn);
        }
        if (collidedOn instanceof Switch) {
            playerVersusSwitch(player, (Switch) collidedOn);
        }
    }

    private void ghostColliding(Ghost ghost, Unit collidedOn) {
        if (collidedOn instanceof Player) {
            playerVersusGhost((Player) collidedOn, ghost);
        }
    }

    private void pelletColliding(Pellet pellet, Unit collidedOn) {
        if (collidedOn instanceof Player) {
            playerVersusPellet((Player) collidedOn, pellet);
        }
    }

    /**
     * Actual case of player bumping into ghost or vice versa.
     *
     * @param player
     *          The player involved in the collision.
     * @param ghost
     *          The ghost involved in the collision.
     */
    public void playerVersusGhost(Player player, Ghost ghost) {
        pointCalculator.collidedWithAGhost(player, ghost);
        player.setAlive(false);
        player.setKiller(ghost);
    }

    /**
     * Actual case of player consuming a pellet.
     *
     * @param player
     *           The player involved in the collision.
     * @param pellet
     *           The pellet involved in the collision.
     */
    public void playerVersusPellet(Player player, Pellet pellet) {
        pointCalculator.consumedAPellet(player, pellet);
        pellet.leaveSquare;
    }

    /**
     * Actual case of player activating a switch.
     *
     * @param player
     *           The player activating the switch.
     * @param sw
     *           The switch involved in the collision.
     */
    public void playerVersusSwitch(Player player, Switch sw) {
        sw.changeState;
    }
}
</CodeBlock>

**O que foi modificado:**
- Adicionado o tratamento `collidedOn instanceof Switch` em `playerColliding`.
- Criado `playerVersusSwitch`, que chama `sw.changeState`.
- Mantive as regras existentes para fantasmas e pellets.
- Não adicionei tratamento de colisão de fantasmas com interruptores: somente o jogador os ativa.

**Atenção:** isso pressupõe que `Switch.changeState` já esteja associado ao `Circuit` correspondente, como implementamos anteriormente.

<table style="border-collapse:collapse;border:0;width:auto;max-width:100%;margin:0 0 16px"><tbody><tr><td style="border:0;border-radius:8px;padding:10px 16px 2px;text-align:left;background:rgba(59,130,246,.15)"><p style="margin:0 0 8px"><strong>funcionou!! agora a feature 1 está implementada e funcionando</strong></p></td></tr></tbody></table>

Boa!! Parabéns! 🎉

Você já conseguiu integrar várias partes importantes do JPacman:

- `Gate` e `Switch` como novos elementos do jogo.
- `Circuit` para gerenciar a relação entre interruptores e portões.
- `MapParser` reconhecendo os novos símbolos.
- `PlayerCollisions` tratando a ativação dos interruptores.
- Alteração do estado dos portões a partir do circuito.

E o mais importante: tudo isso está funcionando integrado ao jogo!

**Minha sugestão antes de partir para a próxima feature:** faça um commit dessa versão funcional no Git. Assim você terá um ponto de recuperação caso as próximas alterações introduzam algum problema.

Também vale registrar quais testes manuais você já realizou.

Quando quiser, podemos partir para a feature 2, mantendo a mesma abordagem: entender a arquitetura existente, identificar os pontos de extensão e implementar sem criar dependências desnecessárias. 🚀
