package de.crazypokemondev.minecraftUnoRebuilt.gui.uno;

import de.crazypokemondev.minecraftUnoRebuilt.games.uno.UnoState;
import de.crazypokemondev.minecraftUnoRebuilt.helpers.ItemHelper;
import de.crazypokemondev.minecraftUnoRebuilt.helpers.Updatable;
import de.crazypokemondev.uniGUI.api.Gui;
import de.crazypokemondev.uniGUI.api.GuiState;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.eu.zajc.juno.cards.UnoCard;
import org.eu.zajc.juno.players.UnoPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.api.menu.ItemButton;
import xyz.janboerman.guilib.api.menu.MenuButton;

import java.util.Iterator;

public class VanillaUnoGui extends VanillaUnoCardsScreen implements Gui {
    private final @Nullable UnoState.UnoPlayer player;
    private final UnoState state;

    public VanillaUnoGui(Player player, GuiState state) {
        super("UNO");
        this.state = (UnoState) state;
        this.player = this.state.getPlayers().stream()
                .filter(p -> p.getPlayer().getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);
        this.state.registerUpdateHandler(this::update);

        WaitingIndicatorButton waitingButton = new WaitingIndicatorButton();
        for (int i = 0; i < 9; i++) {
            setButton(i, waitingButton);
        }
        setButton(12, new DrawPileButton());
        setButton(14, new DiscardPileButton());

        Iterator<UnoState.UnoPlayer> players = this.state.getPlayers().iterator();
        for (int i = 18; i < 27; i++) {
            if (!players.hasNext()) break;
            setButton(i, new OpponentButton(players.next()));
        }

        setUnoCardButtons();
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        update();
        super.onClick(event);
    }

    private void update() {
        setUnoCardButtons();
        getButtons().forEach(this::updateButton);
    }

    private void updateButton(int ignored, MenuButton<?> button) {
        if (button instanceof Updatable u) {
            u.update();
        }
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
    protected UnoState.@Nullable UnoPlayer getPlayer() {
        return player;
    }

    @Override
    protected MenuButton<? extends VanillaUnoCardsScreen> createCardButton(UnoCard card) {
        return new UnoCardButton(card);
    }

    private class DrawPileButton extends ItemButton<VanillaUnoGui> {
        public DrawPileButton() {
            ItemStack icon = ItemHelper.drawPileIcon();
            setIcon(icon);
        }

        @Override
        public void onClick(VanillaUnoGui holder, InventoryClickEvent event) {
            if (player == null || player != state.getNextPlayer()) return;
            player.sendCardCallback(null);
        }
    }

    private class DiscardPileButton extends ItemButton<VanillaUnoGui> implements Updatable {
        public DiscardPileButton() {
            super(ItemHelper.fromCard(state.getGame().getDiscard().getTop()));
        }

        @Override
        public void update() {
            setIcon(ItemHelper.fromCard(state.getGame().getDiscard().getTop()));
        }
    }

    private static class OpponentButton extends ItemButton<VanillaUnoGui> implements Updatable {
        private final UnoState.UnoPlayer player;
        private final ItemStack icon;

        public OpponentButton(UnoState.UnoPlayer player) {
            this.player = player;

            int amount = Math.max(player.getHandSize(), 1);

            icon = new ItemBuilder(Material.PLAYER_HEAD)
                    .name(player.getName())
                    .changeMeta((SkullMeta meta) -> meta.setOwningPlayer(player.getPlayer()))
                    .amount(amount)
                    .build();

            setIcon(icon);
        }

        @Override
        public void update() {
            int amount = Math.max(player.getHandSize(), 1);
            icon.setAmount(amount);
            setIcon(icon);
        }
    }

    private class UnoCardButton extends ItemButton<VanillaUnoGui> {
        private final UnoCard card;

        public UnoCardButton(UnoCard card) {
            super(ItemHelper.fromCard(card));
            this.card = card;
        }

        @Override
        public void onClick(VanillaUnoGui holder, InventoryClickEvent event) {
            if (player == null || player != state.getNextPlayer()) return;
            player.sendCardCallback(card);
        }
    }

    private class WaitingIndicatorButton extends ItemButton<VanillaUnoGui> implements Updatable {
        ItemStack notWaitingIcon = new ItemBuilder(Material.RED_CONCRETE).name("Not your turn").build();
        ItemStack waitingIcon = new ItemBuilder(Material.LIME_CONCRETE).name("Your turn").build();
        ItemStack finishedIcon = new ItemBuilder(Material.GOLD_BLOCK).name("The game is finished!").build();

        public WaitingIndicatorButton() {
            update();
        }

        @Override
        public void update() {
            UnoPlayer nextPlayer = state.getNextPlayer();
            ItemStack icon = state.isFinished() ? finishedIcon : (player != null && player == nextPlayer ? waitingIcon : notWaitingIcon);
            if (icon == waitingIcon && nextPlayer != null) {
                icon.editMeta(meta -> meta.displayName(Component.text(nextPlayer.getName() + "'s turn")));
            }
            setIcon(icon);
        }
    }
}
