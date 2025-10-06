package com.unieuro;

import com.unieuro.agents.CampoAgent;
import com.unieuro.agents.JogadorAgent;
import com.unieuro.agents.TempoAgent;

import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;

public class Main {
    public static void main(String[] args) {
        IPlatformConfiguration config = PlatformConfigurationHandler.getMinimal();
        config.addComponent(CampoAgent.class);
        config.addComponent(JogadorAgent.class);
        config.addComponent(TempoAgent.class);
        Starter.createPlatform(config).get();
    }
}
