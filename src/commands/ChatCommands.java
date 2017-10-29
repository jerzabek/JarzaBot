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

  public static void init() {
    ChatCommands.commandMap.put("rule", (event, args) -> {
      //j.rule deny say everyone general
      //j.rule allow say staff general
      //j.rule meme deny everyone
      //j.rule meme allow everyone botcommands
      //j.rule satansbae deny jarza bot
      //[deny or allow] [command name] (user default: everyone) (channel default: all) (role default: everyone)
//      DataStore.setPermission(Permission.commandParse(args, event.getGuild().getLongID()), event.getGuild().getLongID());
      DataManager.setPermission(Permission.commandParse(args, event.getGuild().getLongID()), event.getGuild().getLongID());
    });

    ChatCommands.commandMap.put("logoff", (event, args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        Util.sendMessage(event.getChannel(), "Turning off...");
        RequestBuffer.request(() -> MainBot.cli.logout());
        DataManager.finish();
        System.exit(0);
      }
    });

    ChatCommands.commandMap.put("godmode", (event, args) -> {
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

    ChatCommands.commandMap.put("game", (MessageReceivedEvent event, List<String> args) -> {
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
      builder.withThumbnail(MainBot.cli.getApplicationIconURL());

      RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
    });

    commandMap.put("help", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version),
          true);
      builder.withAuthorName("Jarza Bot Manual!");
      builder.withAuthorIcon(event.getAuthor().getAvatarURL());
      builder.withFooterText("Still under development! uwu");
      builder.withColor(new Color(112, 137, 255));
      for (int x = Util.cmdinfo.length; x > 0; x--) {
        builder.appendField(Util.cmdinfo[Util.cmdinfo.length - x][0],
            Util.cmdinfo[Util.cmdinfo.length - x][1], false);
      }
      RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

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
