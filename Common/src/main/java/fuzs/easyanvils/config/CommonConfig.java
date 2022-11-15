package fuzs.easyanvils.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.ValueCallback;
import fuzs.puzzleslib.config.core.AbstractConfigBuilder;

public class CommonConfig implements ConfigCore {
    public boolean thingsIntegration;
    public boolean apotheosisIntegration;

    @Override
    public void addToBuilder(AbstractConfigBuilder builder, ValueCallback callback) {
        builder.push("integration");
        callback.accept(builder.comment("Allow for integration with the Things mod.").define("things", true), v -> this.thingsIntegration = v);
        builder.pop();
//        if (ModLoaderEnvironment.INSTANCE.getModLoader().isForge()) {
//            callback.accept(builder.comment("Allow for integration with the Apotheosis mod.").define("apotheosis", true), v -> this.apotheosisIntegration = v);
//        }
    }
}
