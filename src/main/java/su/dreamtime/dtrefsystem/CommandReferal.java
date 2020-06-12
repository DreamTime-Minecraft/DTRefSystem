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
        boolean help = false;
        boolean help2 = false;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("dreamtimereferalsystem.reload")) {
                    DTRefSystem.getInstance().reloadConfig();
                    sender.sendMessage(new TextComponent("§aКонфиг успешно перезагружен"));
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
                TextComponent info = new TextComponent("§7Информация о рефералах:");
                //ToDo кто пригласил кто не приглашал кто лох кто не лох
                TextComponent infot1 = new TextComponent("§7Вас пригласил: {name}");
                TextComponent infot2 = new TextComponent("§7Ваше приглашение:");
                TextComponent infot3 = new TextComponent("  §e→ §7Ваш код: {code}");
                infot3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7Нажми здесь, чтобы скопировать свой реферальный код")}));
                infot3.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "{code}"));
                TextComponent infot4 = new TextComponent("  §e→ §7Приглашено: {count} человек");
                TextComponent infot5 = new TextComponent("  §e→ §7За приглашение:");
                TextComponent infot6 = new TextComponent("    §6→ §7Вы получаете: {ref-tk} тк");
                TextComponent infot7 = new TextComponent("    §6→ §7Приглашённый: {rer-tk} тк");

                sender.sendMessage(info);
                sender.sendMessage(infot1);
                sender.sendMessage(infot2);
                sender.sendMessage(infot3);
                sender.sendMessage(infot4);
                sender.sendMessage(infot5);
                sender.sendMessage(infot6);
                sender.sendMessage(infot7);
            } else if (args[0].equalsIgnoreCase("help")) {
                help2 = true;
            } else if (args[0].equalsIgnoreCase("guide")) {
                help = true;
            }
        }
        else {
            help2 = true;
        }

        if(help) {
            TextComponent helpref = new TextComponent("§7Напишите §e/ref help§7 для просмотра списка команд.");
            helpref.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref help"));
            TextComponent[] helpop = new TextComponent[]{new TextComponent("§7Описание команды §e/ref§a:"),
                    new TextComponent("§eRefSystem §7- реферальная система сервера §5Dream§9Time§7,"),
                    new TextComponent("§7благодаря которой можно заработать таймкоины за приглашения!"),
                    new TextComponent("§7Всё очень §aпросто§7: " +
                            "\n§71) Вы создаёте код приглашения §b(/ref create)" +
                            "\n§72) Вы приглашаете игроков на наш сервер;" +
                            "\n§73) Они наигрывают на сервере сутки;" +
                            "\n§74) Вводят Ваш код §b(/ref use <код>)" +
                            "\n§75) Вы и игроки получаете награду!" +
                            "\n§7"),
                    helpref};

            for(TextComponent t : helpop) {
                sender.sendMessage(t);
            }
        }

        if(help2) {
            TextComponent helpt1 = new TextComponent("§7Помощь по команде §e/ref§7:");
            TextComponent helpt2 = new TextComponent("§a>§7 /ref guide - описание реферальной системы.");
            helpt2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref guide"));
            TextComponent helpt3 = new TextComponent("§a>§7 /ref create - создать реферальный код.");
            helpt3.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref create"));
            TextComponent helpt4 = new TextComponent("§c   ВНИМАНИЕ!§7 У Вас присутствует уникальная возможность");
            TextComponent helpt5 = new TextComponent("§7создавать свой код! Для этого используйте §e/ref create [код]§7!");
            TextComponent helpt6 = new TextComponent("§a> §7/ref use <код> - использовать реферальный код.");
            helpt6.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref use "));
            TextComponent helpt7 = new TextComponent("§a> §7/ref info - узнать информацию о своих рефералах.");
            helpt7.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref info"));
            TextComponent helpt8 = new TextComponent("§a> §7/ref reload - перезагрузить конфиг плагина.");
            helpt8.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref reload"));

            sender.sendMessage(helpt1);
            sender.sendMessage(helpt2);
            sender.sendMessage(helpt3);
            if(sender.hasPermission("dreamtimereferalsystem.create.custom")) {
                sender.sendMessage(helpt4);
                sender.sendMessage(helpt5);
            }
            sender.sendMessage(helpt6);
            sender.sendMessage(helpt7);
            if(sender.hasPermission("dreamtimereferalsystem.reload")) {
                sender.sendMessage(helpt8);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completer = new ArrayList<>();
        if (args.length == 1) {
            completer.add("guide");
            completer.add("help");
            completer.add("create");
            completer.add("use");
            completer.add("info");
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
