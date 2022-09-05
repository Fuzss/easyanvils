package fuzs.easyanvils.init;

import fuzs.easyanvils.EasyAnvils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModRegistry {
    public static final TagKey<Item> ANVIL_REPAIR_ITEMS_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(EasyAnvils.MOD_ID, "anvil_repair_items"));

    public static void touch() {

    }
}
