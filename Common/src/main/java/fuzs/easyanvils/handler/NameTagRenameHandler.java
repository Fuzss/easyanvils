package fuzs.easyanvils.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.S2COpenNameTagEditorMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class NameTagRenameHandler {

    public static Optional<InteractionResultHolder<ItemStack>> onRightClickItem(Level level, Player player, InteractionHand hand) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).editNameTags) return Optional.empty();
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && stack.is(Items.NAME_TAG)) {
            if (!level.isClientSide) {
                EasyAnvils.NETWORK.sendTo(new S2COpenNameTagEditorMessage(hand, stack.getHoverName().getString()), (ServerPlayer) player);
            }
            return Optional.of(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
        }
        return Optional.empty();
    }
}
