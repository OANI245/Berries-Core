package berries.servermod.tcm.client.screen;

import berries.servermod.tcm.UFEInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public interface GUILocations {
    ResourceLocation LOGO_LOCATION = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/logo.png");

    ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/background.png");
    ResourceLocation DIALOG_LOCATION = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/dialog.png");
    ResourceLocation XIBAO_BACKGROUND_LOCATION = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/xibao_background.png");

    ResourceLocation MAIN_SCREEN_NAVIGATION_BAR_LOCATION = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/main_screen_navigation_bar.png");
    ResourceLocation MAIN_SCREEN_NAVIGATION_BAR_EXPANDED_LOCATION = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/main_screen_navigation_bar_expanded.png");
    ResourceLocation MAIN_SCREEN_CONTENT_LOCATION = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/main_screen_content.png");

    ResourceLocation MAIN_SCREEN_ICON_HOME = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/navigation_item_home.png");
    ResourceLocation MAIN_SCREEN_ICON_PLAYER = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/navigation_item_player.png");
    ResourceLocation MAIN_SCREEN_ICON_ABOUT = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/navigation_item_about.png");
    ResourceLocation MAIN_SCREEN_ICON_SETTINGS = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/navigation_item_settings.png");
    ResourceLocation MAIN_SCREEN_ICON_PAGE = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/icon/page.png");

    ResourceLocation VISTA_STARTUP_BAR_BORDER = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/loading/vista_startup_border.png");
    ResourceLocation VISTA_STARTUP_BAR_MAIN = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/loading/vista_startup_main.png");

    ResourceLocation LOADING_ANIMATION_STYLE_1 = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/loading/loading_style_1.png");
    ResourceLocation LOADING_ANIMATION_STYLE_2 = new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/loading/loading_style_2.png");

    static ResourceLocation getTitleLocation(String name) {
        return new ResourceLocation(UFEInfo.MOD_ID, "textures/gui/title/" + Minecraft.getInstance().getLanguageManager().getSelected().trim().toLowerCase(Locale.ROOT) + "/" + name + ".png");
    }
}
