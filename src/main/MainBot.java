package main;

import commands.ChatCommands;
import commands.memes.MemeCL;
import commands.moderation.ModerationCL;
import db.DataManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.api.IDiscordClient;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainBot {
  public static IDiscordClient cli;
  public static JSONObject config;
  public static Thread writer;

  public static void main(String[] args) {
    // //In case you build, uncomment dis
    // if (args.length != 1) {
    // System.out.println("Pls token: java -jar thisjar.jar tokenhere");
    // return;
    // }
    writer = new Thread(null, () -> {
      JSONObject sets;
      JSONObject war;
      JSONArray me;
      JSONObject cof;
      try {
        sets = (JSONObject) (new JSONParser().parse(DataManager.settings.toJSONString()));
        war = (JSONObject) (new JSONParser().parse(DataManager.warns.toJSONString()));
        me = (JSONArray) (new JSONParser().parse(DataManager.memes.toJSONString()));
        cof = (JSONObject) (new JSONParser().parse(MainBot.config.toJSONString()));
      } catch (ParseException e) {
        e.printStackTrace();
        return;
      }

      try {
        FileWriter fw = new FileWriter(DataManager.MEMES);
        try {
          fw.write(me.toJSONString());
          fw.flush();
          fw.close();
          fw = new FileWriter(DataManager.SETTINGS);
          fw.write(sets.toJSONString());
          fw.flush();
          fw.close();
          fw = new FileWriter(DataManager.WARNS);
          fw.write(war.toJSONString());
          fw.flush();
          fw.close();
          fw = new FileWriter("config.json");
          Long a = (Long) MainBot.config.get("commands");
          MainBot.config.put("commands", a + Util.totcom);
          cof.put("commands", a + Util.totcom);
          fw.write(MainBot.config.toJSONString());
        } catch (Throwable e) {
          e.printStackTrace();
        } finally {
          fw.flush();
          fw.close();
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }, "FileSaving");
    try {
      config = (JSONObject) (new JSONParser().parse(new FileReader("config.json")));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    Util.jarza = Long.parseLong((String) config.get("owner"));
    String token = (String) config.get("key");
    Util.totcom = Integer.parseInt(config.get("commands") + "");
    cli = Util.getBuiltDiscordClient(token); // args[0]

    cli.getDispatcher().registerListener(new ChatEvents());

    // Only login after all events are registered otherwise some may be missed.
    Util.init();
    ChatCommands.init();
    MemeCL.init();
    ModerationCL.init();
    DataManager.init();
    cli.login();

    //    cli.changePlayingText("my own creation");
    //    Database.loadData();
  }
}
