package fuzs.easyanvils.mixin;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow
    @Final
    private DataSlot cost;
    @Unique
    private ItemStack easyanvils_resultStack;
    @Unique
    private int easyanvils_leftItemMaxLevel;
    @Unique
    private int easyanvils_rightItemMaxLevel;

    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @ModifyVariable(method = "createResult", at = @At(value = "STORE", ordinal = 0), ordinal = 8)
    public int createResult$modifyVariable$store$1(int leftItemMaxLevel) {
        // store this
        this.easyanvils_leftItemMaxLevel = leftItemMaxLevel;
        return leftItemMaxLevel;
    }

    @ModifyVariable(method = "createResult", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    public int createResult$modifyVariable$store$2(int rightItemMaxLevel) {
        // store this
        this.easyanvils_rightItemMaxLevel = rightItemMaxLevel;
        return rightItemMaxLevel;
    }

    @ModifyVariable(method = "createResult", at = @At(value = "STORE"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I", ordinal = 0)), ordinal = 7)
    public int createResult$modifyVariable$store$3(int rightItemMaxLevel) {
        // prevent a level higher than max level from being lowered to max value
        // feature specifically requested by a user
        int maxLevel = Math.max(this.easyanvils_leftItemMaxLevel, this.easyanvils_rightItemMaxLevel);
        return Math.max(maxLevel, rightItemMaxLevel);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", ordinal = 0, shift = At.Shift.AFTER), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setHoverName(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;")))
    public void createResult$inject$invoke$set(CallbackInfo callback) {
        ItemStack leftStack = this.inputSlots.getItem(0);
        ItemStack rightStack = this.inputSlots.getItem(1);
        if (EasyAnvils.CONFIG.get(ServerConfig.class).disablePriorWorkPenalty) {
            int j = leftStack.getBaseRepairCost() + (rightStack.isEmpty() ? 0 : rightStack.getBaseRepairCost());
            this.cost.set(this.cost.get() - j);
        }
        if (EasyAnvils.CONFIG.get(ServerConfig.class).freeNameTagRenames) {
            if (leftStack.is(Items.NAME_TAG) && rightStack.isEmpty() && this.cost.get() > 0) {
                this.cost.set(0);
            }
        }
    }

    @ModifyVariable(method = "createResult", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z", ordinal = 1), ordinal = 1)
    public ItemStack createResult$modifyVariable$field$instabuild(ItemStack resultStack) {
        // store result stack before next mixin as it might be set to empty
        this.easyanvils_resultStack = resultStack;
        return resultStack;
    }

    @ModifyVariable(method = "createResult", at = @At(value = "STORE"), ordinal = 1, slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z", ordinal = 1)))
    public ItemStack createResult$modifyVariable$store(ItemStack resultStack) {
        // restore result stack which is empty here
        if (this.cost.get() < EasyAnvils.CONFIG.get(ServerConfig.class).maxAnvilRepairCost) return this.easyanvils_resultStack;
        return resultStack;
    }
}
