package com.kadmuffin.bikesarepain.fabric.client;

import com.kadmuffin.bikesarepain.client.ClientConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> ClientConfig.CONFIG.instance().getScreen(parentScreen);
    }
}
