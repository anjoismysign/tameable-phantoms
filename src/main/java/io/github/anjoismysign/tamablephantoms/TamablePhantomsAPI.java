package io.github.anjoismysign.tamablephantoms;

import io.github.anjoismysign.tamablephantoms.entity.TameablePhantom;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TamablePhantomsAPI {

    @Nullable
    TameablePhantom isTameablePhantom(@NotNull Entity entity);

    @NotNull
    TameablePhantom tame(@NotNull Phantom phantom, @NotNull AnimalTamer tamer);

    void setSaddle(@NotNull Phantom phantom, boolean saddle);

}
