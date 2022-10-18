package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.core.ModServices;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.mixin.accessor.AnvilMenuAccessor;
import fuzs.easyanvils.mixin.accessor.ItemCombinerMenuAccessor;
import net.minecraft.Util;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Map;
import java.util.function.IntConsumer;

public class ModAnvilMenu extends AnvilMenu implements ContainerListener {

    public ModAnvilMenu(int id, Inventory inventory) {
        // passing this to super instead of the other constructor is important, vanilla super adds a listener to the SimpleContainer which is required for the renaming edit box
        // we have nothing in the other constructor that is required on the client anyway
        super(id, inventory);
    }

    public ModAnvilMenu(int id, Inventory inventory, Container inputSlots, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, containerLevelAccess);
        ((ItemCombinerMenuAccessor) this).setInputSlots(inputSlots);
        this.slots.set(0, Util.make(new Slot(inputSlots, 0, 27, 47), slot -> slot.index = 0));
        this.slots.set(1, Util.make(new Slot(inputSlots, 1, 76, 47), slot -> slot.index = 1));
        this.addSlotListener(this);
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
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        String itemName = ((AnvilMenuAccessor) this).getItemName();
        MutableInt cost = new MutableInt();
        this.createResult(left, right, itemName, cost, i -> ((AnvilMenuAccessor) this).setRepairItemCountCost(i));
        this.setData(0, cost.intValue());
    }

    private void createResult(ItemStack left, ItemStack right, String itemName, MutableInt cost, IntConsumer repairItemCountCost) {
        cost.setValue(1);
        if (left.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            cost.setValue(0);
        } else {
            ItemStack output = left.copy();
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(output);
            int baseRepairCost = left.getBaseRepairCost() + (right.isEmpty() ? 0 : right.getBaseRepairCost());
            // no prior work penalty, or fixed
            baseRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.operator.applyAsInt(baseRepairCost);
            repairItemCountCost.accept(0);
            boolean isBook = false;

            if (!ModServices.ABSTRACTIONS.onAnvilChange(this, left, right, this.resultSlots, itemName, baseRepairCost, this.player)) {
                return;
            }

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
                        cost.setValue(0);
                        return;
                    }

                    int repairMaterials;
                    for (repairMaterials = 0; l2 > 0 && repairMaterials < right.getCount(); ++repairMaterials) {
                        int j3 = output.getDamageValue() - l2;
                        output.setDamageValue(j3);
                        ++repairOperationCost;
                        l2 = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
                    }

                    repairItemCountCost.accept(repairMaterials);
                } else {
                    if (!isBook && (!output.is(right.getItem()) || !output.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        cost.setValue(0);
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
                        cost.setValue(0);
                        return;
                    }
                }
            }

            boolean hasRenamedItem = false;
            if (StringUtils.isBlank(itemName)) {
                if (left.hasCustomHoverName()) {
                    renameOperationCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(left) ? 0 : 1;
                    hasRenamedItem = true;
                    output.resetHoverName();
                }
            } else if (!itemName.equals(left.getHoverName().getString())) {
                renameOperationCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(left) ? 0 : 1;
                hasRenamedItem = true;
                output.setHoverName(Component.literal(itemName));
            }

            if (isBook && !ModServices.ABSTRACTIONS.isBookEnchantable(output, right)) {
                output = ItemStack.EMPTY;
            }

            int allOperationsCost = enchantOperationCost + repairOperationCost + renameOperationCost;
            if (allOperationsCost == 0) {
                cost.setValue(0);
                // when renaming is free make sure to let the item stack pass without being cleared
                if (!hasRenamedItem) {
                    output = ItemStack.EMPTY;
                }
            } else {
                cost.setValue(baseRepairCost + allOperationsCost);
            }

            if (enchantOperationCost == 0 && cost.getValue() >= maxAnvilRepairCost && EasyAnvils.CONFIG.get(ServerConfig.class).alwaysRenameAndRepair) {
                cost.setValue(maxAnvilRepairCost - 1);
            }

            // allow for custom max enchantment levels limit
            if (cost.getValue() >= maxAnvilRepairCost && !this.player.getAbilities().instabuild) {
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
