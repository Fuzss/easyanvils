package fuzs.easyanvils;

import fuzs.easyanvils.data.ModLanguageProvider;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import fuzs.puzzleslib.core.CoreServices;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(EasyAnvils.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EasyAnvilsForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        CoreServices.FACTORIES.modConstructor(EasyAnvils.MOD_ID).accept(new EasyAnvils());
        registerHandlers();
    }

    private static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener((final PlayerInteractEvent.RightClickItem evt) -> {
            ItemInteractionHandler.onRightClickItem(evt.getLevel(), evt.getEntity(), evt.getHand()).ifPresent(result -> {
                evt.setCancellationResult(result.getResult());
                evt.setCanceled(true);
            });
        });
        MinecraftForge.EVENT_BUS.addListener((final PlayerInteractEvent.RightClickBlock evt) -> {
            ItemInteractionHandler.onRightClickBlock(evt.getLevel(), evt.getEntity(), evt.getHand(), evt.getHitVec()).ifPresent(result -> {
                evt.setCancellationResult(result);
                evt.setCanceled(true);
            });
        });
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent evt) {
        DataGenerator generator = evt.getGenerator();
        final ExistingFileHelper existingFileHelper = evt.getExistingFileHelper();
        generator.addProvider(true, new ModLanguageProvider(generator, EasyAnvils.MOD_ID));
    }
}
