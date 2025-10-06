package com.unieuro.agents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.micro.annotation.Agent;

@Agent
public class CampoAgent {

    // Tamanho padrao do campo (ajuste se quiser)
    private final int rows = 9;
    private final int cols = 9;
    private final int mines = 10; // numero de minas

    @OnStart
    void onStart(IInternalAccess me) {
        try {
            int[][] campo = gerarCampo(rows, cols, mines);
            salvarCampoCSV(campo, "campo.csv");
            System.out.println("[CampoAgent] Campo gerado e salvo em campo.csv");
        } catch (IOException e) {
            System.err.println("[CampoAgent] Erro ao salvar campo: " + e.getMessage());
        }
    }

    private int[][] gerarCampo(int r, int c, int minas) {
        int[][] campo = new int[r][c]; // 0 = vazio, 1 = mina
        Random rnd = new Random();
        int colocadas = 0;
        while (colocadas < minas) {
            int x = rnd.nextInt(r);
            int y = rnd.nextInt(c);
            if (campo[x][y] == 0) {
                campo[x][y] = 1;
                colocadas++;
            }
        }
        return campo;
    }

    private void salvarCampoCSV(int[][] campo, String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campo.length; i++) {
            for (int j = 0; j < campo[i].length; j++) {
                sb.append(campo[i][j]);
                if (j < campo[i].length - 1) sb.append(",");
            }
            sb.append("\n");
        }
        Path p = Paths.get(filename);
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(p, bytes);
    }
}
