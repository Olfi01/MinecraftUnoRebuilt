package de.crazypokemondev.minecraftUnoRebuilt.config;

import de.crazypokemondev.minecraftUnoRebuilt.config.serializers.MaterialSerializer;
import org.bukkit.Material;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

public class ConfigHelper {
    private ConfigHelper() {
    }

    static final int LATEST_VERSION = 1;

    public static ConfigurationTransformation.Versioned getVersionUpdateTransformation() {
        return ConfigurationTransformation.versionedBuilder()
                .addVersion(LATEST_VERSION, initialTransform())
                .build();
    }

    private static ConfigurationTransformation initialTransform() {
        return ConfigurationTransformation.builder().build();
    }

    public static HoconConfigurationLoader.Builder configureLoader() {
        return HoconConfigurationLoader.builder()
                .defaultOptions(ConfigHelper::setLoaderOptions)
                .emitComments(true)
                .prettyPrinting(true);
    }

    public static ConfigurationOptions setLoaderOptions(ConfigurationOptions options) {
        return options.serializers(builder -> builder.register(Material.class, MaterialSerializer.INSTANCE));
    }
}
