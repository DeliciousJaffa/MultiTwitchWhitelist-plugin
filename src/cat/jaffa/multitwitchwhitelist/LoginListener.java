package cat.jaffa.multitwitchwhitelist;

/**
 * Created by Jaffa on 03/07/2017.
 */
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;

public class LoginListener implements Listener {

    @EventHandler
    public void onPlayerJoin (PlayerJoinEvent e) {
        if (MultiTwitchWhitelist.cfg.getBoolean("Enabled")) {
            Player p = e.getPlayer();
            WhitelistData data = TwitchData.get(p);
            if (data != null) {
                if (MultiTwitchWhitelist.cfg.getBoolean("ChangeDisplayname")) {
                    p.setDisplayName(data.getUser().getDisplayname());
                }
                if (MultiTwitchWhitelist.cfg.getBoolean("ChangeListname")) {
                    p.setPlayerListName(data.getUser().getDisplayname());
                }
            }
        }
    }

    private void handleLogin(PlayerLoginEvent e, WhitelistData data) {
        Player p = e.getPlayer();
        process:
        //If data returned
        if (data != null) {
            //Handle errors, user not linked or general error.
            if (data.getStatusCode() != 200) {
                switch (data.getStatusCode()) {
                    case 422:
                        e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Connect your Twitch account at:\nwhitelist.jaffa.cat");
                        break;
                    default:
                        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unable to connect to whitelisting server\nPlease try again later.");
                        break;
                }
                break process;
            }
            List<WhitelistData.Ban> bans = data.getBans();
            //Check if user has a ban
            //TODO: Check ban expiration
            if (bans.size() > 0) {
                WhitelistData.Ban ban = bans.get(0);
                if (ban.isGlobal()) {
                    e.disallow(PlayerLoginEvent.Result.KICK_BANNED, String.format("Global Banned\nReason: %s", ban.getReason()));
                } else {
                    e.disallow(PlayerLoginEvent.Result.KICK_BANNED, String.format("Banned by: %s\nReason: %s", ban.getInvokerDisplayname(), ban.getReason()));
                }
                break process;
            }
            //Check if manually whitelisted.
            if (!data.isManual()) {
                //Check if user is subscribed.
                if (!data.isSubbed()) {
                    e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Your Twitch account " + data.getUser().getDisplayname() + " is not subscribed to the appropriate streamers.");
                    break process;
                }
            }

            //Do things to the user if successful.
            TwitchData.set(p, data);
            if (MultiTwitchWhitelist.cfg.getBoolean("ChangeDisplayname")) {
                p.setDisplayName(data.getUser().getDisplayname());
            }
            if (MultiTwitchWhitelist.cfg.getBoolean("ChangeListname")) {
                p.setPlayerListName(data.getUser().getDisplayname());
            }
            //Handle malformed/no response from API
        } else {
            //Check if we have a cached data object

            if (MultiTwitchWhitelist.cfg.getBoolean("KickOnConnectionFail")) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unable to connect to whitelisting server\nPlease try again later.");
            }
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e)
    {
        Player p = e.getPlayer();
        MultiTwitchWhitelist.log.info(p.getName() + " Attempting login. ("+ p.getUniqueId()+")");
        if (MultiTwitchWhitelist.cfg.getBoolean("Enabled")) {
            //Grab data from API
            WhitelistData data = WhitelistDataCreator.fromUser(p);
            //Check if data was returned, if it wasn't, check for cached data.
            if (data != null) {
                handleLogin(e,data);
            } else {
                WhitelistData cachedata = TwitchData.get(p);
                if (cachedata != null && MultiTwitchWhitelist.cfg.getBoolean("TryCacheOnFail")) {
                    handleLogin(e,cachedata);
                } else {
                    //Kick player on fail with no cache if option is enabled.
                    if (MultiTwitchWhitelist.cfg.getBoolean("KickOnConnectionFail")) {
                        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unable to connect to whitelisting server\nPlease try again later.");
                    }
                }
            }
        }
    }
}
