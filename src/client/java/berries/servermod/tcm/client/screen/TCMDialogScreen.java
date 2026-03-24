package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
    public final List<Tuple<Component, BiConsumer<Minecraft, AbstractButton>>> buttonSettings;

    public final int dialogPadding = 18;
    public final int titleTopPadding = 20;
    public final int titleAndContentPadding = 18;
    public final int buttonTopDownPadding = 18;

    public static final int DIALOG_IMAGE_WIDTH = 192;
    public static final int DIALOG_IMAGE_HEIGHT = 165;

    public int dialogWidth = DIALOG_IMAGE_WIDTH;

    public final List<AbstractButton> buttons = new ArrayList<>();
    public MultiLineLabel titleLabel;
    public MultiLineLabel[] content;

    public boolean checkBox;
    public Checkbox checkBoxInstance = null;

    public TCMDialogScreen(Component title, Component description, List<Tuple<Component, BiConsumer<Minecraft, AbstractButton>>> buttons) {
        super(title.copy().withStyle(Style.EMPTY.withBold(true)));
        String[] descriptions = description.getString().split("\\|");
        this.splitDescription = new Component[descriptions.length];
        for (int i = 0; i < descriptions.length; i++) {
            splitDescription[i] = TCMComponent.text(descriptions[i]);
        }
        this.buttonSettings = buttons;
        this.checkBox = false;
    }

    public TCMDialogScreen(Component title, Component[] descriptions, List<Tuple<Component, BiConsumer<Minecraft, AbstractButton>>> buttons) {
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
        this.buttonSettings = new ArrayList<>(List.of(new Tuple<Component, BiConsumer<Minecraft, AbstractButton>>(TCMComponent.translatable("gui.tcm.update_dialog.closeButton.text"), (mc, btn) -> {
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

        titleLabel = MultiLineLabel.create(minecraft.font, getTitle(), dialogWidth - 2 * dialogPadding);

        content = new MultiLineLabel[splitDescription.length];
        for (int i = 0; i < splitDescription.length; i++) {
            Component text = splitDescription[i];
            content[i] = MultiLineLabel.create(minecraft.font, text, dialogWidth - 2 * dialogPadding);
        }

        int buttonWidth = dialogWidth / (buttonSettings.size() + (checkBox ? 1 : 0)) - ((2 * dialogPadding) / (buttonSettings.size() + (checkBox ? 1 : 0))) - (buttonSettings.size() < 2 ? 0 : 4);
        int buttonHeight = 20;

        for (int i = 0; i < (buttonSettings.size() + (checkBox ? 1 : 0)); i++) {
            int x = (int) ((float) ((float) width / 2 - ((float) dialogWidth / 2)) + dialogPadding + ((buttonWidth + 2) * i));
            int y = (int) ((float) ((float) height / 2 + ((float) DIALOG_IMAGE_HEIGHT / 2)) - buttonHeight - buttonTopDownPadding - 2);

            if (checkBox && i == 0) {
                checkBoxInstance = new Checkbox(x, y, 20, 20, TCMComponent.translatable("gui.tcm.update_dialog.checkBox.text").copy().withStyle(Style.EMPTY.withColor(0x555555)), Config.INSTANCE.neverShowUpdateDialog);
                buttons.add(
                        addRenderableWidget(checkBoxInstance)
                );
            } else {
                Tuple<Component, BiConsumer<Minecraft, AbstractButton>> buttonSetting = buttonSettings.get(i - (checkBox ? 1 : 0));
                buttons.add(
                        addRenderableWidget(Button.builder(buttonSetting.getA(), (button) -> {
                                    buttonSetting.getB().accept(minecraft, button);
                                }).pos(x + (checkBox ? 6 : 0), y).size(buttonWidth, buttonHeight).build()
                        ));
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        if (minecraft == null) return;

        this.renderBackground(guiGraphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
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
        );


        titleLabel.renderLeftAlignedNoShadow(
                guiGraphics,
                (int) ((float) ((float) width / 2 - ((float) dialogWidth / 2)) + dialogPadding),
                (int) ((float) ((float) height / 2 - ((float) DIALOG_IMAGE_HEIGHT / 2)) + titleTopPadding),
                16, 0
        );

        int contentTopPadding = (int) (((float) height / 2) - ((float) DIALOG_IMAGE_HEIGHT / 2) + titleTopPadding + minecraft.font.lineHeight + titleAndContentPadding);
        int contentRegionY = minecraft.font.lineHeight + 3;

        int c = 0;
        for (MultiLineLabel label : content) {
            label.renderLeftAlignedNoShadow(
                    guiGraphics,
                    (int) ((float) ((float) width / 2 - ((float) dialogWidth / 2)) + dialogPadding),
                    contentTopPadding + (c * 16),
                    16, 0
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
        super.onClose();
    }
}
