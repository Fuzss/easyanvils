package fuzs.easyanvils.handler;

import com.google.common.collect.Lists;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * approach for changing only a few minor anvil mechanics without overriding the whole method (to stay compatible with mods using mixins)
 * all actions changing the menu are enqueued, to execute those actions at the end when something differs from vanilla {@link #applyLater()} needs to have been called at some point
 */
public class AnvilRepairContext {
    protected final AnvilMenu anvilMenu;
    protected final Player player;
    protected final String itemName;
    protected final Container resultSlots;
    private final IntConsumer repairItemCountCost;
    private final IntConsumer cost;
    private final List<Runnable> actions = Lists.newArrayList();
    private boolean applyLater;

    public AnvilRepairContext(AnvilMenu anvilMenu, Player player, String itemName, Container resultSlots, IntConsumer repairItemCountCost, IntConsumer cost) {
        this.anvilMenu = anvilMenu;
        this.player = player;
        this.itemName = itemName;
        this.resultSlots = resultSlots;
        this.repairItemCountCost = repairItemCountCost;
        this.cost = cost;
    }

    protected final void applyLater() {
        this.applyLater = true;
    }

    protected final void enqueueAction(Runnable runnable) {
        this.actions.add(runnable);
    }

    public final boolean createResult(ItemStack leftInput, ItemStack rightInput) {
        this.actions.clear();
        MutableInt cost = new MutableInt();
        this.createResult(leftInput, rightInput, cost);
        if (this.applyLater) {
            this.actions.forEach(Runnable::run);
            this.cost.accept(cost.intValue());
            return true;
        }
        return false;
    }

    private void createResult(ItemStack leftInput, ItemStack rightInput, MutableInt cost) {
        cost.setValue(1);
        int i = 0;
        int j = 0;
        int renamingCost = 0;
        if (leftInput.isEmpty()) {
            this.enqueueAction(() -> this.resultSlots.setItem(0, ItemStack.EMPTY));
            cost.setValue(0);
        } else {
            ItemStack itemstack1 = leftInput.copy();
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
            j += leftInput.getBaseRepairCost() + (rightInput.isEmpty() ? 0 : rightInput.getBaseRepairCost());
            this.enqueueAction(() -> this.repairItemCountCost.accept(0));
            boolean flag = false;

            if (this.onAnvilChange(leftInput, rightInput, j)) return;
            if (!rightInput.isEmpty()) {
                flag = rightInput.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(rightInput).isEmpty();
                if (itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(leftInput, rightInput)) {
                    int l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    if (l2 <= 0) {
                        this.enqueueAction(() -> this.resultSlots.setItem(0, ItemStack.EMPTY));
                        cost.setValue(0);
                        return;
                    }

                    int i3;
                    for(i3 = 0; l2 > 0 && i3 < rightInput.getCount(); ++i3) {
                        int j3 = itemstack1.getDamageValue() - l2;
                        itemstack1.setDamageValue(j3);
                        ++i;
                        l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    }

                    final int i33 = i3;
                    this.enqueueAction(() -> this.repairItemCountCost.accept(i33));
                } else {
                    if (!flag && (!itemstack1.is(rightInput.getItem()) || !itemstack1.isDamageableItem())) {
                        this.enqueueAction(() -> this.resultSlots.setItem(0, ItemStack.EMPTY));
                        cost.setValue(0);
                        return;
                    }

                    if (itemstack1.isDamageableItem() && !flag) {
                        int l = leftInput.getMaxDamage() - leftInput.getDamageValue();
                        int i1 = rightInput.getMaxDamage() - rightInput.getDamageValue();
                        int j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                        int k1 = l + j1;
                        int l1 = itemstack1.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < itemstack1.getDamageValue()) {
                            itemstack1.setDamageValue(l1);
                            i += 2;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(rightInput);
                    boolean flag2 = false;
                    boolean flag3 = false;

                    for(Enchantment enchantment1 : map1.keySet()) {
                        if (enchantment1 != null) {
                            int i2 = map.getOrDefault(enchantment1, 0);
                            int j2 = map1.get(enchantment1);
                            j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
                            boolean flag1 = enchantment1.canEnchant(leftInput);
                            if (this.player.getAbilities().instabuild || leftInput.is(Items.ENCHANTED_BOOK)) {
                                flag1 = true;
                            }

                            for(Enchantment enchantment : map.keySet()) {
                                if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                                    flag1 = false;
                                    ++i;
                                }
                            }

                            if (!flag1) {
                                flag3 = true;
                            } else {
                                flag2 = true;
                                if (j2 > enchantment1.getMaxLevel()) {
                                    j2 = enchantment1.getMaxLevel();
                                }

                                // prevent a level higher than max level from being lowered to max value
                                // feature specifically requested by a user
                                if (EasyAnvils.CONFIG.get(ServerConfig.class).noAnvilMaxLevelLimit) {
                                    int maxLevel = Math.max(map.getOrDefault(enchantment1, 0), map1.get(enchantment1));
                                    maxLevel = Math.max(maxLevel, j2);
                                    if (maxLevel != j2) {
                                        j2 = maxLevel;
                                        this.applyLater();
                                    }
                                }

                                map.put(enchantment1, j2);
                                int k3 = 0;
                                switch (enchantment1.getRarity()) {
                                    case COMMON:
                                        k3 = 1;
                                        break;
                                    case UNCOMMON:
                                        k3 = 2;
                                        break;
                                    case RARE:
                                        k3 = 4;
                                        break;
                                    case VERY_RARE:
                                        k3 = 8;
                                }

                                if (flag) {
                                    k3 = Math.max(1, k3 / 2);
                                }

                                i += k3 * j2;
                                if (leftInput.getCount() > 1) {
                                    i = EasyAnvils.CONFIG.get(ServerConfig.class).maxAnvilRepairCost;
                                    this.applyLater();
                                }
                            }
                        }
                    }

                    if (flag3 && !flag2) {
                        this.enqueueAction(() -> this.resultSlots.setItem(0, ItemStack.EMPTY));
                        cost.setValue(0);
                        return;
                    }
                }
            }

            boolean hasRenamedItem = false;
            if (StringUtils.isBlank(this.itemName)) {
                if (leftInput.hasCustomHoverName()) {
                    hasRenamedItem = true;
                    renamingCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(leftInput) ? 0 : 1;
                    if (renamingCost != 1) this.applyLater();
                    i += renamingCost;
                    itemstack1.resetHoverName();
                }
            } else if (!this.itemName.equals(leftInput.getHoverName().getString())) {
                hasRenamedItem = true;
                renamingCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(leftInput) ? 0 : 1;
                if (renamingCost != 1) this.applyLater();
                i += renamingCost;
                itemstack1.setHoverName(Component.literal(this.itemName));
            }
            itemstack1 = this.testBookEnchantable(rightInput, itemstack1, flag);

            // no prior work penalty, this does not trigger apply later, we also inject this via mixin
            // otherwise every anvil operation would trigger apply later
            if (EasyAnvils.CONFIG.get(ServerConfig.class).disablePriorWorkPenalty) {
                cost.setValue(i);
            } else {
                cost.setValue(j + i);
            }

            // when renaming is free make sure to let the item stack pass without being cleared
            if (i <= 0 && !hasRenamedItem) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (renamingCost == i && hasRenamedItem && cost.getValue() >= 40) {
                cost.setValue(39);
            }

            if (!this.player.getAbilities().instabuild) {
                // allow for custom max enchantment levels limit
                if (cost.getValue() >= 40) {
                    if (cost.getValue() < EasyAnvils.CONFIG.get(ServerConfig.class).maxAnvilRepairCost) {
                        this.applyLater();
                    } else {
                        itemstack1 = ItemStack.EMPTY;
                    }
                } else if (cost.getValue() >= EasyAnvils.CONFIG.get(ServerConfig.class).maxAnvilRepairCost) {
                    this.applyLater();
                    itemstack1 = ItemStack.EMPTY;
                }
            }

            if (!itemstack1.isEmpty()) {
                int k2 = itemstack1.getBaseRepairCost();
                if (!rightInput.isEmpty() && k2 < rightInput.getBaseRepairCost()) {
                    k2 = rightInput.getBaseRepairCost();
                }

                if (renamingCost != i || !hasRenamedItem) {
                    k2 = AnvilMenu.calculateIncreasedRepairCost(k2);
                }

                // don't add tag when there is no repair cost
                if (k2 > 0) {
                    itemstack1.setRepairCost(k2);
                } else {
                    this.applyLater();
                }
                EnchantmentHelper.setEnchantments(map, itemstack1);
            }

            final ItemStack itemstack11 = itemstack1;
            this.enqueueAction(() -> this.resultSlots.setItem(0, itemstack11));
            this.enqueueAction(this.anvilMenu::broadcastChanges);
        }
    }

    protected ItemStack testBookEnchantable(ItemStack rightInput, ItemStack itemstack1, boolean flag) {
        return itemstack1;
    }

    protected boolean onAnvilChange(ItemStack leftInput, ItemStack rightInput, int j) {
        return false;
    }
}
