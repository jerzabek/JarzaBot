package main;

import db.DataManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
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
      "https://discordapp.com/oauth2/authorize?client_id=334665490612092929&scope=bot&permissions=1610083446";
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
    return a.format(DateTimeFormatter.ofPattern("dd-MM-yyyy - HH:mm"));
  }

  public static void sendMessage(MessageEvent event, String msg) {
    RequestBuffer.request(() -> {
      try {
        event.getChannel().sendMessage(msg);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
              RequestBuffer.request(() -> MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `j.disable " + event.getGuild().getLongID() + "`"));
            }
          } else {
            RequestBuffer.request(() -> event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?"));
          }
        }
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
      }
    });
  }

  public static void sendMessage(MessageEvent event, EmbedObject msg) {
    RequestBuffer.request(() -> {
      try {
        event.getChannel().sendMessage(msg);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
//              System.out.println(DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()));
              RequestBuffer.request(() -> MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `j.disable " + event.getGuild().getLongID() + "`"));
            }
          } else {
            RequestBuffer.request(() -> event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?"));
          }
        }
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
      }
    });
  }

  public static void sendMessage(MessageEvent event, String msg, EmbedObject obj) {
    RequestBuffer.request(() -> {
      try {
        event.getChannel().sendMessage(msg, obj);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
              RequestBuffer.request(() -> MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `j.disable " + event.getGuild().getLongID() + "`"));
            }
          } else {
            RequestBuffer.request(() -> event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?"));
          }
        }
      }catch (DiscordException e) {
        System.err.println("Couldn't send message. Here's why:");
        e.printStackTrace();
      }
    });
  }


  public static void sendMessage(MessageEvent event, String s, InputStream image, String s1) {
    RequestBuffer.request(() -> {
      try {
        event.getChannel().sendFile(s, image, s1);
      } catch(MissingPermissionsException e) {
        if (DataManager.getBotComChan(event.getGuild().getLongID()) != -2L) {
          if (DataManager.getBotComChan(event.getGuild().getLongID()) == -1L) {
            if (DataManager.getBotComChan(event.getGuild().getLongID(), event.getAuthor().getLongID()) != -1L) {
              RequestBuffer.request(() -> MainBot.cli.getOrCreatePMChannel(event.getAuthor()).sendMessage(
                "Hey, would you mind telling the staff in `" + event.getGuild().getName() + "` that I can't respond to you in " + event.getChannel()
                  + " because I am missing message sending perms. Thank you! ;D\np.s. if you don't want this notification just respond with `j.disable " + event.getGuild().getLongID() + "`"));
            }
          } else {
            RequestBuffer.request(() -> event.getGuild().getChannelByID(DataManager.getBotComChan(event.getGuild().getLongID()))
              .sendMessage(event.getAuthor() + " Hey I can't respond to you in " + event.getChannel() + " so do your thing in here, aight?"));
          }
        }
      }catch (DiscordException e) {
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
