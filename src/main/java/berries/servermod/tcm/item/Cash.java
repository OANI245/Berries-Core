package berries.servermod.tcm.item;

import net.minecraft.world.item.Item;

public class Cash extends Item {
    public final short denomination;

    public Cash(int denomination) {
        super(new Item.Properties());
        this.denomination = (short) Math.min(denomination, Short.MAX_VALUE);
    }
}
