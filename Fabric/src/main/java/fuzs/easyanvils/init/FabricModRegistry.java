package fuzs.easyanvils.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.init.RegistryManager;
import fuzs.puzzleslib.init.RegistryReference;
import fuzs.puzzleslib.init.builder.ModBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EasyAnvils.MOD_ID);
    public static final RegistryReference<BlockEntityType<AnvilBlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityTypeBuilder("anvil", () -> ModBlockEntityTypeBuilder.of(AnvilBlockEntity::new, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL));

    public static void touch() {

    }
}
