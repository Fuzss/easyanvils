package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.core.CommonAbstractions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class VanillaAnvilMenu extends BuiltInAnvilMenu {

    public VanillaAnvilMenu(Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(inventory, containerLevelAccess);
    }

    @Override
    public void createResult() {
        ItemStack itemStack = this.inputSlots.getItem(0);
        this.setCost(1);
        int i = 0;
        int j = 0;
        int k = 0;
        if (itemStack.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.setCost(0);
        } else {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = this.inputSlots.getItem(1);
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack2);
            j += itemStack.getBaseRepairCost() + (itemStack3.isEmpty() ? 0 : itemStack3.getBaseRepairCost());
            this.setRepairItemCountCost(0);
            boolean bl = false;
            if (!itemStack3.isEmpty()) {
                bl = itemStack3.is(Items.ENCHANTED_BOOK) && !EnchantedBookItem.getEnchantments(itemStack3).isEmpty();
                if (itemStack2.isDamageableItem() && itemStack2.getItem().isValidRepairItem(itemStack, itemStack3)) {
                    int l = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
                    if (l <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    int m;
                    for(m = 0; l > 0 && m < itemStack3.getCount(); ++m) {
                        int n = itemStack2.getDamageValue() - l;
                        itemStack2.setDamageValue(n);
                        ++i;
                        l = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
                    }

                    this.setRepairItemCountCost(m);
                } else {
                    if (!bl && (!itemStack2.is(itemStack3.getItem()) || !itemStack2.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    if (itemStack2.isDamageableItem() && !bl) {
                        int l = itemStack.getMaxDamage() - itemStack.getDamageValue();
                        int m = itemStack3.getMaxDamage() - itemStack3.getDamageValue();
                        int n = m + itemStack2.getMaxDamage() * 12 / 100;
                        int o = l + n;
                        int p = itemStack2.getMaxDamage() - o;
                        if (p < 0) {
                            p = 0;
                        }

                        if (p < itemStack2.getDamageValue()) {
                            itemStack2.setDamageValue(p);
                            i += 2;
                        }
                    }

                    Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(itemStack3);
                    boolean bl2 = false;
                    boolean bl3 = false;

                    for(Enchantment enchantment : map2.keySet()) {
                        if (enchantment != null) {
                            int q = map.getOrDefault(enchantment, 0);
                            int r = map2.get(enchantment);
                            r = q == r ? r + 1 : Math.max(r, q);
                            boolean bl4 = enchantment.canEnchant(itemStack);
                            if (this.player.getAbilities().instabuild || itemStack.is(Items.ENCHANTED_BOOK)) {
                                bl4 = true;
                            }

                            for(Enchantment enchantment2 : map.keySet()) {
                                if (enchantment2 != enchantment && !enchantment.isCompatibleWith(enchantment2)) {
                                    bl4 = false;
                                    ++i;
                                }
                            }

                            if (!bl4) {
                                bl3 = true;
                            } else {
                                bl2 = true;
                                if (r > enchantment.getMaxLevel()) {
                                    r = enchantment.getMaxLevel();
                                }

                                map.put(enchantment, r);
                                int s = 0;
                                switch(enchantment.getRarity()) {
                                    case COMMON:
                                        s = 1;
                                        break;
                                    case UNCOMMON:
                                        s = 2;
                                        break;
                                    case RARE:
                                        s = 4;
                                        break;
                                    case VERY_RARE:
                                        s = 8;
                                }

                                if (bl) {
                                    s = Math.max(1, s / 2);
                                }

                                i += s * r;
                                if (itemStack.getCount() > 1) {
                                    i = 40;
                                }
                            }
                        }
                    }

                    if (bl3 && !bl2) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }
                }
            }

            if (StringUtils.isBlank(this.getItemName())) {
                if (itemStack.hasCustomHoverName()) {
                    k = 1;
                    i += k;
                    itemStack2.resetHoverName();
                }
            } else if (!this.getItemName().equals(itemStack.getHoverName().getString())) {
                k = 1;
                i += k;
                itemStack2.setHoverName(new TextComponent(this.getItemName()));
            }

            if (bl && !CommonAbstractions.INSTANCE.isBookEnchantable(itemStack2, itemStack3)) {
                itemStack2 = ItemStack.EMPTY;
            }

            this.setCost(j + i);
            if (i <= 0) {
                itemStack2 = ItemStack.EMPTY;
            }

            if (k == i && k > 0 && this.getCost() >= 40) {
                this.setCost(39);
            }

            if (this.getCost() >= 40 && !this.player.getAbilities().instabuild) {
                itemStack2 = ItemStack.EMPTY;
            }

            if (!itemStack2.isEmpty()) {
                int t = itemStack2.getBaseRepairCost();
                if (!itemStack3.isEmpty() && t < itemStack3.getBaseRepairCost()) {
                    t = itemStack3.getBaseRepairCost();
                }

                if (k != i || k == 0) {
                    t = calculateIncreasedRepairCost(t);
                }

                itemStack2.setRepairCost(t);
                EnchantmentHelper.setEnchantments(map, itemStack2);
            }

            this.resultSlots.setItem(0, itemStack2);
            this.broadcastChanges();
        }
    }
}
