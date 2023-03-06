package fuzs.easyanvils.client.gui.screens.inventory;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.components.OpenEditBox;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.mixin.client.accessor.AnvilScreenAccessor;
import fuzs.easyanvils.network.client.C2SRenameItemMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModAnvilScreen extends AnvilScreen {
    private EditBox name;

    public ModAnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
        super(anvilMenu, inventory, component);
    }

    @Override
    protected void subInit() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (EasyAnvils.CONFIG.get(ServerConfig.class).renamingSupportsFormatting) {
            this.name = new OpenEditBox(this.font, i + 62, j + 24, 103, 12, Component.translatable("container.repair"));
        } else {
            this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, Component.translatable("container.repair"));
        }
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addWidget(this.name);
        this.setInitialFocus(this.name);
        this.name.setEditable(false);
        this.name.setVisible(false);
        ((AnvilScreenAccessor) this).setName(this.name);
    }

    private void onNameChanged(String input) {
        Slot slot = this.menu.getSlot(0);
        if (!slot.hasItem()) return;
        if (!slot.getItem().hasCustomHoverName() && input.equals(slot.getItem().getHoverName().getString())) {
            input = "";
        }

        this.menu.setItemName(input);
        EasyAnvils.NETWORK.sendToServer(new C2SRenameItemMessage(input));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        boolean visible = this.name.isVisible();
        super.resize(minecraft, width, height);
        this.name.setVisible(visible);
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (dataSlotIndex == 0) {
            this.name.setVisible(!stack.isEmpty());
        }
        super.slotChanged(containerToSend, dataSlotIndex, stack);
    }
}
