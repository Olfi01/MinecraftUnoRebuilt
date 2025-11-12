package de.crazypokemondev.minecraftUnoRebuilt.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;

/**
 * An object representing the full set of configurations for the plugin.
 * Keep in mind, if you perform any changes other than adding new config options, a new version and a new transformation
 * need to be created in {@link ConfigHelper#getVersionUpdateTransformation()}.
 */
@ConfigSerializable
public class PluginConfig {
    public Uno uno = new Uno();

    @ConfigSerializable
    public static class Uno {
        public Deck deck = new Deck();

        @ConfigSerializable
        public static class Deck {
            @Comment("Whether the deck should be craftable. Default: true")
            public boolean disableCrafting = true;
            @Comment("The recipe for the deck. See https://docs.papermc.io/paper/dev/recipes/#shapedrecipe for details.")
            public UnoDeckRecipe recipe = new UnoDeckRecipe();

            @ConfigSerializable
            public static class UnoDeckRecipe {
                public String[] shape = new String[]{"DMD", "MHM", "DMD"};
                public List<Ingredient> ingredients = List.of(
                        new Ingredient('D', Material.COBBLED_DEEPSLATE),
                        new Ingredient('M', Material.NETHER_WART_BLOCK),
                        new Ingredient('H', Material.HAY_BLOCK)
                );

                @NoArgsConstructor
                @AllArgsConstructor
                @ConfigSerializable
                public static class Ingredient {
                    public char key;
                    public Material material;
                }
            }
        }
    }
}
