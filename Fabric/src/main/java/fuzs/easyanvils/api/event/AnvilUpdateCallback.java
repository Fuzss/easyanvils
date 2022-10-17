package fuzs.easyanvils.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Optional;

@FunctionalInterface
public interface AnvilUpdateCallback {
    Event<AnvilUpdateCallback> EVENT = EventFactory.createArrayBacked(AnvilUpdateCallback.class, listeners -> (ItemStack left, ItemStack right, MutableObject<ItemStack> output, String name, MutableInt cost, MutableInt materialCost, Player player) -> {
        for (AnvilUpdateCallback event : listeners) {
            if (event.onAnvilUpdate(left, right, output, name, cost, materialCost, player).isPresent()) {
                return Optional.of(Unit.INSTANCE);
            }
        }
        return Optional.empty();
    });

    /**
     * called before a result item is generated from the two input slots in an anvil
     *
     * @param left              the item stack placed in the left anvil input slot
     * @param right             the item stack placed in the right anvil input slot
     * @param output            access to the item that will be placed in the result slot, always empty by default, vanilla logic will be cancelled when this is no longer empty
     * @param name              item name entered into the anvil name text box
     * @param cost              level cost for this operation
     * @param materialCost      material repair cost for this operation
     * @param player            the player interacting with the menu
     * @return                  is present when vanilla logic is cancelled, nothing else from the event is kept; to use your own <code>cost</code> and so make sure <code>output</code> is not empty
     */
    Optional<Unit> onAnvilUpdate(ItemStack left, ItemStack right, MutableObject<ItemStack> output, String name, MutableInt cost, MutableInt materialCost, Player player);
}
