package fuzs.easyanvils.core;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

public final class ForgeAbstractions implements CommonAbstractions {

    public boolean onAnvilChange(AnvilMenu container, @NotNull ItemStack left, @NotNull ItemStack right, Container outputSlot, String name, int baseCost, Player player) {
        return ForgeHooks.onAnvilChange(container, left, right, outputSlot, name, baseCost, player);
    }

    @Override
    public boolean isBookEnchantable(ItemStack inputStack, ItemStack bookStack) {
        return inputStack.isBookEnchantable(bookStack);
    }
}
