package berries.servermod.tcm;

import com.mojang.datafixers.util.Function3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.mtr.mapping.holder.ClientPlayNetworkHandler;

import java.lang.module.ResolutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class TCMRegistry<T> {
    public static final TCMRegistry<Block> BLOCK = new TCMRegistry<>((l, b) ->
            Registry.register(BuiltInRegistries.BLOCK, l, b));

    public static final TCMRegistry<Item> ITEM = new TCMRegistry<>((l, i) ->
            Registry.register(BuiltInRegistries.ITEM, l, i));

    public static final TCMRegistry<BlockEntityType<? extends BlockEntity>> BLOCK_ENTITY_TYPE = new TCMRegistry<>((l, be) ->
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, l, be));

    private final BiFunction<ResourceLocation, T, T> function;

    public TCMRegistry(BiFunction<ResourceLocation, T, T> fc) {
        this.function = fc;
    }

    @SuppressWarnings("all")
    public T register(ResourceLocation l, T t) {
        return function.apply(l, t);
    }

    public static void registerPlayerJoinEvent(Consumer<ServerPlayer> consumer) {
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverWorld) -> {
            if (entity instanceof ServerPlayer) {
                consumer.accept((ServerPlayer) entity);
            }
        });
    }

    public static void registerPlayerLeaveEvent(Consumer<ServerPlayer> consumer) {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, serverWorld) -> {
            if (entity instanceof ServerPlayer) {
                consumer.accept((ServerPlayer) entity);
            }
        });
    }

    public static void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf packet) {
        ServerPlayNetworking.send(player, id, packet);
    }

    public static void registerNetworkReceiver(ResourceLocation id, ServerPlayNetworking.PlayChannelHandler cb) {
        ServerPlayNetworking.registerGlobalReceiver(id, cb);
    }
}
