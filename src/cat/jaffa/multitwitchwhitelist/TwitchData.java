package cat.jaffa.multitwitchwhitelist;

import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Jaffa on 04/08/2017.
 */




public class TwitchData {
    private static HashMap<UUID,WhitelistData> data = new HashMap<UUID,WhitelistData>();
    public static WhitelistData get (Player player)
    {
        return data.get(player.getUniqueId());
    }
    protected static void set (Player player, WhitelistData obj){
        data.put(player.getUniqueId(),obj);
    }

    public static int getID (Player player)
    {
        return data.get(player.getUniqueId()).getUser().getId();
    }

    public static String getUsername (Player player)
    {
        return data.get(player.getUniqueId()).getUser().getUsername();
    }

    public static String getDisplayname (Player player)
    {
        return data.get(player.getUniqueId()).getUser().getDisplayname();
    }

    public static Date getCreated (Player player)
    {
        return data.get(player.getUniqueId()).getUser().getAccountcreation();
    }
}