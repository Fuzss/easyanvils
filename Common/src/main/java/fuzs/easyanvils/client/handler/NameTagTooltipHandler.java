package fuzs.easyanvils.client.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class NameTagTooltipHandler {

    public static void onItemTooltip(ItemStack stack, TooltipFlag context, List<Component> lines) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).editNameTagsNoAnvil) return;
        if (stack.is(Items.NAME_TAG)) {
            Component component = Component.translatable("easyanvils.item.name_tag.description").withStyle(ChatFormatting.GRAY);
            if (context.isAdvanced()) {
                lines.add(lines.size() - 1, component);
            } else {
                lines.add(component);
            }
        }
    }
}
