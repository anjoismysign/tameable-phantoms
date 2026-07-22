package io.github.anjoismysign.tameablephantoms.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface TameablePhantomAbilityRegistry {

    void registerAbility(@NotNull NamespacedKey namespacedKey, @NotNull TameablePhantomAbility ability);

}
