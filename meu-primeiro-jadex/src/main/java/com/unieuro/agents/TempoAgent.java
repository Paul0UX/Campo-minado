package com.unieuro.agents;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;

@Agent
public class TempoAgent {

    @OnStart
    void onStart(IInternalAccess me) {
        final long inicio = System.currentTimeMillis();
        // imprime a cada 5000ms (5s)
        me.repeatStep(0, 5000, dummy -> {
            long agora = System.currentTimeMillis();
            long elapsed = (agora - inicio) / 1000;
            System.out.println("[TempoAgent] Tempo decorrido: " + elapsed + "s");
            return IFuture.DONE;
        });
    }
}
