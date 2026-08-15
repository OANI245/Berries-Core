package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.screen.widget.*;
import berries.servermod.tcm.client.screen.widget.ImageWidget;
import berries.servermod.tcm.util.TCMComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import icyllis.modernui.mc.MuiScreen;
import icyllis.modernui.mc.ScreenCallback;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TCMMainScreen extends Screen {
    public AbstractButton[] navigationItems;
    /*@SuppressWarnings("all")
    public final ScrollingContainer contentScroller = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return new Rectangle(0, 0, 1, TCMMainScreen.this.height);
        }

        @Override
        public int getMaxScrollHeight() {
            return (int) TCMMainScreen.this.getPageMaximumScrolled();
        }

        @Override
        public int getScrollBarX() {
            return TCMMainScreen.this.width - 6;
        }

        public void updatePosition(float delta) {
            super.updatePosition(delta);
        }
    };*/
    public ScrollBar scrollBar;
    public NavigationBar nav;

    public Page pageHome = null;
    public Page pagePlayerUtils = null;
    public Page pageModSettings = null;
    public Page pageAbout = null;

    public String currentPage = "NULL";
    public double currentScroll = 0.0;

    private final Screen perviousScreen;
    private final PanoramaRenderer panoramaRenderer;

    private final Queue<BiConsumer<ObjectObjectImmutablePair<GuiGraphics, Float>, IntIntImmutablePair>> latestRenderers = new LinkedList<>();

    public static final String[] PAGE_IDS = new String[] {"PAGE_MAIN", "PAGE_PLAYER_UTILS", "PAGE_ABOUT_MOD", "PAGE_MOD_SETTINGS"};

    private TCMMainScreen(Screen perviousScreen) {
        super(TCMComponent.text(""));
        this.perviousScreen = perviousScreen;
        Config.INSTANCE.readConfig();
        this.panoramaRenderer = new PanoramaRenderer(TitleScreen.CUBE_MAP);
    }

    private TCMMainScreen(Screen perviousScreen, String showPage) {
        this(perviousScreen);
        this.currentPage = showPage;
    }

    @SuppressWarnings("all")
    public static Screen open(Minecraft mc, Screen perviousScreen) {
        Screen instance = /*Config.INSTANCE.isFirstUse ? new OOBEScreen(perviousScreen) : */new TCMMainScreen(perviousScreen);
        mc.setScreen(instance);
        return instance;
    }

    public static TCMMainScreen getInstance(Screen perviousScreen) {
        return new TCMMainScreen(perviousScreen);
    }

    public void renderLastest(BiConsumer<ObjectObjectImmutablePair<GuiGraphics, Float>, IntIntImmutablePair> event) {
        latestRenderers.add(event);
    }

    @Override
    protected void init() {
        if (minecraft == null) return;
        nav = new NavigationBar(0, 0, 96, height);

        TCMMainScreenPages pagesCreator = new TCMMainScreenPages(this);

        pageHome = pagesCreator.getHomePage();
        pagePlayerUtils = pagesCreator.getPlayerUtilsPage();
        pageAbout = pagesCreator.getAboutPage();
        pageModSettings = pagesCreator.getSettingsPage();


        Page showingPage = getPageWithID(currentPage != null && !currentPage.equals("NULL") ? currentPage : Config.INSTANCE.defaultPage);
        if (showingPage == null) {
            /*Config.INSTANCE.defaultPage = "PAGE_MAIN";
            Config.INSTANCE.saveConfig();*/
            showingPage = pageHome;
        }

        nav.addNavItem(
                        TCMComponent.translatable("gui.tcm.main.nav.home"),
                        GUILocations.MAIN_SCREEN_ICON_HOME,
                        (item) -> {
                            if (!currentPage.equals(PAGE_IDS[0])) {
                                changePage(pageHome, item);
                            }
                        }
                )
                .addNavItem(
                        TCMComponent.translatable("gui.tcm.main.nav.player"),
                        GUILocations.MAIN_SCREEN_ICON_PLAYER,
                        (item) -> {
                            if (!currentPage.equals(PAGE_IDS[1])) {
                                changePage(pagePlayerUtils, item);
                            }
                        }
                )
                .addNavItem(
                        TCMComponent.translatable("gui.tcm.main.nav.about"),
                        GUILocations.MAIN_SCREEN_ICON_ABOUT,
                        (item) -> {
                            if (!currentPage.equals(PAGE_IDS[2])) {
                                changePage(pageAbout, item);
                            }
                        }
                )
                .addBottomNavItem(
                        TCMComponent.translatable("gui.tcm.main.nav.settings"),
                        GUILocations.MAIN_SCREEN_ICON_SETTINGS,
                        (item) -> {
                            if (!currentPage.equals(PAGE_IDS[3])) {
                                changePage(pageModSettings, item);
                            }
                        }
                );
        ((NavigationBarItem) nav.items.get(showingPage.id)).checked = true;
        scrollBar = new ScrollBar(width, 0, height, (float) showingPage.getHeight());
        scrollBar.scrollTo(currentScroll, false);
        showingPage.addAllRenderable(this::addRenderableWidget);
        this.addRenderableWidget(nav);
        nav.addAllRenderable(this::addRenderableWidget);

        this.currentPage = PAGE_IDS[showingPage.id];

        this.addRenderableWidget(scrollBar);

        super.init();
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (f != 0d && scrollBar != null) {
            this.currentScroll = 16.0 * -f * 2 + scrollBar.scrollTarget;
            scrollBar.scrollTo(currentScroll, true);
            return true;
        }
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return scrollBar != null && scrollBar.mouseDragged(d, e, i, f, g);
        //return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (minecraft == null) return;
        if (getPageWithID(currentPage) != null) {
            getPageWithID(currentPage).setScroll(scrollBar.scrollAmount);
        }

        if (this.minecraft.level != null) {
            this.renderBackground(guiGraphics);
        } else {
            //this.fillGradient(poseStack, 0, 0, this.width, this.height, -0x00CECECE, -0x00CACACA);
            panoramaRenderer.render(f, Mth.clamp(1.0F, 0.0F, 1.0F));
            /*RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GUILocations.BACKGROUND_LOCATION);

            guiGraphics.blit(
                    GUILocations.BACKGROUND_LOCATION,
                    0, 0, 0, 0,
                    Math.max(this.width, this.height / 9 * 16), Math.max(this.height, this.width / 16 * 9), Math.max(this.width, this.height / 9 * 16), Math.max(this.height, this.width / 16 * 9)
            );*/
        }

        super.render(guiGraphics, i, j, f);
        BiConsumer<ObjectObjectImmutablePair<GuiGraphics, Float>, IntIntImmutablePair> e;
        while ((e = latestRenderers.poll()) != null) {
            e.accept(new ObjectObjectImmutablePair<>(guiGraphics, f), new IntIntImmutablePair(i, j));
        }
    }

    public void fillMetroTile(GuiGraphics guiGraphics, int x, int y, int width, int height, int rgb) {
        guiGraphics.fillGradient(x, y, width, height, -(0xFFFFFFFF - rgb), -(0xFFFFFFFF - rgb));
    }

    @Override
    public void onClose() {
        Config.INSTANCE.saveConfig();
        if (minecraft != null) {
            minecraft.setScreen(perviousScreen);
        }
    }

    private Page getPageWithID(String id) {
        switch (id) {
            case "PAGE_PLAYER_UTILS" -> {
                return pagePlayerUtils;
            }
            case "PAGE_MOD_SETTINGS" -> {
                return pageModSettings;
            }
            case "PAGE_ABOUT_MOD" -> {
                return pageAbout;
            }
            case "PAGE_MAIN" -> {
                return pageHome;
            }
            default -> {
                return null;
            }
        }
    }

    private void changePage(Page page, NavigationBarItem item) {
        page.setScroll(0);
        Page current = getPageWithID(currentPage);
        if (current != null) {
            current.setScroll(0);
            this.scrollBar.setMaxScroll(page.getHeight());
            for (int i = 0; i < nav.items.size(); i++) {
                AbstractButton showingItem = nav.items.get(i);
                if (showingItem instanceof NavigationBarItem && showingItem != item) {
                    ((NavigationBarItem) showingItem).checked = false;
                }
            }
            item.checked = true;
            pageHome.removeAllRenderable(this::removeWidget);
            pagePlayerUtils.removeAllRenderable(this::removeWidget);
            pageAbout.removeAllRenderable(this::removeWidget);
            pageModSettings.removeAllRenderable(this::removeWidget);
            page.addAllRenderable(this::addRenderableWidget);
            this.scrollBar.resetScrolling();
            this.currentPage = PAGE_IDS[page.id];
        }
    }

    public double getPageMaximumScrolled() {
        if (getPageWithID(currentPage) != null) {
            return (double) getPageWithID(currentPage).getHeight();
        }
        return 0;
    }

    /*public static class OOBEScreen extends ScreenMapper {
        public Screen perviousScreen;

        public OOBEScreen(Screen perviousScreen) {
            super(TCMComponent.text(""));
            this.perviousScreen = perviousScreen;
        }

        @Override
        protected void init() {
            super.init();
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            this.renderBackground(poseStack, i);
            super.render(poseStack, i, j, f);
        }

        @Override
        public void onClose() {
            if (minecraft != null) {
                minecraft.setScreen(perviousScreen);
            }
        }
    }*/

    public Page createPage(int id, Object... instances) {
        List<Tuple<Integer, AbstractWidget>> varWidgets = new ArrayList<>();
        List<Tuple<Integer, MultiLineLabel>> varTextLabels = new ArrayList<>();

        for (Object instance : instances) {
            if (instance != null) {
                if (instance instanceof AbstractWidget) {
                    ((AbstractWidget) instance).setX(((AbstractWidget) instance).getX() + nav.getWidth() / 3);
                    varWidgets.add(new Tuple<>(((AbstractWidget) instance).getY(), (AbstractWidget) instance));
                } else if (instance instanceof MultiLineLabel) {
                    varTextLabels.add(new Tuple<>(0, (MultiLineLabel) instance));
                }
            }
        }

        return new Page(id) {
            private final List<Tuple<Integer, AbstractWidget>> widgets = varWidgets;
            private final List<Tuple<Integer, MultiLineLabel>> textLabels = varTextLabels;

            @Override
            public List<Tuple<Integer, AbstractWidget>> getWidgets() {
                return widgets;
            }

            @Override
            public List<Tuple<Integer, MultiLineLabel>> getTextLabels() {
                return textLabels;
            }

            @Override
            public void addWidget(AbstractWidget widget) {
                this.widgets.add(new Tuple<>(widget.getY(), widget));
            }
        };
    }
}
