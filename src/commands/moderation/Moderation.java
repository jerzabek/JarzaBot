package commands.moderation;

import db.DataManager;
import exceptions.InvalidWarningException;
import main.MainBot;
import main.Util;
import sun.applet.Main;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.util.List;

public class Moderation {

  public static void warn(Long user, String reason, MessageReceivedEvent event) {
     if (MainBot.cli.getUserByID(user) != null) {
      if(DataManager.getKickp(event.getGuild().getLongID()) > 0 || DataManager.getBanp(event.getGuild().getLongID()) > 0){
        try {
          DataManager.warn(new Warning(event.getAuthor().getLongID(), user, event.getGuild().getLongID(), reason, false, 0L, Util.toTimeStamp(event.getMessage().getTimestamp())));
        } catch (InvalidWarningException e) {
          e.printStackTrace();
        }
        Util.sendMessage(event.getChannel(),
            "Warned *" + event.getGuild().getUserByID(user).getName() + "#" + event.getGuild().getUserByID(user).getDiscriminator() + "* for *'" + reason + "'*.");
        checkStuff(user, reason, event);
      }else {
        Util.sendMessage(event.getChannel(), "**Error: no punishments set, please do j.warnp *number* *(kick or ban)***");
      }
    }

  }

  public static void warnp(int warnings, Punishments effect, MessageReceivedEvent event) {
    switch (effect) {
      case ban:
        DataManager.setBanp(event.getGuild().getLongID(), warnings);
        Util.sendMessage(event.getChannel(),
            "**>Set punishment for " + warnings + " warnings to ban.**");
        break;
      case kick:
        DataManager.setKickp(event.getGuild().getLongID(), warnings);
        Util.sendMessage(event.getChannel(),
            "**>Set punishment for " + warnings + " warnings to kick.**");
        break;
    }
  }

  public static void clearWarn(int warnId, Long user, MessageReceivedEvent event) {
    List<Warning> w = (warnId == -1 ?
      DataManager.getWarns(event.getGuild().getLongID()) :
      DataManager.getWarns(event.getGuild().getLongID(), user));
    if (warnId > -1) {
      if (w.get(warnId) != null) {
        try {
          DataManager.clearWarns(event.getChannel().getLongID(), user, event.getAuthor().getLongID(), warnId);
        } catch (InvalidWarningException e) {
          e.printStackTrace();
        }
        Util.sendMessage(event.getChannel(),
            "**>Removed *" + event.getGuild().getUserByID(user).getName() + "'s* warning.**");

      } else {
        Util.sendMessage(event.getChannel(), "**>Error: warning no existo!**");
      }

      // if(warnings.get(user).get(warnId) != null) {
      // warnings.get(user).remove(warnId);
      // Util.sendMessage(event.getChannel(), "**>Removed *" +
      // event.getGuild().getUserByID(user).getName() + "'s* warning.**");
      // }else {
      // Util.sendMessage(event.getChannel(), "**>Error: warning no existo!**");
      // }
    } else if (warnId == -1) {
      for(Warning wt : w){
//        DataManager.clearWarns(wt.msgid);

      }
      Util.sendMessage(event.getChannel(), "Removing all warnings is currently disabled :/");
//      Util.sendMessage(event.getChannel(), "**>Removed all of *" + event.getGuild().getUserByID(user).getName() + "'s* warnings.**");
    } else {
      Util.sendMessage(event.getChannel(), "**>Error: bad warning!**");
    }
    // }else {
    // Util.sendMessage(event.getChannel(), "**>Error: user has no warnings!**");
    // }
  }

  public static void listWarns(Long user, String mode, MessageReceivedEvent event) {
    List<Warning> group = (mode.equals("a") ?
        DataManager.getWarns(event.getGuild().getLongID()) :
      DataManager.getWarns(event.getGuild().getLongID(), user));

    // TODO: give ya the option to switch

    EmbedBuilder builder = new EmbedBuilder();
    builder.withColor(150, 150, 255);
    if (group.size() == 0) {
      builder.appendField("No warnings available",
          "¯\\_(ツ)_/¯", false);
      RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
    } else if (group.size() < 26) {
      for (Warning a : group) {
        if (!a.cleared) {
          builder.appendField("**'" + a.reason + "'**",
              "Warning for: " + event.getGuild().getUserByID(a.victim).getName() + "#" + event.getGuild().getUserByID(a.victim).getDiscriminator() + " - by *"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "*",
              false);
        } else {
          builder.appendField("**~~'" + a.reason + "'~~**",
              "Warning for: " + event.getGuild().getUserByID(a.victim).getName() + "#" + event.getGuild().getUserByID(a.victim).getDiscriminator() + " - by *~~"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "~~* (cleared by "
                + event.getGuild().getUserByID(a.clearedby).getName() + "#" + event.getGuild().getUserByID(a.clearedby).getDiscriminator() + ")",
              false);
        }
      }
      RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
    } else {
      String m = "";
      for (Warning a : group) {
        if (!a.cleared) {
          m += "**'" + a.reason + "'**\n^Warning for: " + event.getGuild().getUserByID(a.victim).getName() + "#" + event.getGuild().getUserByID(a.victim).getDiscriminator() + " - by *"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "*\n";
        } else {
          m += "**~~'" + a.reason + "'~~**\n^Warning for: " + event.getGuild().getUserByID(a.victim).getName() + "#" + event.getGuild().getUserByID(a.victim).getDiscriminator() + " - by *~~"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "~~* (cleared by " + event.getGuild().getUserByID(a.clearedby).getName() + "#" + event.getGuild().getUserByID(a.clearedby).getDiscriminator() + ")\n";
        }
      }
      Util.sendMessage(event.getChannel(), m);
    }
    

  }

