package fuzs.easyanvils.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.S2CAnvilRepairMessage;
import fuzs.easyanvils.network.S2COpenNameTagEditorMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;

public class ItemInteractionHandler {

    public static Optional<InteractionResultHolder<ItemStack>> onRightClickItem(Level level, Player player, InteractionHand hand) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).editNameTagsNoAnvil) return Optional.empty();
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && stack.is(Items.NAME_TAG)) {
            if (!level.isClientSide) {
                EasyAnvils.NETWORK.sendTo(new S2COpenNameTagEditorMessage(hand, stack.getHoverName().getString()), (ServerPlayer) player);
            }
            return Optional.of(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
        }
        return Optional.empty();
    }

    public static Optional<InteractionResult> onRightClickBlock(Level level, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).anvilRepairing) return Optional.empty();
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.IRON_BLOCK)) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.ANVIL) && tryRepairAnvil(level, pos, state)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
            }
        }
        return Optional.empty();
    }

    public static boolean tryRepairAnvil(Level level, BlockPos pos, BlockState state) {
        BlockState repairedState = getRepairedState(state);
        if (repairedState != null) {
            if (!level.isClientSide) {
                level.setBlock(pos, repairedState, 2);
                EasyAnvils.NETWORK.sendToAllNear(new S2CAnvilRepairMessage(pos, repairedState), pos, level);
            }
            return true;
        }
        return false;
    }

    @Nullable
    private static BlockState getRepairedState(BlockState state) {
        if (state.is(Blocks.DAMAGED_ANVIL)) {
            return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
        } else {
            return state.is(Blocks.CHIPPED_ANVIL) ? Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)) : null;
        }
    }

    public static OptionalDouble onAnvilRepair(Player player, ItemStack left, ItemStack right, ItemStack output, double breakChance) {
        if (EasyAnvils.CONFIG.get(ServerConfig.class).riskFreeAnvilRenaming && right.isEmpty()) return OptionalDouble.of(0.0);
        return OptionalDouble.of(EasyAnvils.CONFIG.get(ServerConfig.class).anvilBreakChance);
    }
}
