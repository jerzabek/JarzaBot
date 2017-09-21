package main;

import commands.ChatCommands;
import commands.memes.MemeCL;
import commands.moderation.ModerationCL;
import db.DataManager;
import sx.blah.discord.api.IDiscordClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainBot {
  public static IDiscordClient cli;

  public static void main(String[] args) {
    // //In case you build, uncomment dis
    // if (args.length != 1) {
    // System.out.println("Pls token: java -jar thisjar.jar tokenhere");
    // return;
    // }
    List<String> data = new ArrayList<>();
    try {
      data = Files.readAllLines(Paths.get("config.dat"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    Util.jarza = Long.parseLong(data.get(1));
    Util.testserver = Long.parseLong(data.get(2));
    String token = data.get(0);

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
