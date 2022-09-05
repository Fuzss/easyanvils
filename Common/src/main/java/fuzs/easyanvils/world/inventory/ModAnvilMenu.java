package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.mixin.accessor.ItemCombinerMenuAccessor;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModAnvilMenu extends AnvilMenu {

    public ModAnvilMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(2), ContainerLevelAccess.NULL);
    }

    public ModAnvilMenu(int id, Inventory inventory, Container inputSlots, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, containerLevelAccess);
        ((ItemCombinerMenuAccessor) this).setInputSlots(inputSlots);
        this.slots.set(0, Util.make(new Slot(inputSlots, 0, 27, 47), slot -> slot.index = 0));
        this.slots.set(1, Util.make(new Slot(inputSlots, 1, 76, 47), slot -> slot.index = 1));
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.ANVIL_MENU_TYPE.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inputSlots.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        // copied from container super method
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack itemstack = this.getCarried();
            if (!itemstack.isEmpty()) {
                if (player.isAlive() && !serverPlayer.hasDisconnected()) {
                    player.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    player.drop(itemstack, false);
                }
                this.setCarried(ItemStack.EMPTY);
            }
        }
    }
}
