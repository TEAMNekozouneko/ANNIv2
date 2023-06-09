package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.spectator.SpectatorManager;
import net.nekozouneko.anniv2.message.MessageManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerDamageListener implements Listener {

    private static final Map<UUID, Long> fighting = new HashMap<>();
    private static final List<Material> AXES = Arrays.asList(
            Material.STONE_AXE, Material.DIAMOND_AXE,
            Material.WOODEN_AXE, Material.STONE_AXE,
            Material.NETHERITE_AXE, Material.IRON_AXE
    );

    public static boolean isFighting(Player player) {
        new HashSet<>(fighting.keySet()).forEach(key -> {
            if (fighting.getOrDefault(key, 0L) <= System.currentTimeMillis())
                fighting.remove(key);
        });

        return !(fighting.getOrDefault(player.getUniqueId(), 0L) <= System.currentTimeMillis());
    }

    public static void setNotFighting(Player player) {
        fighting.remove(player.getUniqueId());
    }

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = ((Player) e.getEntity());

            if (SpectatorManager.isSpectating(p)) {
                e.setCancelled(true);
                return;
            }

            if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                p.sendMessage(mm.build("notify.removed_invisibility"));
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            ItemStack main = damager.getInventory().getItemInMainHand();
            // 斧で攻撃した場合
            if (AXES.contains(main.getType())) {
                // 4ダメージより上なら4ダメージを減らして0.4 * ダメージ増加 (ない場合0)のレベル減らしてそうではないならそのまま通す
                e.setDamage(e.getDamage() > 4 ? e.getDamage() - 4 - (0.4 * main.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) : e.getDamage());
            }
        }

        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            fighting.put(e.getEntity().getUniqueId(), System.currentTimeMillis() + 10000);
        }
    }

}
