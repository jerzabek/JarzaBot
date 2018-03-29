package main;

import main.commands.ChatCommands;
import main.commands.memes.MemeCL;
import main.commands.moderation.ModerationCL;
import main.db.DataManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.api.IDiscordClient;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainBot {
  public static IDiscordClient cli;
  public static JSONObject config;
  public static Thread writer;

  public static void main(String[] args) {
    // //In case you build, uncomment dis
    // if (args.length != 1) {
    // System.out.println("Pls token: java -jar thisjar.jar tokenhere");
    // return;
    // }F
//    try {
//      PrintStream out = new PrintStream(new FileOutputStream("log.txt", true));
//      System.setOut(out);
//      System.setErr(new PrintStream(new FileOutputStream("errlog.txt", true)));
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    }
    writer = new Thread(null, () -> {
      JSONObject sets;
      JSONObject war;
      JSONArray me;
      JSONObject cof;
      JSONArray glmem;
      try {
        sets = (JSONObject) (new JSONParser().parse(DataManager.settings.toJSONString()));
        war = (JSONObject) (new JSONParser().parse(DataManager.warns.toJSONString()));
        me = (JSONArray) (new JSONParser().parse(DataManager.memes.toJSONString()));
        cof = (JSONObject) (new JSONParser().parse(MainBot.config.toJSONString()));
        glmem = (JSONArray) (new JSONParser().parse(DataManager.globalmemes.toJSONString()));
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
          cof.put("commands", Util.totcom);
          fw.write(cof.toJSONString());
          fw.flush();
          fw.close();
          fw = new FileWriter(DataManager.GLMEMES);
          fw.write(glmem.toJSONString());
          fw.flush();
          fw.close();
          fw = new FileWriter(DataManager.PREMIUMS);
          fw.write(DataManager.premiums.toJSONString());
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
//    String token = "MzAxMDE3NzUwNTAxODUxMTM3.DTVKNA.aw0AsfeU6akAi4roDlveuHLZo2k";
    Util.totcom = Integer.parseInt(config.get("commands") + "");
    cli = Util.getBuiltDiscordClient(token); // args[0]

    cli.getDispatcher().registerListener(new ChatEvents());

//    Timer t = new Timer();
//
//    t.schedule(new TimerTask() {
//      @Override public void run() {
//        MainBot.writer.run();
//      }
//    }, 5000L, TimeUnit.MINUTES.toMillis(10L));

    // Only login after all events are registered otherwise some may be missed.
    Util.init();
    ChatCommands.init();
    MemeCL.init();
    ModerationCL.init();
    DataManager.init();
    try {
      cli.login();
    } catch (IllegalStateException e) {

    }
    //    cli.changePlayingText("my own creation");
    //    Database.loadData();
  }
}
