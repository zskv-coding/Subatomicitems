package net.zskv.subatomicitems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.zskv.subatomicitems.menu.CustomItemsMenu;

public class Subatomicitems implements ModInitializer {

    public static final String MOD_ID = "subatomicitems";

    @Override
    public void onInitialize() {
        ExploitedItems.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("customitems").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                        (syncId, inventory, p) -> new CustomItemsMenu(syncId, inventory, player),
                        CustomItemsMenu.TITLE));
                return 1;
            }));
        });
    }
}