package fuzs.easyanvils.client;

import fuzs.easyanvils.client.renderer.blockentity.AnvilRenderer;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.client.core.ClientModConstructor;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

public class EasyAnvilsClient implements ClientModConstructor {

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.get(), AnvilRenderer::new);
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.ANVIL_MENU_TYPE.get(), AnvilScreen::new);
    }
}
