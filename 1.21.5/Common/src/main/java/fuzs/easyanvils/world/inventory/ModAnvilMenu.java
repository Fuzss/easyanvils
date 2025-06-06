package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.RenameAndRepairCost;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.services.CommonAbstractions;
import fuzs.easyanvils.util.ComponentDecomposer;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import fuzs.easyanvils.world.inventory.state.AnvilMenuState;
import fuzs.easyanvils.world.inventory.state.BuiltInAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.container.v1.QuickMoveRuleSet;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Objects;

public abstract class ModAnvilMenu extends AnvilMenu {
    private final Container container;
    private final AnvilMenuState builtInAnvilState;
    private final AnvilMenuState vanillaAnvilState;

    public ModAnvilMenu(int id, Inventory inventory) {
        super(id, inventory);
        // never used since still valid is not called on clients, might as well be null
        this.container = new SimpleContainer();
        this.builtInAnvilState = new BuiltInAnvilMenu(inventory, ContainerLevelAccess.NULL);
        this.vanillaAnvilState = CommonAbstractions.INSTANCE.createVanillaAnvilMenu(inventory,
                ContainerLevelAccess.NULL);
        this.createResult();
    }

    public ModAnvilMenu(int id, Inventory inventory, AnvilBlockEntity blockEntity, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, containerLevelAccess);
        // we just need this for checking if the block entity is still valid
        this.container = blockEntity;
        this.builtInAnvilState = new BuiltInAnvilMenu(inventory, containerLevelAccess);
        this.vanillaAnvilState = CommonAbstractions.INSTANCE.createVanillaAnvilMenu(inventory, containerLevelAccess);
        this.initializeSlots(blockEntity);
        this.createResult();
    }

    private void initializeSlots(AnvilBlockEntity blockEntity) {
        ((SimpleContainer) this.inputSlots).items = blockEntity.getItems();
        ((SimpleContainer) this.inputSlots).addListener((Container container) -> blockEntity.setChanged());
        this.resultSlots.itemStacks = blockEntity.getResult();
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.ANVIL_MENU_TYPE.value();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasStack) {
        // change the cost requirement from > 0 to >= 0 to allow for free name tag renames
        return (player.getAbilities().instabuild || player.experienceLevel >= this.getCost()) && this.getCost() >= 0;
    }

    protected final void createAnvilResult() {
        // this is called during <init> when these aren't populated yet
        if (this.builtInAnvilState == null || this.vanillaAnvilState == null) return;
        // to not break custom anvil recipes from other mods, we compare the outcome from the vanilla anvil logic and
        // the actual current anvil logic (with possible alterations from mods via Mixin or the Forge event)
        // if the result is not equal, we do nothing and let the interfering mod take the upper hand
        ItemStack primaryItemStack = this.inputSlots.getItem(0);
        ItemStack secondaryItemStack = this.inputSlots.getItem(1);
        this.builtInAnvilState.init(primaryItemStack, secondaryItemStack, this.itemName);
        this.vanillaAnvilState.init(primaryItemStack, secondaryItemStack, this.itemName);
        this.builtInAnvilState.fillResultSlots();
        this.vanillaAnvilState.fillResultSlots();
        if (!AnvilMenuState.equals(this.builtInAnvilState, this.vanillaAnvilState)) {
            super.createResult();
        } else {
            this.createAnvilResult(primaryItemStack, secondaryItemStack, this.itemName);
        }
    }

    private void createAnvilResult(ItemStack primaryItemStack, ItemStack secondaryItemStack, String itemName) {
        this.setCost(1);
        if (primaryItemStack.isEmpty() || !EnchantmentHelper.canStoreEnchantments(primaryItemStack)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.setCost(0);
        } else {
            ItemStack output = primaryItemStack.copy();
            ItemEnchantments.Mutable leftEnchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(
                    output));
            int baseRepairCost = primaryItemStack.getOrDefault(DataComponents.REPAIR_COST, 0) +
                    (secondaryItemStack.isEmpty() ? 0 : secondaryItemStack.getOrDefault(DataComponents.REPAIR_COST, 0));
            // no prior work penalty, or fixed
            baseRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.priorWorkPenalty.operator.applyAsInt(
                    baseRepairCost);
            this.repairItemCountCost = 0;
            boolean isBook = false;

            int repairOperationCost = 0;
            int enchantOperationCost = 0;
            int renameOperationCost = 0;
            if (!secondaryItemStack.isEmpty()) {
                isBook = secondaryItemStack.has(DataComponents.STORED_ENCHANTMENTS);
                if (output.isDamageableItem() && output.isValidRepairItem(secondaryItemStack)) {
                    int l2 = (int) Math.min(output.getDamageValue(),
                            Math.floor(output.getMaxDamage() *
                                    EasyAnvils.CONFIG.get(ServerConfig.class).costs.repairWithMaterialRestoredDurability));
                    if (l2 <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    int repairMaterials;
                    for (repairMaterials = 0;
                         l2 > 0 && repairMaterials < secondaryItemStack.getCount(); ++repairMaterials) {
                        int j3 = output.getDamageValue() - l2;
                        output.setDamageValue(j3);
                        repairOperationCost += EasyAnvils.CONFIG.get(ServerConfig.class).costs.repairWithMaterialUnitCost;
                        l2 = (int) Math.min(output.getDamageValue(),
                                Math.floor(output.getMaxDamage() *
                                        EasyAnvils.CONFIG.get(ServerConfig.class).costs.repairWithMaterialRestoredDurability));
                    }

                    this.repairItemCountCost = repairMaterials;
                } else {
                    if (!isBook && (!output.is(secondaryItemStack.getItem()) || !output.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    if (output.isDamageableItem() && !isBook) {
                        int l = primaryItemStack.getMaxDamage() - primaryItemStack.getDamageValue();
                        int i1 = secondaryItemStack.getMaxDamage() - secondaryItemStack.getDamageValue();
                        int j1 = i1 + (int) Math.floor(output.getMaxDamage() *
                                EasyAnvils.CONFIG.get(ServerConfig.class).costs.repairWithOtherItemBonusDurability);
                        int k1 = l + j1;
                        int l1 = output.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < output.getDamageValue()) {
                            output.setDamageValue(l1);
                            repairOperationCost += EasyAnvils.CONFIG.get(ServerConfig.class).costs.repairWithOtherItemCost;
                        }
                    }

                    ItemEnchantments rightEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(secondaryItemStack);
                    boolean itemWithCompatibleEnchantment = false;
                    boolean itemWithIncompatibleEnchantment = false;

                    for (Holder<Enchantment> rightHolder : rightEnchantments.keySet()) {
                        Enchantment rightEnchantment = rightHolder.value();
                        int leftEnchantmentLevel = leftEnchantments.getLevel(rightHolder);
                        int enchantmentLevel = rightEnchantments.getLevel(rightHolder);
                        enchantmentLevel = leftEnchantmentLevel == enchantmentLevel ? enchantmentLevel + 1 :
                                Math.max(enchantmentLevel, leftEnchantmentLevel);
                        boolean compatibleWithItem = rightEnchantment.canEnchant(primaryItemStack);
                        if (this.player.getAbilities().instabuild || primaryItemStack.is(Items.ENCHANTED_BOOK)) {
                            compatibleWithItem = true;
                        }

                        for (Holder<Enchantment> leftHolder : leftEnchantments.keySet()) {
                            if (!leftHolder.equals(rightHolder) &&
                                    !Enchantment.areCompatible(rightHolder, leftHolder)) {
                                compatibleWithItem = false;
                                ++enchantOperationCost;
                            }
                        }

                        if (!compatibleWithItem) {
                            // allow using items with just incompatible enchantments as repair material
                            if (repairOperationCost > 0) continue;
                            itemWithIncompatibleEnchantment = true;
                        } else {
                            itemWithCompatibleEnchantment = true;
                            if (enchantmentLevel > rightEnchantment.getMaxLevel()) {
                                enchantmentLevel = rightEnchantment.getMaxLevel();
                            }

                            // prevent a level higher than max level from being lowered to max value
                            int maxLevel = Math.max(leftEnchantments.getLevel(rightHolder),
                                    rightEnchantments.getLevel(rightHolder));
                            maxLevel = Math.max(maxLevel, enchantmentLevel);
                            if (maxLevel != enchantmentLevel) {
                                enchantmentLevel = maxLevel;
                            }

                            int rarityCostMultiplier = rightEnchantment.getAnvilCost();
                            if (isBook && EasyAnvils.CONFIG.get(ServerConfig.class).costs.halvedBookCosts) {
                                rarityCostMultiplier = Math.max(1, rarityCostMultiplier / 2);
                            }

                            // don't increase repair cost when an enchantment is already present and the level does not change (already at max level probably)
                            int oldEnchantmentLevel = leftEnchantments.getLevel(rightHolder);
                            leftEnchantments.set(rightHolder, enchantmentLevel);
                            if (oldEnchantmentLevel != 0 || oldEnchantmentLevel != enchantmentLevel) {
                                enchantOperationCost += rarityCostMultiplier * enchantmentLevel;
                            }

                            // different implementation for showing 'Too Expensive!' client-side from vanilla
                            if (primaryItemStack.getCount() > 1 && !this.player.getAbilities().instabuild) {
                                this.resultSlots.setItem(0, ItemStack.EMPTY);
                                this.setCost(-1);
                                return;
                            }
                        }
                    }

                    if (itemWithIncompatibleEnchantment && !itemWithCompatibleEnchantment) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }
                }
            }

            boolean hasRenamedItem = false;
            if (ComponentDecomposer.getStringLength(itemName) == 0) {
                if (primaryItemStack.has(DataComponents.CUSTOM_NAME)) {
                    renameOperationCost =
                            EasyAnvils.CONFIG.get(ServerConfig.class).costs.freeRenames.filter.test(primaryItemStack) ?
                                    0 : 1;
                    hasRenamedItem = true;
                    output.remove(DataComponents.CUSTOM_NAME);
                }
            } else if (!itemName.equals(ComponentDecomposer.toFormattedString(primaryItemStack.getHoverName()))) {
                renameOperationCost =
                        EasyAnvils.CONFIG.get(ServerConfig.class).costs.freeRenames.filter.test(primaryItemStack) ? 0 :
                                1;
                hasRenamedItem = true;
                output.set(DataComponents.CUSTOM_NAME, ComponentDecomposer.toFormattedComponent(itemName));
            }

            int allOperationsCost = enchantOperationCost + repairOperationCost + renameOperationCost;
            if (allOperationsCost == 0) {
                this.setCost(0);
                // when renaming is free, make sure to let the item stack pass without being cleared
                if (!hasRenamedItem) {
                    output = ItemStack.EMPTY;
                }
            } else {

                int newCost;
                if (enchantOperationCost == 0 &&
                        EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.renameAndRepairCosts ==
                                RenameAndRepairCost.FIXED) {
                    newCost = allOperationsCost;
                } else {
                    newCost = baseRepairCost + allOperationsCost;
                }

                this.setCost(Mth.clamp(newCost, 0, 2147483647));
            }

            if (hasRenamedItem && allOperationsCost == renameOperationCost) {
                this.onlyRenaming = true;
            }

            int maxAnvilRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).costs.tooExpensiveLimit;
            boolean hasNoLimit = maxAnvilRepairCost == -1;
            // we have removed the max repair limit, so just use the vanilla limit here
            if (hasNoLimit) maxAnvilRepairCost = 40;
            if (this.getCost() >= maxAnvilRepairCost) {

                if (enchantOperationCost == 0 &&
                        EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.renameAndRepairCosts ==
                                RenameAndRepairCost.LIMITED) {
                    this.setCost(maxAnvilRepairCost - 1);
                } else if (!hasNoLimit && !this.player.getAbilities().instabuild) {
                    // allow for custom max enchantment levels limit
                    output = ItemStack.EMPTY;
                }
            }

            if (!output.isEmpty()) {

                int outputRepairCost = output.getOrDefault(DataComponents.REPAIR_COST, 0);
                if (!secondaryItemStack.isEmpty() &&
                        outputRepairCost < secondaryItemStack.getOrDefault(DataComponents.REPAIR_COST, 0)) {
                    outputRepairCost = secondaryItemStack.getOrDefault(DataComponents.REPAIR_COST, 0);
                }

                if (allOperationsCost > 0) {
                    if (enchantOperationCost > 0 && (!isBook || !primaryItemStack.is(Items.ENCHANTED_BOOK) ||
                            !EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.penaltyFreeEnchantsForBooks) ||
                            !EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.penaltyFreeRenamesAndRepairs) {
                        outputRepairCost = AnvilMenu.calculateIncreasedRepairCost(outputRepairCost);
                    }
                }

                // don't add tag when there is no repair cost
                if (outputRepairCost > 0) {
                    output.set(DataComponents.REPAIR_COST, outputRepairCost);
                }

                EnchantmentHelper.setEnchantments(output, leftEnchantments.toImmutable());
            }

            this.resultSlots.setItem(0, output);
            this.broadcastChanges();
        }
    }

    public void setCost(int cost) {
        this.setData(0, cost);
    }

    @Override
    public void removed(Player player) {
        // prevent items from being cleared out of the container
        ContainerLevelAccess containerLevelAccess = this.access;
        this.access = ContainerLevelAccess.NULL;
        super.removed(player);
        this.access = containerLevelAccess;
    }

    @Override
    public boolean setItemName(String newName) {
        newName = FormattedStringDecomposer.filterText(newName);
        if (ComponentDecomposer.getStringLength(newName) <= 50 && !Objects.equals(newName, this.itemName)) {
            this.itemName = newName.trim();
            if (this.getSlot(2).hasItem()) {
                ItemStack itemStack = this.getSlot(2).getItem();
                setFormattedItemName(this.itemName, itemStack);
            }

            this.createResult();
            return true;
        }

        return false;
    }

    public static void setFormattedItemName(String newName, ItemStack itemStack) {
        Component component = ComponentDecomposer.toFormattedComponent(newName);
        if (component.getString().isEmpty()) {
            itemStack.remove(DataComponents.CUSTOM_NAME);
        } else {
            itemStack.set(DataComponents.CUSTOM_NAME, component);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return QuickMoveRuleSet.of(this, this::moveItemStackTo)
                .addContainerSlotRule(0, 1)
                .addInventoryRules()
                .addInventoryCompartmentRules()
                .quickMoveStack(player, index);
    }
}
