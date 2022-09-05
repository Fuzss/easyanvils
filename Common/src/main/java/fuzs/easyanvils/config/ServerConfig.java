package fuzs.easyanvils.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig implements ConfigCore {
    @Config(description = "Edit name tags without cost nor anvil, simply by sneak-right-clicking.")
    public boolean editNameTags = true;
}
