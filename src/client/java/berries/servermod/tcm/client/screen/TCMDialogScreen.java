package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.flueroui.FlueroUI;
import berries.servermod.tcm.client.flueroui.TextDrawer;
import berries.servermod.tcm.client.flueroui.widget.FlueroButton;
import berries.servermod.tcm.client.screen.widget.ImageWidget;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TCMDialogScreen extends Screen {
    public final Component[] splitDescription;
    public final List<ObjectObjectImmutablePair<ObjectObjectImmutablePair<Component, Boolean>, BiConsumer<Minecraft, AbstractButton>>> buttonSettings;

    public final int dialogPadding = 18;
    public final int titleTopPadding = 23;
    public final int titleAndContentPadding = 18;
    public final int buttonTopDownPadding = 14;

    public static final int DIALOG_IMAGE_WIDTH = 192;
    public static final int DIALOG_IMAGE_HEIGHT = 165;

    public int dialogWidth = DIALOG_IMAGE_WIDTH;
    public int dialogHeight = DIALOG_IMAGE_HEIGHT;

    public final List<AbstractButton> buttons = new ArrayList<>();
    public MultiLineLabel[] content;

    public boolean checkBox;
    public Checkbox checkBoxInstance = null;

    public boolean isUpdateDialog = false;
    public String newVersion = "null";

    public Screen screenOnClose = null;

    public TCMDialogScreen(Component title, Component description, List<ObjectObjectImmutablePair<ObjectObjectImmutablePair<Component, Boolean>, BiConsumer<Minecraft, AbstractButton>>> buttons) {
        super(title.copy().withStyle(Style.EMPTY.withBold(true)));
        String[] descriptions = description.getString().split("\\|");
        this.splitDescription = new Component[descriptions.length];
        for (int i = 0; i < descriptions.length; i++) {
            splitDescription[i] = TCMComponent.text(descriptions[i]);
        }
        this.buttonSettings = buttons;
        this.checkBox = false;
    }

    public TCMDialogScreen(Component title, Component[] descriptions, List<ObjectObjectImmutablePair<ObjectObjectImmutablePair<Component, Boolean>, BiConsumer<Minecraft, AbstractButton>>> buttons) {
        super(title.copy().withStyle(Style.EMPTY.withBold(true)));
        this.splitDescription = descriptions;
        this.buttonSettings = buttons;
        this.checkBox = false;
    }

    public TCMDialogScreen(Component title, Component description) {
        super(title.copy().withStyle(Style.EMPTY.withBold(true)));
        String[] descriptions = description.getString().split("\\|");
        this.splitDescription = new Component[descriptions.length];
        for (int i = 0; i < descriptions.length; i++) {
            splitDescription[i] = TCMComponent.text(descriptions[i]);
        }
        this.buttonSettings = new ArrayList<>(List.of(new ObjectObjectImmutablePair<>(new ObjectObjectImmutablePair<>(TCMComponent.translatable("gui.tcm.update_dialog.closeButton.text"), false), (mc, btn) -> {
            if (mc != null) {
                onClose();
                mc.setScreen(null);
            }
        })));
        this.checkBox = false;
    }

    public TCMDialogScreen(Component title, Component description, boolean checkBox) {
        this(title, description);
        this.checkBox = checkBox;
    }

    @Override
    protected void init() {
        if (minecraft == null) return;

        Config.INSTANCE.readConfig();

        content = new MultiLineLabel[splitDescription.length];
        for (int i = 0; i < splitDescription.length; i++) {
            Component text = splitDescription[i];
            content[i] = MultiLineLabel.create(minecraft.font, text, dialogWidth - 2 * dialogPadding - (isUpdateDialog ? (int)Math.ceil(dialogHeight / 3.4) + 16 : 0));
        }

        int buttonWidth = dialogWidth / (buttonSettings.size() + (checkBox ? 1 : 0)) - ((2 * dialogPadding) / (buttonSettings.size() + (checkBox ? 1 : 0))) - (buttonSettings.size() < 2 ? 0 : 4);
        buttonWidth += 1;
        int buttonHeight = 16;

        for (int i = 0; i < (buttonSettings.size() + (checkBox ? 1 : 0)); i++) {
            int x = (int) ((float) ((float) width / 2 - ((float) dialogWidth / 2)) + dialogPadding + ((buttonWidth + 2) * i));
            int y = (int) ((float) ((float) height / 2 + ((float) dialogHeight / 2)) - buttonHeight - buttonTopDownPadding - 2);

            if (checkBox && i == 0) {
                checkBoxInstance = new Checkbox(x, y - 3, 20, 20, TCMComponent.translatable("gui.tcm.update_dialog.checkBox.text").copy().withStyle(Style.EMPTY.withColor(0xEEEEEE)), Config.INSTANCE.neverShowUpdateDialog);
                buttons.add(
                        addRenderableWidget(checkBoxInstance)
                );
            } else {
                ObjectObjectImmutablePair<ObjectObjectImmutablePair<Component, Boolean>, BiConsumer<Minecraft, AbstractButton>> buttonSetting = buttonSettings.get(i - (checkBox ? 1 : 0));
                var btn = new FlueroButton(x + (checkBox ? 6 : 0), y, buttonWidth, buttonHeight, buttonSetting.left().left(), (button) -> {
                    buttonSetting.right().accept(minecraft, button);
                });
                btn.setAccent(buttonSetting.left().right());
                buttons.add(
                        addRenderableWidget(btn/*Button.builder(buttonSetting.getA(), (button) -> {
                                    buttonSetting.getB().accept(minecraft, button);
                                }).pos(x + (checkBox ? 6 : 0), y).size(buttonWidth, buttonHeight).build()*/
                        ));
            }
        }
    }

    @Override
    @SuppressWarnings("all")
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        if (minecraft == null) return;

        this.renderBackground(guiGraphics);
        /*RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUILocations.DIALOG_LOCATION);

        guiGraphics.blit(
                GUILocations.DIALOG_LOCATION,
                (int) ((float) width / 2 - Math.ceil((float) dialogWidth / 2)),   //x
                (int) ((float) height / 2 - ((float) DIALOG_IMAGE_HEIGHT / 2)), //y
                0,
                0,
                64, DIALOG_IMAGE_HEIGHT, DIALOG_IMAGE_WIDTH, DIALOG_IMAGE_HEIGHT
        );

        guiGraphics.blit(
                GUILocations.DIALOG_LOCATION,
                (int) ((float) width / 2 - Math.ceil((float) dialogWidth / 2) + 64),   //x
                (int) ((float) height / 2 - ((float) DIALOG_IMAGE_HEIGHT / 2)), //y
                64,
                0,
                dialogWidth - 128, DIALOG_IMAGE_HEIGHT, dialogWidth, DIALOG_IMAGE_HEIGHT
        );

        guiGraphics.blit(
                GUILocations.DIALOG_LOCATION,
                (int) ((float) width / 2 + Math.floor((float) dialogWidth / 2) - 64),   //x
                (int) ((float) height / 2 - ((float) DIALOG_IMAGE_HEIGHT / 2)), //y
                DIALOG_IMAGE_WIDTH - 64,
                0,
                64, DIALOG_IMAGE_HEIGHT, DIALOG_IMAGE_WIDTH, DIALOG_IMAGE_HEIGHT
        );*/
        FlueroUI.renderCenteredDialog(guiGraphics, width, height, dialogWidth, dialogHeight);

        var ps0 = guiGraphics.pose();
        ps0.pushPose();
        ps0.scale(2.0f, 2.0f, 2.0f);
        TextDrawer.drawText(guiGraphics, minecraft.font, this.title, TextDrawer.Alignment.LEFT, (int) (((float) ((float) width / 2 - ((float) dialogWidth / 2)) + dialogPadding) / 2.0f),
                (int) (((float) ((float) height / 2 - ((float) dialogHeight / 2)) + titleTopPadding) / 2.0f), FlueroUI.rgb(255, 255, 255), false);
        ps0.popPose();

        int contentTopPadding = (int) (((float) height / 2) - ((float) dialogHeight / 2) + titleTopPadding + minecraft.font.lineHeight + titleAndContentPadding);
        int contentRegionY = minecraft.font.lineHeight + 3;

        int leftOffset = (int) ((float) ((float) width / 2 - ((float) dialogWidth / 2)) + dialogPadding);
        if (isUpdateDialog) {
            var i0s = (int)Math.ceil(dialogHeight / 3.4);
            var i0x = leftOffset;
            var i0y = contentTopPadding + 1;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(GUILocations.LOGO_LOCATION, i0x, i0y, 0, 0, i0s, i0s, i0s, i0s);
            ps0.pushPose();
            ps0.scale(0.85f, 0.85f, 0.85f);
            TextDrawer.drawText(guiGraphics, minecraft.font, TCMComponent.text(UFEInfo.MOD_NAME), TextDrawer.Alignment.CENTER,
                    (int) ((i0x + i0s / 2.0) / 0.85), (int) ((i0y + i0s + 6) / 0.85), FlueroUI.rgb(228, 228, 228), false);
            TextDrawer.drawText(guiGraphics, minecraft.font, TCMComponent.text(newVersion), TextDrawer.Alignment.CENTER,
                    (int) ((i0x + i0s / 2.0) / 0.85), (int) ((i0y + i0s + 16) / 0.85), FlueroUI.rgb(128, 128, 128), false);
            ps0.popPose();

            leftOffset = i0x + i0s + 16;
        }

        int c = 0;
        for (MultiLineLabel label : content) {
            label.renderLeftAlignedNoShadow(
                    guiGraphics,
                    leftOffset,
                    contentTopPadding + (c * 12),
                    12, FlueroUI.rgb(255, 255, 255)
            );
            c += label.getLineCount();
        }

        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void onClose() {
        if (checkBox && checkBoxInstance != null) {
            Config.INSTANCE.neverShowUpdateDialog = checkBoxInstance.selected();
            Config.INSTANCE.saveConfig();
        }
        if (minecraft != null) {
            minecraft.setScreen(screenOnClose);
        }
    }
}
