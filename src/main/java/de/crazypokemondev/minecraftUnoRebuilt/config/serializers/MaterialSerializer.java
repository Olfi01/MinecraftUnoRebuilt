package de.crazypokemondev.minecraftUnoRebuilt.config.serializers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MaterialSerializer implements TypeSerializer<Material> {
    public static MaterialSerializer INSTANCE = new MaterialSerializer();

    @Override
    public Material deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String name = node.getString();
        if (name == null) throw new SerializationException(node, Material.class, "No material found!");
        try {
            return Enum.valueOf(Material.class, name);
        } catch (IllegalArgumentException e) {
            throw new SerializationException(node, Material.class, name + " is not a valid crafting material!");
        }
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Material material, @NotNull ConfigurationNode target) {
        if (material == null) {
            target.raw(null);
            return;
        }
        target.raw(material.toString());
    }
}
