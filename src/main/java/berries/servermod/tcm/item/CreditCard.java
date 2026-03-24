package berries.servermod.tcm.item;

import net.minecraft.world.item.Item;

public class CreditCard extends Item {
    private final String bankCompany;

    public CreditCard(String bankCompany) {
        super(new Item.Properties());
        this.bankCompany = bankCompany;
    }

    public String getBankCompany() {
        return bankCompany;
    }
}
