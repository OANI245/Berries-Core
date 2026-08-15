package berries.servermod.tcm.util;

import berries.servermod.tcm.item.Cash;
import berries.servermod.tcm.item.Items;
import berries.servermod.tcm.packet.PacketPayServer;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PayServer {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final List<Integer> deno = List.of(100, 50, 10, 5, 1);

    public static final Map<GameProfile, Consumer<Integer>> onFinishPlayersMap = new HashMap<>();

    public static void pay(MinecraftServer server, ServerPlayer serverPlayer, float money) {
        executor.submit(() -> {
            try {
                Thread.currentThread().setName("TCM-Pay");

                AtomicInteger currentMoney = new AtomicInteger((int) Math.ceil(money));

                AtomicInteger allPlayerCashes = new AtomicInteger(0);

                serverPlayer.getInventory().items.forEach((item) -> {
                    if (item.getItem() instanceof Cash cashItem) {
                        allPlayerCashes.addAndGet((item.getCount() * (int)cashItem.denomination));
                    }
                });

                /*System.out.println("All cashes in the player: " + allPlayerCashes.get());
                System.out.println("Need Money: " + money);*/

                if (allPlayerCashes.get() < (int)Math.ceil(money)) {
                    server.tell(new TickTask(1, () -> {
                        PacketPayServer.sendPayResultS2C(serverPlayer, 1);
                        if (onFinishPlayersMap.containsKey(serverPlayer.getGameProfile())) {
                            onFinishPlayersMap.get(serverPlayer.getGameProfile()).accept(1);
                            onFinishPlayersMap.remove(serverPlayer.getGameProfile());
                        }
                    }));
                    return;
                }

                for (ItemStack item : serverPlayer.getInventory().items) {
                    if (item.getItem() instanceof Cash cashItem) {
                        if (deno.contains((int) cashItem.denomination)) {
                            int removeCount = Math.min(item.getCount(), cashItem.denomination < currentMoney.get() ?
                                    (int) (currentMoney.get() / cashItem.denomination) : 1);
                            item.setCount(item.getCount() - removeCount);
                            currentMoney.addAndGet(-(cashItem.denomination * removeCount));
                            if (currentMoney.get() <= 0) {
                                break;
                            }
                        }
                    }
                }

                var changes = calculateChange(-currentMoney.get());
                server.tell(new TickTask(0, () -> {
                    giveItem(Items.StaticFields.POUND_100, serverPlayer, changes.get(deno.get(0)));
                    giveItem(Items.StaticFields.POUND_50, serverPlayer, changes.get(deno.get(1)));
                    giveItem(Items.StaticFields.POUND_10, serverPlayer, changes.get(deno.get(2)));
                    giveItem(Items.StaticFields.POUND_5, serverPlayer, changes.get(deno.get(3)));
                    giveItem(Items.StaticFields.POUND_1, serverPlayer, changes.get(deno.get(4)));
                    serverPlayer.containerMenu.broadcastChanges();
                }));

                server.tell(new TickTask(1, () -> {
                    PacketPayServer.sendPayResultS2C(serverPlayer, 0);
                    if (onFinishPlayersMap.containsKey(serverPlayer.getGameProfile())) {
                        onFinishPlayersMap.get(serverPlayer.getGameProfile()).accept(0);
                        onFinishPlayersMap.remove(serverPlayer.getGameProfile());
                    }
                }));
            } catch (Throwable e) {
                e.printStackTrace();
                server.tell(new TickTask(1, () -> {
                    PacketPayServer.sendPayResultS2C(serverPlayer, 2);
                    if (onFinishPlayersMap.containsKey(serverPlayer.getGameProfile())) {
                        onFinishPlayersMap.get(serverPlayer.getGameProfile()).accept(2);
                        onFinishPlayersMap.remove(serverPlayer.getGameProfile());
                    }
                }));
            }
        });
    }

    /**
     * Credit by DeepSeek.
     *
     * @param amount 找零
     * @return 各种面额钱币数量
     */
    public static Map<Integer, Integer> calculateChange(int amount) {
        Map<Integer, Integer> change = new HashMap<>();

        for (int denomination : deno) {
            if (amount >= denomination) {
                int count = (int) (amount / denomination);
                change.put(denomination, count);
                amount %= denomination;
            } else {
                change.put(denomination, 0);
            }
        }

        return change;
    }

    private static void giveItem(Item item, ServerPlayer serverPlayer, int i) {
        int j = item.getMaxStackSize();
        int k = j * 100;
        if (i > k) {
            serverPlayer.sendSystemMessage(TCMComponent.translatable("commands.give.failed.toomanyitems").copy().withStyle(ChatFormatting.RED));
            return;
        } else {
            int l = i;

            while (l > 0) {
                int m = Math.min(j, l);
                ItemStack stack = new ItemStack(item, m);
                l -= m;
                boolean bl = serverPlayer.getInventory().add(stack);
                if (bl && stack.isEmpty()) {
                    stack.setCount(1);
                    serverPlayer.containerMenu.broadcastChanges();
                }
            }
        }
    }
}
