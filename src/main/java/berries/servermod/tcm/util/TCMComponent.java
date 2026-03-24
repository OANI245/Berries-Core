package berries.servermod.tcm.util;

import net.minecraft.network.chat.Component;

public interface TCMComponent {
    static Component text(String text) {
        return Component.literal(text);
    }

    static Component text(String format, Object... args) {
        return Component.literal(String.format(format, args));
    }

    static Component translatable(String key) {
        return Component.translatable(key);
    }

    static Component translatable(String key, Object... args) {
        return Component.translatable(key, args);
    }
}
