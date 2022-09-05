package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.mixin.accessor.ItemCombinerMenuAccessor;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class ModAnvilMenu extends AnvilMenu implements ContainerListener {

    public ModAnvilMenu(int id, Inventory inventory) {
        // this is important, vanilla super adds a listener to the SimpleContainer which is required for the renaming edit box
        // we have nothing in the other constructor that is required on the client anyways
        super(id, inventory);
    }

    public ModAnvilMenu(int id, Inventory inventory, Container inputSlots, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, containerLevelAccess);
        ((ItemCombinerMenuAccessor) this).setInputSlots(inputSlots);
        this.slots.set(0, Util.make(new Slot(inputSlots, 0, 27, 47), slot -> slot.index = 0));
        this.slots.set(1, Util.make(new Slot(inputSlots, 1, 76, 47), slot -> slot.index = 1));
        this.addSlotListener(this);
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
    protected boolean mayPickup(Player player, boolean hasStack) {
        // change cost requirement from > 0 to >= 0 to allow for free name tag renames
        return (player.getAbilities().instabuild || player.experienceLevel >= this.getCost()) && this.getCost() >= 0;
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
        this.removeSlotListener(this);
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        // this is only ever run server side
        if (containerToSend == this) {
            if (dataSlotIndex >= 0 && dataSlotIndex < 2) {
                this.slotsChanged(this.inputSlots);
            }
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {

    }
}
