import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInteractEntity;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiFreecam extends JavaPlugin {

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        
        PacketEvents.getAPI().getEventManager().registerListener(
            new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
                
                @Override
                public void onPacketReceive(PacketReceiveEvent event) {
                    Player player = (Player) event.getPlayer();
                    if (player == null || player.isOp()) return;

                    if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                        try {
                            WrapperPlayClientUseItem wrapper = new WrapperPlayClientUseItem(event);
                            com.github.retrooper.packetevents.util.Vector3i vec = wrapper.getBlockPosition();
                            
                            if (vec != null) {
                                Location blockLoc = new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ());
                                Location playerLoc = player.getEyeLocation();
                                
                                if (playerLoc.distance(blockLoc) > 6.0) {
                                    event.setCancelled(true);
                                    flagStaff(player, "Freecam (Block Interact)");
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    if (event.getPacketType() == PacketType.Play.Client.PLAYER_INTERACT_ENTITY) {
                        try {
                            WrapperPlayClientPlayerInteractEntity wrapper = new WrapperPlayClientPlayerInteractEntity(event);
                            org.bukkit.entity.Entity target = Bukkit.getEntity(wrapper.getTargetId());
                            
                            if (target != null) {
                                double distance = player.getLocation().distance(target.getLocation());
                                
                                if (distance > 6.0) {
                                    event.setCancelled(true);
                                    flagStaff(player, "Freecam (Entity Interact)");
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        );
    }

    private void flagStaff(Player hacker, String checkName) {
        String alertMessage = ChatColor.RED + "[AntiCheat] " 
                + ChatColor.YELLOW + hacker.getName() 
                + ChatColor.GRAY + " failed " 
                + ChatColor.AQUA + checkName 
                + ChatColor.GRAY + "!";
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.isOp()) {
                onlinePlayer.sendMessage(alertMessage);
            }
        }
        Bukkit.getLogger().warning("[AntiCheat] " + hacker.getName() + " flagged " + checkName);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
