package main;

import main.db.DataManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.member.GuildMemberEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Util {

  public static String prefix = "h!";
  public static List<Long> botAdmins;
  public static double version = 0.17;
  private static int cnum = 10;
  private static JSONObject commands;
  public static String COMMANDS = "commands.json";
  public static List<String> catnames;
  public static HashMap<String, HashMap<String, String>> cats;
  public static int totcom = 0;
  public static ArrayList<String> premcoms;
  public static String link =
      "https://discordapp.com/api/oauth2/authorize?client_id=398878661953978368&permissions=2146954486&scope=bot";
  public static boolean gmode = true;
  public static Long jarza = 0l;

  public static void init() {
    try {
      commands = (JSONObject) (new JSONParser().parse(new FileReader(COMMANDS)));
      JSONArray adm = (JSONArray) MainBot.config.get("admins");
      botAdmins = new ArrayList<>();
      for(Object i : adm){
        botAdmins.add(Long.parseLong(i.toString()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    premcoms = new ArrayList<>();
    premcoms.add("convert");
    premcoms.add("premium");
    premcoms.add("discrim");
    premcoms.add("ar");
    premcoms.add("p");
    premcoms.add("q");
    premcoms.add("qr");
    premcoms.add("leave");
    premcoms.add("playing");
    premcoms.add("join");
    premcoms.add("skip");

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
    return a.format(DateTimeFormatter.ofPattern("dd-MM-yyyy - HH:mm"));
  }


  /**
   * wait dis is wip no use
   * @param c
   * @param msg
   */
  public static IMessage sendMessage(IChannel c, String msg) {
    return RequestBuffer.request(() -> {
      try {
        return c.sendMessage(msg);
      } catch (Throwable e) {
        e.printStackTrace();
        return null;
      }
    }).get();
  }

  public static IMessage sendMessage(MessageEvent event, String msg) {
    return RequestBuffer.request(() -> {
      try {
        return event.getChannel().sendMessage(msg);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
              MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `" + prefix + "disable " + event.getGuild().getLongID() + "`");
            }
          } else {
            event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?");
          }
        }
        return null;
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
        return null;
      }
    }).get();
  }

  public static IMessage sendMessage(MessageEvent event, EmbedObject msg) {
    return RequestBuffer.request(() -> {
      try {
        return event.getChannel().sendMessage(msg);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
//              System.out.println(DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()));
              MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `" + prefix + "disable " + event.getGuild().getLongID() + "`");
            }
          } else {
            event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?");
          }
        }
        return null;
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
        return null;
      }
    }).get();
  }

  public static IMessage sendLog(IChannel channel, String msg) {
    return RequestBuffer.request(() -> {
      try {
        return channel.sendMessage(msg);
      } catch(MissingPermissionsException e) {
        e.printStackTrace();
        return null;
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
        return null;
      }
    }).get();
  }

  public static IMessage sendLog(IChannel channel, EmbedObject msg) {
    return RequestBuffer.request(() -> {
      try {
        return channel.sendMessage(msg);
      } catch(MissingPermissionsException e) {
        e.printStackTrace();
        return null;
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
        return null;
      }
    }).get();
  }

  public static IMessage sendMessage(MessageEvent event, String msg, EmbedObject obj) {
    return RequestBuffer.request(() -> {
      try {
        return event.getChannel().sendMessage(msg, obj);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
              MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `" + prefix + "disable " + event.getGuild().getLongID() + "`");
            }
          } else {
            event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?");
          }
        }
        return null;
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
        return null;
      }
    }).get();
  }


  public static IMessage sendMessage(MessageEvent event, String s, InputStream image, String s1) {
    return RequestBuffer.request(() -> {
      try {
        return event.getChannel().sendFile(s, image, s1);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
              MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `" + prefix + "disable " + event.getGuild().getLongID() + "`");
            }
          } else {
            event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?");
          }
        }
        return null;
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
        return null;
      }
    }).get();
  }

  public static boolean botCommand(String t) {
    boolean r = false;

    if (t.startsWith(prefix) || t.startsWith("+") || t.startsWith("!") || t.startsWith("?")
        || t.startsWith("-") || t.startsWith("b.") || t.startsWith("f'") || t.startsWith("p!")
        || t.startsWith("=") || t.startsWith(".")) {
      r = true;
    }

    return r;
  }

  @Deprecated
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
