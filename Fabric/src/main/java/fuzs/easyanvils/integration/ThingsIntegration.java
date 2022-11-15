package fuzs.easyanvils.integration;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Optional;
import java.util.function.Supplier;

public class ThingsIntegration {
    public static final TagKey<Item> HARDENING_CATALYST_BLACKLIST = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("things", "hardening_catalyst_blacklist"));
    private static final ResourceLocation HARDENING_CATALYST_ID = new ResourceLocation("things", "hardening_catalyst");
    private static final Supplier<Item> HARDENING_CATALYST_SUPPLIER = Suppliers.memoize(() -> Registry.ITEM.get(HARDENING_CATALYST_ID));

    public static Optional<Unit> onAnvilUpdate(ItemStack left, ItemStack right, MutableObject<ItemStack> output, String name, MutableInt cost, MutableInt materialCost, Player player) {

        if (!right.is(HARDENING_CATALYST_SUPPLIER.get())) return Optional.empty();

        // move this reference here to depend on as few classes from Things as possible
        if (left.getItem().getMaxDamage() == 0 || left.is(HARDENING_CATALYST_BLACKLIST)) return Optional.empty();
        if (left.getOrCreateTag().getByte("Unbreakable") == (byte) 1) return Optional.empty();

        ItemStack newOutput = left.copy();
        newOutput.getOrCreateTag().putByte("Unbreakable", (byte) 1);

        if (!StringUtils.isBlank(name)) {
            newOutput.setHoverName(Component.literal(name));
        } else {
            newOutput.resetHoverName();
        }

        output.setValue(newOutput);
        cost.setValue(30);
        return Optional.empty();
    }
}
