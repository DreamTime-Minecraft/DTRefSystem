package su.dreamtime.dtrefsystem;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import su.dreamtime.dtrefsystem.utils.TimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// Тот, кто переходит по ссылке
public class Referal {
    private String name;
    private long referrerId;
    private String ip;

    private long playtimeSeconds;

    private long timeJoinMills;

    private String perm;
    private int customCoins;
    public Referal(String name, long referrer_id, String ip, long playtimeSeconds, long currentTimeMillis) {
        this.name = name;
        this.referrerId = referrer_id;
        this.ip = ip;
        this.playtimeSeconds = playtimeSeconds;
        this.timeJoinMills = currentTimeMillis;

    }

    public static void use(final ProxiedPlayer sender, final String code) {
        Runnable runnable = () -> {
            Referal ref = DTRefSystem.updateReferalTime(sender.getName());
            if (ref == null) {
                sender.sendMessage(ChatColor.RED +"Упс.. Ваши данные не загружены! Перезайдите на сервер.");
                return;
            }
            if (ref.getReferrerId() != 0) {
                sender.sendMessage(ChatColor.RED +"Вы уже вводили код! Код можно ввести лишь один раз за всё время");
                return;
            }
            String sql = "SELECT * FROM `dt_ref_referals` where name = ? and `referrer_id` != 0";
            String checkIpSql = "SELECT * FROM `dt_ref_referals` WHERE `ip` = ? and `referrer_id` != 0";
            String getReferrerSql = "SELECT * FROM `dt_ref_referrers` WHERE `code` = ?";
            try (ResultSet rs = DTRefSystem.getInstance().getDb().query(sql, sender.getName());
                 ResultSet rsIp = DTRefSystem.getInstance().getDb().query(checkIpSql, sender.getSocketAddress().toString().split("\\/")[0]);
                 ResultSet rsReferrer = DTRefSystem.getInstance().getDb().query(getReferrerSql, code)
            ) {
                if (!rsReferrer.next()) {
                    sender.sendMessage(ChatColor.RED +"Такого кода не существует!");
                    return;
                } else if (rsIp.next()) { // Если айпи есть, то код не даём
                    sender.sendMessage(ChatColor.RED +"Вы уже вводили код! Код можно ввести лишь один раз за всё время");
                    return;
                } else if (rs.next()) { // Если есть запись, значит уже вводили код и получали награду
                    sender.sendMessage(ChatColor.RED +"Вы уже вводили код! Код можно ввести лишь один раз за всё время");
                    return;
                } else { // Иначе, проверяем на наигранное время
                    if (ref == null) {
                        return;
                    } else if (ref.getName().equalsIgnoreCase(rsReferrer.getString("name"))) {
                        sender.sendMessage(ChatColor.RED +"Вы не можете использовать свой же код!");
                        return;
                    }

                    TimeUtils.TimeUnit timeUnit = TimeUtils.TimeUnit.valueOf(DTRefSystem.getInstance().getConfig().getString("settings.time-format"));
                    long needSeconds = timeUnit.toSeconds(DTRefSystem.getInstance().getConfig().getLong("settings.need-playtime"));
                    if (ref.getPlaytimeSeconds() < needSeconds) {
                        sender.sendMessage(ChatColor.RED + "Перед тем, как вводить код приглашения, вы должны наиграть " + TimeUtils.getTimeString(needSeconds*1000));
                        sender.sendMessage(ChatColor.RED + "Осталось: " + TimeUtils.getTimeString((needSeconds - ref.getPlaytimeSeconds())*1000));
                        return;
                    }
                    int referalCoins = DTRefSystem.getCoinsRewardReferal(rsReferrer.getString("name"));
                    int referrerCoins = DTRefSystem.getCoinsRewardReferrer(rsReferrer.getString("name"));
                    sender.sendMessage(ChatColor.GREEN + "Отлично! Вы получили " +ChatColor.YELLOW + referalCoins + ChatColor.GREEN + " таймкоинов за приглашение!");
                    ProxiedPlayer referrerPlayer = ProxyServer.getInstance().getPlayer(rsReferrer.getString("name"));
                    DTRefSystem.getInstance().getDb().execute("UPDATE `dtcoins` SET `coins` = `coins`+ ? WHERE `uuid` = ?;", referalCoins, sender.getUniqueId().toString());
                    DTRefSystem.getInstance().getDb().execute("UPDATE `dtcoins` SET `coins` = `coins`+ ? WHERE `uuid` = ?;", referrerCoins, rsReferrer.getString("uuid"));
                    ref.setReferrerId(rsReferrer.getLong("id"));
                    sql = "INSERT INTO `dt_ref_referals` (`name`, `referrer_id`, `ip`, `playtimeSeconds`) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `playtimeSeconds` = ?, `referrer_id` = ?";
                    DTRefSystem.getInstance().getDb().execute(sql, ref.getName(), ref.getReferrerId(), ref.getIp(), ref.getPlaytimeSeconds(), ref.getPlaytimeSeconds(), ref.getReferrerId());
                    DTRefSystem.addReferals(ref);

                    DTRefSystem.updateCoins(sender, UUID.fromString(rsReferrer.getString("uuid")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        ProxyServer.getInstance().getScheduler().runAsync(DTRefSystem.getInstance(), runnable);
    }

    private void giveReward(ProxiedPlayer player) {

    }

    public String getName() {
        return name;
    }

    public long getReferrerId() {
        return referrerId;
    }

    public String getIp() {
        return ip;
    }

    public long getPlaytimeSeconds() {
        return playtimeSeconds;
    }

    public long getTimeJoinMills() {
        return timeJoinMills;
    }

    public void setReferrerId(long referrerId) {
        this.referrerId = referrerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Referal referal = (Referal) o;
        return name.equalsIgnoreCase(referal.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    public void setPlaytimeSeconds(long playtimeSeconds) {
        this.playtimeSeconds = playtimeSeconds;
    }

    public void setTimeJoinMills(long timeJoinMills) {
        this.timeJoinMills = timeJoinMills;
    }

    @Override
    public String toString() {
        return "Referal{" +
                "name='" + name + '\'' +
                ", referrerId=" + referrerId +
                ", ip='" + ip + '\'' +
                ", playtimeSeconds=" + playtimeSeconds +
                ", timeJoinMills=" + timeJoinMills +
                '}';
    }

    public String getPerm() {
        return perm;
    }

    public void setPerm(String perm) {
        this.perm = perm;
    }

    public int getCustomCoins() {
        return customCoins;
    }

    public void setCustomCoins(int customCoins) {
        this.customCoins = customCoins;
    }
}

