package com.unieuro.agents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;

@Agent
public class JogadorAgent {

    // controle de velocidade e verbosidade
    private final int SLEEP_MS = 300;   // pausa entre jogadas (ms)
    private final int PRINT_EVERY = 5;  // imprime a cada N jogadas (se nao for mina)

    // flag para garantir que o agente jogue apenas uma vez
    private volatile boolean played = false;

    @OnStart
    void onStart(IInternalAccess me) {
        // repetidamente verifica se o arquivo existe, mas sai se ja jogou
        me.repeatStep(0, 1000, dummy -> {
            if (played) {
                return IFuture.DONE; // ja jogou, nao faz nada
            }

            Path p = Paths.get("campo.csv");
            if (Files.exists(p)) {
                try {
                    int[][] campo = lerCampoCSV(p);
                    System.out.println("[JogadorAgent] Campo carregado. Comecando a jogar (saidas reduzidas)...");
                    jogarCampo(campo);

                    // marcar como executado para nao reiniciar
                    played = true;

                    // opcional: deletar o arquivo para evitar re-execucao acidental
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ex) {
                        System.err.println("[JogadorAgent] Aviso: nao foi possivel deletar campo.csv: " + ex.getMessage());
                    }

                } catch (IOException e) {
                    System.err.println("[JogadorAgent] Erro ao ler campo: " + e.getMessage());
                }
            }
            return IFuture.DONE;
        });
    }

    private int[][] lerCampoCSV(Path p) throws IOException {
        List<String> linhas = Files.readAllLines(p, StandardCharsets.UTF_8);
        int r = linhas.size();
        int c = linhas.get(0).split(",").length;
        int[][] campo = new int[r][c];
        for (int i = 0; i < r; i++) {
            String[] parts = linhas.get(i).trim().split(",");
            for (int j = 0; j < c; j++) {
                campo[i][j] = Integer.parseInt(parts[j]);
            }
        }
        return campo;
    }

    private void jogarCampo(int[][] campo) {
        int r = campo.length;
        int c = campo[0].length;
        List<int[]> possiveis = new ArrayList<>();
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) possiveis.add(new int[]{i, j});

        Random rnd = new Random();
        int jogadas = 0;
        int minasEncontradas = 0;

        while (!possiveis.isEmpty()) {
            int idx = rnd.nextInt(possiveis.size());
            int[] pos = possiveis.remove(idx);
            int x = pos[0], y = pos[1];
            jogadas++;

            if (campo[x][y] == 1) {
                minasEncontradas++;
                System.out.printf("[JogadorAgent] Jogada %d: (%d,%d) => MINA! Total minas encontradas: %d%n",
                                  jogadas, x, y, minasEncontradas);
            } else {
                if (jogadas % PRINT_EVERY == 0) {
                    int vizinhas = contarMinasVizinhas(campo, x, y);
                    System.out.printf("[JogadorAgent] Jogada %d: (%d,%d) => seguro (%d minas ao redor)%n",
                                      jogadas, x, y, vizinhas);
                }
            }

            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException ignored) {}
        }

        System.out.println("[JogadorAgent] Fim das jogadas. Jogadas totais: " + jogadas +
                           ", Minas encontradas: " + minasEncontradas);
    }

    private int contarMinasVizinhas(int[][] campo, int x, int y) {
        int r = campo.length, c = campo[0].length;
        int count = 0;
        for (int i = Math.max(0, x - 1); i <= Math.min(r - 1, x + 1); i++) {
            for (int j = Math.max(0, y - 1); j <= Math.min(c - 1, y + 1); j++) {
                if (i == x && j == y) continue;
                if (campo[i][j] == 1) count++;
            }
        }
        return count;
    }
}