  public static void setWarnRole(IRole role, MessageReceivedEvent event) {
    boolean authorMod = false;

    if(!DataManager.getModrole(event.getGuild().getLongID()).isEmpty()){
      for(Long l : DataManager.getModrole(event.getGuild().getLongID())){
        if(event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(l)))
          authorMod = true;
      }

//      for(IRole a : event.getAuthor().getRolesForGuild(event.getGuild())){
//        //System.out.println(a.getLongID());
//      }
      //System.out.println(DataManager.getModrole(event.getGuild().getLongID()));
    }else{
      Util.sendMessage(event.getChannel(),
          "**>Error: bot editing permission has not been set up. Please run j.modr roleName**");
      return;
    }

    if (authorMod) {
      DataManager.setWarnrole(event.getGuild().getLongID(), role.getLongID());
      Util.sendMessage(event.getChannel(), "**>Set warning role to: *" + role.getName() + "*.**");
    } else {
      Util.sendMessage(event.getChannel(),
          "**>Error: you dont have permission to edit the bot settings.**");
    }
  }

  public static void setModRole(IRole role, MessageReceivedEvent event) {
    boolean authorMod = false;

    if(!DataManager.getModrole(event.getGuild().getLongID()).isEmpty()){
//      for(Long x : DataManager.getModrole(event.getGuild().getLongID())){
//        for(IRole y : event.getAuthor().getRolesForGuild(event.getGuild())){
//          if(x.equals(y.getLongID())){
//            authorMod = true;
//            break;
//          }
//        }
//      }

      for(Long l : DataManager.getModrole(event.getGuild().getLongID())){
        if(event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(l)))
          authorMod = true;
      }
    }else{
      authorMod = true;
    }

    if (authorMod) {
      DataManager.setModrole(event.getGuild().getLongID(), role.getLongID());
      Util.sendMessage(event.getChannel(), "**>Set bot moderator role to: *" + role.getName() + "*.**");
    } else {
      Util.sendMessage(event.getChannel(),
          "**>Error: you dont have permission to edit the bot settings.**");
    }
  }

  private static void checkStuff(Long user, String reason, MessageReceivedEvent event) {
    List<Warning> warnings = DataManager.getWarns(event.getGuild().getLongID(), user);
    warnings.removeIf(a -> a.cleared);
    if (DataManager.getBanp(event.getGuild().getLongID()) != -1
        && warnings.size() >= DataManager.getBanp(event.getGuild().getLongID())) {
      Util.sendMessage(event.getChannel(), "**Banned *" + event.getGuild().getUserByID(user)
          + "* because they reached the warning limit.**");
      event.getGuild().banUser(event.getGuild().getUserByID(user), "Banned '" + event.getGuild().getUserByID(user)
        + "' because they reached the warning limit.");
    } else if (DataManager.getKickp(event.getGuild().getLongID()) != -1
        && warnings.size() >= DataManager.getKickp(event.getGuild().getLongID())) {
      Util.sendMessage(event.getChannel(), "**Kicked *" + event.getGuild().getUserByID(user)
          + "* because they reached the warning limit.**");
      event.getGuild().kickUser(event.getGuild().getUserByID(user), "Kicked '" + event.getGuild().getUserByID(user)
        + "' because they reached the warning limit.");
    }
  }

  public static boolean hasPermission(String command, IUser user, Long guildid, Long channelid){
    boolean has = true;
    for(Permission p : DataManager.getPerms(guildid)){
      if(p.command.equals(command)) {
        if (p.channel != 0 || p.role != 0){
          if(p.role == 0 || user.getRolesForGuild(MainBot.cli.getGuildByID(guildid)).contains(MainBot.cli.getRoleByID(p.role))){
            if(p.channel == 0 || p.channel.equals(channelid)){
              has = p.value;
            }
          }
        }else{
          has = p.value;
        }
      }
    }
    return has;
  }
  @Deprecated
  public static void setPermission(IMessage m){
    DataManager.setPermission(Permission.toPerms(m).get(1), m.getGuild().getLongID());
  }
}
