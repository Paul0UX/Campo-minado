package com.unieuro.ui;

import java.util.Random;

public class CampoMinadoConsole {
    private static final int TAMANHO = 9;
    private static final char VAZIO = '.';
    private static final char ABERTO = ' ';
    private static final char MARCA = '⚑';
    private static final char BOMBA = '*';

    private char[][] tabuleiro = new char[TAMANHO][TAMANHO];
    private boolean[][] bombas = new boolean[TAMANHO][TAMANHO];
    private Random random = new Random();

    public CampoMinadoConsole() {
        inicializar();
        jogarIA();
    }

    private void inicializar() {
        // Preenche o tabuleiro com células vazias
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                tabuleiro[i][j] = VAZIO;
            }
        }

        // Adiciona bombas aleatórias
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                bombas[i][j] = random.nextDouble() < 0.15; // 15% de chance de bomba
            }
        }
    }

    private void jogarIA() {
        System.out.println("🤖 IA iniciando o jogo do Campo Minado...\n");
        for (int rodada = 1; rodada <= 20; rodada++) {
            int x = random.nextInt(TAMANHO);
            int y = random.nextInt(TAMANHO);

            System.out.println("Rodada " + rodada + ": IA escolheu (" + x + "," + y + ")");

            // ... (código existente de jogarIA) ...

            if (bombas[x][y]) {
                tabuleiro[x][y] = BOMBA;
                mostrarTabuleiro();
                System.out.println("\n💥 A IA encontrou uma bomba! Fim de jogo.");
                return;
            } else {
                // ANTES: tabuleiro[x][y] = ABERTO;
                // AGORA: Conta as bombas adjacentes e coloca no tabuleiro
                int numBombasAdjacentes = contarBombasAdjacentes(x, y);
                if (numBombasAdjacentes == 0) {
                    tabuleiro[x][y] = ABERTO; // Se não houver bombas, mostra '□'
                } else {
                    // Converte o número para char (ex: '1', '2', etc.)
                    tabuleiro[x][y] = Character.forDigit(numBombasAdjacentes, 10);
                }
            }

            mostrarTabuleiro();

// ... (resto do código de jogarIA) ...


            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n✅ A IA terminou suas jogadas sem explodir!");
    }

    private void mostrarTabuleiro() {
        System.out.println("\n   ────────────────────────────────");
        for (int i = 0; i < TAMANHO; i++) {
            System.out.print((i + 1) + " | ");
            for (int j = 0; j < TAMANHO; j++) {
                System.out.print(tabuleiro[i][j] + " ");
            }
            System.out.println("|");
        }
        System.out.println("   ────────────────────────────────");
        System.out.print("    ");
        for (int j = 0; j < TAMANHO; j++) System.out.print((j + 1) + " ");
        System.out.println("\n");
    }

    public static void main(String[] args) {
        new CampoMinadoConsole();
    }
    // ... (código existente da classe CampoMinadoConsole) ...

    /**
     * Conta o número de bombas adjacentes (incluindo diagonais) a uma dada célula.
     * @param r Linha da célula.
     * @param c Coluna da célula.
     * @return O número de bombas adjacentes.
     */
    private int contarBombasAdjacentes(int r, int c) {
        int contador = 0;
        for (int dr = -1; dr <= 1; dr++) { // dr = delta row (mudança na linha)
            for (int dc = -1; dc <= 1; dc++) { // dc = delta column (mudança na coluna)
                // Ignora a própria célula central (r, c)
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int nr = r + dr; // nova linha
                int nc = c + dc; // nova coluna

                // Verifica se a nova posição está dentro dos limites do tabuleiro
                if (nr >= 0 && nr < TAMANHO && nc >= 0 && nc < TAMANHO) {
                    if (bombas[nr][nc]) {
                        contador++;
                    }
                }
            }
        }
        return contador;
    }

// ... (resto do código da classe CampoMinadoConsole) ...

}
