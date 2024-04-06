package ru.totemus.modfabric.no;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class TotemusMod implements ModInitializer {
    ForkJoinPool ThreadPool = new ForkJoinPool(2);
    static Logger logger = LogManager.getLogger("TotemusMod");
    ResourcePack ResPack;
    TotemusSettings settings = new TotemusSettings(new File("totemusSettings.json"));
    TotemusSocketConector connector;
    //private static final String TotemusServer = "ws://apiconnectormaster.vanillaland.ru:25080/";
    private static final String TotemusServer = "ws://localhost:8080/";
    private static boolean isModInitedSuccess = false;
    private static boolean isModOffed = true;
    @Override
    public void onInitialize() {
        ResPack = new ResourcePack(new File(MinecraftClient.getInstance().getResourcePackDir().toFile().getAbsolutePath() + File.separator + "TotemusPack"));

        ClientLifecycleEvents.CLIENT_STARTED.register(new ClientLifecycleEvents.ClientStarted() {
            @Override
            public void onClientStarted(MinecraftClient client) {
                connector = new TotemusSocketConector(TotemusServer + settings.UserToken, new TotemusSocketConector.SocketConnect() {
                    @Override
                    public void onConnect(WebSocketClient client) {
                        isModInitedSuccess = true;
                        nextInit();
                        logger.info("SUCCESS!");
                    }

                    @Override
                    public void onAccessDenied(WebSocketClient client, String reason) {
                        isModInitedSuccess = false;
                        logger.info("NO SUCCESS!");
                        if(MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().player.getWorld() == null)
                            connect(reason, MinecraftClient.getInstance().currentScreen);
                    }
                }, ResPack);

                connect(null, MinecraftClient.getInstance().currentScreen);
            }
        });
    }

    private void nextInit(){
        ResPack.getTotemWithConnector(MinecraftClient.getInstance().getSession().getUsername(), connector);

        MinecraftClient.getInstance().getResourcePackManager().scanPacks();

        if(!MinecraftClient.getInstance().getResourcePackManager().enable("file/TotemusPack"))
            logger.error("Totemus Pack not enabled!");

        MinecraftClient.getInstance().reloadResourcesConcurrently();

        ModifyItemAttributeModifiersCallback.EVENT.register(new ModifyItemAttributeModifiersCallback() {
            @Override
            public void modifyAttributeModifiers(ItemStack stack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers) {
                if(stack.getItem() == Items.TOTEM_OF_UNDYING) ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(!stack.getName().getString().equals(Items.TOTEM_OF_UNDYING.getName().getString())) {
                            ResPack.getTotemWithConnector(stack.getName().getString(), connector);
                            connector.checkAndReconnect();
                        }
                    }
                });
            }
        });

        ClientPlayConnectionEvents.INIT.register(new ClientPlayConnectionEvents.Init() {
            @Override
            public void onPlayInit(ClientPlayNetworkHandler handler, MinecraftClient client) {
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ResPack.checkResources();

                        for(PlayerListEntry l: handler.getPlayerList()) if(l.getDisplayName() != null)
                            ResPack.getTotemWithConnector(l.getDisplayName().getString().toLowerCase(), connector);

                        connector.checkAndReconnect();
                    }
                });
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register(new ClientPlayConnectionEvents.Disconnect() {
            @Override
            public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
                if(ResPack.isNeedReloadResources)
                    MinecraftClient.getInstance().reloadResourcesConcurrently();
                connector.checkAndReconnect();
            }
        });
    }

    private void connect(String fail, Screen current) {
        if (fail != null) {
            var a = new InputScreen(current, "Настройка Totemus Mod | "+fail, "Пожалуйста введите ваш токен", new InputScreen.StringCallback() {
                @Override
                public void onCallback(String str) {
                    ThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (str == null || str.isEmpty())
                                if(questionScreen(current,
                                        "Без токена Totemus Mod работать не будет. Может быть вернутся к настройке токена?",
                                        "Нет спасибо", "Вернутся")){
                                    isModInitedSuccess = false;
                                    isModOffed = true;
                                    return;
                                }

                            settings.setToken(str);
                            connect(null, current);
                        }
                    });
                }
            });

            RenderSystem.recordRenderCall(() -> {
                MinecraftClient.getInstance().setScreen(a);
            });

            return;
        } else if (settings.UserToken == null) {
            var a = new InputScreen(current, "Настройка Totemus Mod", "Пожалуйста введите ваш токен", new InputScreen.StringCallback() {
                @Override
                public void onCallback(String str) {
                    ThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (str == null || str.isEmpty())
                                if(questionScreen(current,
                                        "Без токена Totemus Mod работать не будет. Может быть вернутся к настройке токена?",
                                        "Нет спасибо", "Вернутся")){
                                    isModInitedSuccess = false;
                                    isModOffed = true;
                                    return;
                                }

                            settings.setToken(str);
                            connect(null, current);
                        }
                    });
                }
            });

            RenderSystem.recordRenderCall(() -> {
                MinecraftClient.getInstance().setScreen(a);
            });

            return;
        }

        try {
            connector.init(TotemusServer+settings.UserToken);
        }catch (URISyntaxException e){
            connect(e.getMessage(), current);
        }
    }

    public boolean questionScreen(Screen parent, String question, String TrueButton, String falseButton){
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        var a = new QuestionScreen(parent, question, new QuestionScreen.BooleanCallback() {
            @Override
            public void onCallback(Boolean str) {
                completableFuture.complete(str);
            }
        });

        a.setTrueButtonText(TrueButton);
        a.setFalseButtonText(falseButton);

        RenderSystem.recordRenderCall(() -> {
            MinecraftClient.getInstance().setScreen(a);
        });

        try {
            return completableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
