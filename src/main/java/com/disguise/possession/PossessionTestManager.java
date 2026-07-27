package com.disguise.possession;

import com.disguise.packet.PacketUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Phase 0 experiment: use Paper player input to drive a real sheep entity.
 */
public class PossessionTestManager implements Listener {

    public enum CameraMode {
        SPECTATOR,
        FOLLOW_PLAYER
    }

    private static final double WALK_SPEED = 0.22;
    private static final double SPRINT_SPEED = 0.34;
    private static final double STRAFE_SPEED = 0.18;
    private static final double JUMP_SPEED = 0.42;

    private final JavaPlugin plugin;
    private final Map<UUID, TestSession> sessions = new HashMap<>();

    public PossessionTestManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(Player player, CameraMode cameraMode) {
        stop(player, false);

        Sheep sheep = player.getWorld().spawn(player.getLocation(), Sheep.class);
        sheep.setAI(false);
        sheep.setSilent(false);
        sheep.setPersistent(true);
        sheep.setRemoveWhenFarAway(false);
        sheep.setInvulnerable(false);
        sheep.setMaxHealth(20.0);
        sheep.setHealth(20.0);

        // PDC 标记：写入主人 UUID，标识为玩家变身的羊
        PacketUtils.saveData(sheep, player);

        TestSession session = new TestSession(player, sheep, cameraMode);
        session.originalGameMode = player.getGameMode();
        session.originalLocation = player.getLocation().clone();
        session.originalCollidable = player.isCollidable();
        session.originalAllowFlight = player.getAllowFlight();
        session.originalFlying = player.isFlying();
        session.originalInvisible = player.isInvisible();

        player.setCollidable(false);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.hidePlayer(plugin, player);
            }
        }

        if (cameraMode == CameraMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(sheep);
        } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setInvisible(true);
        }

        session.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> tick(session), 0L, 1L);
        sessions.put(player.getUniqueId(), session);

        player.sendMessage("§aPhase 0 测试已开始：§e/bs test " + modeName(cameraMode));
        player.sendMessage("§7WASD 控制羊，Space 跳跃，Sprint 加速，/bs test stop 结束。");
    }

    public void stop(Player player, boolean notify) {
        TestSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            if (notify) {
                player.sendMessage("§e当前没有正在运行的 Phase 0 测试。");
            }
            return;
        }

        if (session.task != null) {
            session.task.cancel();
        }

        if (player.isOnline()) {
            player.setSpectatorTarget(null);
            player.setGameMode(session.originalGameMode);
            player.setCollidable(session.originalCollidable);
            player.setAllowFlight(session.originalAllowFlight);
            player.setFlying(session.originalFlying);
            player.setInvisible(session.originalInvisible);

            if (session.cameraMode == CameraMode.FOLLOW_PLAYER && session.sheep.isValid()) {
                player.teleport(session.sheep.getLocation());
            } else {
                player.teleport(session.originalLocation);
            }

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player)) {
                    other.showPlayer(plugin, player);
                }
            }

            if (notify) {
                player.sendMessage("§aPhase 0 测试已结束。");
            }
        }

        if (!session.sheep.isDead()) {
            session.sheep.remove();
        }
    }

    public void cleanup() {
        for (UUID uuid : sessions.keySet().toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                stop(player, false);
            }
        }
        sessions.clear();
    }

    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        TestSession session = sessions.get(event.getPlayer().getUniqueId());
        if (session == null) {
            return;
        }

        String state = "W=" + event.getInput().isForward()
                + " A=" + event.getInput().isLeft()
                + " S=" + event.getInput().isBackward()
                + " D=" + event.getInput().isRight()
                + " Jump=" + event.getInput().isJump()
                + " Sneak=" + event.getInput().isSneak()
                + " Sprint=" + event.getInput().isSprint();

        if (!state.equals(session.lastInputState)) {
            session.lastInputState = state;
            plugin.getLogger().info("[Phase0Input] " + event.getPlayer().getName() + " " + state);
        }
        session.currentInput = event.getInput();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stop(event.getPlayer(), false);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        for (TestSession session : sessions.values().toArray(TestSession[]::new)) {
            if (session.sheep.equals(entity)) {
                session.player.sendMessage("§c测试羊死亡，Phase 0 测试结束。");
                stop(session.player, false);
                return;
            }
        }
    }

    private void tick(TestSession session) {
        Player player = session.player;
        Sheep sheep = session.sheep;

        if (!player.isOnline() || sheep.isDead() || !sheep.isValid()) {
            stop(player, false);
            return;
        }

        Input input = session.currentInput;
        boolean forward = input != null && input.isForward();
        boolean backward = input != null && input.isBackward();
        boolean left = input != null && input.isLeft();
        boolean right = input != null && input.isRight();
        boolean sprint = input != null && input.isSprint();
        boolean jump = input != null && input.isJump();
        float yaw = player.getLocation().getYaw();
        Vector movement = calculateMovement(yaw, forward, backward, left, right, sprint);

        Vector velocity = sheep.getVelocity();
        velocity.setX(movement.getX());
        velocity.setZ(movement.getZ());

        if (jump && sheep.isOnGround()) {
            velocity.setY(JUMP_SPEED);
            sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_SHEEP_STEP, 0.6f, 1.2f);
        }

        sheep.setVelocity(velocity);
        sheep.setRotation(yaw, 0.0f);

        if (session.cameraMode == CameraMode.SPECTATOR) {
            if (!sheep.equals(player.getSpectatorTarget())) {
                player.setSpectatorTarget(sheep);
            }
        } else {
            Location camera = sheep.getLocation().clone().add(0.0, 1.35, 0.0);
            camera.setYaw(player.getLocation().getYaw());
            camera.setPitch(player.getLocation().getPitch());
            player.teleport(camera);
        }

        player.sendActionBar(Component.text("Phase0 " + modeName(session.cameraMode) + " | " + session.lastInputState));
    }

    private Vector calculateMovement(float yaw, boolean forward, boolean backward, boolean left, boolean right,
                                     boolean sprint) {
        double radians = Math.toRadians(yaw);
        Vector forwardVector = new Vector(-Math.sin(radians), 0.0, Math.cos(radians));
        Vector rightVector = new Vector(Math.cos(radians), 0.0, Math.sin(radians));

        Vector movement = new Vector();
        double forwardSpeed = sprint ? SPRINT_SPEED : WALK_SPEED;

        if (forward) {
            movement.add(forwardVector.clone().multiply(forwardSpeed));
        }
        if (backward) {
            movement.subtract(forwardVector.clone().multiply(WALK_SPEED * 0.65));
        }
        if (left) {
            movement.subtract(rightVector.clone().multiply(STRAFE_SPEED));
        }
        if (right) {
            movement.add(rightVector.clone().multiply(STRAFE_SPEED));
        }

        if (movement.lengthSquared() > forwardSpeed * forwardSpeed) {
            movement.normalize().multiply(forwardSpeed);
        }

        return movement;
    }

    private String modeName(CameraMode cameraMode) {
        return cameraMode == CameraMode.SPECTATOR ? "spectator" : "follow";
    }

    private static class TestSession {
        private final Player player;
        private final Sheep sheep;
        private final CameraMode cameraMode;
        private GameMode originalGameMode;
        private Location originalLocation;
        private boolean originalCollidable;
        private boolean originalAllowFlight;
        private boolean originalFlying;
        private boolean originalInvisible;
        private BukkitTask task;
        private Input currentInput;
        private String lastInputState = "W=false A=false S=false D=false Jump=false Sneak=false Sprint=false";

        private TestSession(Player player, Sheep sheep, CameraMode cameraMode) {
            this.player = player;
            this.sheep = sheep;
            this.cameraMode = cameraMode;
        }
    }
}
