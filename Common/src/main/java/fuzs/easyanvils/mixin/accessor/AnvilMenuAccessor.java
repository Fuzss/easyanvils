package fuzs.easyanvils.mixin.accessor;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilMenu.class)
public interface AnvilMenuAccessor {

    @Accessor
    String getItemName();

    @Accessor
    void setRepairItemCountCost(int repairItemCountCost);
}
