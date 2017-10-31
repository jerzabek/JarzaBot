package main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Util {

  public static String prefix = "j.";
  public static List<Long> botAdmins;
  public static double version = 0.15;
  private static int cnum = 10;
  private static JSONObject commands;
  public static String COMMANDS = "commands.json";
  public static List<String> catnames;
  public static HashMap<String, HashMap<String, String>> cats;
  public static int totcom = 0;
  public static String link =
      "https://discordapp.com/oauth2/authorize?client_id=334665490612092929&scope=bot&permissions=335932502";
  public static boolean gmode = true;
  public static Long jarza = 0l;

  public static void init() {
    try {
      commands = (JSONObject) (new JSONParser().parse(new FileReader(COMMANDS)));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    catnames = new ArrayList<>();
    cats = new HashMap<>();
    for(Object a : commands.keySet()){
      String b = (String) a;
      JSONObject c = ((JSONObject) commands.get(b));
      catnames.add(b);
      HashMap<String, String> t = new HashMap<>();
      for(Object d : c.keySet()){
        t.put((String) d, (String) c.get(d));
      }
      cats.put(b, t);
    }

  }

  static IDiscordClient getBuiltDiscordClient(String token) {
    return new ClientBuilder().withToken(token).build();

  }

  public static String toTimeStamp(LocalDateTime a){
    return a.getDayOfMonth() + "/" + a.getMonth().getValue() + "/" + a.getYear() + " - " + a.getHour() + ":" + a.getMinute();
  }

  public static void sendMessage(IChannel channel, String message) {
    RequestBuffer.request(() -> {
      try {
        channel.sendMessage(message);
      } catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
      }
    });
  }

  public static boolean botCommand(String t) {
    boolean r = false;

    if (t.startsWith("j.") || t.startsWith("+") || t.startsWith("!") || t.startsWith("?")
        || t.startsWith("-") || t.startsWith("b.") || t.startsWith("f'") || t.startsWith("p!")
        || t.startsWith("=") || t.startsWith(".")) {
      r = true;
    }

    return r;
  }

  public static void kickUser(String user, String reason) {

  }

  public static Long userToID(String user) {
    Long id = 0L;
    try {
      id = Long.parseLong(user.substring(2, user.length() - 1));
    } catch (Exception e) {
      id = Long.parseLong(user.substring(3, user.length() - 1));
    }
    return id;
  }

}
