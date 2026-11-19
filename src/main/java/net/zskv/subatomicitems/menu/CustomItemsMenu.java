package net.zskv.subatomicitems.menu;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.zskv.subatomicitems.ExploitedItems;

public class CustomItemsMenu extends GenericContainerScreenHandler {

    public static final Text TITLE = Text.literal("Exploited Items");

    private static final int HEADER = 4;
    private static final int PREV = 48;
    private static final int CLOSE = 49;
    private static final int NEXT = 50;
    private static final int PER_PAGE = 28;

    private static final ItemStack GRAY = GuiItems.pane(Items.GRAY_STAINED_GLASS_PANE);
    private static final ItemStack PURPLE = GuiItems.pane(Items.PURPLE_STAINED_GLASS_PANE);
    private static final ItemStack MAGENTA = GuiItems.pane(Items.MAGENTA_STAINED_GLASS_PANE);
    private static final ItemStack BLACK = GuiItems.pane(Items.BLACK_STAINED_GLASS_PANE);

    private static final int[] BORDER = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            17, 26, 35, 44,
            53, 52, 51, 50, 49, 48, 47, 46, 45,
            36, 27, 18, 9
    };

    private final ServerPlayerEntity player;
    private final Inventory inv;

    private int page = 0;
    private int ticks = 0;
    private int lastAge = -1;
    private int pageLoadedAt = 0;
    private boolean closing = false;

    private final ItemStack[] pageItems = new ItemStack[PER_PAGE];
    private final int[] itemIndex = new int[PER_PAGE];
    private final boolean[] shown = new boolean[PER_PAGE];
    private final int[] glowUntil = new int[PER_PAGE];

    public CustomItemsMenu(int syncId, PlayerInventory playerInventory, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, new SimpleInventory(54), 6);
        this.player = player;
        this.inv = getInventory();

        for (int slot : BORDER) {
            inv.setStack(slot, BLACK.copy());
        }
        loadPage();
        playSound(RegistryEntry.of(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME), 0.6f, 1.4f);
    }

    @Override
    public void sendContentUpdates() {
        if (player.age != lastAge) {
            lastAge = player.age;
            ticks++;
            animateBorder();
            revealItems();
            removeGlow();
        }
        super.sendContentUpdates();
    }

    private void animateBorder() {
        if (ticks <= 5) {
            for (int slot : BORDER) {
                if (!isButton(slot) && Math.abs(slot % 9 - 4) < ticks) {
                    inv.setStack(slot, GRAY.copy());
                }
            }
            return;
        }

        if (ticks % 3 != 0) return;

        int a = (ticks / 3) % BORDER.length;
        int b = (a + BORDER.length / 2) % BORDER.length;

        for (int i = 0; i < BORDER.length; i++) {
            if (isButton(BORDER[i])) continue;

            int behindA = (a - i + BORDER.length) % BORDER.length;
            int behindB = (b - i + BORDER.length) % BORDER.length;

            ItemStack pane = GRAY;
            if (behindA == 0 || behindB == 0) {
                pane = MAGENTA;
            } else if (behindA <= 2 || behindB <= 2) {
                pane = PURPLE;
            }
            inv.setStack(BORDER[i], pane.copy());
        }
    }

    private void revealItems() {
        for (int i = 0; i < PER_PAGE; i++) {
            if (shown[i]) continue;
            if (i / 7 + i % 7 <= ticks - pageLoadedAt) {
                shown[i] = true;
                inv.setStack(slotFor(i), pageItems[i] == null ? ItemStack.EMPTY : pageItems[i].copy());
            }
        }
    }

    private void removeGlow() {
        for (int i = 0; i < PER_PAGE; i++) {
            if (glowUntil[i] != 0 && ticks >= glowUntil[i]) {
                glowUntil[i] = 0;
                inv.setStack(slotFor(i), pageItems[i].copy());
            }
        }
    }

    private void loadPage() {
        int total = ExploitedItems.ITEMS.size();

        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            if (index < total) {
                itemIndex[i] = index;
                ItemStack stack = ExploitedItems.ITEMS.get(index).create(player.getRegistryManager());
                pageItems[i] = GuiItems.addLore(stack, "Click to get one", Formatting.DARK_GRAY);
            } else {
                itemIndex[i] = -1;
                pageItems[i] = null;
            }
            shown[i] = false;
            glowUntil[i] = 0;
            inv.setStack(slotFor(i), ItemStack.EMPTY);
        }

        if (total == 0) {
            pageItems[10] = GuiItems.item(Items.COBWEB, "No items yet", Formatting.GRAY);
        }

        pageLoadedAt = ticks;

        inv.setStack(HEADER, GuiItems.item(Items.NETHER_STAR, "Exploited Items", Formatting.LIGHT_PURPLE,
                total + " items", "Page " + (page + 1) + "/" + pageCount()));
        inv.setStack(CLOSE, GuiItems.item(Items.BARRIER, "Close", Formatting.RED));
        inv.setStack(PREV, page > 0 ? GuiItems.item(Items.ARROW, "Previous page", Formatting.WHITE) : GRAY.copy());
        inv.setStack(NEXT, page < pageCount() - 1 ? GuiItems.item(Items.ARROW, "Next page", Formatting.WHITE) : GRAY.copy());
    }

    @Override
    public void onSlotClick(int slot, int button, SlotActionType action, PlayerEntity clicker) {
        if (action != SlotActionType.PICKUP && action != SlotActionType.QUICK_MOVE) return;
        if (slot < 0 || slot >= 54) return;

        if (slot == CLOSE) {
            playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 0.8f);
            closing = true;
            return;
        }

        if (slot == PREV && page > 0) {
            page--;
            loadPage();
            playSound(RegistryEntry.of(SoundEvents.ITEM_BOOK_PAGE_TURN), 1f, 0.9f);
            return;
        }

        if (slot == NEXT && page < pageCount() - 1) {
            page++;
            loadPage();
            playSound(RegistryEntry.of(SoundEvents.ITEM_BOOK_PAGE_TURN), 1f, 1.1f);
            return;
        }

        int i = indexFor(slot);
        if (i == -1 || !shown[i] || itemIndex[i] == -1) return;

        ItemStack item = ExploitedItems.ITEMS.get(itemIndex[i]).create(player.getRegistryManager());
        player.sendMessage(Text.literal("Got ").formatted(Formatting.GRAY).append(item.getName()), true);
        player.getInventory().offerOrDrop(item);
        playSound(RegistryEntry.of(SoundEvents.ENTITY_ITEM_PICKUP), 0.6f, 1f + player.getRandom().nextFloat() * 0.3f);

        ItemStack glowing = pageItems[i].copy();
        glowing.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        inv.setStack(slot, glowing);
        glowUntil[i] = ticks + 6;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return !closing;
    }

    private int pageCount() {
        return Math.max(1, (ExploitedItems.ITEMS.size() + PER_PAGE - 1) / PER_PAGE);
    }

    private boolean isButton(int slot) {
        if (slot == HEADER || slot == CLOSE) return true;
        if (slot == PREV) return page > 0;
        if (slot == NEXT) return page < pageCount() - 1;
        return false;
    }

    private int slotFor(int i) {
        return (i / 7 + 1) * 9 + i % 7 + 1;
    }

    private int indexFor(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        if (row < 1 || row > 4 || col < 1 || col > 7) return -1;
        return (row - 1) * 7 + col - 1;
    }

    private void playSound(RegistryEntry<SoundEvent> sound, float volume, float pitch) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(sound, SoundCategory.MASTER,
                player.getX(), player.getY(), player.getZ(), volume, pitch, player.getRandom().nextLong()));
    }
}