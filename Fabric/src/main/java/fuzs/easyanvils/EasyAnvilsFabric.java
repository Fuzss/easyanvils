package fuzs.easyanvils;

import fuzs.easyanvils.handler.ItemInteractionHandler;
import fuzs.puzzleslib.core.CoreServices;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class EasyAnvilsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor(EasyAnvils.MOD_ID).accept(new EasyAnvils());
        registerHandlers();
    }

    private static void registerHandlers() {
        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            return ItemInteractionHandler.onRightClickItem(world, player, hand).orElse(InteractionResultHolder.pass(ItemStack.EMPTY));
        });
        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            return ItemInteractionHandler.onRightClickBlock(world, player, hand, hitResult).orElse(InteractionResult.PASS);
        });
    }
}
