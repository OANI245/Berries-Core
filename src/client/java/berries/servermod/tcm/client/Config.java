package berries.servermod.tcm.client;

import berries.servermod.tcm.util.TCMComponent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Mod的设置类
 * <br/>设有读取数据 <code>readConfig()</code> 以及写入数据 <code>saveConfig()</code>
 * <br/>在模组第一次加载时，会自动读取数据。
 * @author OANI_245
 */
@Environment(EnvType.CLIENT)
public class Config {
    /**
     * Config类可用实例
     */
    public static final Config INSTANCE;

    private static final MutableComponent PREFIX = TCMComponent.text("天城提醒您").copy()
            .withStyle(
                    Style.EMPTY
                            .withColor(0xFFB6C1)
                            .withBold(true)
            ).append(
                    "："
            );
    private static final String LINE_SEPARATOR = "\n";

    static {
        //在Config类中，实现功能不选择使用100%static方法和成员变量。
        INSTANCE = new Config();
        INSTANCE.readConfig();
    }

    public boolean isFirstUse = true;
    public boolean neverShowUpdateDialog = false;
    public boolean sendMorningAndNightMessage = true;
    public boolean useCustomLEDFont = true;
    public String ringLoadingAnimation =
            System.getProperty("os.name").toLowerCase().contains("windows") &&
                    !System.getProperty("os.name").toLowerCase().endsWith("11")  ? "style2" : "style1";
    public boolean developerMode = false;
    public boolean isInstalling = false;
    public boolean guiDarkMode = true;
    public int lastBuildVersion = 239;
    /**
     * 可用页面：PAGE_MAIN, PAGE_PLAYER_UTILS, PAGE_ABOUT_MOD, PAGE_MOD_SETTINGS
     * */
    public String defaultPage = "PAGE_MAIN";

    public Map<Long, MutableComponent> timeMessages;

    /**
     * 私有构造方法，仅供本类static块使用。
     * <br/>获取实例需使用 <code>Config.INSTANCE</code>
     */
    private Config() {
        this.timeMessages = new HashMap<>();
        timeMessages.put(100L, PREFIX.copy().append(
                TCMComponent.text(
                        "各位玩家"
                ).copy().withStyle(
                        Style.EMPTY.withColor(0x39C5BB).withBold(false)
                )
        ).append("，").append(
                TCMComponent.text(
                        "早上好！"
                ).copy().withStyle(
                        Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(false)
                )
        ));
    }

