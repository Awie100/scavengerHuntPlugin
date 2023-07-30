package me.awie1000.riddlebot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class RiddleBot extends JavaPlugin {

    ScavengerHunt hunt;
    boolean autoEnd;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("[HUNT]: Loading...");

        hunt = new ScavengerHunt();
        autoEnd = true;

        this.getCommand("hunt").setExecutor(new HuntCommand(this));
        this.getCommand("hunt-admin").setExecutor(new HuntAdminCommand(this));

        getLogger().info("[HUNT]: Ready!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

class HuntCommand implements TabExecutor {

    RiddleBot plugin;
    final String[] subCommands = new String[] {"submit", "list", "players"};
    final Component endText = Component.text().content("Hunt has ended!").color(NamedTextColor.GOLD).build();

    public HuntCommand(RiddleBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length < 1) return false;

        Player player = (Player)commandSender;

        try {
            switch (args[0]) {
                case "submit":
                    ItemStack item = player.getInventory().getItemInMainHand();
                    Material mat = item.getType();
                    plugin.hunt.addPlayerToMaterial(mat, player);
                    item = item.subtract();
                    player.getInventory().setItemInMainHand(item);
                    Component submitText = Component.text()
                            .content(String.format("Submitted %s!", MaterialClassifier.matToName(mat)))
                            .color(NamedTextColor.GREEN)
                            .build();
                    player.sendMessage(submitText);
                    Component submitAllText = Component.text()
                            .content(String.format("%s found %s!", player.getName(), MaterialClassifier.matToName(mat)))
                            .color(NamedTextColor.GOLD)
                            .build();
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(submitAllText));


                    //winner check
                    if(plugin.hunt.getPlayerObjectiveCount(player) >= plugin.hunt.size()) {
                        Component winnerText = Component.text()
                                .content(String.format("Player %s has gotten all items!", player.getName()))
                                .color(NamedTextColor.GOLD)
                                .build();
                        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(winnerText));
                        if(plugin.autoEnd) {
                            plugin.hunt.end();
                            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(endText));
                        }
                    }
                    break;
                case "list":
                    Component listText = Component.text().build();
                    for(Map.Entry<Material, Boolean> entry : plugin.hunt.getPlayerProgress(player).entrySet()) {
                        listText = listText.append(Component.text()
                                .content(String.format("%c %s\n", entry.getValue() ? '\u2714' : '-', MaterialClassifier.matToName(entry.getKey())))
                                .color(entry.getValue() ? NamedTextColor.GREEN : NamedTextColor.GRAY)
                                .build()
                        );
                    }
                    player.sendMessage(listText.append(Component.text()
                            .content(String.format("Items Found: %d/%d", plugin.hunt.getPlayerObjectiveCount(player), plugin.hunt.size()))
                            .color(plugin.hunt.getPlayerObjectiveCount(player) == plugin.hunt.size() ? NamedTextColor.GOLD : NamedTextColor.WHITE)
                            .build()
                    ));
                    break;
                case "players":
                    String[] playerList = plugin.hunt.getAllPlayerObjectiveCount().entrySet().stream()
                            .sorted(Comparator.comparingInt(Map.Entry::getValue))
                            .map(entry -> String.format("%s %d/%d", (Bukkit.getPlayer(entry.getKey()) != null ? Bukkit.getPlayer(entry.getKey()).getName() : "????"), entry.getValue(), plugin.hunt.size()))
                            .toArray(String[]::new);
                    player.sendMessage(Component.text()
                            .content(String.join("\n", playerList))
                            .color(NamedTextColor.GREEN)
                            .build()
                    );
                    break;
                default:
                    throw new ScavengerError(String.format("Unknown control sequence '%s'", args[0]));
            }
        } catch (ScavengerError e) {
            Component errorText = Component.text()
                    .content(String.format("[ERR]: %s", e.getMessage()))
                    .color(NamedTextColor.RED)
                    .build();
            player.sendMessage(errorText);
            return true;
        }

        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> autoCompletes = new ArrayList<>();

        if (args.length == 1) {
            autoCompletes.addAll(Arrays.stream(subCommands)
                    .filter(sub -> sub.startsWith(args[0]))
                    .collect(Collectors.toList())
            );
        }

        return autoCompletes;
    }
}

class HuntAdminCommand implements TabExecutor {

    RiddleBot plugin;
    final String[] subCommands = new String[] {"start", "stop", "end", "toggleAutoEnd", "restart", "add", "generate"};
    final Component startText = Component.text().content("Hunt has begun!").color(NamedTextColor.GOLD).build();
    final Component stopText = Component.text().content("Hunt has stopped!").color(NamedTextColor.GOLD).build();
    final Component endText = Component.text().content("Hunt has ended!").color(NamedTextColor.GOLD).build();
    final Component generatedText = Component.text().content("Generated new list! \nItems: ").color(NamedTextColor.GREEN).build();

    public HuntAdminCommand(RiddleBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length < 1) return false;

        Player player = (Player)commandSender;

        try {
            switch (args[0]) {
                case "toggleAutoEnd":
                    plugin.autoEnd = !plugin.autoEnd;
                    player.sendMessage(Component.text()
                            .content(String.format("AutoEnd set to %b", plugin.autoEnd))
                            .color(NamedTextColor.GREEN)
                            .build()
                    );
                    break;
                case "start":
                    plugin.hunt.start();
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(startText));
                    break;
                case "stop":
                    plugin.hunt.stop();
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(stopText));
                    break;
                case "end":
                    plugin.hunt.end();
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(endText));
                    break;
                case "restart":
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(stopText));
                    plugin.hunt.restart();
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(startText));
                    break;
                case "add":
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(item.getType().isEmpty()) throw new ScavengerError("You do not have an item selected.");
                    plugin.hunt.addMaterialToList(item.getType());
                    Component addText = Component.text()
                            .content(String.format("Added %s.", MaterialClassifier.matToName(item.getType())))
                            .color(NamedTextColor.GREEN)
                            .build();
                    player.sendMessage(addText);
                    break;
                case "generate":
                    if(args.length < 2) throw new ScavengerError("Proper usage: /hunt generate <NUMBER_OF_ITEMS>");
                    if(!args[1].matches("^[-+]?\\d+$")) throw new ScavengerError(String.format("'%s' is not an integer amount.", args[1]));
                    plugin.hunt.generate(Integer.parseInt(args[1]));
                    Component listText = generatedText;
                    for(Material mat : plugin.hunt.getPlayerProgress(player).keySet()) {
                        listText = listText.append(Component.text()
                                .content(String.format("%s, ", MaterialClassifier.matToName(mat)))
                                .color(NamedTextColor.GREEN)
                                .build()
                        );
                    }
                    player.sendMessage(listText);
                    break;
                default:
                    throw new ScavengerError(String.format("Unknown control sequence '%s'", args[0]));
            }
        } catch (ScavengerError e) {
            Component errorText = Component.text()
                    .content(String.format("[ERR]: %s", e.getMessage()))
                    .color(NamedTextColor.RED)
                    .build();
            player.sendMessage(errorText);
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> autoCompletes = new ArrayList<>();

        if (args.length == 1) {
            autoCompletes.addAll(Arrays.stream(subCommands)
                    .filter(sub -> sub.startsWith(args[0]))
                    .collect(Collectors.toList())
            );
        }

        return autoCompletes;
    }
}