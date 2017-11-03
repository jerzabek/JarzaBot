package main;

import commands.ChatCommands;
import commands.memes.MemeCL;
import commands.moderation.ModerationCL;
import db.DataManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.api.IDiscordClient;

import java.io.FileReader;
import java.io.IOException;

public class MainBot {
  public static IDiscordClient cli;

  public static void main(String[] args) {
    // //In case you build, uncomment dis
    // if (args.length != 1) {
    // System.out.println("Pls token: java -jar thisjar.jar tokenhere");
    // return;
    // }
    JSONObject config = new JSONObject();
    try {
      config = (JSONObject) (new JSONParser().parse(new FileReader("config.json")));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    Util.jarza = Long.parseLong((String) config.get("owner"));
    String token = (String) config.get("key");

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
