package ru.totemus.modfabric.no.client;

import net.fabricmc.api.ClientModInitializer;
import shcm.shsupercm.fabric.citresewn.CITResewn;

public class TotemusModClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        CITResewn c = new CITResewn();
        c.onInitializeClient();
    }
}
