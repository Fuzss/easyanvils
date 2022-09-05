package fuzs.easyanvils;

import fuzs.easyanvils.handler.NameTagRenameHandler;
import fuzs.puzzleslib.core.CoreServices;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EasyAnvilsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor(EasyAnvils.MOD_ID).accept(new EasyAnvils());
        registerHandlers();
    }

    private static void registerHandlers() {
        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            return NameTagRenameHandler.onRightClickItem(world, player, hand).orElse(InteractionResultHolder.pass(ItemStack.EMPTY));
        });
    }
}
