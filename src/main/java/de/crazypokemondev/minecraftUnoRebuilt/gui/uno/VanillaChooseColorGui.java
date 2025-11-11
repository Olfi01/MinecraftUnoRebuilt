package de.crazypokemondev.minecraftUnoRebuilt.gui.uno;

import de.crazypokemondev.minecraftUnoRebuilt.MinecraftUnoRebuilt;
import de.crazypokemondev.minecraftUnoRebuilt.games.uno.UnoState;
import de.crazypokemondev.minecraftUnoRebuilt.helpers.ItemHelper;
import de.crazypokemondev.uniGUI.api.Gui;
import de.crazypokemondev.uniGUI.api.GuiState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.eu.zajc.juno.cards.UnoCard;
import org.eu.zajc.juno.cards.UnoCardColor;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.api.menu.ItemButton;
import xyz.janboerman.guilib.api.menu.MenuButton;
import xyz.janboerman.guilib.util.Scheduler;

public class VanillaChooseColorGui extends VanillaUnoCardsScreen implements Gui {
    private final UnoState.UnoPlayer state;
    private final Player player;

    public VanillaChooseColorGui(Player player, GuiState state) {
        super("Choose color:");
        this.player = player;
        this.state = (UnoState.UnoPlayer) state;
        setButton(10, new ChooseColorButton(UnoCardColor.RED, new ItemBuilder(Material.RED_WOOL).name("Red").build()));
        setButton(12, new ChooseColorButton(UnoCardColor.GREEN, new ItemBuilder(Material.GREEN_WOOL).name("Green").build()));
        setButton(14, new ChooseColorButton(UnoCardColor.BLUE, new ItemBuilder(Material.BLUE_WOOL).name("Blue").build()));
        setButton(16, new ChooseColorButton(UnoCardColor.YELLOW, new ItemBuilder(Material.YELLOW_WOOL).name("Yellow").build()));

        setUnoCardButtons();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        switch (event.getReason()) {
            case PLUGIN:
                return;
            case DEATH:
            case DISCONNECT:
                state.sendColorCallback(null);
                return;
        }
        Scheduler scheduler = Scheduler.get();
        scheduler.runTaskLater(MinecraftUnoRebuilt.INSTANCE, event.getPlayer(),
                () -> MinecraftUnoRebuilt.GUI_HANDLER.openGui(player, MinecraftUnoRebuilt.GuiIds.UNO_CHOOSE_COLOR, state));
    }

    @Override
    public void open(Player player) {
        player.openInventory(this.getInventory());
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    @Override
    protected UnoState.UnoPlayer getPlayer() {
        return state;
    }

    @Override
    protected MenuButton<? extends VanillaUnoCardsScreen> createCardButton(UnoCard card) {
        return new ItemButton<VanillaChooseColorGui>(ItemHelper.fromCard(card));
    }

    private class ChooseColorButton extends ItemButton<VanillaChooseColorGui> {
        private final UnoCardColor color;

        private ChooseColorButton(UnoCardColor color, ItemStack icon) {
            super(icon);
            this.color = color;
        }

        @Override
        public void onClick(VanillaChooseColorGui holder, InventoryClickEvent event) {
            state.sendColorCallback(color);
        }
    }
}
