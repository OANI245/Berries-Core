package berries.servermod.tcm.mixin;

import berries.servermod.tcm.packet.PacketPayServer;
import berries.servermod.tcm.util.PayServer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.InventoryHelper;
import org.mtr.mapping.mapper.PlayerHelper;
import org.mtr.mod.data.TicketSystem;
import org.mtr.mod.packet.PacketAddBalance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.mtr.mod.packet.PacketAddBalance.getAddAmount;

@Mixin(value = PacketAddBalance.class, remap = false)
public class MixinPacketAddBalance {
    @Final
    @Shadow
    private int index;

    @Inject(
            method = "runServer",
            at = @At("HEAD"),
            cancellable = true)
    public void runServer(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, CallbackInfo ci) {
        PacketPayServer.sendPayScreenS2C(serverPlayerEntity.data, getAddAmount(index), (status) -> {
            if (status == 0) {
                ServerWorld serverWorld = serverPlayerEntity.getServerWorld();
                TicketSystem.addBalance(new World((Level) serverWorld.data), new PlayerEntity((Player) serverPlayerEntity.data), getAddAmount(this.index));
                serverWorld.playSound((PlayerEntity) null, serverPlayerEntity.getBlockPos(), SoundEvents.getEntityExperienceOrbPickupMapped(), SoundCategory.getBlocksMapped(), 1.0F, 1.0F);
            }
        });
        ci.cancel();
    }
}
