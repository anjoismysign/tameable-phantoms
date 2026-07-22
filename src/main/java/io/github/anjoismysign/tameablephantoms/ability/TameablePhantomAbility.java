package io.github.anjoismysign.tameablephantoms.ability;

import io.github.anjoismysign.tameablephantoms.entity.TameablePhantom;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface TameablePhantomAbility {

    /**
     * What to run (on each tick) while the ability is active.
     * @param tameablePhantom the phantom that has the ability.
     * @param rider the player that's riding the phantom.
     */
    void run(@NotNull TameablePhantom tameablePhantom, @NotNull Player rider);

}
