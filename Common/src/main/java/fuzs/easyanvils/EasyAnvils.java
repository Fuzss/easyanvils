package fuzs.easyanvils;

import fuzs.easyanvils.config.ClientConfig;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.network.S2CAnvilRepairMessage;
import fuzs.easyanvils.network.S2COpenNameTagEditorMessage;
import fuzs.easyanvils.network.client.C2SNameTagUpdateMessage;
import fuzs.easyanvils.network.client.C2SRenameItemMessage;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyAnvils implements ModConstructor {
    public static final String MOD_ID = "easyanvils";
    public static final String MOD_NAME = "Easy Anvils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = CoreServices.FACTORIES.network(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder CONFIG = CoreServices.FACTORIES
            .clientConfig(ClientConfig.class, () -> new ClientConfig())
            .serverConfig(ServerConfig.class, () -> new ServerConfig());

    @Override
    public void onConstructMod() {
        CONFIG.bakeConfigs(MOD_ID);
        ModRegistry.touch();
        registerMessages();
    }

    @Override
    public void onCommonSetup() {
        DispenserBlock.registerBehavior(Items.IRON_BLOCK, new OptionalDispenseItemBehavior() {

            @Override
            public ItemStack execute(BlockSource source, ItemStack stack) {
                if (!EasyAnvils.CONFIG.get(ServerConfig.class).anvilRepairing) return super.execute(source, stack);
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos pos = source.getPos().relative(direction);
                Level level = source.getLevel();
                BlockState state = level.getBlockState(pos);
                this.setSuccess(true);
                if (state.is(BlockTags.ANVIL)) {
                    if (ItemInteractionHandler.tryRepairAnvil(level, pos, state)) {
                        stack.shrink(1);
                    } else {
                        this.setSuccess(false);
                    }
                    return stack;
                } else {
                    return super.execute(source, stack);
                }
            }
        });
    }

    private static void registerMessages() {
        NETWORK.register(S2COpenNameTagEditorMessage.class, S2COpenNameTagEditorMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SNameTagUpdateMessage.class, C2SNameTagUpdateMessage::new, MessageDirection.TO_SERVER);
        NETWORK.register(S2CAnvilRepairMessage.class, S2CAnvilRepairMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SRenameItemMessage.class, C2SRenameItemMessage::new, MessageDirection.TO_SERVER);
    }
}
