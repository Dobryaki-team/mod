package ru.totemus.modfabric.no;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class QuestionScreen extends Screen {
    private static final Text ENTER_IP_TEXT = Text.translatable("addServer.enterIp");
    private ButtonWidget okButon;
    private final BooleanCallback callback;
    private final Screen parent;
    public QuestionScreen(Screen parent, String question, BooleanCallback callback) {
        super(Text.of(question));
        this.parent = parent;
        this.callback = callback;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.okButon.active || this.getFocused() != this.okButon || keyCode != 257 && keyCode != 335) {
            this.saveAndClose(false);
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            this.saveAndClose(true);
            return true;
        }
    }

    private Text FalseButtonText = ScreenTexts.NO;
    public void setFalseButtonText(String text){
        FalseButtonText = Text.of(text);
    }

    private Text TrueButtonText = ScreenTexts.OK;
    public void setTrueButtonText(String text){
        TrueButtonText = Text.of(text);
    }

    protected void init() {
        this.okButon = this.addDrawableChild(ButtonWidget.builder(TrueButtonText, (button) -> {
            this.saveAndClose(true);
        }).dimensions(this.width / 2 - 100, this.height / 8 + 96 + 12, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(FalseButtonText, (button) -> {
            this.saveAndClose(false);
        }).dimensions(this.width / 2 - 100, this.height / 8 + 120 + 12, 200, 20).build());
    }

    public void resize(MinecraftClient client, int width, int height) {
        this.init(client, width, height);
    }

    private void saveAndClose(boolean is) {
        this.callback.onCallback(is);
        close();
    }

    public void close() {
        this.client.setScreen(this.parent);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        super.render(context, mouseX, mouseY, delta);
    }

    public interface BooleanCallback{
        void onCallback(Boolean str);
    }
}
