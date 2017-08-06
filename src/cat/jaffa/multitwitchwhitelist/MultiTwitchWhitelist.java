package cat.jaffa.multitwitchwhitelist;
/**
 * Created by Jaffa on 03/07/2017.
 */

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class MultiTwitchWhitelist extends JavaPlugin {
     protected static Logger log;
     protected static Configuration cfg;
     protected static String apiURL = "https://whitelist.jaffa.cat/api";
    @Override
    public void onEnable() {
        this.log = getLogger();

        cfg = this.getConfig();
        cfg.addDefault("Enabled",false);
        cfg.addDefault("ClientID","Client ID");
        cfg.addDefault("ClientSecret","Client Secret");
        cfg.addDefault("ChangeDisplayname",false);
        cfg.addDefault("ChangeListname",false);
        cfg.addDefault("TryCacheOnFail",true);
        cfg.addDefault("KickOnFail",true);
        cfg.addDefault("Debug.enableDevFeatures",false);
        cfg.addDefault("Debug.apiURL","default");
        cfg.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new LoginListener(), this);
    }

    protected static String getApiURL() {
        String url = apiURL;
        if (cfg.getBoolean("Debug.enableDevFeatures") && cfg.getString("Debug.apiURL").equalsIgnoreCase("default")) {
            return cfg.getString("Debug.apiURL");
        } else {
            return apiURL;
        }
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
                        Bukkit.broadcast("MultiTwitchWhitelist reloaded by " + sender.getName(), "mtwl.admin");
                    }
                    break;
                    case "enable": {
                        cfg.set("Enabled", true);
                        saveConfig();
                        Bukkit.broadcast("MultiTwitchWhitelist enabled by " + sender.getName(), "mtwl.admin");
                        log.info("Enabled by command (" + sender.getName() + ")");
                    }
                    break;
                    case "disable": {
                        cfg.set("Enabled", false);
                        saveConfig();
                        Bukkit.broadcast("MultiTwitchWhitelist disabled by " + sender.getName(), "mtwl.admin");
                        log.info("Disabled by command (" + sender.getName() + ")");
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
