package fuzs.easyanvils.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;

public class ModAnvilScreen extends AnvilScreen {
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");

    public ModAnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
        super(anvilMenu, inventory, component);
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
}
