package cat.jaffa.multitwitchwhitelist;
/**
 * Created by Jaffa on 03/07/2017.
 */

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class MultiTwitchWhitelist extends JavaPlugin {
     static Logger log;
     static String apiURL = "https://whitelist.jaffa.cat/api";
     static Plugin instance;
     static Boolean validKey = true;


    static Configuration cfg() {
        return instance.getConfig();
    }

    static String getApiURL() {
        String url = apiURL;
        if (!cfg().getString("Debug.apiURL").equalsIgnoreCase("default")) {
            return instance.getConfig().getString("Debug.apiURL");
        } else {
            return apiURL;
        }
    }

    static void softEnable() {
        instance.getConfig().set("Enabled", true);
        instance.saveConfig();
    }
    static void softDisable() {
        instance.getConfig().set("Enabled", false);
        instance.saveConfig();
    }

    @Override
    public void onEnable() {
        this.log = getLogger();
        this.instance = this;

        cfg().addDefault("Enabled",false);
        cfg().addDefault("ClientID","Client ID");
        cfg().addDefault("ClientSecret","Client Secret");
        cfg().addDefault("ChangeDisplayname",false);
        cfg().addDefault("ChangeListname",false);
        cfg().addDefault("TryCacheOnFail",true);
        cfg().addDefault("KickOnFail",true);
        cfg().addDefault("Debug.apiURL","default");
        cfg().options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new LoginListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }


    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {
        if (command.getName().equalsIgnoreCase("mtwl")) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "reload": {
                        reloadConfig();
                        validKey = true;
                        Bukkit.broadcast("MultiTwitchWhitelist reloaded by " + sender.getName(), "mtwl.admin");
                    }
                    break;
                    case "enable": {
                        if (!cfg().getBoolean("Enabled")) {
                            softEnable();
                            validKey = true;
                            Bukkit.broadcast("MultiTwitchWhitelist enabled by " + sender.getName(), "mtwl.admin");
                            log.info("Enabled by command (" + sender.getName() + ")");
                        } else sender.sendMessage("MultiTwitchWhitelist is already enabled.");
                    }
                    break;
                    case "disable": {
                        if (cfg().getBoolean("Enabled")) {
                            softDisable();
                            Bukkit.broadcast("MultiTwitchWhitelist disabled by " + sender.getName(), "mtwl.admin");
                            log.info("Disabled by command (" + sender.getName() + ")");
                        } else sender.sendMessage("MultiTwitchWhitelist is already disabled.");
                    }
                    break;
                    default: {
                        sender.sendMessage("Unknown command, check your spelling.");
                    }
                }
            } else {
                sender.sendMessage("Possible commands: enable, disable, reload");
            }
            return true;
        }
        return false;
    }

}
