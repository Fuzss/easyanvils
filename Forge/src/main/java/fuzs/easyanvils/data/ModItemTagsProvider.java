package fuzs.easyanvils.data;

import fuzs.easyanvils.init.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemTagsProvider extends TagsProvider<Item> {

    public ModItemTagsProvider(DataGenerator p_126546_, String modId, ExistingFileHelper fileHelperIn) {
        super(p_126546_, Registry.ITEM, modId, fileHelperIn);
    }

    @Override
    protected void addTags() {
        this.tag(ModRegistry.ANVIL_REPAIR_ITEMS_TAG).add(Items.IRON_BLOCK).replace(false);
    }
}
