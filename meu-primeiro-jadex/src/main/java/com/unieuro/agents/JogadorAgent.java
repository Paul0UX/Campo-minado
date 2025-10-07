package com.unieuro.agents;

import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;

import java.io.*;
import java.util.*;

@Agent
@Description("Agente que joga o Campo Minado automaticamente.")
public class JogadorAgent {

    private int[][] campo;          // 0 = vazio, 1 = mina
    private int[][] adjacencias;    // -1 = mina, 0–8 = minas ao redor
    private boolean[][] revelado;
    private int tamanho = 9;
    private Random random = new Random();

    @AgentBody
    public void execute() {
        carregarCampo();
        carregarAdjacencias();
        jogar();
    }

    private void carregarCampo() {
        try (BufferedReader br = new BufferedReader(new FileReader("campo.csv"))) {
            List<int[]> linhas = new ArrayList<>();
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split("\\s*,\\s*");
                int[] nums = new int[partes.length];
                for (int i = 0; i < partes.length; i++) {
                    nums[i] = Integer.parseInt(partes[i]);
                }
                linhas.add(nums);
            }
            campo = linhas.toArray(new int[0][]);
            tamanho = campo.length;
            revelado = new boolean[tamanho][tamanho];
            System.out.println("[JogadorAgent] Campo carregado. Começando a jogar...\n");
        } catch (IOException e) {
            System.err.println("Erro ao carregar campo: " + e.getMessage());
        }
    }

    private void carregarAdjacencias() {
        try (BufferedReader br = new BufferedReader(new FileReader("adjacencias.csv"))) {
            List<int[]> linhas = new ArrayList<>();
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split("\\s*,\\s*");
                int[] nums = new int[partes.length];
                for (int i = 0; i < partes.length; i++) {
                    nums[i] = Integer.parseInt(partes[i]);
                }
                linhas.add(nums);
            }
            adjacencias = linhas.toArray(new int[0][]);
        } catch (IOException e) {
            System.err.println("Erro ao carregar adjacencias: " + e.getMessage());
        }
    }

    private void jogar() {
        int jogada = 1;

        while (true) {
            int x = random.nextInt(tamanho);
            int y = random.nextInt(tamanho);

            if (revelado[x][y])
                continue;

            boolean bomba = campo[x][y] == 1;
            int minasAoRedor = adjacencias[x][y];
            revelado[x][y] = true;

            // Mostra coordenadas em formato humano (1-based)
            System.out.printf("[JogadorAgent] Jogada %d: (%d,%d) => %s (%d minas ao redor)%n",
                    jogada, x + 1, y + 1,
                    bomba ? "BOMBA" : "seguro", minasAoRedor);

            if (bomba) {
                imprimirCampo();
                System.out.println("\n💥 BOOM! Fim de jogo!");
                break;
            }

            // Se a célula for 0, revela automaticamente as vizinhas (usando BFS para evitar recursão profunda)
            if (minasAoRedor == 0) {
                revelarVizinhosBFS(x, y);
            }

            imprimirCampo();

            jogada++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Revela recursivamente as casas vizinhas de uma célula vazia (0 minas ao redor). */
    // Versão iterativa (BFS) para maior robustez em campos grandes
    private void revelarVizinhosBFS(int startX, int startY) {
        Queue<int[]> queue = new LinkedList<>();
        // Garantir que a célula inicial esteja marcada
        if (!revelado[startX][startY]) {
            revelado[startX][startY] = true;
        }
        queue.add(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0];
            int y = cur[1];

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && ny >= 0 && nx < tamanho && ny < tamanho && !revelado[nx][ny]) {
                        revelado[nx][ny] = true; // marca como revelada
                        if (adjacencias[nx][ny] == 0) {
                            // se também for zero, empilha para expandir suas vizinhas
                            queue.add(new int[]{nx, ny});
                        }
                    }
                }
            }
        }
    }

    /** Imprime o campo, mostrando números e espaços como no jogo real. */
    private void imprimirCampo() {
        System.out.println("\n===== CAMPO MINADO =====");
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (revelado[i][j]) {
                    int val = adjacencias[i][j];
                    if (val == -1) {
                        System.out.print(" * "); // mina
                    } else if (val == 0) {
                        System.out.print("   "); // vazio
                    } else {
                        System.out.print(" " + val + " "); // número de minas
                    }
                } else {
                    System.out.print(" ? ");
                }
            }
            System.out.println();
        }
        System.out.println("========================");
    }
}
