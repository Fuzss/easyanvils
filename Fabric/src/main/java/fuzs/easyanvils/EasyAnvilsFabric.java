package fuzs.easyanvils;

import fuzs.easyanvils.api.event.AnvilUpdateCallback;
import fuzs.easyanvils.api.event.entity.player.AnvilRepairCallback;
import fuzs.easyanvils.config.CommonConfig;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import fuzs.easyanvils.init.FabricModRegistry;
import fuzs.easyanvils.integration.ThingsIntegration;
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
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class EasyAnvilsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricModRegistry.touch();
        CoreServices.FACTORIES.modConstructor(EasyAnvils.MOD_ID).accept(new EasyAnvils());
        registerHandlers();
        registerIntegration();
    }

    private static void registerHandlers() {
        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            return ItemInteractionHandler.onRightClickItem(world, player, hand).orElse(InteractionResultHolder.pass(ItemStack.EMPTY));
        });
        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            return ItemInteractionHandler.onRightClickBlock(world, player, hand, hitResult).orElse(InteractionResult.PASS);
        });
        AnvilRepairCallback.EVENT.register(ItemInteractionHandler::onAnvilRepair);
    }

    @SuppressWarnings("Convert2MethodRef")
    private static void registerIntegration() {
        if (EasyAnvils.CONFIG.get(CommonConfig.class).thingsIntegration && CoreServices.ENVIRONMENT.isModLoaded("things")) {
            AnvilUpdateCallback.EVENT.register((ItemStack left, ItemStack right, MutableObject<ItemStack> output, String name, MutableInt cost, MutableInt materialCost, Player player) -> {
                return ThingsIntegration.onAnvilUpdate(left, right, output, name, cost, materialCost, player);
            });
        }
    }
}
