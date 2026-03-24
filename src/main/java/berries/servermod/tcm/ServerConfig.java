package berries.servermod.tcm;

import berries.servermod.tcm.util.TCMComponent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

import java.io.*;
import java.util.*;

public class ServerConfig {
    /**
     * Config类可用实例
     */
    public static final ServerConfig INSTANCE;
    private static final MutableComponent PREFIX = TCMComponent.text(UFEInfo.MOD_NAME + " 提醒您：").copy()
            .withStyle(
                    Style.EMPTY
                            .withColor(0xFFB6C1)
                            .withBold(true)
            );
    private static final String LINE_SEPARATOR = "\n";

    static {
        //在Config类中，实现功能不选择使用100%static方法和成员变量。
        INSTANCE = new ServerConfig();
    }

    public List<Tuple<Tuple<String, String>, Vec3>> teleports = List.of(
            new Tuple<>(
                    new Tuple<>(
                            "默认地点",
                            "这是一个代码中默认的传送地点，非调试情况下，你不应该看到它。"
                    ),
                    new Vec3(-116.5, 66.5, -92.5)
            )
    );

    public Map<Long, MutableComponent> timeMessages;

    public int port = 8400;
    public String directionIP = "localhost:8400";

    /**
     * 私有构造方法，仅供本类static块使用。
     * <br/>获取实例需使用 <code>Config.INSTANCE</code>
     */
    private ServerConfig() {
        this.timeMessages = new HashMap<>();
        timeMessages.put(100L, PREFIX.copy().append(
                TCMComponent.text(
                        "各位玩家"
                ).copy().withStyle(
                        Style.EMPTY.withColor(0x39C5BB)
                )
        ).append("，").append(
                TCMComponent.text(
                        "早上好！"
                ).copy().withStyle(
                        Style.EMPTY.withColor(ChatFormatting.YELLOW)
                )
        ));
    }

    /**
     * 读取模组设置的方法，此方法会更新实例内的成员变量
     */
    public void readConfig() {
        String filePath = String.format("%s%sberries_core_server.json", FabricLoader.getInstance().getConfigDir(), File.separator);
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            saveConfig();
        }

        try (FileReader reader = new FileReader(configFile)) {
            this.teleports = new ArrayList<>();
            JsonElement element = JsonParser.parseReader(new JsonReader(new BufferedReader(reader)));
            JsonObject root = element.getAsJsonObject();
            if (root.has("teleports")) {
                JsonArray tpsArray = root.get("teleports").getAsJsonArray();
                for (JsonElement jsonElement : tpsArray) {
                    JsonObject block = jsonElement.getAsJsonObject();
                    String title = "";
                    String description = "";
                    Vec3 pos = new Vec3(0, 0, 0);
                    if (block.has("pos")) {
                        JsonArray posArray = block.get("pos").getAsJsonArray();
                        if (posArray.size() >= 3) {
                            pos = new Vec3(posArray.get(0).getAsLong(), posArray.get(1).getAsLong(), posArray.get(2).getAsLong());
                        } else {
                            pos = new Vec3(0, 64, 0);
                        }
                    }
                    if (block.has("title")) {
                        title = block.get("title").getAsString();
                    }
                    if (block.has("description")) {
                        description = block.get("description").getAsString();
                    }

                    this.teleports.add(new Tuple<>(new Tuple<>(title, description), pos));
                }
            }

            if (root.has("time_messages")) {
                /*
                {
                    "time_messages": [
                        {
                            "text": [{"text": "Test 1", "color": 0x66CCFF, "bold": true}]
                        }
                    ]
                }
                 */

                this.timeMessages = new HashMap<>();
                JsonArray tmsArray = root.get("time_messages").getAsJsonArray();
                for (JsonElement jsonElement : tmsArray) {
                    JsonObject block = jsonElement.getAsJsonObject();
                    MutableComponent message = Component.empty().copy();
                    long tickTime = 100;

                    if (block.has("time")) {
                        tickTime = block.get("time").getAsLong();
                    }

                    if (timeMessages.containsKey(tickTime)) {
                        message = timeMessages.get(tickTime).copy().append(LINE_SEPARATOR);
                    }

                    if (block.has("text")) {
                        JsonArray jsonTextArray = block.get("text").getAsJsonArray();
                        for (Object jsonText : jsonTextArray) {
                            if (jsonText instanceof JsonObject) {
                                if (!((JsonObject) jsonText).has("text")) {
                                    continue;
                                }
                                MutableComponent child = TCMComponent.text(((JsonObject) jsonText).get("text").getAsString()).copy();
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
            }

            if (root.has("web_port")) {
                this.port = root.get("web_port").getAsInt();
            }

            if (root.has("direction_ip")) {
                this.directionIP = root.get("direction_ip").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入模组设置的方法，此方法不会更新实例内的成员变量
     */
    public void saveConfig() {
        String filePath = String.format("%s%sberries_core_server.json", FabricLoader.getInstance().getConfigDir(), File.separator);
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
            JsonArray tpsArray = new JsonArray();
            for (Tuple<Tuple<String, String>, Vec3> teleport : teleports) {
                JsonObject block = new JsonObject();
                block.addProperty("title", teleport.getA().getA());
                block.addProperty("description", teleport.getA().getB());

                Vec3 pos = teleport.getB();
                JsonArray posArray = new JsonArray();
                posArray.add(pos.x());
                posArray.add(pos.y());
                posArray.add(pos.z());
                block.add("pos", posArray);

                tpsArray.add(block);
            }
            root.add("teleports", tpsArray);

            JsonArray tmsArray = new JsonArray(); //"time_messages"
            timeMessages.forEach((tickTime, text) -> {
                JsonObject block = new JsonObject();
                block.addProperty("time", tickTime);
                JsonArray textArray = new JsonArray();
                Component temp = null;
                if (!text.plainCopy().getString().isEmpty()) {
                    temp = text.plainCopy();
                    JsonObject textBlock = new JsonObject();
                    textBlock.addProperty("text", text.plainCopy().getString());
                    Style style = text.getStyle();
                    if (style.getColor() != null) {
                        textBlock.addProperty("color", style.getColor().getValue());
                    }
                    textBlock.addProperty("bold", style.isBold());
                    textBlock.addProperty("font", style.getFont().toString());
                    textArray.add(textBlock);
                }
                for (Component sibling : text.getSiblings()) {
                    if (temp != null && sibling.getString().equals(temp.getString())) {
                        continue;
                    }

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
            root.add("time_messages", tmsArray);
            root.addProperty("web_port", port);
            root.addProperty("direction_ip", directionIP);
            String json = root.toString();
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
