package de.crazypokemondev.minecraftUnoRebuilt.games.uno;

import de.crazypokemondev.minecraftUnoRebuilt.MinecraftUnoRebuilt;
import de.crazypokemondev.minecraftUnoRebuilt.games.lobby.GameState;
import de.crazypokemondev.minecraftUnoRebuilt.helpers.Callback;
import de.crazypokemondev.uniGUI.api.Gui;
import de.crazypokemondev.uniGUI.api.GuiState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.eu.zajc.juno.cards.UnoCard;
import org.eu.zajc.juno.cards.UnoCardColor;
import org.eu.zajc.juno.game.UnoGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Getter
@Slf4j
public class UnoState implements GameState {
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final List<UnoPlayer> players;
    @Setter
    private McUnoGame game;
    private boolean finished = false;
    @Setter
    private org.eu.zajc.juno.players.UnoPlayer nextPlayer;

    public UnoState(List<Player> players) {
        this.players = players.stream().map(UnoPlayer::new).toList();
    }

    public void setFinished() {
        if (!finished) {
            finished = true;
            notifyFinished();
        }
    }

    @Override
    public void start() {
        players.stream().map(UnoPlayer::getPlayer).forEach(this::join);
        scheduler.runTaskAsynchronously(MinecraftUnoRebuilt.INSTANCE, () -> {
            try {
                game.play();
            } catch (Exception ex) {
                log.error("An error occurred during a game.", ex);
            }
        });
    }

    @Override
    public void join(Player player) {
        final Optional<UnoPlayer> unoPlayer = players.stream().filter(p -> p.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst();
        scheduler.runTask(MinecraftUnoRebuilt.INSTANCE,
                () -> {
                    Gui gui = MinecraftUnoRebuilt.GUI_HANDLER.openGui(player, MinecraftUnoRebuilt.GuiIds.UNO, this);
                    unoPlayer.ifPresent(p -> p.setGui(gui));
                });
    }

    @Override
    public void abort() {
        game.endGame();
        scheduler.runTask(MinecraftUnoRebuilt.INSTANCE,
                () -> players.forEach(
                        player -> player.getOpenedGui().close(player.getPlayer())
                ));
    }

    @Getter
    public class UnoPlayer extends org.eu.zajc.juno.players.UnoPlayer implements GuiState {
        private final Player player;
        private final Callback<UnoCard> cardCallback = new Callback<>();
        private final Callback<UnoCardColor> colorCallback = new Callback<>();
        private final Callback<Boolean> booleanCallback = new Callback<>();
        @Setter
        private Gui gui;
        @Nullable
        private Gui popup;

        public UnoPlayer(Player player) {
            super(player.getName());
            this.player = player;
        }

        public Gui getOpenedGui() {
            return popup != null ? popup : gui;
        }

        public void sendCardCallback(UnoCard card) {
            cardCallback.set(card);
        }

        public void sendColorCallback(UnoCardColor color) {
            colorCallback.set(color);
        }

        public void sendBooleanCallback(Boolean bool) {
            booleanCallback.set(bool);
        }

        @Nullable
        @Override
        public UnoCard playCard(UnoGame unoGame) {
            cardCallback.reset();
            UnoState.this.setNextPlayer(UnoPlayer.this);
            UnoState.this.notifyStateChanged();
            do {
                try {
                    return cardCallback.get();
                } catch (InterruptedException e) {
                    log.error("A callback has been interrupted!", e);
                }
            } while (true);
        }

        @NotNull
        @Override
        public UnoCardColor chooseColor(UnoGame unoGame) {
            colorCallback.reset();
            UnoState.this.setNextPlayer(UnoPlayer.this);
            UnoState.this.notifyStateChanged();
            scheduler.runTask(MinecraftUnoRebuilt.INSTANCE, () -> {
                gui.close(player);
                popup = MinecraftUnoRebuilt.GUI_HANDLER.openGui(player, MinecraftUnoRebuilt.GuiIds.UNO_CHOOSE_COLOR, UnoPlayer.this);
            });
            try {
                UnoCardColor color = colorCallback.get();
                if (color == null) {
                    unoGame.onEvent("A player left unexpectedly!");
                    unoGame.endGame();
                    return UnoCardColor.RED;
                }
                scheduler.runTask(MinecraftUnoRebuilt.INSTANCE, () -> {
                    if (popup != null) popup.close(player);
                    gui.open(player);
                });
                return color;
            } catch (InterruptedException e) {
                log.error("A callback has been interrupted!", e);
                unoGame.endGame();
                return UnoCardColor.RED;
            }
        }

        @Override
        public boolean shouldPlayDrawnCard(UnoGame unoGame, UnoCard unoCard) {
            booleanCallback.reset();
            UnoState.this.setNextPlayer(UnoPlayer.this);
            UnoState.this.notifyStateChanged();
            scheduler.runTask(MinecraftUnoRebuilt.INSTANCE, () -> {
                gui.close(player);
                popup = MinecraftUnoRebuilt.GUI_HANDLER.openGui(player, MinecraftUnoRebuilt.GuiIds.UNO_PLAY_YES_NO, new YesNoState(UnoPlayer.this, unoCard));
            });
            try {
                Boolean bool = booleanCallback.get();
                if (bool == null) {
                    unoGame.onEvent("A player left unexpectedly!");
                    unoGame.endGame();
                    return false;
                }
                scheduler.runTask(MinecraftUnoRebuilt.INSTANCE, () -> {
                    if (popup != null) popup.close(player);
                    gui.open(player);
                });
                return bool;
            } catch (InterruptedException e) {
                log.error("A callback has been interrupted!", e);
                unoGame.endGame();
                return false;
            }
        }
    }
}
