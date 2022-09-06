package fuzs.easyanvils.api.event.entity.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

@FunctionalInterface
public interface AnvilRepairCallback {
    Event<AnvilRepairCallback> EVENT = EventFactory.createArrayBacked(AnvilRepairCallback.class, listeners -> (Player player, ItemStack left, ItemStack right, ItemStack output, double breakChance) -> {
        for (AnvilRepairCallback event : listeners) {
            OptionalDouble optional = event.onAnvilRepair(player, left, right, output, breakChance);
            if (optional.isPresent()) breakChance = optional.getAsDouble();
        }
        return OptionalDouble.of(breakChance);
    });

    /**
     * called when the player takes the output item from an anvil, used to determine the chance by which the anvil will break down one stage
     *
     * @param player            the player interacting with the anvil
     * @param left              left input item stack
     * @param right             right input item stack
     * @param output            the output stack the player is about to take
     * @param breakChance       chance for the anvil to break down one stage
     * @return                  the new <code>breakChance</code>
     */
    OptionalDouble onAnvilRepair(Player player, ItemStack left, ItemStack right, ItemStack output, double breakChance);
}
