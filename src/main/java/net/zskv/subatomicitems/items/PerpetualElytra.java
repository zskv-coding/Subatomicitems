package net.zskv.subatomicitems.items;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.zskv.subatomicitems.ExploitedItems;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PerpetualElytra {

    public static final String ID = "perpetual_elytra";

    private static final HashMap<UUID, FireworkRocketEntity> rockets = new HashMap<>();

    public static void register() {
        ExploitedItems.register(ID, registries -> {
            ItemStack elytra = new ItemStack(Items.ELYTRA);
            elytra.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Perpetual Elytra").styled(s -> s.withColor(Formatting.AQUA).withItalic(false)));
            elytra.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Always boosted, no rockets needed").styled(s -> s.withColor(Formatting.GRAY).withItalic(false)))));
            elytra.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
            return elytra;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                FireworkRocketEntity rocket = rockets.get(player.getUuid());
                boolean flying = player.isGliding() && ExploitedItems.is(player.getEquippedStack(EquipmentSlot.CHEST), ID);

                if (!flying) {
                    if (rocket != null) {
                        rocket.discard();
                        rockets.remove(player.getUuid());
                    }
                    continue;
                }

                if (rocket != null && rocket.isAlive() && rocket.getEntityWorld() == player.getEntityWorld()) continue;
                if (rocket != null) rocket.discard();

                rockets.put(player.getUuid(), spawnRocket(player));
            }

            rockets.entrySet().removeIf(entry -> {
                if (server.getPlayerManager().getPlayer(entry.getKey()) != null) return false;
                entry.getValue().discard();
                return true;
            });
        });
    }

    private static FireworkRocketEntity spawnRocket(ServerPlayerEntity player) {
        ServerWorld world = player.getEntityWorld();
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(3, List.of()));

        FireworkRocketEntity rocket = new FireworkRocketEntity(world, stack, player);
        rocket.setSilent(true);
        world.spawnEntity(rocket);
        return rocket;
    }
}