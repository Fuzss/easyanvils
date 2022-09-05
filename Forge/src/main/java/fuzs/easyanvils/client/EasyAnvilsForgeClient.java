package fuzs.easyanvils.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.handler.NameTagTooltipHandler;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = EasyAnvils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EasyAnvilsForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientCoreServices.FACTORIES.clientModConstructor(EasyAnvils.MOD_ID).accept(new EasyAnvilsClient());
        registerHandlers();
    }

    private static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener((final ItemTooltipEvent evt) -> {
            NameTagTooltipHandler.onItemTooltip(evt.getItemStack(), evt.getFlags(), evt.getToolTip());
        });
    }
}
