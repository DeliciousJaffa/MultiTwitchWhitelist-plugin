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
    public void onPlayerJoin(PlayerJoinEvent e) {
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
        if (data != null) {

            //Handle errors, user not linked or general error.
            int statusCode = data.getStatusCode();
            if (statusCode == 422) {
                if (p.hasPermission("mtwl.bypass.register")) {
                    MultiTwitchWhitelist.log.info(p.getName() + " Bypassing register requirement");
                } else {
                    e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Connect your Twitch account at:\nwhitelist.jaffa.cat");
                    break process;
                }
            } else if (statusCode != 200) {
                if (p.hasPermission("mtwl.bypass.fail")) {
                    MultiTwitchWhitelist.log.info(p.getName() + " Bypassing API fail");
                } else {
                    e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unable to connect to whitelisting server\nPlease try again later.");
                    break process;
                }
            } else {
                //Cache data
                TwitchData.set(p, data);
            }

            //Check if user has a ban
            List<WhitelistData.Ban> bans = data.getBans();
            if (bans.size() > 0) {
                //TODO: Check ban expiration
                WhitelistData.Ban ban = bans.get(0);
                if (ban.isGlobal()) {
                    e.disallow(PlayerLoginEvent.Result.KICK_BANNED, String.format("Global Banned\nReason: %s", ban.getReason()));
                    break process;
                } else if (p.hasPermission("mtwl.bypass.ban")) {
                    MultiTwitchWhitelist.log.info(String.format("%s (%s) Bypassing ban", p.getName(), data.getUser().getUsername()));
                } else {
                    e.disallow(PlayerLoginEvent.Result.KICK_BANNED, String.format("Banned by: %s\nReason: %s", ban.getInvokerDisplayname(), ban.getReason()));
                    break process;
                }
            }

            //Check if manually whitelisted or subscribed.
            if (!data.isManual() && !data.isSubbed()) {
                if (p.hasPermission("mtwl.bypass.list")) {
                    MultiTwitchWhitelist.log.info(String.format("%s (%s) Bypassing whitelist", p.getName(), data.getUser().getUsername()));
                } else {
                    e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Your Twitch account " + data.getUser().getDisplayname() + " is not subscribed to the appropriate streamers.");
                    break process;
                }
            }

            //Do things to the user if whitelisted.
            if (MultiTwitchWhitelist.cfg.getBoolean("ChangeDisplayname")) {
                p.setDisplayName(data.getUser().getDisplayname());
            }
            if (MultiTwitchWhitelist.cfg.getBoolean("ChangeListname")) {
                p.setPlayerListName(data.getUser().getDisplayname());
            }
            MultiTwitchWhitelist.log.info(String.format("%s (%s) Passed all checks", p.getName(), data.getUser().getUsername()));

        } else {
            MultiTwitchWhitelist.log.warning("Null whitelist data passed to handleLogin, report this error.");
            if (MultiTwitchWhitelist.cfg.getBoolean("KickOnConnectionFail")) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unable to connect to whitelisting server\nPlease try again later.");
            }
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        if (MultiTwitchWhitelist.cfg.getBoolean("Enabled")) {
            //Grab data from API
            try {
                WhitelistData data = WhitelistDataCreator.fromUser(p);
                //Check if data was returned, if it wasn't, check for cached data.
                if (data != null) {
                    handleLogin(e, data);
                } else {
                    WhitelistData cachedata = TwitchData.get(p);
                    if (cachedata != null && MultiTwitchWhitelist.cfg.getBoolean("TryCacheOnFail")) {
                        MultiTwitchWhitelist.log.warning("API returned invalid or no data, falling back to player cache");
                        handleLogin(e, cachedata);
                    } else {
                        MultiTwitchWhitelist.log.warning("API returned invalid or no data, no player cache available");
                        if (MultiTwitchWhitelist.cfg.getBoolean("KickOnConnectionFail") && !p.hasPermission("mtwl.bypass.fail")) {
                            //Kick player on fail with no cache if option is enabled.
                            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unable to connect to whitelisting server\nPlease try again later.");
                        }
                    }
                }
            } catch (Exception ex)  {
                MultiTwitchWhitelist.log.severe("Some kind of exception happened handling a login, please report this error with the stack trace.");
                ex.printStackTrace();
                if (!p.hasPermission("mtwl.bypass.severe")) e.disallow(PlayerLoginEvent.Result.KICK_OTHER,"Multi Twitch Whitelist Severe Error\nPlease try again later and contact a server admin.");
            }
        }
    }
}
