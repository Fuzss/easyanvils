package fuzs.easyanvils.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.ValueCallback;
import fuzs.puzzleslib.config.core.AbstractConfigBuilder;
import fuzs.puzzleslib.core.ModLoaderEnvironment;

public class CommonConfig implements ConfigCore {
    public boolean thingsIntegration;
    public boolean apotheosisIntegration;

    @Override
    public void addToBuilder(AbstractConfigBuilder builder, ValueCallback callback) {
        builder.push("integration");
        if (ModLoaderEnvironment.INSTANCE.getModLoader().isFabric()) {
            callback.accept(builder.comment("Allow for integration with the Things mod.").define("things", true), v -> this.thingsIntegration = v);
        }
        if (ModLoaderEnvironment.INSTANCE.getModLoader().isForge()) {
            callback.accept(builder.comment("Allow for integration with the Apotheosis mod.").define("apotheosis", true), v -> this.apotheosisIntegration = v);
        }
        builder.pop();
    }
}
