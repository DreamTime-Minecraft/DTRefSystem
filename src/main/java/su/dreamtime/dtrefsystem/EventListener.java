package su.dreamtime.dtrefsystem;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventListener implements Listener {
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        final ProxiedPlayer p = event.getPlayer();
        join(p);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        quit(event.getPlayer());
    }

    public static void join(ProxiedPlayer p) {

        final String ip = p.getSocketAddress().toString().split("\\/")[0];
        final String name = p.getName();

        Runnable runnable = () -> {
            Referal ref = null;
            String sql = "SELECT * FROM `dt_ref_referals` WHERE `name` = ?";
            try (ResultSet rs = DTRefSystem.getInstance().getDb().query(sql, name);) {
                if (rs.next()){
                    ref = new Referal(name, rs.getLong("referrer_id"), rs.getString("ip"), rs.getLong("playtimeSeconds"), System.currentTimeMillis());

                } else {
                    ref = new Referal(name, 0, ip, 0, System.currentTimeMillis());
                }
                System.out.println(ref.toString());
                DTRefSystem.addReferals(ref);
                p.hasPermission("");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        ProxyServer.getInstance().getScheduler().runAsync(DTRefSystem.getInstance(), runnable);
    }

    public static void quit(ProxiedPlayer player) {
        Runnable runnable = () -> {
            Referal ref = DTRefSystem.updateReferalTime(player.getName());
            if (ref == null) {
                System.out.println("PLAYER DISCONNECT: " + ChatColor.RED + "Referal is null");

                return;
            }
            String sql = "INSERT INTO `dt_ref_referals` (`name`, `referrer_id`, `ip`, `playtimeSeconds`) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `playtimeSeconds` = ?, `referrer_id` = ?";
            DTRefSystem.getInstance().getDb().execute(sql, ref.getName(), ref.getReferrerId(), ref.getIp(), ref.getPlaytimeSeconds(), ref.getPlaytimeSeconds(), ref.getReferrerId());
            DTRefSystem.getReferals().remove(ref);
        };
        ProxyServer.getInstance().getScheduler().runAsync(DTRefSystem.getInstance(), runnable);
    }
}
