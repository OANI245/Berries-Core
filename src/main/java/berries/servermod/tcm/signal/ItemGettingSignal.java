package berries.servermod.tcm.signal;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

/**
 * 一个枚举，存储物品id信息，GUI获取物品必须调用此枚举（可以防止某些人利用GUI获取代码获取tnt等危险物品）
 */
public enum ItemGettingSignal {
    ELYTRA(BuiltInRegistries.ITEM.getKey(Items.ELYTRA)),
    FIREWORK_ROCKET(BuiltInRegistries.ITEM.getKey(Items.FIREWORK_ROCKET)),
    EMERALD(BuiltInRegistries.ITEM.getKey(Items.EMERALD));

    private final ResourceLocation location; //物品id

    private ItemGettingSignal(ResourceLocation rl) {
        this.location = rl;
    }

    public ResourceLocation getLocation() {
        return location;
    }
}
