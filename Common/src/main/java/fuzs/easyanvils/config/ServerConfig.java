package fuzs.easyanvils.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig implements ConfigCore {
    @Config(description = "Allow using iron blocks to repair an anvil by one stage.")
    public boolean anvilRepairing = true;
    @Config(description = "Edit name tags without cost nor anvil, simply by sneak + right-clicking.")
    public boolean editNameTags = true;
}
