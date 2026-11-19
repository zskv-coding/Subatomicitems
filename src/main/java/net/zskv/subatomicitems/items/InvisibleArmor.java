package net.zskv.subatomicitems.items;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.zskv.subatomicitems.ExploitedItems;

import java.util.List;

public class InvisibleArmor {

    public static final String HELMET = "invisible_helmet";
    public static final String CHESTPLATE = "invisible_chestplate";
    public static final String LEGGINGS = "invisible_leggings";
    public static final String BOOTS = "invisible_boots";

    public static void register() {
        ExploitedItems.register(HELMET, r -> {
            ItemStack helmet = makeArmor(r, Items.DIAMOND_HELMET, EquipmentSlot.HEAD, "Fully Invisible Helmet", "Hides your head");
            enchant(r, helmet, Enchantments.RESPIRATION, 3);
            enchant(r, helmet, Enchantments.AQUA_AFFINITY, 1);
            return helmet;
        });

        ExploitedItems.register(CHESTPLATE, r -> makeArmor(r, Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST, "Fully Invisible Chestplate", "Hides your body and arms"));

        ExploitedItems.register(LEGGINGS, r -> {
            ItemStack leggings = makeArmor(r, Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS, "Fully Invisible Leggings", "Hides your legs");
            enchant(r, leggings, Enchantments.SWIFT_SNEAK, 3);
            return leggings;
        });

        ExploitedItems.register(BOOTS, r -> {
            ItemStack boots = makeArmor(r, Items.DIAMOND_BOOTS, EquipmentSlot.FEET, "Fully Invisible Boots", "Doesn't show when worn");
            enchant(r, boots, Enchantments.FEATHER_FALLING, 4);
            enchant(r, boots, Enchantments.DEPTH_STRIDER, 3);
            enchant(r, boots, Enchantments.SOUL_SPEED, 3);
            return boots;
        });
    }

    private static ItemStack makeArmor(RegistryWrapper.WrapperLookup r, Item item, EquipmentSlot slot, String name, String lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).styled(s -> s.withColor(Formatting.WHITE).withItalic(false)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(lore).styled(s -> s.withColor(Formatting.GRAY).withItalic(false)))));

        // this model doesn't exist so the armor doesn't render
        stack.set(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(slot)
                .equipSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND)
                .model(RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of("subatomicitems", "invisible")))
                .build());

        enchant(r, stack, Enchantments.PROTECTION, 4);
        enchant(r, stack, Enchantments.UNBREAKING, 3);
        enchant(r, stack, Enchantments.MENDING, 1);
        return stack;
    }

    private static void enchant(RegistryWrapper.WrapperLookup r, ItemStack stack, RegistryKey<Enchantment> enchantment, int level) {
        stack.addEnchantment(r.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantment), level);
    }
}