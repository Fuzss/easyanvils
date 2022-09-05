package fuzs.easyanvils.mixin;

import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(AnvilBlock.class)
abstract class AnvilBlockMixin extends FallingBlock implements EntityBlock {

    public AnvilBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use$inject$head(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        if (!worldIn.isClientSide) {
            if (worldIn.getBlockEntity(pos) instanceof AnvilBlockEntity blockEntity) {
                player.openMenu(blockEntity);
                if (player.containerMenu instanceof ModAnvilMenu) {
                    // items might still be in inventory slots, so this needs to update so that correct result is shown
                    player.containerMenu.slotsChanged(blockEntity);
                }
                player.awardStat(Stats.INTERACT_WITH_ANVIL);
                callbackInfo.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }

    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    public void getMenuProvider$inject$head(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> callback) {
        callback.setReturnValue(level.getBlockEntity(pos) instanceof MenuProvider blockEntity ? blockEntity : null);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.get().create(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (worldIn.getBlockEntity(pos) instanceof AnvilBlockEntity blockEntity) {
                Containers.dropContents(worldIn, pos, blockEntity);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }
}
