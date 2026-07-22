package io.github.anjoismysign.tameablephantoms;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.github.anjoismysign.tameablephantoms.entity.TameablePhantom;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class TameablePhantoms extends JavaPlugin implements TameablePhantomsAPI, Listener {

    private static TameablePhantoms instance;

    public static TameablePhantoms getInstance(){
        return instance;
    }

    private NamespacedKey tamerKey;
    private NamespacedKey saddleKey;
    private final Map<UUID, TameablePhantom> tameablePhantoms = new WeakHashMap<>();
    private final Set<TameablePhantom> unriding = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        tamerKey = new NamespacedKey(this, "tamer");
        saddleKey = new NamespacedKey(this, "saddle");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::tick, 1L, 1L);
    }

    private void tick() {
        tameablePhantoms.values().stream().map(TameablePhantom::getPhantom).filter(Entity::isValid).forEach(phantom -> {
            @Nullable var player = hasPlayer(phantom);
            if (player == null){
                phantom.setRotation(phantom.getYaw(), 0);
                return;
            }
            var input = player.getCurrentInput();
            var playerLocation = player.getEyeLocation();
            var direction = player.getEyeLocation().getDirection();
            var speed = Objects.requireNonNull(phantom.getAttribute(Attribute.MOVEMENT_SPEED), "phantoms don't have movement_speed attribute").getValue() * Objects.requireNonNull(phantom.getAttribute(Attribute.SCALE), "phantoms don't have scale attribute").getValue();
            var velocity = input.isForward() ? direction.multiply(speed) : input.isBackward() ? direction.multiply(-speed) : new Vector(0,0,0);
            if (input.isForward() || input.isBackward()) {
                phantom.setVelocity(velocity);
                phantom.setRotation(playerLocation.getYaw(), 0);
            } else {
                phantom.setRotation(phantom.getYaw(), 0);
            }
        });
    }

    @EventHandler
    public void onInputChange(PlayerInputEvent event){
        var player = event.getPlayer();
        @Nullable var vehicle = player.getVehicle();
        if (vehicle == null){
            return;
        }
        @Nullable var tameablePhantom = isTameablePhantom(vehicle);
        if (tameablePhantom == null){
            return;
        }
        var phantom = tameablePhantom.getPhantom();
        var input = event.getInput();
        if (!input.isForward() && !input.isBackward() || input.isSneak()){
            phantom.setAI(false);
            return;
        }
        phantom.setAI(true);
    }

    @EventHandler
    public void onSaddle(PlayerInteractEntityEvent event){
        var player = event.getPlayer();
        var entity = event.getRightClicked();
        @Nullable var tameablePhantom = isTameablePhantom(entity);
        if (tameablePhantom == null) {
            return;
        }
        if (tameablePhantom.hasSaddle()){
            return;
        }
        var playerEquipment = player.getEquipment();
        if (!consumeItem(Material.SADDLE, playerEquipment)){
            return;
        }
        var phantom = tameablePhantom.getPhantom();
        setSaddle(phantom, true);
    }

    @EventHandler
    public void onTame(PlayerInteractEntityEvent event){
        var player = event.getPlayer();
        var entity = event.getRightClicked();
        @Nullable var tameablePhantom = isTameablePhantom(entity);
        if (tameablePhantom != null) {
            return;
        }
        if (entity.getType() != EntityType.PHANTOM){
            return;
        }
        var playerEquipment = player.getEquipment();
        if (!consumeItem(Material.CLOCK, playerEquipment)){
            return;
        }
        var phantom = (Phantom) entity;
        tame(phantom, player);
    }

    @EventHandler
    public void onRide(PlayerInteractEntityEvent event) {
        var player = event.getPlayer();
        var entity = event.getRightClicked();
        @Nullable var tameablePhantom = isTameablePhantom(entity);
        if (tameablePhantom == null) {
            return;
        }
        if (!tameablePhantom.hasSaddle()) {
            return;
        }
        var phantom = tameablePhantom.getPhantom();
        if (hasPlayer(phantom) != null) {
            return;
        }

        // Restore the hologram of whatever tamed phantom the player is currently riding
        @Nullable var currentVehicle = player.getVehicle();
        if (currentVehicle != null) {
            @Nullable var current = isTameablePhantom(currentVehicle);
            if (current != null) {
                currentVehicle.removePassenger(player);
                unriding.add(current);
            }
        }

        updateHologram(tameablePhantom, false);
        phantom.addPassenger(player);
    }

    @EventHandler
    public void onVehicleLeave(EntityDismountEvent event) {
        var dismounter = event.getEntity();
        if (dismounter.getType() != EntityType.PLAYER) {
            return;
        }
        var vehicle = event.getDismounted();
        @Nullable var tameablePhantom = isTameablePhantom(vehicle);
        if (tameablePhantom == null) {
            return;
        }
        var player = (Player) dismounter;
        if (player.getCurrentInput().isSneak() || unriding.contains(tameablePhantom)){
            unriding.remove(tameablePhantom);
            updateHologram(tameablePhantom, true);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onRemove(EntityRemoveEvent event){
        var entity = event.getEntity();
        @Nullable var tameablePhantom = isTameablePhantom(entity);
        if (tameablePhantom == null){
            return;
        }
        updateHologram(tameablePhantom, false);
    }

    @EventHandler
    public void onSpawn(EntityAddToWorldEvent event) {
        var entity = event.getEntity();
        if (entity.getType() != EntityType.PHANTOM) {
            return;
        }
        var dataContainer = entity.getPersistentDataContainer();
        @Nullable var compactUniqueId = dataContainer.get(tamerKey, PersistentDataType.STRING);
        if (compactUniqueId == null) {
            return;
        }
        var formattedUniqueId = new StringBuilder(compactUniqueId)
                .insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-")
                .toString();
        var uniqueId = UUID.fromString(formattedUniqueId);
        var phantom = (Phantom) entity;
        mutate(phantom, new AnimalTamer() {
            @Override
            public @Nullable String getName() {
                @Nullable var player = Bukkit.getPlayer(uniqueId);
                if (player == null) {
                    return null;
                }
                return player.getName();
            }
            @Override
            public @NotNull UUID getUniqueId() {
                return uniqueId;
            }
        });
    }

    @Override
    public @Nullable TameablePhantom isTameablePhantom(@NotNull Entity entity) {
        return tameablePhantoms.get(entity.getUniqueId());
    }

    @NotNull
    @Override
    public TameablePhantom tame(@NotNull Phantom phantom, @NotNull AnimalTamer tamer) {
        @Nullable var result = isTameablePhantom(phantom);
        if (result != null) {
            throw new IllegalStateException("Phantom is already tamed. Call #isTamed(Phantom) before calling #tame(Phantom, AnimalTamer).");
        }
        var dataContainer = phantom.getPersistentDataContainer();
        dataContainer.set(tamerKey, PersistentDataType.STRING, tamer.getUniqueId().toString().replace("-", ""));
        phantom.setPersistent(true);
        var tameablePhantom = mutate(phantom, tamer);
        updateHologram(tameablePhantom, hasPlayer(phantom) == null);
        return tameablePhantom;
    }

    @Override
    public void setSaddle(@NotNull Phantom phantom, boolean saddle){
        @Nullable var tameablePhantom = isTameablePhantom(phantom);
        if (tameablePhantom == null) {
            return;
        }
        if (tameablePhantom.hasSaddle()){
            return;
        }
        phantom.getPersistentDataContainer().set(saddleKey, PersistentDataType.BOOLEAN, saddle);
        updateHologram(tameablePhantom, hasPlayer(phantom) == null);
    }

    private TameablePhantom mutate(@NotNull Phantom phantom, @NotNull AnimalTamer tamer) {
        var tameablePhantom = new TameablePhantom() {
            @Override
            public @NotNull Phantom getPhantom() {
                return phantom;
            }

            @Override
            public @NotNull AnimalTamer getOwner() {
                return tamer;
            }

            @Override
            public boolean hasSaddle() {
                return phantom.getPersistentDataContainer().getOrDefault(saddleKey, PersistentDataType.BOOLEAN, false);
            }
        };
        phantom.setShouldBurnInDay(false);
        tameablePhantoms.put(phantom.getUniqueId(), tameablePhantom);
        var mobGoals = Bukkit.getMobGoals();
        mobGoals.removeGoal(phantom, VanillaGoal.PHANTOM_ATTACK_PLAYER);
        mobGoals.removeGoal(phantom, VanillaGoal.PHANTOM_ATTACK_STRATEGY);
        mobGoals.removeGoal(phantom, VanillaGoal.PHANTOM_SWEEP_ATTACK);
        mobGoals.removeGoal(phantom, VanillaGoal.PHANTOM_CIRCLE_AROUND_ANCHOR);
        mobGoals.getAllGoals(phantom).forEach(phantomGoal -> getLogger().info(phantomGoal.getKey().getNamespacedKey().toString()));
        return tameablePhantom;
    }

    private boolean consumeItem(@NotNull Material material, @NotNull EntityEquipment equipment){
        var mainHand = equipment.getItemInMainHand();
        if (mainHand.getType() != material){
            return false;
        }
        mainHand.setAmount(mainHand.getAmount()-1);
        return true;
    }

    @Nullable
    private Player hasPlayer(@NotNull Phantom phantom){
        var passengers = phantom.getPassengers();
        if (passengers.isEmpty()){
            return null;
        }
        var firstPassenger = passengers.getFirst();
        if (firstPassenger instanceof Player player){
            return player;
        }
        return null;
    }

    private boolean hasHologram(@NotNull Phantom phantom){
        var passengers = phantom.getPassengers();
        if (passengers.isEmpty()){
            return false;
        }
        return passengers.getFirst().getType() == EntityType.TEXT_DISPLAY;
    }

    private void updateHologram(@NotNull TameablePhantom tameablePhantom, boolean visible){
        var phantom = tameablePhantom.getPhantom();
        var hasHologram = hasHologram(phantom);
        if (hasHologram){
            if (visible){
                return;
            }
            phantom.getPassengers().getFirst().remove();
            return;
        }
        if (!visible){
            return;
        }
        var textDisplay = (TextDisplay) phantom.getWorld().spawnEntity(phantom.getLocation(), EntityType.TEXT_DISPLAY);
        textDisplay.setBackgroundColor(Color.fromARGB(0,0,0,0));
        textDisplay.setBillboard(Display.Billboard.VERTICAL);
        var phantomScale = Objects.requireNonNull(phantom.getAttribute(Attribute.SCALE), "phantoms don't have scale attribute").getValue();
        var textScale = (float) (2.5 * phantomScale);
        textDisplay.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(textScale), new AxisAngle4f()));
        var miniMessage = MiniMessage.miniMessage();
        var nameTagComponent = miniMessage.deserialize("<sprite:items:item/name_tag>");
        var saddleComponent = miniMessage.deserialize("<sprite:items:item/saddle>");
        var text = tameablePhantom.hasSaddle() ? nameTagComponent.append(saddleComponent) : nameTagComponent;
        textDisplay.text(text);
        textDisplay.setRotation(0, 0);
        phantom.addPassenger(textDisplay);
    }
}
