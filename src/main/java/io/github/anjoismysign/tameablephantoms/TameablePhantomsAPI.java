package io.github.anjoismysign.tameablephantoms;

import io.github.anjoismysign.tameablephantoms.ability.TameablePhantomAbilityRegistry;
import io.github.anjoismysign.tameablephantoms.entity.TameablePhantom;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main API interface for the TameablePhantoms plugin.
 * Provides methods to tame phantoms, check if an entity is a tameable phantom,
 * and equip them with saddles.
 */
public interface TameablePhantomsAPI {

    @NotNull
    TameablePhantomAbilityRegistry getAbilityRegistry();

    /**
     * Checks if the given entity is a tameable phantom.
     *
     * @param entity the entity to check
     * @return the TameablePhantom wrapper if the entity is a tameable phantom, null otherwise
     */
    @Nullable
    TameablePhantom isTameablePhantom(@NotNull Entity entity);

    /**
     * Tames a phantom, assigning it to the specified tamer.
     *
     * @param phantom the phantom to tame
     * @param tamer   the owner to assign to the phantom
     * @return the TameablePhantom wrapper for the newly tamed phantom
     * @throws IllegalStateException if the phantom is already tamed
     */
    @NotNull
    TameablePhantom tame(@NotNull Phantom phantom, @NotNull AnimalTamer tamer);

    /**
     * Sets whether a phantom has a saddle.
     * A saddled phantom can be ridden by its owner.
     *
     * @param phantom the phantom to set the saddle on
     * @param saddle  true to add a saddle, false to remove it
     */
    void setSaddle(@NotNull Phantom phantom, boolean saddle);

}
