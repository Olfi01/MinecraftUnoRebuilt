package de.crazypokemondev.minecraftUnoRebuilt.gui.uno;

import de.crazypokemondev.minecraftUnoRebuilt.MinecraftUnoRebuilt;
import de.crazypokemondev.minecraftUnoRebuilt.games.uno.UnoState;
import de.crazypokemondev.minecraftUnoRebuilt.games.uno.YesNoState;
import de.crazypokemondev.minecraftUnoRebuilt.helpers.ItemHelper;
import de.crazypokemondev.uniGUI.api.GuiState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.eu.zajc.juno.cards.UnoCard;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.api.menu.ItemButton;
import xyz.janboerman.guilib.api.menu.MenuButton;
import xyz.janboerman.guilib.util.Scheduler;

public class VanillaUnoYesNo extends VanillaUnoCardsScreen {
    private final Player player;
    private final YesNoState state;

    public VanillaUnoYesNo(Player player, GuiState state) {
        super("Place drawn card?");
        this.player = player;
        this.state = (YesNoState) state;

        setButton(12, new BooleanButton(true, new ItemBuilder(Material.GREEN_CONCRETE).name("Yes").build()));
        setButton(14, new BooleanButton(false, new ItemBuilder(Material.RED_CONCRETE).name("No").build()));
        setButton(13, createCardButton(this.state.card()));

        setUnoCardButtons();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        switch (event.getReason()) {
            case PLUGIN:
                return;
            case DEATH:
            case DISCONNECT:
                state.player().sendBooleanCallback(null);
                return;
        }
        Scheduler scheduler = Scheduler.get();
        scheduler.runTaskLater(MinecraftUnoRebuilt.INSTANCE, event.getPlayer(),
                () -> MinecraftUnoRebuilt.GUI_HANDLER.openGui(player, MinecraftUnoRebuilt.GuiIds.UNO_PLAY_YES_NO, state));
    }

    @Override
    protected UnoState.UnoPlayer getPlayer() {
        return state != null ? state.player() : null;
    }

    @Override
    protected MenuButton<? extends VanillaUnoCardsScreen> createCardButton(UnoCard card) {
        return new ItemButton<VanillaUnoYesNo>(ItemHelper.fromCard(card));
    }

    private class BooleanButton extends ItemButton<VanillaUnoYesNo> {
        private final boolean value;

        public BooleanButton(boolean value, ItemStack icon) {
            super(icon);
            this.value = value;
        }

        @Override
        public void onClick(VanillaUnoYesNo holder, InventoryClickEvent event) {
            state.player().sendBooleanCallback(value);
        }
    }
}
