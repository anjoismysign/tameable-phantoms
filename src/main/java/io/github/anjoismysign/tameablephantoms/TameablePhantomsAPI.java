package io.github.anjoismysign.tameablephantoms;

import io.github.anjoismysign.tameablephantoms.entity.TameablePhantom;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TameablePhantomsAPI {

    @Nullable
    TameablePhantom isTameablePhantom(@NotNull Entity entity);

    @NotNull
    TameablePhantom tame(@NotNull Phantom phantom, @NotNull AnimalTamer tamer);

    void setSaddle(@NotNull Phantom phantom, boolean saddle);

}
