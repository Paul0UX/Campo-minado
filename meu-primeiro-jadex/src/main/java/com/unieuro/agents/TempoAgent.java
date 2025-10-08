package com.unieuro.agents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;

@Agent
public class TempoAgent {

    // flag para parar de imprimir apos o encerramento
    private volatile boolean stopped = false;

    private final Path tempoPath = Paths.get("tempo.txt");
    private final Path resultadoPath = Paths.get("resultado.txt");

    @OnStart
    void onStart(IInternalAccess me) {
        final long inicio = System.currentTimeMillis();

        me.repeatStep(0, 1000, dummy -> { // atualizar a cada 1 segundo
            if (stopped) {
                return IFuture.DONE;
            }

            // Se já existe arquivo de resultado do jogo, escrevemos o tempo final e paramos
            if (Files.exists(resultadoPath)) {
                long agora = System.currentTimeMillis();
                long elapsed = (agora - inicio) / 1000;
                System.out.println("[TempoAgent] Resultado detectado. Tempo total: " + elapsed + "s");
                try {
                    Files.writeString(tempoPath, String.valueOf(elapsed));
                } catch (IOException e) {
                    System.err.println("[TempoAgent] Erro ao escrever tempo_final: " + e.getMessage());
                }
                stopped = true;
                return IFuture.DONE;
            }

            long agora = System.currentTimeMillis();
            long elapsed = (agora - inicio) / 1000; // segundos

            System.out.println("[TempoAgent] Tempo decorrido: " + elapsed + "s");

            try {
                Files.writeString(tempoPath, String.valueOf(elapsed));
            } catch (IOException e) {
                System.err.println("[TempoAgent] Erro ao atualizar tempo.txt: " + e.getMessage());
            }

            return IFuture.DONE;
        });
    }
}
