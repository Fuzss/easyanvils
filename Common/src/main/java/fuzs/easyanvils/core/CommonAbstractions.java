package fuzs.easyanvils.core;

import fuzs.easyanvils.handler.AnvilRepairContext;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;

import java.util.function.IntConsumer;

public interface CommonAbstractions {

    default AnvilRepairContext anvilRepairContextOf(AnvilMenu anvilMenu, Player player, String itemName, Container resultSlots, IntConsumer repairItemCountCost, IntConsumer cost) {
        return new AnvilRepairContext(anvilMenu, player, itemName, resultSlots, repairItemCountCost, cost);
    }
}
