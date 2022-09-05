package fuzs.easyanvils.client;

import fuzs.easyanvils.client.handler.NameTagTooltipHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class EasyAnvilsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerHandlers();
    }

    private static void registerHandlers() {
        ItemTooltipCallback.EVENT.register(NameTagTooltipHandler::onItemTooltip);
    }
}
