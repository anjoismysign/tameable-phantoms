package io.github.anjoismysign.tameablephantoms.entity;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a tamed phantom, wrapping the underlying Bukkit {@link Phantom}
 * and providing access to its owner and saddle state.
 */
public interface TameablePhantom {

    /**
     * Gets the underlying Bukkit phantom entity.
     *
     * @return the phantom entity
     */
    @NotNull
    Phantom getPhantom();

    /**
     * Gets the owner (tamer) of this phantom.
     *
     * @return the animal tamer who owns this phantom
     */
    @NotNull
    AnimalTamer getOwner();

    /**
     * Checks whether this phantom has a saddle equipped.
     *
     * @return true if the phantom has a saddle, false otherwise
     */
    boolean hasSaddle();

    /**
     * Gets this phantom's ability
     * @return null if this phantom has no ability, the ability otherwise
     */
    @Nullable
    NamespacedKey getAbility();

    /**
     * Sets the ability to this phantom.
     * @param ability null to remove the previous, the ability NamespacedKey otherwise
     * @see io.github.anjoismysign.tameablephantoms.ability.TameablePhantomAbilityRegistry
     */
    void setAbility(@Nullable NamespacedKey ability);

}
