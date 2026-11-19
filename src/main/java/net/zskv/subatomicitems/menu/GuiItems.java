package net.zskv.subatomicitems.menu;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class GuiItems {

    public static ItemStack pane(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(true, new LinkedHashSet<>()));
        return stack;
    }

    public static ItemStack item(Item item, String name, Formatting color, String... lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, text(name, color));

        List<Text> lines = new ArrayList<>();
        for (String line : lore) {
            lines.add(text(line, Formatting.GRAY));
        }
        if (!lines.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
        }
        return stack;
    }

    public static ItemStack addLore(ItemStack stack, String line, Formatting color) {
        LoreComponent lore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        stack.set(DataComponentTypes.LORE, lore.with(text(line, color)));
        return stack;
    }

    public static Text text(String text, Formatting color) {
        return Text.literal(text).styled(s -> s.withColor(color).withItalic(false));
    }
}