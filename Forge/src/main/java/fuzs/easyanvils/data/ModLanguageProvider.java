package fuzs.easyanvils.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(DataGenerator gen, String modId) {
        super(gen, modId, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("easyanvils.name_tag.edit", "Edit %s");
        this.add("easyanvils.item.name_tag.description", "Sneak + right-click to set a new name.");
    }
}
