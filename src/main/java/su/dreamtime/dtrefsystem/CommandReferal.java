package su.dreamtime.dtrefsystem;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import su.dreamtime.dtrefsystem.data.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandReferal extends Command implements TabExecutor {

    public CommandReferal(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("dreamtimereferalsystem.reload")) {
                    DTRefSystem.getInstance().reloadConfig();
                }
            } else if (args[0].equalsIgnoreCase("create")) {
                Runnable runnable = () -> {
                    String sql = "SELECT * FROM `dt_ref_referrers` WHERE `name` = ?";

                    try (ResultSet rs = DTRefSystem.getInstance().getDb().query(sql, sender.getName())) {
                        if (rs.next()) {
                            String code = rs.getString("code");
                            TextComponent copyComponent = new TextComponent("У вас уже есть реферальный код! Нажмите на текст чтобы скопировать его.");
                            copyComponent.setBold(true);
                            copyComponent.setColor(ChatColor.RED);
                            copyComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, code));
                            copyComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "Нажмите чтобы скопировать.")));
                            sender.sendMessage(copyComponent);
                        } else {

                            if (!(sender instanceof ProxiedPlayer)) {
                                return;
                            }
                            ProxiedPlayer p = (ProxiedPlayer)sender;
                            Referrer ref = Referrer.createReferrer(sender.getName());
                            DTRefSystem.getInstance().getDb().execute("INSERT INTO `dt_ref_referrers` (`name`, `uuid`, `code`) VALUES (?, ?, ?)", ref.getName(), p.getUniqueId().toString(), ref.getCode());
                            TextComponent copyComponent = new TextComponent(ChatColor.GREEN + "Реферальный код успешно сгенерирован!\n");
                            copyComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "Нажмите чтобы скопировать.")));
                            copyComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ref.getCode()));

                            ComponentBuilder builder = new ComponentBuilder(copyComponent);
                            builder.append(ChatColor.GRAY + "Чтобы использовать этот код, ваш друг должен ввести команду\n" + ChatColor.WHITE + "/referal use " + ref.getCode() + "\n");

                            builder.append(ChatColor.GRAY + "Нажмите на текст чтобы скопировать код.\n");
                            sender.sendMessage(builder.create());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                };
                ProxyServer.getInstance().getScheduler().runAsync(DTRefSystem.getInstance(), runnable);
            } else if (args[0].equalsIgnoreCase("use")) {
                if (args.length >= 2) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer)sender;
                        Referal.use(p, args[1]);
                    }
                }
            } else if (args[0].equalsIgnoreCase("info")) {

            }
        }
        else {
            // TODO description
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completer = new ArrayList<>();
        if (args.length == 1) {
            completer.add("create");
            completer.add("use");
            if (sender.hasPermission("dreamtimereferalsystem.reload")) {
                completer.add("reload");
            }
            Set<String> match = new HashSet<>();
            String search = args[0].toLowerCase();
            for (String str : completer) {
                if (str.startsWith(search)) {
                    match.add(str);
                }
            }

            return match;
        }
        return completer;
    }
}
