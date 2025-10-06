# Projeto-Campo Minado com multi-agentes utilizando Jadex

# Linguagens / Bibliotecas Utilizadas
JAVA / MAVEN / Ubuntun          


# Introdução 
O projeto foi criado para um estudo acadêmico, onde foi utilizado o java com Jadex como plataforma para o desenvolvimento de sistemas multiagentes em Java. Os multiagentes são vários agentes que atuam em um mesmo sistema, cada um com suas próprias funções. 

# Campo Minado
Objetivo:
O objetivo principal é descobrir todos os quadrados que não contêm minas, sem detonar nenhuma delas.

Como funcinciona:
O Tabuleiro: O jogo começa com uma grade de quadrados todos cobertos. Por baixo desses quadrados, existem minas escondidas e quadrados vazios.

Primeiro Clique: Ao clicar em um quadrado, ele será revelado. A primeira jogada é sempre segura, ou seja, nunca haverá uma mina no seu primeiro clique.

Quadrados com Números: Se você clicar em um quadrado e ele revelar um número (de 1 a 8), esse número indica quantas minas estão diretamente adjacentes a esse quadrado (incluindo as diagonais). Por exemplo, se um quadrado mostra "3", significa que há 3 minas nos 8 quadrados ao redor dele.

Quadrados Vazios: Se você clicar em um quadrado e ele não revelar nenhum número (ficar vazio), isso significa que não há minas nos quadrados adjacentes a ele. O jogo automaticamente revelará todos os quadrados vizinhos que também são vazios, continuando até encontrar quadrados com números ou a borda do tabuleiro.

Marcando Minas: Quando você tem certeza (ou suspeita fortemente) que um quadrado contém uma mina, você pode marcá-lo com uma bandeira (geralmente clicando com o botão direito do mouse, ou pressionando e segurando em dispositivos móveis). Isso serve para lembrá-lo de não clicar ali e também para ajudar a contabilizar as minas.

Detonar uma Mina: Se você clicar em um quadrado que contém uma mina, a mina explode e o jogo termina. Você perde.

Vitória: Você ganha o jogo quando todos os quadrados que não contêm minas são revelados e todas as minas são marcadas corretamente (ou os quadrados com minas são os únicos restantes cobertos).


# Instruções
*********************************



# Conclusão
**************************************
