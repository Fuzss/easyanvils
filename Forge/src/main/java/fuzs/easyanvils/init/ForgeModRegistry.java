package fuzs.easyanvils.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.integration.ApothAnvilBlockEntity;
import fuzs.easyanvils.world.level.block.entity.ForgeAnvilBlockEntity;
import fuzs.puzzleslib.core.CommonFactories;
import fuzs.puzzleslib.core.ModLoaderEnvironment;
import fuzs.puzzleslib.init.RegistryManager;
import fuzs.puzzleslib.init.RegistryReference;
import fuzs.puzzleslib.init.builder.ModBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ForgeModRegistry {
    private static final RegistryManager REGISTRY = CommonFactories.INSTANCE.registration(EasyAnvils.MOD_ID);
    public static final RegistryReference<BlockEntityType<BlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityTypeBuilder("anvil", () -> ModBlockEntityTypeBuilder.of(getBlockEntityFactory(), Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL));

    private static ModBlockEntityTypeBuilder.ModBlockEntitySupplier<BlockEntity> getBlockEntityFactory() {
        if (ModLoaderEnvironment.INSTANCE.isModLoadedSafe("apotheosis")) {
            return new ModBlockEntityTypeBuilder.ModBlockEntitySupplier<BlockEntity>() {

                @Override
                public BlockEntity create(BlockPos blockPos, BlockState blockState) {
                    return new ApothAnvilBlockEntity(blockPos, blockState);
                }
            };
        }
        return (blockPos, blockState) -> new ForgeAnvilBlockEntity(blockPos, blockState);
    }

    public static void touch() {

    }
}
