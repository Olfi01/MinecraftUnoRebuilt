package de.crazypokemondev.minecraftUnoRebuilt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.crazypokemondev.minecraftUnoRebuilt.helpers.ItemHelper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class UnoCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("uno")
                .then(Commands.literal("deck")
                        .then(Commands.literal("give")
                                .requires(context -> context.getExecutor() instanceof Player && context.getSender().hasPermission("uno.deck.give.self"))
                                .executes(context -> giveDeck(context, List.of((Player) Objects.requireNonNull(context.getSource().getExecutor()))))
                                .then(Commands.argument("to", ArgumentTypes.players())
                                        .requires(context -> context.getSender().hasPermission("uno.deck.give"))
                                        .executes(UnoCommand::giveDeck)
                                )
                        )
                );
    }

    private static int giveDeck(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetResolver = context.getArgument("to", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = targetResolver.resolve(context.getSource());
        return giveDeck(context, targets);
    }

    private static int giveDeck(CommandContext<CommandSourceStack> context, List<Player> targets) {
        CommandSender sender = context.getSource().getSender();
        for (final Player target : targets) {
            target.give(ItemHelper.createUnoDeck());
            sender.sendRichMessage("Gave UNO deck to <target>.", Placeholder.component("target", target.name()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
