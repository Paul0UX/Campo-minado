package com.unieuro.agents;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;

@Agent
@Description("Agente que cria o campo minado.")
public class CampoAgent {

    private int tamanho = 9;
    private int minas = 10;
    private int[][] campo;
    private int[][] adjacencias;

    @AgentBody
    public void execute() {
        // Gera campos até existir ao menos uma célula com adjacencia == 0 (garante primeira jogada em cascata)
        do {
            gerarCampo();
            calcularAdjacencias();
        } while (!temZero());
        salvarCampo();
        salvarAdjacencias();
        // inicializa arquivo de minas encontradas com 0 para UI
        inicializarArquivoMinasEncontradas();
        System.out.println("[CampoAgent] Campo gerado e salvo em campo.csv e adjacencias.csv");
    }

    /** Retorna true quando existe ao menos uma célula segura com adjacencia 0. */
    private boolean temZero() {
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (campo[i][j] == 0) {
                    // precisa de adjacencias já calculadas para checar; se adjacencias for null ainda, assume false
                    if (adjacencias == null) return false;
                    if (adjacencias[i][j] == 0) return true;
                }
            }
        }
        return false;
    }

    private void gerarCampo() {
        campo = new int[tamanho][tamanho];
        Random random = new Random();
        int colocadas = 0;
        while (colocadas < minas) {
            int x = random.nextInt(tamanho);
            int y = random.nextInt(tamanho);
            if (campo[x][y] == 0) {
                campo[x][y] = 1;
                colocadas++;
            }
        }
    }

    private void calcularAdjacencias() {
        adjacencias = new int[tamanho][tamanho];
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (campo[i][j] == 1) {
                    adjacencias[i][j] = -1; // -1 = mina
                } else {
                    int count = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0)
                                continue;
                            int nx = i + dx;
                            int ny = j + dy;
                            if (nx >= 0 && ny >= 0 && nx < tamanho && ny < tamanho && campo[nx][ny] == 1)
                                count++;
                        }
                    }
                    adjacencias[i][j] = count;
                }
            }
        }
    }

    private void salvarCampo() {
        try (FileWriter fw = new FileWriter("campo.csv")) {
            for (int[] linha : campo) {
                for (int j = 0; j < linha.length; j++) {
                    fw.write(linha[j] + (j < linha.length - 1 ? "," : ""));
                }
                fw.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar campo: " + e.getMessage());
        }
    }

    private void salvarAdjacencias() {
        try (FileWriter fw = new FileWriter("adjacencias.csv")) {
            for (int[] linha : adjacencias) {
                for (int j = 0; j < linha.length; j++) {
                    fw.write(linha[j] + (j < linha.length - 1 ? "," : ""));
                }
                fw.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar adjacencias: " + e.getMessage());
        }
    }

    private void inicializarArquivoMinasEncontradas() {
        try {
            Path p = Paths.get("minas_encontradas.txt");
            Files.writeString(p, "00");
        } catch (IOException e) {
            System.err.println("[CampoAgent] Erro ao inicializar minas_encontradas.txt: " + e.getMessage());
        }
    }
}