    /**
     * 读取模组设置的方法，此方法会更新实例内的成员变量
     */
    public void readConfig() {
        String filePath = String.format("%s%sberries_core.json", FabricLoader.getInstance().getConfigDir(), File.separator);
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            saveConfig();
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonElement element = JsonParser.parseReader(new JsonReader(new BufferedReader(reader)));
            JsonObject root = element.getAsJsonObject();
            if (root.has("first_use_mod")) {
                this.isFirstUse = root.get("first_use_mod").getAsBoolean();
            }
            if (root.has("default_main_page")) {
                this.defaultPage = root.get("default_main_page").getAsString();
            }
            if (root.has("send_morning_and_night_message")) {
                this.sendMorningAndNightMessage = root.get("send_morning_and_night_message").getAsBoolean();
            }
            if (root.has("never_show_update_dialog")) {
                this.neverShowUpdateDialog = root.get("never_show_update_dialog").getAsBoolean();
            }
            if (root.has("use_custom_led_font")) {
                this.useCustomLEDFont = root.get("use_custom_led_font").getAsBoolean();
            }
            if (root.has("use_ring_loading_animation")) {
                this.ringLoadingAnimation = root.get("use_ring_loading_animation").getAsBoolean() ? "style1" : "disabled";
            }
            if (root.has("ring_loading_animation")) {
                this.ringLoadingAnimation = root.get("ring_loading_animation").getAsString();
            }
            if (root.has("developer_mode")) {
                this.developerMode = root.get("developer_mode").getAsBoolean();
            }
            if (root.has("installing")) {
                this.isInstalling = root.get("installing").getAsBoolean();
            }
            if (root.has("gui_dark_mode")) {
                this.guiDarkMode = root.get("gui_dark_mode").getAsBoolean();
            }
            if (root.has("last_build")) {
                this.lastBuildVersion = root.get("last_build").getAsInt();
            }

            /*if (root.has("time_messages")) {
                *//*
                {
                    "time_messages": [
                        {
                            "text": [{"text": "Test 1", "color": 0x66CCFF, "bold": true}]
                        }
                    ]
                }
                 *//*

                this.timeMessages = new HashMap<>();
                JsonArray tmsArray = root.get("time_messages").getAsJsonArray();
                for (JsonElement jsonElement : tmsArray) {
                    JsonObject block = jsonElement.getAsJsonObject();
                    MutableComponent message = TextComponent.EMPTY.copy();
                    long tickTime = 100;

                    if (block.has("time")) {
                        tickTime = block.get("time").getAsLong();
                    }

                    if (timeMessages.containsKey(tickTime)) {
                        message = timeMessages.get(tickTime).append(LINE_SEPARATOR);
                    }

                    if (block.has("text")) {
                        JsonArray jsonTextArray = block.get("text").getAsJsonArray();
                        for (Object jsonText : jsonTextArray) {
                            if (jsonText instanceof JsonObject) {
                                if (((JsonObject) jsonText).has("text")) {
                                    continue;
                                }
                                MutableComponent child = new TextComponent(((JsonObject) jsonText).get("text").getAsString()).copy();
                                Style style = Style.EMPTY;
                                try {
                                    if (((JsonObject) jsonText).has("color")) {
                                        style = style.withColor(((JsonObject) jsonText).get("color").getAsInt());
                                    }
                                    if (((JsonObject) jsonText).has("bold")) {
                                        style = style.withBold(((JsonObject) jsonText).get("bold").getAsBoolean());
                                    }
                                    if (((JsonObject) jsonText).has("font")) {
                                        style = style.withFont(new ResourceLocation(((JsonObject) jsonText).get("font").getAsString()));
                                    }
                                } catch (Exception ignored) {
                                }

                                child.withStyle(style);
                                message.append(child);
                            }
                        }
                    }

                    timeMessages.put(tickTime, message);
                }
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入模组设置的方法，此方法不会更新实例内的成员变量
     */
    public void saveConfig() {
        String filePath = String.format("%s%sberries_core.json", FabricLoader.getInstance().getConfigDir(), File.separator);
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            try {
                if (!configFile.createNewFile()) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            JsonObject root = new JsonObject();
            root.addProperty("never_show_update_dialog", this.neverShowUpdateDialog);
            root.addProperty("first_use_mod", isFirstUse);
            root.addProperty("send_morning_and_night_message", sendMorningAndNightMessage);
            root.addProperty("use_custom_led_font", useCustomLEDFont);
            root.addProperty("default_main_page", defaultPage);
            root.addProperty("developer_mode", developerMode);
            root.addProperty("installing", isInstalling);
            root.addProperty("last_build", lastBuildVersion);
            root.addProperty("ring_loading_animation", ringLoadingAnimation);
            root.addProperty("gui_dark_mode", guiDarkMode);

            /*JsonArray tmsArray = new JsonArray(); //"time_messages"
            timeMessages.forEach((tickTime, text) -> {
                JsonObject block = new JsonObject();
                block.addProperty("time", tickTime);
                JsonArray textArray = new JsonArray();
                for (Component sibling : text.getSiblings()) {
                    JsonObject textBlock = new JsonObject();
                    textBlock.addProperty("text", sibling.getString());
                    Style style = sibling.getStyle();
                    if (style.getColor() != null) {
                        textBlock.addProperty("color", style.getColor().getValue());
                    }
                    textBlock.addProperty("bold", style.isBold());
                    textBlock.addProperty("font", style.getFont().toString());
                    textArray.add(textBlock);
                }
                block.add("text", textArray);
                tmsArray.add(block);
            });
            root.add("time_messages", tmsArray);*/
            String json = root.toString();
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
