package commands;

import commands.moderation.Permission;
import db.DataManager;
import main.MainBot;
import main.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
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
        Util.sendMessage(event.getChannel(), "**>Error: inssuficient permission**");
        return;
      }
      if(DataManager.getPerms(event.getGuild().getLongID()).size() >= 25) {
        Util.sendMessage(event.getChannel(), "Error: you can only have up to 25 permissions.");
        return;
      }

      DataManager.setPermission(Permission.commandParse(args, event.getGuild().getLongID()), event.getGuild().getLongID());
    });

    ChatCommands.adminMap.put("logoff", (event, args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        Util.sendMessage(event.getChannel(), "Turning off...");
        RequestBuffer.request(() -> MainBot.cli.logout());
        DataManager.finish();
        System.exit(0);
      }
    });

    ChatCommands.adminMap.put("godmode", (event, args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        Util.gmode = !Util.gmode;
        if (Util.gmode) {
          Util.sendMessage(event.getChannel(), "GodMode On - Only jaja can do commands now hehehe");
        } else {
          Util.sendMessage(event.getChannel(), "GodMode Off - *just don't spam too much pls*");
        }
      }
    });

    ChatCommands.commandMap.put("invite", (event, args) -> {
      Util.sendMessage(event.getChannel(), "Invite link for Jarzu botto: " + Util.link);
    });

    ChatCommands.adminMap.put("game", (MessageReceivedEvent event, List<String> args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        if (args.size() != 0) {
          String text = "";
          for (Object a : args.toArray()) {
            text += " " + a;
          }
          Util.sendMessage(event.getChannel(), "Changed game status to: " + text);
          MainBot.cli.changePlayingText(text);
        }
      }
    });

    commandMap.put("ping", (event, args) -> {
      Util.sendMessage(event.getChannel(), "pong");
    });

    commandMap.put("info", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      builder.appendField("Jarza Bot!", String.format("Version %1$s", Util.version), true);
      builder.withAuthorName(event.getAuthor().getName());
      builder.withColor(new Color(112, 137, 255));
      builder.withAuthorIcon(event.getAuthor().getAvatarURL());
      builder.withFooterText("Still under development! uwu");
      builder.withThumbnail(MainBot.cli.getOurUser().getAvatarURL());

      RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
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
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
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
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
      }
    });

    ChatCommands.commandMap.put("test", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      builder.appendField("Test command!", String.format("Version %1$s", Util.version), true);
      builder.appendField("Commands used:", Util.totcom + "", false);
      builder.withColor(100, 255, 199);
      builder.withAuthorName(event.getAuthor().getName());
      builder.withAuthorIcon(event.getAuthor().getAvatarURL());
      builder.withFooterText("Still under development! uwu");
      builder.withThumbnail(
          "https://cdn.discordapp.com/avatars/218787234910961665/34d21653c66436b94c2af78b30156e91.webp?size=64");

      RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
//      MainBot.cli.changeAvatar(Image.forFile(new File("gfx/drawing.png")));
    });

    commandMap.put("say", (event, args) -> {
      String text = "";
      {
        String t = "";
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
      event.getMessage().delete();
      Util.sendMessage(event.getChannel(), text);
    });

    commandMap.put("satansbae", (event, args) -> {
      String song =
          "*Archangel*\n*Darkangel*\n*Bring us elevation*\n*Through hell and through heaven*\n***Until we reach ascention***";
      RequestBuffer.request(() -> event.getChannel().sendMessage(song));
    });
  }

}
