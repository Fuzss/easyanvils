package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.core.ModServices;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.mixin.accessor.AnvilMenuAccessor;
import fuzs.easyanvils.mixin.accessor.ItemCombinerMenuAccessor;
import fuzs.easyanvils.mixin.accessor.SlotAccessor;
import fuzs.easyanvils.util.ComponentDecomposer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public class ModAnvilMenu extends AnvilMenu implements ContainerListener {
    private final AnvilMenuState builtInAnvilState;
    private final AnvilMenuState vanillaAnvilState;

    public ModAnvilMenu(int id, Inventory inventory) {
        // this constructor may not override inputSlots as vanilla adds a listener to it which is necessary client-side for the renaming edit box
        // so let this go to super instead of the main constructor to skip any inputSlots shenanigans
        super(id, inventory);
        this.builtInAnvilState = new BuiltInAnvilMenu(inventory);
        this.vanillaAnvilState = new VanillaAnvilMenu(inventory);
    }

    public ModAnvilMenu(int id, Inventory inventory, Container inputSlots, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, containerLevelAccess);
        ((ItemCombinerMenuAccessor) this).setInputSlots(inputSlots);
        // we could also replace slots directly in the slots list as we have direct access to it,
        // but the Ledger mod hooks into AbstractContainerMenu::addSlot which breaks that mod as we wouldn't be using AbstractContainerMenu::addSlot
        // (they store the menu as a @NotNull value, leads to an NPE)
        ((SlotAccessor) this.slots.get(0)).setContainer(inputSlots);
        ((SlotAccessor) this.slots.get(1)).setContainer(inputSlots);
        this.addSlotListener(this);
        this.builtInAnvilState = new BuiltInAnvilMenu(inventory);
        this.vanillaAnvilState = new VanillaAnvilMenu(inventory);
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.ANVIL_MENU_TYPE.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inputSlots.stillValid(player);
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasStack) {
        // change cost requirement from > 0 to >= 0 to allow for free name tag renames
        return (player.getAbilities().instabuild || player.experienceLevel >= this.getCost()) && this.getCost() >= 0;
    }

    @Override
    public void createResult() {
        // this is called during <init> when these aren't populated yet
        if (this.builtInAnvilState == null || this.vanillaAnvilState == null) return;
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        String itemName = ((AnvilMenuAccessor) this).easyanvils$getItemName();
        this.builtInAnvilState.init(left, right, itemName);
        this.vanillaAnvilState.init(left, right, itemName);
        this.builtInAnvilState.fillResultSlots();
        this.vanillaAnvilState.fillResultSlots();
        if (!AnvilMenuState.equals(this.builtInAnvilState, this.vanillaAnvilState)) {
            super.createResult();
        } else {
            this.createResult(left, right, itemName);
        }
    }

    private void createResult(ItemStack left, ItemStack right, String itemName) {
        this.setCost(1);
        if (left.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.setCost(0);
        } else {
            ItemStack output = left.copy();
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(output);
            int baseRepairCost = left.getBaseRepairCost() + (right.isEmpty() ? 0 : right.getBaseRepairCost());
            // no prior work penalty, or fixed
            baseRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.operator.applyAsInt(baseRepairCost);
            this.setRepairItemCountCost(0);
            boolean isBook = false;

            int repairOperationCost = 0;
            int enchantOperationCost = 0;
            int renameOperationCost = 0;
            final int maxAnvilRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).maxAnvilRepairCost;
            if (!right.isEmpty()) {
                isBook = right.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(right).isEmpty();
                if (output.isDamageableItem() && output.getItem().isValidRepairItem(left, right)) {
                    int l2 = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
                    if (l2 <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    int repairMaterials;
                    for (repairMaterials = 0; l2 > 0 && repairMaterials < right.getCount(); ++repairMaterials) {
                        int j3 = output.getDamageValue() - l2;
                        output.setDamageValue(j3);
                        ++repairOperationCost;
                        l2 = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
                    }

                    this.setRepairItemCountCost(repairMaterials);
                } else {
                    if (!isBook && (!output.is(right.getItem()) || !output.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    if (output.isDamageableItem() && !isBook) {
                        int l = left.getMaxDamage() - left.getDamageValue();
                        int i1 = right.getMaxDamage() - right.getDamageValue();
                        int j1 = i1 + output.getMaxDamage() * 12 / 100;
                        int k1 = l + j1;
                        int l1 = output.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < output.getDamageValue()) {
                            output.setDamageValue(l1);
                            repairOperationCost += 2;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(right);
                    boolean flag2 = false;
                    boolean flag3 = false;

                    for (Enchantment enchantment1 : map1.keySet()) {
                        if (enchantment1 != null) {
                            int otherEnchantmentLevel = map.getOrDefault(enchantment1, 0);
                            int enchantmentLevel = map1.get(enchantment1);
                            enchantmentLevel = otherEnchantmentLevel == enchantmentLevel ? enchantmentLevel + 1 : Math.max(enchantmentLevel, otherEnchantmentLevel);
                            boolean compatibleWithItem = enchantment1.canEnchant(left);
                            if (this.player.getAbilities().instabuild || left.is(Items.ENCHANTED_BOOK)) {
                                compatibleWithItem = true;
                            }

                            for (Enchantment enchantment : map.keySet()) {
                                if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                                    compatibleWithItem = false;
                                    ++enchantOperationCost;
                                }
                            }

                            if (!compatibleWithItem) {
                                flag3 = true;
                            } else {
                                flag2 = true;
                                if (enchantmentLevel > enchantment1.getMaxLevel()) {
                                    enchantmentLevel = enchantment1.getMaxLevel();
                                }

                                // prevent a level higher than max level from being lowered to max value
                                if (EasyAnvils.CONFIG.get(ServerConfig.class).noAnvilMaxLevelLimit) {
                                    int maxLevel = Math.max(map.getOrDefault(enchantment1, 0), map1.get(enchantment1));
                                    maxLevel = Math.max(maxLevel, enchantmentLevel);
                                    if (maxLevel != enchantmentLevel) {
                                        enchantmentLevel = maxLevel;
                                    }
                                }

                                map.put(enchantment1, enchantmentLevel);
                                int rarityCostMultiplier = switch (enchantment1.getRarity()) {
                                    case COMMON -> 1;
                                    case UNCOMMON -> 2;
                                    case RARE -> 4;
                                    case VERY_RARE -> 8;
                                };

                                if (isBook) {
                                    rarityCostMultiplier = Math.max(1, rarityCostMultiplier / 2);
                                }

                                enchantOperationCost += rarityCostMultiplier * enchantmentLevel;
                                if (left.getCount() > 1) {
                                    enchantOperationCost = maxAnvilRepairCost;
                                }
                            }
                        }
                    }

                    if (flag3 && !flag2) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }
                }
            }

            boolean hasRenamedItem = false;
            if (ComponentDecomposer.getStringLength(itemName) == 0) {
                if (left.hasCustomHoverName()) {
                    renameOperationCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(left) ? 0 : 1;
                    hasRenamedItem = true;
                    output.resetHoverName();
                }
            } else if (!itemName.equals(ComponentDecomposer.toFormattedString(left.getHoverName()))) {
                renameOperationCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(left) ? 0 : 1;
                hasRenamedItem = true;
                output.setHoverName(ComponentDecomposer.toFormattedComponent(itemName));
            }

            if (isBook && !ModServices.ABSTRACTIONS.isBookEnchantable(output, right)) {
                output = ItemStack.EMPTY;
            }

            int allOperationsCost = enchantOperationCost + repairOperationCost + renameOperationCost;
            if (allOperationsCost == 0) {
                this.setCost(0);
                // when renaming is free make sure to let the item stack pass without being cleared
                if (!hasRenamedItem) {
                    output = ItemStack.EMPTY;
                }
            } else {
                this.setCost(baseRepairCost + allOperationsCost);
            }

            if (enchantOperationCost == 0 && this.getCost() >= maxAnvilRepairCost && EasyAnvils.CONFIG.get(ServerConfig.class).alwaysRenameAndRepair) {
                this.setCost(maxAnvilRepairCost - 1);
            }

            // allow for custom max enchantment levels limit
            if (this.getCost() >= maxAnvilRepairCost && !this.player.getAbilities().instabuild) {
                output = ItemStack.EMPTY;
            }

            if (!output.isEmpty()) {
                int outputRepairCost = output.getBaseRepairCost();
                if (!right.isEmpty() && outputRepairCost < right.getBaseRepairCost()) {
                    outputRepairCost = right.getBaseRepairCost();
                }

                if (enchantOperationCost > 0 || !EasyAnvils.CONFIG.get(ServerConfig.class).alwaysRenameAndRepair) {
                    outputRepairCost = AnvilMenu.calculateIncreasedRepairCost(outputRepairCost);
                }

                // don't add tag when there is no repair cost
                if (outputRepairCost > 0) {
                    output.setRepairCost(outputRepairCost);
                }
                EnchantmentHelper.setEnchantments(map, output);
            }

            this.resultSlots.setItem(0, output);
            this.broadcastChanges();
        }
    }

    public void setCost(int cost) {
        this.setData(0, cost);
    }

    public void setRepairItemCountCost(int repairItemCountCost) {
        ((AnvilMenuAccessor) this).easyanvils$setRepairItemCountCost(repairItemCountCost);
    }

    @Override
    public void removed(Player player) {
        // copied from container super method
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack itemstack = this.getCarried();
            if (!itemstack.isEmpty()) {
                if (player.isAlive() && !serverPlayer.hasDisconnected()) {
                    player.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    player.drop(itemstack, false);
                }
                this.setCarried(ItemStack.EMPTY);
            }
        }
        this.removeSlotListener(this);
    }

    @Override
    public void setItemName(String newName) {
        ((AnvilMenuAccessor) this).easyanvils$setItemName(newName);
        if (this.getSlot(2).hasItem()) {
            ItemStack itemStack = this.getSlot(2).getItem();
            Component component = ComponentDecomposer.toFormattedComponent(newName);
            if (component.getString().isEmpty()) {
                itemStack.resetHoverName();
            } else {
                itemStack.setHoverName(component);
            }
        }

        this.createResult();
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        // this is only ever run server side
        if (containerToSend == this) {
            if (dataSlotIndex >= 0 && dataSlotIndex < 2) {
                this.slotsChanged(this.inputSlots);
            }
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {

    }

    public static int repairCostToRepairs(int repairCost) {
        repairCost++;
        int repairs = 0;
        while (repairCost >= 2) {
            repairCost /= 2;
            repairs++;
        }
        return repairs;
    }
}
