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
Antes de executar o projeto é necessário seguir e cumprir uma série de requisitos para que tudo funcione da maneira adequada.

1. -> Instalar sua IDE
   O primeiro passo é baixar e fazer a instalação da IDE, recomenda-se o uso do VsCode, você pode instalar pelo link:
   https://code.visualstudio.com/download

2. -> Instalar ferramentas:
   Algumas ferramentas precisam ser instaladas para rodar o projeto, são elas:
   - Java Development Kit (JDK)
      Versão recomendada: JDK 17 ou superior.
      Faça o download em: https://www.oracle.com/java/technologies/javase-downloads.html
      Após a instalação, verifique se o Java foi configurado corretamente rodando o seguinte comando no terminal do seu projeto:
     " java -version "
     O terminal deve exibir algo como: " java version "17.0.x" "

   - Maven (Ferramenta responsável por gerenciar dependências e compilar o projeto)
     Faça o download em: https://maven.apache.org/download.cgi
     Após instalar confira se tudo esta correto com o código:
     " mvn -v "

3. Baixe o repositório ou clone ele via GIT.
   Abra um CMD, entre no local onde você quer clonar o repositório, com seguinte comando:
   " cd local/onde/quero/criar "
   e rode o seguinte comando:
   " git clone https://github.com/Paul0UX/Campo-minado.git "
   ou extraia o arquivo .zip do projeto em uma pasta da sua escolha.

4. Entrando no projeto:
   Abra a pasta que foi baixada ou clonada na sua IDE, abra um terminal e envie o seguinte comando:
   " cd meu-primeiro-jadex " para entrar dentro da pasta "meu-primeiro-jadex"

5. Compilar o projeto:
   Ja dentro da pasta, vamos compilar o projeto.
   Ainda no mesmo bash rode:
   " mvn clean "
   e depois
   " mvn -q compile "
   Isso vai baixar as dependências e compilar o projeto

7. Executar o sistema:
   para executar o sistema rode o seguinte comando:
   " mvn exec:java "

8. Funcionamento geral:
   Ao iniciar, os agentes Jadex serão carregados automaticamente:
   - CampoAgent — responsável por criar o campo minado e gerar a matriz.
   - JogadorAgent — responsável por jogar o campo minado (revelar e marcar células).
   - TempoAgent — controla o tempo total de execução.
   - HelloAgent — agente auxiliar de inicialização.
     
     Durante a execução:
       O campo minado é salvo em arquivos (campo.csv, minas_encontradas.txt, etc.)
       A interface (CampoMinadoUI) exibe o progresso do jogo e o tempo em tempo real.
       O resultado final (tempo e número de minas corretas) é salvo automaticamente.

9. Finalização
   Para finalizar e fazer o projeto para de rodar é bem simples, basta apertar " ctrl + c " dentro do terminal (isso faz o sistema para de rodar e te mantém dentro do terminal do projeto)


   # Conclusão
**************************************


# Conclusão
**************************************
