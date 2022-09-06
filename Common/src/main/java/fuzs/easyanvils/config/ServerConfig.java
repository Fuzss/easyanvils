package fuzs.easyanvils.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig implements ConfigCore {
    @Config(description = "Repairing items in an anvil multiple times will not make subsequent repairs more and more expensive.")
    public boolean disablePriorWorkPenalty = true;
    @Config(description = {"The maximum amount of enchantment level allowed to be spent in an anvil. Everything above will be 'Too Expensive!' and will be disallowed.", "This option isn't that impactful anymore when prior work penalties are disabled; repair costs will almost never reach such a high value anymore."})
    public int maxAnvilRepairCost = 40;
    @Config(description = "Allow using iron blocks to repair an anvil by one stage. Also works automatically using dispensers.")
    public boolean anvilRepairing = true;
    @Config(description = "Renaming name tags in an anvil no longer costs any enchantment levels at all.")
    public boolean freeNameTagRenames = true;
    @Config(description = "Edit name tags without cost nor anvil, simply by sneak + right-clicking.")
    public boolean editNameTagsNoAnvil = true;
    @Config(description = "Chance the anvil will break into chipped or damaged variant, or break completely after using. Value is set to 0.12 in vanilla.")
    @Config.DoubleRange(min = 0.0, max = 1.0)
    public double anvilBreakChance = 0.05;
    @Config(description = "Solely renaming items in an anvil will never cause the anvil to break.")
    public boolean riskFreeAnvilRenaming = true;
}
