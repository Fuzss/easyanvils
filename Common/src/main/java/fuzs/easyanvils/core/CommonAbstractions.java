package fuzs.easyanvils.core;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface CommonAbstractions {

    boolean onAnvilChange(AnvilMenu container, @NotNull ItemStack left, @NotNull ItemStack right, Container outputSlot, String name, int baseCost, Player player);

    boolean isBookEnchantable(ItemStack inputStack, ItemStack bookStack);
}
