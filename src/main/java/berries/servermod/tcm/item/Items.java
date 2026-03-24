package berries.servermod.tcm.item;

import net.minecraft.world.item.Item;
import org.mtr.mod.CreativeModeTabs;

public class Items {
    public interface StaticFields {
        Editor EDITOR = new Editor(new Item.Properties());

        Cash POUND_1 = new Cash(1);
        Cash POUND_5 = new Cash(5);
        Cash POUND_10 = new Cash(10);
        Cash POUND_50 = new Cash(50);
        Cash POUND_100 = new Cash(100);

        CreditCard CREDIT_CARD_TIANCHENG_BUSINESS_BANK = new CreditCard("天城商业银行");
    }
}
