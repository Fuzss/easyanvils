package fuzs.easyanvils.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.init.RegistryManager;
import fuzs.puzzleslib.init.RegistryReference;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EasyAnvils.MOD_ID);
    public static final RegistryReference<BlockEntityType<AnvilBlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = REGISTRY.placeholder(Registry.BLOCK_ENTITY_TYPE_REGISTRY, "anvil");
    public static final RegistryReference<MenuType<ModAnvilMenu>> ANVIL_MENU_TYPE = REGISTRY.registerMenuTypeSupplier("repair", () -> ModAnvilMenu::new);

    public static void touch() {

    }
}
