package fuzs.easyanvils.config;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public class ServerConfig implements ConfigCore {
    @Config(description = "Changes how repairing items in an anvil multiple times will affect the cost of subsequent repairs. In vanilla the penalty doubles with every repair. When set to fixed a constant multiplier will instead be added with every repair.")
    public PriorWorkPenalty priorWorkPenalty = PriorWorkPenalty.FIXED;
    @Config(description = "Multiplier to use when \"prior_work_penalty\" is set to \"FIXED\". Every subsequent operation will increase by this value in levels.")
    @Config.IntRange(min = 1)
    public int priorWorkPenaltyMultiplier = 3;
    @Config(description = {"The maximum amount of enchantment level allowed to be spent in an anvil. Everything above will be 'Too Expensive!' and will be disallowed.", "This option isn't that impactful anymore when prior work penalties are disabled; repair costs will basically never reach such a high value anymore."})
    public int maxAnvilRepairCost = 63;
    @Config(description = "Allow using iron blocks to repair an anvil by one stage. Can be automated using dispensers.")
    public boolean anvilRepairing = true;
    @Config(description = "Renaming any item in an anvil no longer costs any enchantment levels at all. Can be restricted to only name tags.")
    public FreeRenames freeRenames = FreeRenames.ALL_ITEMS;
    @Config(description = "Edit name tags without cost nor anvil, simply by sneak + right-clicking.")
    public boolean editNameTagsNoAnvil = true;
    @Config(description = "Chance the anvil will break into chipped or damaged variant, or break completely after using. Value is set to 0.12 in vanilla.")
    @Config.DoubleRange(min = 0.0, max = 1.0)
    public double anvilBreakChance = 0.05;
    @Config(description = "Solely renaming items in an anvil will never cause the anvil to break.")
    public boolean riskFreeAnvilRenaming = true;
    @Config(description = "When combining enchanted items in an anvil, prevent an enchantment level higher than the max level value for that enchantment from being lowered to said max value.")
    public boolean noAnvilMaxLevelLimit = true;
    @Config(description = {"The naming field in anvils and the name tag gui will support formatting codes for setting custom text colors and styles.", "Check out the Minecraft Wiki for all available formatting codes and their usage: https://minecraft.fandom.com/wiki/Formatting_codes#Usage"})
    public boolean renamingSupportsFormatting = true;
    @Config(description = {"Always allow renaming and repairing items, even when the cost exceeds the max anvil repair cost. The cost will then be capped just below the max cost. This is already done in vanilla for renaming.", "This also prevents the prior work penalty from increasing when the item has only been renamed or repaired."})
    public boolean alwaysRenameAndRepair = true;

    public enum FreeRenames {
        OFF(stack -> false),
        ALL_ITEMS(stack -> true),
        NAME_TAGS_ONLY(stack -> stack.is(Items.NAME_TAG));

        public final Predicate<ItemStack> filter;

        FreeRenames(Predicate<ItemStack> filter) {
            this.filter = filter;
        }
    }

    public enum PriorWorkPenalty {
        OFF(i -> 0),
        VANILLA(IntUnaryOperator.identity()),
        FIXED(repairCost -> ModAnvilMenu.repairCostToRepairs(repairCost) * EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenaltyMultiplier);

        public final IntUnaryOperator operator;

        PriorWorkPenalty(IntUnaryOperator operator) {
            this.operator = operator;
        }
    }
}
