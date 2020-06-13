package su.dreamtime.dtrefsystem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import su.dreamtime.dtrefsystem.data.Database;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class DTRefSystem extends Plugin  {
    private static Set<String> ignoreList = new HashSet<>();
    private static List<Referal> referals = new ArrayList<>();// Список игроков, для подсчёта времени игры
    private static DTRefSystem instance;
    private Database db;
    private Configuration config;
    public void initDB() {
        getLogger().info("initializing database");

        if (db != null) {
            try {
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String login = getConfig().getString("database.login");
        String password = getConfig().getString("database.password");
        String database = getConfig().getString("database.database");
        db = new Database(host, port, login, password, database);
        db.execute("CREATE TABLE IF NOT EXISTS `dt_ref_referrers` (\n" +
                "\t`id` SERIAL NOT NULL PRIMARY KEY,\n" +
                "\t`name` VARCHAR(255),\n" +
                "\t`code` VARCHAR(255),\n" +
                "\t`count_referals` INT(11),\n" +
                "\t`uuid` VARCHAR(255),\n" +
                "\tINDEX (`code`)\n" +
                ")ENGINE=INNODB  DEFAULT CHARSET=UTF8  COLLATE UTF8_GENERAL_CI");
        db.execute("CREATE TABLE IF NOT EXISTS `dt_ref_referals` (\n" +
                "\t`name` VARCHAR(255) PRIMARY KEY,\n" +
                "\t`referrer_id` BIGINT(20) UNSIGNED NOT NULL,\n" +
                "\t`ip` VARCHAR(32),\n" +
                "\t`playtimeSeconds` BIGINT(11)\n" +
                ")ENGINE=INNODB  DEFAULT CHARSET=UTF8  COLLATE UTF8_GENERAL_CI");
        getLogger().info("db was initialized");
    }

    @Override
    public void onEnable() {
        instance = this;

        loadConfig();
        initDB();

        getProxy().registerChannel("BungeeCord");
        getProxy().getPluginManager().registerListener(this, new EventListener());
        getProxy().getPluginManager().registerCommand(this, new CommandReferal("dreamtimereferalsystem", "", "referal", "ref"));
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            EventListener.join(player);
        }
    }

    @Override
    public void onDisable() {
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            EventListener.quit(player);
        }
        closeDB();
    }
    private static void closeDB() {

        try {
            if (getInstance().db != null) {
                getInstance().db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DTRefSystem getInstance() {
        return instance;
    }

    public Database getDb() {
        return db;

    }

    public Configuration getConfig() {
        return config;
    }

    public void loadDefaultConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");


        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void loadConfig() {
        try {
            loadDefaultConfig();
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        loadConfig();
        saveConfig();
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {

        for (ProxiedPlayer player : getInstance().getProxy().getPlayers()) {
            EventListener.quit(player);
        }

        getInstance().reloadConfig();
        closeDB();
        getInstance().initDB();

        for (ProxiedPlayer player : getInstance().getProxy().getPlayers()) {
            EventListener.join(player);
        }
    }

    public static List<Referal> getReferals() {
        return referals;
    }

    public static Referal updateReferalTime(String name) {
        for (Referal ref : referals) {
            if (ref.getName().equalsIgnoreCase(name)) {
                if (ref.getPlaytimeSeconds() < 1000_000_000) {
                    long now = System.currentTimeMillis();
                    long oldSec = ref.getPlaytimeSeconds();
                    long joinTime = ref.getTimeJoinMills();
                    long difference = (now - joinTime) / 1000;
                    if (difference >= 1) {
                        ref.setPlaytimeSeconds(oldSec + difference);
                        ref.setTimeJoinMills(now);
                    }
                }
                return ref;
            }
        }
        return null;
    }

    public static void addReferals(Referal referal) {
        referals.remove(referal);
        referals.add(referal);
    }


    public static Referal getReferal(String name) {
        for (Referal ref : referals) {
            if (ref.getName().equalsIgnoreCase(name)) {
                return ref;
            }
        }
        return null;
    }

    public static void updateCoins(ProxiedPlayer player, UUID target) {

        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();

        if (networkPlayers == null || networkPlayers.isEmpty()) return;
        {
            UUID UniqueId = player.getUniqueId();
            String uuid = player.getUniqueId().toString();
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
//            out.writeUTF("Forward"); // So BungeeCord knows to forward it
//            out.writeUTF("ALL");
            out.writeUTF("dtcoins");

            try (ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                 DataOutputStream msgout = new DataOutputStream(msgbytes);) {
                msgout.writeUTF("update");
                msgout.writeLong(UniqueId.getLeastSignificantBits());
                msgout.writeLong(UniqueId.getMostSignificantBits());

                out.writeShort(msgbytes.toByteArray().length);
                out.write(msgbytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.getServer().getInfo().sendData("BungeeCord", out.toByteArray());
        }
        {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
//        out.writeUTF("Forward"); // So BungeeCord knows to forward it
//        out.writeUTF("ALL");
            out.writeUTF("dtcoins");

            try (ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                 DataOutputStream msgout = new DataOutputStream(msgbytes);) {
                msgout.writeUTF("update");
                msgout.writeLong(target.getLeastSignificantBits());
                msgout.writeLong(target.getMostSignificantBits());

                out.writeShort(msgbytes.toByteArray().length);
                out.write(msgbytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.getServer().getInfo().sendData("BungeeCord", out.toByteArray());
        }
    }

    public static boolean hasPermission(String playerName, String permission) {
        LuckPerms api = LuckPermsProvider.get();
        UserManager um = api.getUserManager();
        CompletableFuture<UUID> uuidFuture = um.lookupUniqueId(playerName);
        try {
            UUID uuid = uuidFuture.get();
            if (uuid != null) {
                CompletableFuture<User> userFuture = um.loadUser(uuid);
                User user = userFuture.get();
                if (user != null) {
                    Node node = Node.builder(permission).value(true).build();
                    SortedSet<Node> nodes = user.resolveDistinctInheritedNodes(QueryOptions.builder(QueryMode.NON_CONTEXTUAL).flag(Flag.RESOLVE_INHERITANCE, true).build());

                    ImmutableContextSet context = api.getContextManager().getContextSetFactory().immutableEmpty();
                    Optional<Node> match = user.resolveInheritedNodes(QueryOptions.nonContextual()).stream()
                            .filter(n -> n.getKey().equalsIgnoreCase(permission) && n.getContexts().equals(context))
                            .findFirst();
                    Tristate t = match.map(n -> Tristate.of(n.getValue())).orElse(Tristate.FALSE);
                    return t.asBoolean();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getCoinsRewardReferrer(String playerName) {
        String perm = "default";
        Configuration sect = getInstance().getConfig().getSection("rewards.referrer");
        for (String key : sect.getKeys()) {
            if (hasPermission(playerName, "refsystem.rewards.referrer." + key)) {
                perm = key;
                break;
            }
        }
        return getInstance().getConfig().getInt("rewards.referrer." + perm + ".coins");
    }
    public static int getCoinsRewardReferal(String playerName) {
        String perm = "default";
        Configuration sect = getInstance().getConfig().getSection("rewards.referal");
        for (String key : sect.getKeys()) {
            if (hasPermission(playerName, "refsystem.rewards.referrer." + key)) { // referrer это так надо. Не менять
                perm = key;
                break;
            }
        }

        return getInstance().getConfig().getInt("rewards.referal." + perm + ".coins");
    }

    public static Set<String> getIgnoreList() {
        return ignoreList;
    }
}
