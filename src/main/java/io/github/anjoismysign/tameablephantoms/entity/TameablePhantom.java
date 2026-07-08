package io.github.anjoismysign.tameablephantoms.entity;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;

public interface TameablePhantom {

    @NotNull
    Phantom getPhantom();

    @NotNull
    AnimalTamer getOwner();

    boolean hasSaddle();

}
