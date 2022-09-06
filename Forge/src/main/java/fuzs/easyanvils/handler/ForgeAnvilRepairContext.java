package fuzs.easyanvils.handler;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntConsumer;

public class ForgeAnvilRepairContext extends AnvilRepairContext {

    public ForgeAnvilRepairContext(AnvilMenu anvilMenu, Player player, String itemName, Container resultSlots, IntConsumer repairItemCountCost, IntConsumer cost) {
        super(anvilMenu, player, itemName, resultSlots, repairItemCountCost, cost);
    }

    @Override
    protected ItemStack testBookEnchantable(ItemStack rightInput, ItemStack itemstack1, boolean flag) {
        if (flag && !itemstack1.isBookEnchantable(rightInput)) itemstack1 = ItemStack.EMPTY;
        return itemstack1;
    }

    @Override
    protected boolean onAnvilChange(ItemStack leftInput, ItemStack rightInput, int j) {
        if (!net.minecraftforge.common.ForgeHooks.onAnvilChange(this.anvilMenu, leftInput, rightInput, this.resultSlots, this.itemName, j, this.player)) {
            this.applyLater();
            return true;
        }
        return false;
    }
}
