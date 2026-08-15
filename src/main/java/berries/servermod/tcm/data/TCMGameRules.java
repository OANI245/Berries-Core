package berries.servermod.tcm.data;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;import net.minecraft.world.level.GameRules;

public interface TCMGameRules {
    GameRules.Key<GameRules.IntegerValue> TICKET_CHECK_START_ARRIVING_TIME =
            GameRuleRegistry.register("ticketCheckStartArrivingTime", GameRules.Category.MISC,
                    GameRuleFactory.createIntRule(13)
            );
    GameRules.Key<GameRules.IntegerValue> TICKET_CHECK_END_ARRIVING_TIME =
            GameRuleRegistry.register("ticketCheckEndArrivingTime", GameRules.Category.MISC,
                    GameRuleFactory.createIntRule(5)
            );

    static void register() {}
}
