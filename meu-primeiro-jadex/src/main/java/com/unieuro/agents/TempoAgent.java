package com.unieuro.agents;

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

    @OnStart
    void onStart(IInternalAccess me) {
        final long inicio = System.currentTimeMillis();
        final Path p = Paths.get("campo.csv");

        me.repeatStep(0, 5000, dummy -> {
            // se ja paramos, nao fazemos nada (silencioso)
            if (stopped) {
                return IFuture.DONE;
            }

            long agora = System.currentTimeMillis();
            long elapsed = (agora - inicio) / 1000; // segundos

            // se o arquivo existe, imprimimos normalmente
            if (Files.exists(p)) {
                System.out.println("[TempoAgent] Tempo decorrido: " + elapsed + "s");
                return IFuture.DONE;
            }

            // se o arquivo nao existe e ja passou um tempo razoavel -> assumimos fim
            if (elapsed > 1) {
                System.out.println("[TempoAgent] campo.csv ausente. Encerrando monitoramento. Tempo total: " + elapsed + "s");
                // marca para nao imprimir mais
                stopped = true;
                return IFuture.DONE;
            }

            return IFuture.DONE;
        });
    }
}
