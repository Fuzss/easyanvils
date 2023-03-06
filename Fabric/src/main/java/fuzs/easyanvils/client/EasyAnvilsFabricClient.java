package fuzs.easyanvils.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.handler.NameTagTooltipHandler;
import fuzs.puzzleslib.client.core.ClientFactories;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class EasyAnvilsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientFactories.INSTANCE.clientModConstructor(EasyAnvils.MOD_ID).accept(new EasyAnvilsClient());
        registerHandlers();
    }

    private static void registerHandlers() {
        ItemTooltipCallback.EVENT.register(NameTagTooltipHandler::onItemTooltip);
    }
}
