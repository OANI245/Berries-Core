package berries.servermod.tcm.client.screen;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class TCMColorSchemes {
    private static final Map<String, Integer> lightSchemes = new HashMap<>();
    private static final Map<String, Integer> darkSchemes = new HashMap<>();

    static {
        addScheme("primary", "#006a64", "#81d5cd");
        addScheme("secondary", "#4a6360", "#b0ccc8");
        addScheme("tertiary", "#48617a", "#afc9e7");
        addScheme("error", "#ba1a1a", "#ffb4ab");
        addScheme("on_primary", "#ffffff", "#003734");
        addScheme("on_secondary", "#ffffff", "#1c3532");
        addScheme("on_tertiary", "#ffffff", "#18324a");
        addScheme("on_error", "#ffffff", "#690005");
        addScheme("primary_container", "#9df2e9", "#00504b");
        addScheme("secondary_container", "#cce8e4", "#324b49");
        addScheme("tertiary_container", "#cfe5ff", "#304962");
        addScheme("error_container", "#ffdad6", "#93000a");
        addScheme("on_primary_container", "#00504b", "#9df2e9");
        addScheme("on_secondary_container", "#324b49", "#cce8e4");
        addScheme("on_tertiary_container", "#304962", "#cfe5ff");
        addScheme("on_error_container", "#93000a", "#ffdad6");
        addScheme("surface_dim", "#d5dbda", "#0e1514");
        addScheme("surface", "#f4fbf9", "#0e1514");
        addScheme("surface_bright", "#f4fbf9", "#343a39");
        addScheme("surface_container_lowest", "#ffffff", "#090f0f");
        addScheme("surface_container_low", "#eff5f3", "#161d1c");
        addScheme("surface_container", "#e9efed", "#1a2120");
        addScheme("surface_container_high", "#e3e9e8", "#252b2a");
        addScheme("surface_container_highest", "#dde4e2", "#2f3635");
        addScheme("on_surface", "#161d1c", "#dde4e2");
        addScheme("on_surface_variant", "#3f4947", "#bec9c6");
        addScheme("outline", "#6f7977", "#899391");
        addScheme("outline_variant", "#bec9c6", "#3f4947");
        addScheme("shadow", "#000000", "#000000");
    }

    private static void addScheme(String key, int value1, int value2) {
        lightSchemes.put(key, value1);
        darkSchemes.put(key, value2);
    }

    private static void addScheme(String key, String value1, String value2) {
        try {
            lightSchemes.put(key, Integer.parseInt(value1.replaceAll("#", ""), 16) + (0xFF << 24));
            darkSchemes.put(key, Integer.parseInt(value2.replaceAll("#", ""), 16) + (0xFF << 24));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void addScheme(String key, Color value1, Color value2) {
        lightSchemes.put(key, value1.getRGB());
        darkSchemes.put(key, value2.getRGB());
    }

    public static int getScheme(String key, boolean isDark) {
        return isDark ? darkSchemes.getOrDefault(key, 0) : lightSchemes.getOrDefault(key, 0);
    }

    private TCMColorSchemes() {}
}
