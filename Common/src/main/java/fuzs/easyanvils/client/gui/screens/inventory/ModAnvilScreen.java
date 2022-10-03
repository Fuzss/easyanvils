package fuzs.easyanvils.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.components.OpenEditBox;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.mixin.client.accessor.AnvilScreenAccessor;
import fuzs.easyanvils.network.client.C2SRenameItemMessage;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModAnvilScreen extends AnvilScreen {
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");

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

    private void onNameChanged(String name) {
        if (!name.isEmpty()) {
            String string = name;
            Slot slot = this.menu.getSlot(0);
            if (slot != null && slot.hasItem() && !slot.getItem().hasCustomHoverName() && name.equals(slot.getItem().getHoverName().getString())) {
                string = "";
            }

            this.menu.setItemName(string);
            EasyAnvils.NETWORK.sendToServer(new C2SRenameItemMessage(string));
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        RenderSystem.disableBlend();
        // copied from AbstractContainerScreen super
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
        int i = this.menu.getCost();
        // changed to allow for >= 0
        if (i >= 0) {
            int j = 8453920;
            Component component;
            // allow for custom max repair cost
            if (i >= EasyAnvils.CONFIG.get(ServerConfig.class).maxAnvilRepairCost && !this.minecraft.player.getAbilities().instabuild) {
                component = TOO_EXPENSIVE_TEXT;
                j = 16736352;
            } else if (!this.menu.getSlot(2).hasItem()) {
                component = null;
            } else {
                component = Component.translatable("container.repair.cost", i);
                if (!this.menu.getSlot(2).mayPickup(this.minecraft.player)) {
                    j = 16736352;
                }
            }
            if (component != null) {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                fill(poseStack, k - 2, 67, this.imageWidth - 8, 79, 1325400064);
                this.font.drawShadow(poseStack, component, (float)k, 69.0F, j);
            }
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (dataSlotIndex == 0) {
            this.name.setVisible(!stack.isEmpty());
        }
        super.slotChanged(containerToSend, dataSlotIndex, stack);
    }
}
