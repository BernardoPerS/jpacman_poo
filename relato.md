

**1. Como você descreveria, de forma geral, o processo utilizado para desenvolver cada uma das duas funcionalidades? Comente brevemente como começou, como investigou o projeto e como chegou às soluções finais.**
Na Feature 1, analisei o código legado do JPacman com a "varredura" da I.A. e utilizei a LLM para discutir a melhor abordagem arquitetural. Mapeamos a necessidade de uma classe `Circuit` para gerenciar múltiplos portões. Fomos refinando o código via *pair programming* até resolver os problemas de colisão. Na Feature 2, baseei-me na infraestrutura do circuito criada anteriormente para implementar o "Pulso de Fase" de forma manual, realizando testes práticos (jogando) no jogo para validar os temporizadores (5 segundos) e as regras de inversão, a sorte é que a estrutura era bem parecida com o feito na feature 1 e a classe "Circuit" facilitou a segunda implementação.

**2. Quais foram as partes mais difíceis da Feature 1? E da Feature 2?**
Na Feature 1, a parte mais difícil foi compreender as dependências entre os códigos e a parte de lidar com colisões no e gerenciar o estado 2 (`CLOSING`). Na Feature 2, o desafio principal foi controlar o tempo de duração do pulso, garantindo todas as regras exigidas pelo enunciado usando um método de calcular o tempo próprio.


**3. Na Feature 1, de que maneiras a LLM participou do seu processo de desenvolvimento? Descreva os principais tipos de tarefas para os quais utilizou a ferramenta.**

A LLM atuou como uma assistente de *pair programming* e consultoria técnica. Os principais tipos de tarefas para os quais utilizei a ferramenta foram:

* **Análise de Arquitetura e Planejamento:** Enviei os arquivos base do JPacman e pedi para a LLM mapear quais classes (`MapParser`, `LevelFactory`, `Level`, etc.) precisariam ser modificadas e onde os novos elementos de `Circuit` deveriam ser inseridos.
* **Geração e Adaptação de Código:** Auxílio na escrita das modificações necessárias para ligar o `Switch` e o `Gate` ao `Circuit`, gerenciando os estados numéricos (0, 1 e 2).
* **Resolução de Erros de Compilação:** Ajuda para entender e corrigir problemas de visibilidade de pacotes do Java (como o erro de herança do método `remove` da classe `Square`, resolvido com o uso do modificador `protected`).
* **Debugging Lógico e de Gameplay:** Investigação de bugs quando as coisas não funcionavam no jogo, como quando o Pac-Man passava pelo botão e nada acontecia.

**4. Durante a Feature 1, houve situações em que as respostas da LLM não foram úteis ou precisaram ser corrigidas, adaptadas ou descartadas? Caso sim, descreva alguns exemplos e como você percebeu que uma alteração era necessária.**

Sim, houveram sugestões e ideias minhas que acredito terem sido mais vantajosas, como deixar o gate como um muro ao invés de um item colecionável.

**5. Como você verificou se as sugestões ou o código fornecido pela LLM durante a Feature 1 eram adequados ao projeto?**

Eu ia avaliando se a lógica de código era razoável e quando era possível eu testava diretamente rodando um teste manual, vendo se o pacman funcionava como o esperando ou dava erros.

**6. Na Feature 2, você utilizou alguma LLM para pesquisa ou consulta? Caso sim, descreva para quais tipos de dúvida ou problema ela foi utilizada.**
Não. Conforme planejado, foram utilizadas 0 conversas na Feature 2. Todo o desenvolvimento do Pulso de Fase foram conduzidos de forma autônoma, utilizando a base técnica consolidada na etapa anterior, por já saber o papel de cada parte do código isso ficou de certo modo mais facilitado, uma parte das estruturas foram "copiadas" da primeira parte.

**7. Durante qualquer uma das duas funcionalidades, você precisou modificar a abordagem inicialmente planejada? Caso sim, explique brevemente o que motivou a mudança.**
Sim, durante a Feature 1. A ideia original era implementar o gate como um objeto da classe unit, entretanto achei que seria mais vantajoso reaproveitar o sistema de colisões da classe Square, fazendo ele como um muro, usando até mesmo o mesmo visual.

**8. Você percebeu alguma dificuldade ou problema que tenha ocorrido repetidamente durante o desenvolvimento? Caso sim, descreva-o e indique em qual das funcionalidades ele ocorreu.**
No geral, nas duas features reparei que modificar uma função ou adicionar uma nova função reverberava em TODOS os outros códigos, isso era realmente chato de se lidar. Cada modificação refletia em revisar cada pedacinho, o uso de LLM ajuda na identificação dessas relações com certeza, na feature 2 eu usei o que aprendi na 1 para não deixar passar alguma chamada oculta.

**9. Como você avalia as soluções que implementou para as duas funcionalidades? Existe algum aspecto que você ainda modificaria ou melhoraria?**
Avalio as soluções como adequadas, pois preservam o design orientado a objetos do JPacman e garantem a jogabilidade. Como melhoria futura, eu extrairia a lógica temporal do Pulso de Fase (Feature 2) e dos circuitos para uma interface comum de "Itens Temporários / Tempo", facilitando a adição de novos itens e mecânicas que necessitam do tempo.

**10. Há algum outro aspecto da experiência que considere relevante e que não tenha sido abordado nas perguntas anteriores?**
A utilidade da LLM para debugar lógicas complexas. Em sistemas legados grandes onde nem sempre a lógica do código é óbvia (como no JPacman), ter uma ferramenta para confirmar rapidamente o impacto de uma mudança sendo refletida em outras partes economizou muito tempo.