package cat.jaffa.multitwitchwhitelist;

import com.google.gson.Gson;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jordan on 05/07/2017.
 */
public class WhitelistDataCreator {
    private static WhitelistData fromURL(URL url) throws IOException {
        System.out.println(url);
        Gson gson = new Gson();
        //InputStreamReader reader = new InputStreamReader(url.openStream());
        //return gson.fromJson(reader,WhitelistData.class);


        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "MTWL-Bukkit-Plugin");
        con.setRequestProperty("api-client", MultiTwitchWhitelist.cfg.getString("ClientID"));
        con.setRequestProperty("api-secret", MultiTwitchWhitelist.cfg.getString("ClientSecret"));

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
        //wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return gson.fromJson(response.toString(),WhitelistData.class);

    }
    public static WhitelistData fromUser(Player p) {

        try {
            return fromURL(new URL(MultiTwitchWhitelist.getApiURL()+"/login/"+p.getUniqueId()));
        } catch (IOException e) {
            MultiTwitchWhitelist.log.warning(String.format(
                    "Error getting details for user %s(%s): %s (%s)",
                    p.getName(),p.getUniqueId(),e.getClass().getName(),e.getMessage())
            );
            return null;
        }
    }
}
