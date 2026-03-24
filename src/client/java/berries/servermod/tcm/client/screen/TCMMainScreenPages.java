package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.data.TCMDynamicResourceCacheV2;
import berries.servermod.tcm.client.packet.PacketClearItemClient;
import berries.servermod.tcm.client.packet.PacketGetItemClient;
import berries.servermod.tcm.client.packet.PacketTeleportClient;
import berries.servermod.tcm.client.screen.widget.*;
import berries.servermod.tcm.signal.ItemGettingSignal;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TCMMainScreenPages {
    private final TCMMainScreen screen;
    private final int contentWidth;
    private final int titleHeight = 22;
    private final int titleX = 18;
    private final int titleY = 30;

    public TCMMainScreenPages(TCMMainScreen screen) {
        this.screen = screen;
        this.contentWidth = this.screen.width - 32;
    }

    public Page getHomePage() {
        Page instance = this.screen.createPage(0, SlideShowWidget.create(0, 0, contentWidth, contentWidth / 16 * 5, true, new ResourceLocation[]{new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/images/i_1.png"), new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/images/i_2.png")}),
                new TextWidget(titleX, titleY, titleHeight * 4 / 7 * 6, titleHeight * 4 / 7, Minecraft.getInstance().font, TCMComponent.translatable("gui.tcm.main.nav.1"), false, 2));

        //instance.addWidget(new Barrier((int) (contentWidth / 2.0F + 32 + 0.5F), contentWidth / 16 * 5 + 16, 1, 96));
        MetroTile[] arrayTiles = new MetroTile[]{new MetroTile(titleX + 32 - 4, contentWidth / 16 * 5 + 8, (contentWidth - (2 * (titleX - 4))) / 2 - 8, 88, new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/bilibili.png"), TCMComponent.text("作者主页"), TCMComponent.text("点击一下，支持一下模组作者吧QAQ"), (button) -> {
            Util.getPlatform().openUri("https://space.bilibili.com/489942446/");
        }), new MetroTile(32 + contentWidth / 2 + 4, contentWidth / 16 * 5 + 8, (contentWidth - (2 * (titleX - 4))) / 2 - 8, 88, new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/afdian.png"), TCMComponent.text("爱发电"), TCMComponent.text("请我们的建设者以及天城模组、资源包制作成员吃个饭吧~"), (button) -> {
            Util.getPlatform().openUri("https://afdian.com/a/mctiancheng");
        })};
        arrayTiles[0].setBackgroundColor(0xFFFFAEC8);
        arrayTiles[1].setBackgroundColor(0xFFD2BBFF);
        instance.addWidgets(arrayTiles);

        return instance;
    }

    public Page getPlayerUtilsPage() {
        Page instance = this.screen.createPage(1, new TextWidget(titleX, titleY, titleHeight * 4 / 7 * 6, titleHeight * 4 / 7, Minecraft.getInstance().font, TCMComponent.translatable("gui.tcm.main.nav.2"), false, 2.6F));

        AtomicInteger heights = new AtomicInteger(0);

        instance.addWidgets(getPropertyCheckBox(heights, titleX + 32, 72, contentWidth - (titleX * 2), TCMComponent.translatable("gui.tcm.main.player_util.property_1.title"), TCMComponent.translatable("gui.tcm.main.player_util.property_1.description"), Config.INSTANCE.sendMorningAndNightMessage, (bl) -> {
            Config.INSTANCE.sendMorningAndNightMessage = bl;
        }));

        List<MetroButtonGrid.MetroButtonData> data0 = new ArrayList<>(List.of(new MetroButtonGrid.MetroButtonData[]{MetroButtonGrid.INSTANCE.dataOfGetItem(TCMComponent.text("获取鞘翅"), ItemGettingSignal.ELYTRA, 1), MetroButtonGrid.INSTANCE.dataOfGetItem(TCMComponent.text("获取烟花火箭*64"), ItemGettingSignal.FIREWORK_ROCKET, 64), MetroButtonGrid.INSTANCE.dataOfGetItem(TCMComponent.text("获取绿宝石*64"), ItemGettingSignal.EMERALD, 64), new MetroButtonGrid.MetroButtonData(TCMComponent.text("清空背包"), (btn) -> {
            PacketClearItemClient.sendClearItemC2S();
            Config.INSTANCE.saveConfig();
            Minecraft.getInstance().setScreen(null);
        })}));

        if (Minecraft.getInstance().getCurrentServer() != null) {
            data0.add(0, new MetroButtonGrid.MetroButtonData(TCMComponent.text("切换TABTPS Bossbar显示"), (btn) -> {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.connection.sendCommand("/tabtps toggle bossbar");
                    Config.INSTANCE.saveConfig();
                    Minecraft.getInstance().setScreen(null);
                }
            }));
        }

        if (Minecraft.getInstance().level != null) {
            instance.addWidget(new TextWidget(titleX + 32, 80 + (heights.get()), contentWidth - (titleX * 2), 10, Minecraft.getInstance().font, TCMComponent.text("快捷功能").copy().withStyle(Style.EMPTY.withBold(true))));
            instance.addWidgets(MetroButtonGrid.INSTANCE.newButtons(heights, 32, 95 + (heights.get()), this.screen.width, titleX - 1, data0.toArray(new MetroButtonGrid.MetroButtonData[0]), true));
        }

        heights.addAndGet(16);

        if (Minecraft.getInstance().level == null) {
            instance.addWidget(new TextWidget(titleX + 32, 90 + (heights.get()), contentWidth - (titleX * 2), 10, Minecraft.getInstance().font, TCMComponent.text("天城主要地点介绍").copy().withStyle(Style.EMPTY.withBold(true))));
        } else {
            instance.addWidget(new TextWidget(titleX + 32, 90 + (heights.get()), contentWidth - (titleX * 2), 10, Minecraft.getInstance().font, TCMComponent.text("快捷传送").copy().withStyle(Style.EMPTY.withBold(true))));
        }

        List<MetroTileGrid.MetroTileData> data1 = new ArrayList<>();
        if (!TCMClient.syncTeleports.isEmpty()) {
            for (Tuple<Tuple<String, String>, Vec3> syncTeleport : TCMClient.syncTeleports) {
                data1.add(new MetroTileGrid.MetroTileData(TCMComponent.text(syncTeleport.getA().getA()), TCMComponent.text(syncTeleport.getA().getB()), (tile) -> {
                    if (Minecraft.getInstance().player != null) {
                        PacketTeleportClient.sendTeleportC2S(syncTeleport.getB());
                        Config.INSTANCE.saveConfig();
                        Minecraft.getInstance().setScreen(null);
                    }
                }));
            }
        } else {
            data1 = List.of(MetroTileGrid.INSTANCE.dataOfTeleport(TCMComponent.text("默认地点"), TCMComponent.text("位于TCMMainScreenPages.java的第111行。非调试情况下，你不应该看到它。"), new Vec3(0, 0, 0) //坐标
            ));
        }

        MetroTile[] arrayTiles = MetroTileGrid.INSTANCE.newTiles(32, 103 + (heights.get()), this.screen.width, titleX - 1, data1.toArray(new MetroTileGrid.MetroTileData[0]), Minecraft.getInstance().level != null);
        instance.addWidgets(arrayTiles);

        return instance;
    }

    public Page getSettingsPage() {
        Page instance = this.screen.createPage(3, new TextWidget(titleX, titleY, titleHeight * 4 / 7 * 6, titleHeight * 4 / 7, Minecraft.getInstance().font, TCMComponent.translatable("gui.tcm.main.nav.4"), false, 2.6F));

        AtomicInteger heights = new AtomicInteger(0);

        instance.addWidgets(getPropertyButton(heights, titleX + 32, 72, contentWidth - (titleX * 2), TCMComponent.translatable("gui.tcm.main.settings.property_1.title"), TCMComponent.translatable("gui.tcm.main.settings.property_1.description"), new Component[]{TCMComponent.translatable("gui.tcm.main.settings.property_1.value_1"), TCMComponent.translatable("gui.tcm.main.settings.property_1.value_2"), TCMComponent.translatable("gui.tcm.main.settings.property_1.value_3")}, switch (Config.INSTANCE.defaultPage) {
            case "PAGE_MAIN":
            default:
                yield 0;
            case "PAGE_PLAYER_UTILS":
                yield 1;
            case "PAGE_ABOUT_MOD":
                yield 2;
        }, (i, btn) -> {
            String[] pages = new String[]{"PAGE_MAIN", "PAGE_PLAYER_UTILS", "PAGE_ABOUT_MOD"};
            Config.INSTANCE.defaultPage = pages[i];
        }));

        /*instance.addWidgets(getPropertyCheckBox(heights, titleX + 32, 72 + heights.get(), contentWidth - (titleX * 2), TCMComponent.translatable("gui.tcm.main.settings.property_2.title"), TCMComponent.translatable("gui.tcm.main.settings.property_2.description"), Config.INSTANCE.useCustomLEDFont, (bl) -> {
            Config.INSTANCE.useCustomLEDFont = bl;
            TCMDynamicResourceCacheV2.instance.reload();
        }));*/

        instance.addWidgets(getPropertyButton(heights, titleX + 32, 72 + heights.get(), contentWidth - (titleX * 2), TCMComponent.translatable("gui.tcm.main.settings.property_3.title"), TCMComponent.translatable("gui.tcm.main.settings.property_3.description"), new Component[]{TCMComponent.translatable("gui.tcm.main.settings.property_3.value_1"), TCMComponent.translatable("gui.tcm.main.settings.property_3.value_2"), TCMComponent.translatable("gui.tcm.main.settings.property_3.value_3")}, switch (Config.INSTANCE.ringLoadingAnimation) {
            case "style1" -> {
                yield 1;
            }
            case "style2" -> {
                yield 2;
            }
            default -> {
                yield 0;
            }
        }, (i, btn) -> {
            String[] selects = new String[]{"disabled", "style1", "style2"};
            Config.INSTANCE.ringLoadingAnimation = selects[i];
        }));

        /*instance.addWidgets(getPropertyButton(heights, titleX + 32, 72 + heights.get(),
                contentWidth - (titleX * 2),
                TCMComponent.translatable("gui.tcm.main.settings.property_3.title"),
                TCMComponent.translatable("gui.tcm.main.settings.property_3.description"),
                new Component[]{
                        TCMComponent.translatable("gui.tcm.main.settings.property_3.button")
                },
                0, (i, btn) -> {
                }));*/

        return instance;
    }

    /*public static AbstractWidget[] getPropertyCheckBox(int x, int y, int width, Component text, boolean selected, Consumer<Boolean> onChange) {
        return new AbstractWidget[]{new TextWidget(x, y + 5, width - 20, 10, Minecraft.getInstance().font, text), new ToggleSwitch(x + width - 20, y, 20, 20, TCMComponent.text(""), selected, onChange)};
    }*/

    public static AbstractWidget[] getPropertyCheckBox(int x, int y, int width, Component text, Component description, boolean selected, Consumer<Boolean> onChange) {
        return new AbstractWidget[]{new TextWidget(x, y - 2, width - 20, 10, Minecraft.getInstance().font, text), new TextWidget(x, y + 20 - 8, width - 20, 10, Minecraft.getInstance().font, description.copy().withStyle(Style.EMPTY.withColor(0xBDBDBD))), new Checkbox(x + width - 20, y, 20, 20, TCMComponent.text(""), selected) {
            @Override
            public void onPress() {
                super.onPress();
                onChange.accept(this.selected());
            }
        }};
    }

    public static AbstractWidget[] getPropertyCheckBox(AtomicInteger input, int x, int y, int width, Component text, Component description, boolean selected, Consumer<Boolean> onChange) {
        AbstractWidget[] temp = getPropertyCheckBox(x, y, width, text, description, selected, onChange);
        if (temp.length < 3) {
            throw new RuntimeException("Array length is too short.");
        }
        input.addAndGet(temp[1].getY() - temp[0].getY() + 26);
        return temp;
    }

    public static AbstractWidget[] getPropertyButton(int x, int y, int width, Component text, Component description, Component[] selects, int index, BiConsumer<Integer, Button> onPress) {
        AtomicInteger i = new AtomicInteger(Math.min(index, selects.length - 1));

        return new AbstractWidget[]{new TextWidget(x, y - 2, width - 60, 10, Minecraft.getInstance().font, text), new TextWidget(x, y + 20 - 8, width - 60, 10, Minecraft.getInstance().font, description.copy().withStyle(Style.EMPTY.withColor(0xBDBDBD))), new MetroButton(x + width - 60, y, 60, 20, selects[i.get()], (button) -> {
            if (i.get() >= selects.length - 1) {
                i.set(0);
            } else {
                i.addAndGet(1);
            }

            try {
                button.setMessage(selects[i.get()]);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                button.setMessage(TCMComponent.text("Unknown Message: " + e.getMessage()));
            }

            onPress.accept(i.get(), button);
        })};
    }

        public static AbstractWidget[] getPropertyButton(AtomicInteger input, int x, int y, int width, Component text, Component description, Component[] selects, int index, BiConsumer<Integer, Button> onPress) {
        AbstractWidget[] temp = getPropertyButton(x, y, width, text, description, selects, index, onPress);
        if (temp.length < 3) {
            throw new RuntimeException("Array length is too short.");
        }
        input.addAndGet(temp[1].getY() - temp[0].getY() + 26);
        return temp;
    }
}
