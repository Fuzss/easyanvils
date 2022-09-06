package fuzs.easyanvils.mixin;

import fuzs.easyanvils.api.event.entity.player.AnvilRepairCallback;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
abstract class FabricAnvilMenuMixin extends ItemCombinerMenu {
    @Unique
    private float easyanvils_breakChance;

    public FabricAnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I")))
    protected void onTake$inject$invoke$setItem(Player player, ItemStack itemStack, CallbackInfo callback) {
        this.easyanvils_breakChance = (float) AnvilRepairCallback.EVENT.invoker().onAnvilRepair(player, this.inputSlots.getItem(0), this.inputSlots.getItem(1), itemStack, 0.12).orElseThrow();
    }

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", shift = At.Shift.AFTER), cancellable = true)
    protected void onTake$inject$invoke$set(Player player, ItemStack itemStack, CallbackInfo callback) {
        // just copy this part from vanilla, not in the mood to mixin into this lambda
        this.access.execute((level, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            if (!player.getAbilities().instabuild && blockState.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < this.easyanvils_breakChance) {
                BlockState blockState2 = AnvilBlock.damage(blockState);
                if (blockState2 == null) {
                    level.removeBlock(blockPos, false);
                    level.levelEvent(1029, blockPos, 0);
                } else {
                    level.setBlock(blockPos, blockState2, 2);
                    level.levelEvent(1030, blockPos, 0);
                }
            } else {
                level.levelEvent(1030, blockPos, 0);
            }

        });
        // always cancel vanilla, we handle everything
        callback.cancel();
    }
}
