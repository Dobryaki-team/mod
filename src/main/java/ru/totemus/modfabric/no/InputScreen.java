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
public class InputScreen extends Screen {
    private static final Text ENTER_IP_TEXT = Text.translatable("addServer.enterIp");
    private ButtonWidget okButon;
    private TextFieldWidget tokenField;
    private final StringCallback callback;
    private final Screen parent;
    private final String textAreaLabel;

    public InputScreen(Screen parent, String text, String textAreaLabel, StringCallback callback) {
        super(Text.of(text));
        this.parent = parent;
        this.callback = callback;
        this.textAreaLabel = textAreaLabel;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.okButon.active || this.getFocused() != this.okButon || keyCode != 257 && keyCode != 335) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            this.saveAndClose();
            return true;
        }
    }

    protected void init() {
        this.tokenField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 116, 200, 20, Text.of(textAreaLabel));
        this.tokenField.setMaxLength(128);

        this.addSelectableChild(this.tokenField);

        this.okButon = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.OK, (button) -> {
            this.saveAndClose();
        }).dimensions(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.callback.onCallback(null);
            close();
        }).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20).build());

        this.setInitialFocus(this.tokenField);
    }

    public void resize(MinecraftClient client, int width, int height) {
        String string = this.tokenField.getText();
        this.init(client, width, height);
        this.tokenField.setText(string);
    }

    private void saveAndClose() {
        this.callback.onCallback(tokenField.getText());
        close();
    }

    public void close() {
        this.client.setScreen(this.parent);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        context.drawTextWithShadow(this.textRenderer, textAreaLabel, this.width / 2 - 100, 100, 10526880);
        this.tokenField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    public interface StringCallback{
        void onCallback(String str);
    }
}
