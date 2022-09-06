package fuzs.easyanvils.core;

import fuzs.easyanvils.handler.AnvilRepairContext;
import fuzs.easyanvils.handler.ForgeAnvilRepairContext;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;

import java.util.function.IntConsumer;

public class ForgeAbstractions implements CommonAbstractions {

    @Override
    public AnvilRepairContext anvilRepairContextOf(AnvilMenu anvilMenu, Player player, String itemName, Container resultSlots, IntConsumer repairItemCountCost, IntConsumer cost) {
        return new ForgeAnvilRepairContext(anvilMenu, player, itemName, resultSlots, repairItemCountCost, cost);
    }
}
