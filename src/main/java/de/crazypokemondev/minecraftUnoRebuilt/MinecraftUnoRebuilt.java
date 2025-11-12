package de.crazypokemondev.minecraftUnoRebuilt;

import com.jeff_media.customblockdata.CustomBlockData;
import de.crazypokemondev.minecraftUnoRebuilt.config.ConfigHelper;
import de.crazypokemondev.minecraftUnoRebuilt.config.PluginConfig;
import de.crazypokemondev.minecraftUnoRebuilt.games.lobby.LobbyState;
import de.crazypokemondev.minecraftUnoRebuilt.gui.lobby.VanillaLobbyGui;
import de.crazypokemondev.minecraftUnoRebuilt.gui.uno.VanillaChooseColorGui;
import de.crazypokemondev.minecraftUnoRebuilt.gui.uno.VanillaUnoGui;
import de.crazypokemondev.minecraftUnoRebuilt.gui.uno.VanillaUnoYesNo;
import de.crazypokemondev.minecraftUnoRebuilt.helpers.ItemHelper;
import de.crazypokemondev.minecraftUnoRebuilt.listeners.DestroyBlockListener;
import de.crazypokemondev.minecraftUnoRebuilt.listeners.GSitListener;
import de.crazypokemondev.minecraftUnoRebuilt.listeners.PlaceBlockListener;
import de.crazypokemondev.minecraftUnoRebuilt.listeners.PlayerInteractListener;
import de.crazypokemondev.uniGUI.api.GuiHandler;
import de.crazypokemondev.uniGUI.api.GuiRegistry;
import de.crazypokemondev.uniGUI.guis.vanilla.VanillaGuiFactoryStateful;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
public final class MinecraftUnoRebuilt extends JavaPlugin {
    public static final String PLUGIN_ID = "MinecraftUnoRebuilt";
    public static MinecraftUnoRebuilt INSTANCE;
    public static GuiHandler GUI_HANDLER;
    /**
     * This config is currently READ ONLY. Changes made on the object will NOT persist after a server restart.
     */
    public static PluginConfig CONFIG;

    public final NamespacedKey UNO_DECK_BLOCK_DATA = new NamespacedKey(this, "uno_deck");
    public final Map<UUID, LobbyState<?>> lobbies = new HashMap<>();

    private static final String CONFIG_FILE_NAME = "UnoRebuilt.conf";

    public MinecraftUnoRebuilt() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        loadConfig();

        RegisteredServiceProvider<GuiRegistry> guiRegistryProvider = getServer().getServicesManager().getRegistration(GuiRegistry.class);
        if (guiRegistryProvider == null) {
            log.error("No GuiRegistry service found! Please make sure UniGUI plugin is present in the plugins folder!");
            return;
        }
        registerGuis(guiRegistryProvider.getProvider());

        RegisteredServiceProvider<GuiHandler> guiHandlerProvider = getServer().getServicesManager().getRegistration(GuiHandler.class);
        if (guiHandlerProvider == null) {
            log.error("No GuiRegistry service found! Please make sure UniGUI plugin is present in the plugins folder!");
            return;
        }
        GUI_HANDLER = guiHandlerProvider.getProvider();

        registerEventListeners();

        registerCraftingRecipes();
    }

    private void loadConfig() {
        final HoconConfigurationLoader loader = ConfigHelper.configureLoader()
                .path(getDataPath().resolve(CONFIG_FILE_NAME))
                .build();

        try {
            CommentedConfigurationNode node = loader.load();
            if (!node.virtual()) {
                final ConfigurationTransformation.Versioned transformation = ConfigHelper.getVersionUpdateTransformation();
                final int startVersion = transformation.version(node);
                transformation.apply(node);
                final int endVersion = transformation.version(node);
                if (startVersion != endVersion) {
                    log.info("Updated config schema from version {} to {}", startVersion, endVersion);
                }
            }
            CONFIG = node.get(PluginConfig.class, (Supplier<PluginConfig>) PluginConfig::new);
            // save in case new defaults have been added
            loader.save(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException("An error occurred while trying to load the configuration file!", e);
        }
    }

    private void registerEventListeners() {
        CustomBlockData.registerListener(this);
        getServer().getPluginManager().registerEvents(new PlaceBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new DestroyBlockListener(this), this);
        if (getServer().getPluginManager().getPlugin("GSit") != null) {
            getServer().getPluginManager().registerEvents(new GSitListener(this), this);
        }
    }

    private void registerCraftingRecipes() {
        if (CONFIG.uno.deck.disableCrafting) {
            ItemStack unoDeck = ItemHelper.createUnoDeck();
            ShapedRecipe unoDeckRecipe = new ShapedRecipe(new NamespacedKey(this, "uno_deck"), unoDeck)
                    .shape(CONFIG.uno.deck.recipe.shape);
            for (PluginConfig.Uno.Deck.UnoDeckRecipe.Ingredient ingredient : CONFIG.uno.deck.recipe.ingredients) {
                unoDeckRecipe.setIngredient(ingredient.key, ingredient.material);
            }
            getServer().addRecipe(unoDeckRecipe);
        }
    }

    private void registerGuis(GuiRegistry registry) {
        registry.register(new VanillaGuiFactoryStateful(GuiIds.LOBBY, VanillaLobbyGui::new));
        registry.register(new VanillaGuiFactoryStateful(GuiIds.UNO, VanillaUnoGui::new));
        registry.register(new VanillaGuiFactoryStateful(GuiIds.UNO_CHOOSE_COLOR, VanillaChooseColorGui::new));
        registry.register(new VanillaGuiFactoryStateful(GuiIds.UNO_PLAY_YES_NO, VanillaUnoYesNo::new));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static class GuiIds {
        public static final String LOBBY = PLUGIN_ID + ":lobby";
        public static final String UNO = PLUGIN_ID + ":uno";
        public static final String UNO_CHOOSE_COLOR = ":uno_choose_color";
        public static final String UNO_PLAY_YES_NO = ":uno_yes_no";
    }
}
