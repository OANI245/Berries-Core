package berries.servermod.tcm.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(GoalSelector.class)
public abstract class MixinGoalSelector {
    @Shadow public abstract void addGoal(int i, Goal goal);

    @Inject(
            method = "addGoal",
            at = @At("HEAD")
    )
    private void injected01(int i, Goal goal, CallbackInfo ci) {
        if (goal instanceof NearestAttackableTargetGoal<?>) {
            try {
                Field targetClassField = ((NearestAttackableTargetGoal<?>)goal).getClass().getDeclaredField("targetType");
                targetClassField.setAccessible(true);
                Field mobField = ((TargetGoal)goal).getClass().getDeclaredField("mob");
                mobField.setAccessible(true);
                if (((Class<?>) targetClassField.get(goal)) == Player.class) {
                    addGoal(i, new NearestAttackableTargetGoal<>((Mob) mobField.get(goal), AbstractVillager.class, true));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
