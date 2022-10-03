package fuzs.easyanvils.network.client;

import fuzs.easyanvils.util.FormattedStringHelper;
import fuzs.puzzleslib.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

public class C2SNameTagUpdateMessage implements Message<C2SNameTagUpdateMessage> {
    private InteractionHand hand;
    private String title;

    public C2SNameTagUpdateMessage() {

    }

    public C2SNameTagUpdateMessage(InteractionHand hand, String title) {
        this.hand = hand;
        this.title = title;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeUtf(this.title);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);
        this.title = buf.readUtf(384);
    }

    @Override
    public MessageHandler<C2SNameTagUpdateMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(C2SNameTagUpdateMessage message, Player player, Object gameInstance) {
                ItemStack stack = player.getItemInHand(message.hand);
                if (stack.is(Items.NAME_TAG)) {
                    if (stack.getHoverName().getString().equals(message.title)) return;
                    if (StringUtils.isBlank(message.title)) {
                        stack.resetHoverName();
                    } else {
                        String s = FormattedStringHelper.filterText(message.title);
                        if (FormattedStringHelper.countFormattedChars(s) <= 50) {
                            stack.setHoverName(Component.literal(s));
                        }
                    }
                }
            }
        };
    }
}
