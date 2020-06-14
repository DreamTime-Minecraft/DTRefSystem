package su.dreamtime.dtrefsystem;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import su.dreamtime.dtrefsystem.utils.TimeUtils;

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
            if (args[0].equalsIgnoreCase("ignore")) {
                if (DTRefSystem.getIgnoreList().remove(sender.getName())) {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Уведомления о приглашениях теперь включены!"));
                } else {
                    DTRefSystem.getIgnoreList().add(sender.getName());
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Уведомления о приглашениях теперь отключены!"));
                }
            }
            else if (args[0].equalsIgnoreCase("reset") && sender.hasPermission("dreamtimereferalsystem.tester")){

                if (args.length >= 2) {
                    String userName = args[1];
                    DTRefSystem.getInstance().getDb().execute("UPDATE `dt_ref_referals` SET `referrer_id` = 0 WHERE `name` = ?", userName);
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(userName);
                    if (p != null) {
                        EventListener.join(p);
                    }
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Данные игрока " + userName + " были обновлены!"));
                    return;
                }
                else if (sender instanceof ProxiedPlayer) {
                    Referal r = DTRefSystem.getReferal(sender.getName());
                    if (r != null) {
                        r.setReferrerId(0);

                        EventListener.quit((ProxiedPlayer)sender);
                        EventListener.join((ProxiedPlayer)sender);
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Ваши данные были обновлены!"));
                    }
                    else {
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Ваши данные не загружены. Перезайдите на сервер!"));
                    }
                }
                else {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Вы не игрок!"));
                }
            }
            else if (args[0].equalsIgnoreCase("skiptime") && sender.hasPermission("dreamtimereferalsystem.tester")){
                if (args.length >= 2) {

                    long seconds = TimeUtils.TimeUnit.valueOf(DTRefSystem.getInstance().getConfig().getString("settings.time-format")).toSeconds(
                            DTRefSystem.getInstance().getConfig().getLong("settings.need-playtime")
                    );

                    String userName = args[1];
                    DTRefSystem.getInstance().getDb().execute("UPDATE `dt_ref_referals` SET `playtimeSeconds` = ? WHERE `name` = ?", seconds, userName);
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(userName);
                    if (p != null) {
                        EventListener.join(p);
                    }
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Данные игрока " + userName + " были обновлены!"));
                    return;
                } else if (sender instanceof ProxiedPlayer) {
                    Referal r = DTRefSystem.getReferal(sender.getName());
                    if (r != null) {
                        long seconds = TimeUtils.TimeUnit.valueOf(DTRefSystem.getInstance().getConfig().getString("settings.time-format")).toSeconds(
                                DTRefSystem.getInstance().getConfig().getLong("settings.need-playtime")
                        );

                        r.setPlaytimeSeconds(seconds);
                        r.setTimeJoinMills(System.currentTimeMillis());

                        EventListener.quit((ProxiedPlayer)sender);
                        EventListener.join((ProxiedPlayer)sender);
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Ваши данные были обновлены!"));
                    }
                    else {
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Ваши данные не загружены. Перезайдите на сервер!"));
                    }
                }
                else {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Вы не игрок!"));
                }
            }
            else if (args[0].equalsIgnoreCase("resettime") && sender.hasPermission("dreamtimereferalsystem.tester")){
                if (args.length >= 2) {

                    String userName = args[1];
                    DTRefSystem.getInstance().getDb().execute("UPDATE `dt_ref_referals` SET `playtimeSeconds` = 0 WHERE `name` = ?", userName);
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(userName);
                    if (p != null) {
                        EventListener.join(p);
                    }
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Данные игрока " + userName + " были обновлены!"));
                    return;
                } else if (sender instanceof ProxiedPlayer) {
                    Referal r = DTRefSystem.getReferal(sender.getName());
                    if (r != null) {
                        r.setTimeJoinMills(System.currentTimeMillis());
                        r.setPlaytimeSeconds(0);

                        EventListener.quit((ProxiedPlayer)sender);
                        EventListener.join((ProxiedPlayer)sender);
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Ваши данные были обновлены!"));
                    }
                    else {
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Ваши данные не загружены. Перезайдите на сервер!"));
                    }
                }
                else {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Вы не игрок!"));
                }
            }
            else if (args[0].equalsIgnoreCase("update") && sender.hasPermission("dreamtimereferalsystem.tester")){
                if (args.length >= 2) {

                    String userName = args[1];
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(userName);
                    if (p != null) {
                        EventListener.join(p);
                        sender.sendMessage(new ComponentBuilder("Данные игрока " + userName + " были обновлены!").color(ChatColor.GREEN).create());
                    } else {
                        sender.sendMessage(new ComponentBuilder("Игрок " + userName + " оффлайн!").color(ChatColor.RED).create());
                    }
                    return;
                } else
                if (sender instanceof ProxiedPlayer) {
                    EventListener.join((ProxiedPlayer) sender);
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Ваши данные были обновлены!"));

                }
                else {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Вы не игрок!"));
                }
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("dreamtimereferalsystem.reload")) {
                    DTRefSystem.reload();
                    sender.sendMessage(TextComponent.fromLegacyText("§aПлагин успешно перезагружен"));
                }
            } else if (args[0].equalsIgnoreCase("create")) {
                Runnable runnable = () -> {
                    String sql = "SELECT * FROM `dt_ref_referrers` WHERE `name` = ?";

                    try (ResultSet rs = DTRefSystem.getInstance().getDb().query(sql, sender.getName())) {
                        if (rs.next()) {
                            String code = rs.getString("code");
                            TextComponent copyComponent = new TextComponent("У вас уже есть код приглашения! Нажмите на текст чтобы скопировать его.");
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
                            TextComponent copyComponent = new TextComponent(TextComponent.fromLegacyText(ChatColor.GREEN + "Код приглашения успешно сгенерирован!\n"));
                            copyComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "Нажмите чтобы скопировать.")));
                            copyComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/referal use " + ref.getCode()));

                            ComponentBuilder builder = new ComponentBuilder(copyComponent);
                            builder.append(TextComponent.fromLegacyText(ChatColor.GRAY + "Чтобы использовать этот код, ваш друг должен ввести команду\n" + ChatColor.YELLOW + "/referal use " + ref.getCode() + " §b(нажмите чтобы скопировать)\n"));
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

                String name = sender.getName();
                if (args.length >= 2 && sender.hasPermission("refsystem.info.other")) {
                    name = args[1];
                }
                Referrer referrer = null;
                String inviter = null;
                int referrerCoins= DTRefSystem.getCoinsRewardReferrer(name);
                int referalCoins= DTRefSystem.getCoinsRewardReferal(name);
                try (ResultSet rs1 = DTRefSystem.getInstance().getDb().query("SELECT r1.name AS `name`, r1.code AS `code`, r1.count_referals AS `count_referals` FROM dt_ref_referrers r1 WHERE r1.`name` = ?;", name);
                     ResultSet rs2 = DTRefSystem.getInstance().getDb().query(" SELECT `name` FROM dt_ref_referrers r2 WHERE (SELECT `referrer_id` FROM dt_ref_referals WHERE `name` = ?) = r2.id;",name)) {
                    if (rs1.next()) {
                        referrer = new Referrer(rs1.getString("name"), rs1.getString("code"), rs1.getInt("count_referals"));
                    }
                    if (rs2.next()){
                        inviter = rs2.getString("name");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (inviter == null) {
                    inviter = "§cВас никто не приглашал!\n§7Вы можете ввести код приглашения\nи получить за это плюшки :O §e/ref use <код>";
                }
                List<BaseComponent> output = new ArrayList<>();

                output.add(new TextComponent(TextComponent.fromLegacyText("§7Информация о приглашениях:")));
                output.add(new TextComponent(TextComponent.fromLegacyText("§7Вас пригласил: §e" + inviter)));
                output.add(new TextComponent(TextComponent.fromLegacyText("§7Ваше приглашение:")));

                if (referrer != null) {
                    TextComponent codeComponent = new TextComponent(TextComponent.fromLegacyText("  §e→ §7Ваш код: \"§e" + referrer.getCode() + "§7\" §b(Нажмите чтобы скопировать)"));
                    codeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы скопировать свой код приглашения"))));
                    codeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, referrer.getCode()));
                    output.add(codeComponent);

                    output.add(new TextComponent("  §e→ §7Вы пригласили: §e"+referrer.getReferalsCount()+"§7 человек"));
                } else {
                    TextComponent codeComponent = new TextComponent(TextComponent.fromLegacyText("  §e→ §7Ваш код: §cУ вас нет кода. \n   §e→ §7Вы можете создать его с помощью команды §e/ref create"));
                    codeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
                    codeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref create"));
                }

                output.add(new TextComponent(TextComponent.fromLegacyText("  §e→ §7За приглашение:")));
                output.add(new TextComponent(TextComponent.fromLegacyText("    §6→ §7Вы получаете: §e" + referrerCoins + "§7 тк")));
                output.add(new TextComponent(TextComponent.fromLegacyText("    §6→ §7Приглашённый: §e" + referalCoins + "§7 тк")));

                for (BaseComponent component : output) {
                    sender.sendMessage(component);
                }
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
            TextComponent helpref = new TextComponent(TextComponent.fromLegacyText("§7Напишите §e/ref help§7 для просмотра списка команд."));
            helpref.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref help"));
            helpref.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7Нажми здесь, чтобы ввести команду")}));

            TextComponent refcreate = new TextComponent(TextComponent.fromLegacyText("§a1)§7 Вы создаёте код приглашения §b(/ref create)"));
            helpref.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref create"));
            helpref.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7Нажми здесь, чтобы ввести команду")}));
            TextComponent[] helpop = new TextComponent[]{
                    new TextComponent(TextComponent.fromLegacyText(
                            "§7Описание команды §e/ref§7:"+
                            "\n§eRefSystem §7- реферальная система сервера §9Dream§bTime§7,"+
                            "\n§7благодаря которой можно заработать таймкоины за приглашения игроков на сервер!"+
                            "\n§7Всё очень §aпросто§7:")),
                    refcreate,
                    new TextComponent(TextComponent.fromLegacyText(
                            "§a2)§7 Вы приглашаете игроков на наш сервер;" +
                            "\n§a3)§7 Они наигрывают на сервере сутки;" +
                            "\n§a4)§7 Вводят Ваш код §b(/ref use <код>)" +
                            "\n§a5)§7 Вы и игроки получаете награду!" +
                            "\n§a")),
                    helpref};
            for (TextComponent component : helpop) {
                sender.sendMessage(component);
            }
        }

        if(help2) {
            TextComponent helpt1 = new TextComponent(TextComponent.fromLegacyText("§7Помощь по команде §e/ref§7:"));
            TextComponent helpt2 = new TextComponent(TextComponent.fromLegacyText("§a>§e /ref guide§7 - описание реферальной системы."));
            helpt2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref guide"));
            helpt2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Нажми здесь, чтобы ввести команду")));
            TextComponent helpt3 = new TextComponent(TextComponent.fromLegacyText("§a>§e /ref create§7 - создать реферальный код."));
            helpt3.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref create"));
            helpt3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
            TextComponent helpt4 = new TextComponent(TextComponent.fromLegacyText("§c   ВНИМАНИЕ!§7 У Вас присутствует уникальная возможность"));
            TextComponent helpt5 = new TextComponent(TextComponent.fromLegacyText("§7создавать свой код! Для этого используйте §e/ref create [код]§7!"));
            TextComponent helpt6 = new TextComponent(TextComponent.fromLegacyText("§a> §e/ref use <код>§7 - использовать реферальный код."));
            helpt6.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref use "));
            helpt6.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
            TextComponent helpt7 = new TextComponent(TextComponent.fromLegacyText("§a> §e/ref info§7 - узнать информацию о своих рефералах."));
            helpt7.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref info"));
            helpt7.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));

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
                TextComponent helpt8 = new TextComponent(TextComponent.fromLegacyText("§a> §e/ref reload§7 - перезагрузить конфиг плагина."));
                helpt8.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref reload"));
                helpt8.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
                sender.sendMessage(helpt8);
            }

            if (sender.hasPermission("dreamtimereferalsystem.tester")) {

                TextComponent helpt9 = new TextComponent(TextComponent.fromLegacyText("§a> §e/ref reset§7 - сбросить введённый код."));
                helpt9.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref reset"));
                helpt9.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
                TextComponent helpt10 = new TextComponent(TextComponent.fromLegacyText("§a> §e/ref skiptime§7 - пропустить время ожидания."));
                helpt10.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref skiptime"));
                helpt10.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
                TextComponent helpt11 = new TextComponent(TextComponent.fromLegacyText("§a> §e/ref resettime§7 - сбросить время ожидания (придётся ждать снова)."));
                helpt11.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref resettime"));
                helpt11.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
                sender.sendMessage(helpt9);
                sender.sendMessage(helpt10);
                sender.sendMessage(helpt11);
            }

            TextComponent helpt12 = new TextComponent(TextComponent.fromLegacyText("§a> §e/ref ignore§7 - выключить/включить сообщения о приглашениях "));
            helpt12.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ref ignore"));
            helpt12.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(("§7Нажми здесь, чтобы ввести команду"))));
            sender.sendMessage(helpt12);
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completer = new ArrayList<>();
        if (args.length == 1) {
            completer.add("create");
            completer.add("use");
            completer.add("help");
            completer.add("info");
            completer.add("guide");
            completer.add("ignore");
            if (sender.hasPermission("dreamtimereferalsystem.reload")) {
                completer.add("reload");
            }
            if (sender.hasPermission("dreamtimereferalsystem.tester")) {
                completer.add("update");
                completer.add("reset");
                completer.add("skiptime");
                completer.add("resettime");
            }
            Set<String> match = new HashSet<>();
            String search = args[0].toLowerCase();
            for (String str : completer) {
                if (str.startsWith(search)) {
                    match.add(str);
                }
            }

            return match;
        } else if (args.length == 2) {

            if ((
                    args[0].equalsIgnoreCase("reset") ||
                    args[0].equalsIgnoreCase("update") ||
                    args[0].equalsIgnoreCase("skiptime") ||
                    args[0].equalsIgnoreCase("resettime")
                )
                &&
                sender.hasPermission("dreamtimereferalsystem.tester")
            ) {

                Set<String> match = new HashSet<>();
                String search = args[1].toLowerCase();
                for (ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
                    if (pl.getName().toLowerCase().startsWith(search)) {
                        match.add(pl.getName());
                    }
                }
                return match;
            }
        }
        return completer;
    }
}
