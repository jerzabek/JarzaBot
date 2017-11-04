package commands;

import commands.moderation.Permission;
import db.DataManager;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import main.MainBot;
import main.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatCommands {

  public static Map<String, ChatCommand> commandMap = new HashMap<>();
  public static Map<String, ChatCommand> adminMap = new HashMap<>();

  public static void init() {
    ChatCommands.commandMap.put("rule", (event, args) -> {
      //j.rule say deny general jarza
      // OR
      //j.rule say deny jarza genearl
      boolean cont = false;
      for(Long a : DataManager.getModrole(event.getGuild().getLongID())){
        if (event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(a))){
          cont = true;
        }
      }
      if (!cont) {
        Util.sendMessage(event, "**>Error: inssuficient permission**");
        return;
      }
      if(DataManager.getPerms(event.getGuild().getLongID()).size() >= 25) {
        Util.sendMessage(event, "Error: you can only have up to 25 permissions.");
        return;
      }
      if(args.isEmpty())
        return;
      if(args.size() == 1)
        return;


      DataManager.setPermission(Permission.commandParse(args, event.getGuild().getLongID(), event), event.getGuild().getLongID());
    });

    ChatCommands.adminMap.put("logoff", (event, args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        Util.sendMessage(event, "Turning off...");
        RequestBuffer.request(() -> MainBot.cli.logout());
        DataManager.finish();
        System.exit(0);
      }
    });

    ChatCommands.adminMap.put("godmode", (event, args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        Util.gmode = !Util.gmode;
        if (Util.gmode) {
          Util.sendMessage(event, "GodMode On - Only jaja can do commands now hehehe");
        } else {
          Util.sendMessage(event, "GodMode Off - *just don't spam too much pls*");
        }
      }
    });

    ChatCommands.commandMap.put("invite", (event, args) -> {
      Util.sendMessage(event, "Invite link for Jarzu botto: " + Util.link);
    });

    ChatCommands.adminMap.put("game", (MessageReceivedEvent event, List<String> args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        if (args.size() != 0) {
          String text = "";
          for (Object a : args.toArray()) {
            text += " " + a;
          }
          Util.sendMessage(event, "Changed game status to: " + text);
          MainBot.cli.changePlayingText(text);
        }
      }
    });

    commandMap.put("info", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      builder.appendField("Jarza Bot!", String.format("Version %1$s", Util.version), true);
      builder.appendField("Commands used so far:", "" + Util.totcom, true);
      builder.appendField("How many servers have me?", MainBot.cli.getGuilds().size() + " so far!", true);
      builder.withAuthorName(event.getAuthor().getName());
      builder.withColor(new Color(112, 137, 255));
      builder.withAuthorIcon(event.getAuthor().getAvatarURL());
      builder.withFooterText("Still under development! uwu");
      builder.withThumbnail(MainBot.cli.getOurUser().getAvatarURL());

      Util.sendMessage(event, builder.build());
    });

    commandMap.put("help", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      if(args.isEmpty()){
        builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version),
          true);
        builder.withAuthorName("Jarza Bot Manual!");
        builder.withAuthorIcon(event.getAuthor().getAvatarURL());
        builder.withFooterText("Still under development! uwu");
        builder.withColor(new Color(112, 137, 255));


        for (String a : Util.catnames) {
          String comms = "";
          for(String c : Util.cats.get(a).keySet()) {
            comms += c + ", ";
          }
          builder.appendField(a, comms, false);
        }
        Util.sendMessage(event, builder.build());
      }else if(args.size() == 1){
        builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version),
          true);
        builder.withAuthorName("Jarza Bot Manual!");
        builder.withAuthorIcon(event.getAuthor().getAvatarURL());
        builder.withFooterText("Still under development! uwu");
        builder.withColor(new Color(112, 137, 255));
        String cmd = null, info = null;
        for (String a : Util.catnames) {
          for(String c : Util.cats.get(a).keySet()) {
            if(c.equals(args.get(0).toLowerCase())){
              cmd = c;
              info = Util.cats.get(a).get(c);
            }
          }
        }

        if(cmd == null)
          return;

        builder.appendField(cmd, info, false);
        Util.sendMessage(event, builder.build());
      }
    });

    commandMap.put("say", (MessageReceivedEvent event, List<String> args) -> {
      String text = "";
      {
        String t;
        int counter = 0;
        for (Object a : args.toArray()) {
          counter++;
          if (!(counter == args.size())) {
            t = " ";
          } else {
            t = "";
          }
          text += a + t;
        }
      }
      System.out.println(event.getAuthor().getName() + " said: " + text);
      if(event.getGuild() != null)
        event.getMessage().delete();

      Util.sendMessage(event, text);
    });

    commandMap.put("satansbae", (event, args) -> {
      String song =
          "*Archangel*\n*Darkangel*\n*Bring us elevation*\n*Through hell and through heaven*\n***Until we reach ascention***";
      Util.sendMessage(event, song);
    });

    commandMap.put("disable", (event, args) -> {
      if(args.isEmpty())
        return;

      Long g;
      try{
        g = Long.parseLong(args.get(0));
      }catch(NumberFormatException e){return;}

      DataManager.setUserNotifi(g, event.getAuthor().getLongID());
    });

    adminMap.put("write", (event, args) -> {
      MainBot.writer.run();
    });
  }

}
