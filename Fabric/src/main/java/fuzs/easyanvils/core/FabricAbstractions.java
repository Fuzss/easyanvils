package fuzs.easyanvils.core;

import fuzs.easyanvils.api.event.AnvilUpdateCallback;
import fuzs.easyanvils.mixin.accessor.AnvilMenuAccessor;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class FabricAbstractions implements CommonAbstractions {

    @Override
    public boolean onAnvilChange(AnvilMenu container, @NotNull ItemStack left, @NotNull ItemStack right, Container outputSlot, String name, int baseCost, Player player) {
        MutableObject<ItemStack> output = new MutableObject<>(ItemStack.EMPTY);
        MutableInt cost = new MutableInt(baseCost);
        MutableInt materialCost = new MutableInt(0);
        Optional<Unit> result = AnvilUpdateCallback.EVENT.invoker().onAnvilUpdate(left, right, output, name, cost, materialCost, player);
        if (result.isPresent()) return false;
        if (output.getValue().isEmpty()) return true;
        outputSlot.setItem(0, output.getValue());
        container.setData(0, cost.intValue());
        ((AnvilMenuAccessor) container).setRepairItemCountCost(materialCost.intValue());
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack inputStack, ItemStack bookStack) {
        return true;
    }
}
