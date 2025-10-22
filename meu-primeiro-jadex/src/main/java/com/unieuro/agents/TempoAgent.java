package com.unieuro.agents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;

@Agent
public class TempoAgent {

    private volatile boolean stopped = false;

    private final Path tempoPath = Paths.get("tempo.txt");
    private final Path resultadoPath = Paths.get("resultado.txt");
    private final Path tempoFinalPath = Paths.get("tempo_final.txt");

    // inicio como campo da classe (persistente)
    private long inicio = -1L;

    @OnStart
    void onStart(IInternalAccess me) {
        // registra o instante de início do agente (ponto de referência)
        this.inicio = System.currentTimeMillis();
        System.out.println("[TempoAgent] onStart: inicio = " + this.inicio + ", resultado existe? " + Files.exists(resultadoPath));

        me.repeatStep(0, 1000, dummy -> { // atualizar a cada 1 segundo
            if (stopped) {
                return IFuture.DONE;
            }

            long agora = System.currentTimeMillis();
            long elapsed = (agora - inicio) / 1000; // segundos

            // Verifica se existe resultado; mas só aceita como "fim" se o arquivo foi criado/alterado
            // depois do inicio do agente. Isso evita pegar resultados de execuções anteriores.
            if (Files.exists(resultadoPath)) {
                try {
                    FileTime ft = Files.getLastModifiedTime(resultadoPath);
                    long modMillis = ft.toMillis();
                    if (modMillis >= inicio) {
                        // resultado válido para esta execução -> finaliza
                        System.out.println("[TempoAgent] Resultado detectado (arquivo novo). Tempo total: " + elapsed + "s");
                        try {
                            Files.writeString(tempoPath, String.valueOf(elapsed));
                            Files.writeString(tempoFinalPath, String.valueOf(elapsed));
                        } catch (IOException e) {
                            System.err.println("[TempoAgent] Erro ao escrever tempo_final: " + e.getMessage());
                        }
                        stopped = true;
                        return IFuture.DONE;
                    } else {
                        // resultado.txt é de execução anterior: ignorar
                        // (opcional: logar apenas na primeira vez)
                        // System.out.println("[TempoAgent] resultado.txt pré-existente (ignorado). lastModified=" + modMillis);
                    }
                } catch (IOException e) {
                    System.err.println("[TempoAgent] Erro ao checar lastModified de resultado.txt: " + e.getMessage());
                }
            }

            // Caso normal: atualiza tempo.txt a cada segundo
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
