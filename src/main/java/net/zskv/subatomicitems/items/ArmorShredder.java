package net.zskv.subatomicitems.items;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.zskv.subatomicitems.ExploitedItems;

import java.util.List;

public class ArmorShredder {

    public static final String ID = "armor_shredder";

    public static void register() {
        ExploitedItems.register(ID, registries -> {
            ItemStack bow = new ItemStack(Items.BOW);
            bow.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Armor Shredder").styled(s -> s.withColor(Formatting.RED).withItalic(false)));
            bow.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Arrows break armor and shields on hit").styled(s -> s.withColor(Formatting.GRAY).withItalic(false)))));
            return bow;
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((target, source, baseDamage, damageTaken, blocked) -> {
            if (!(source.getSource() instanceof PersistentProjectileEntity arrow)) return;
            if (!ExploitedItems.is(arrow.getWeaponStack(), ID)) return;

            int broken = 0;

            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
                ItemStack held = target.getEquippedStack(slot);
                if (held.isOf(Items.SHIELD) && held.isDamageable()) {
                    if (target.isUsingItem()) target.clearActiveItem();
                    breakItem(target, held, slot);
                    broken++;
                }
            }

            if (!blocked) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack piece = target.getEquippedStack(slot);
                    if (slot.isArmorSlot() && !piece.isEmpty() && piece.isDamageable()) {
                        breakItem(target, piece, slot);
                        broken++;
                    }
                }
            }

            if (broken > 0 && source.getAttacker() instanceof ServerPlayerEntity shooter) {
                shooter.sendMessage(Text.literal("Broke " + broken + " items").formatted(Formatting.GRAY), true);
            }
        });
    }

    // stack.damage() gets reduced by unbreaking so just remove it
    private static void breakItem(LivingEntity target, ItemStack stack, EquipmentSlot slot) {
        Item item = stack.getItem();
        stack.decrement(1);
        target.sendEquipmentBreakStatus(item, slot);
    }
}