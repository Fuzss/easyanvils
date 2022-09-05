package fuzs.easyanvils.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ClientConfig implements ConfigCore {
    @Config(description = "Render inventory contents of an anvil.")
    public boolean renderAnvilContents = true;
}
