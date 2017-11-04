package main.commands.moderation;

import main.db.DataManager;
import main.exceptions.InvalidWarningException;
import main.MainBot;
import main.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

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
        Util.sendMessage(event,
            "Warned *" + event.getGuild().getUserByID(user).getName() + "#" + event.getGuild().getUserByID(user).getDiscriminator() + "* for *'" + reason + "'*.");
        checkStuff(user, reason, event);
      }else {
        Util.sendMessage(event, "**Error: no punishments set, please do j.warnp *number* *(kick or ban)***");
      }
    }

  }

  public static void warnp(int warnings, Punishments effect, MessageReceivedEvent event) {
    switch (effect) {
      case ban:
        DataManager.setBanp(event.getGuild().getLongID(), warnings);
        Util.sendMessage(event,
            "**>Set punishment for " + warnings + " warnings to ban.**");
        break;
      case kick:
        DataManager.setKickp(event.getGuild().getLongID(), warnings);
        Util.sendMessage(event,
            "**>Set punishment for " + warnings + " warnings to kick.**");
        break;
    }
  }

  public static void clearWarn(int warnId, Long user, MessageReceivedEvent event) {
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
    List<Warning> w = DataManager.getWarns(event.getGuild().getLongID(), user);
    if (warnId > -1) {
      if (w.get(warnId) != null) {
        try {
          DataManager.clearWarns(event.getChannel().getLongID(), user, event.getAuthor().getLongID(), warnId);
        } catch (InvalidWarningException e) {
          e.printStackTrace();
        }
        Util.sendMessage(event,
            "**>Removed *" + event.getGuild().getUserByID(user).getName() + "'s* warning.**");

      } else {
        Util.sendMessage(event, "**>Error: warning no existo!**");
      }

      // if(warnings.get(user).get(warnId) != null) {
      // warnings.get(user).remove(warnId);
      // Util.sendMessage(event.getChannel(), "**>Removed *" +
      // event.getGuild().getUserByID(user).getName() + "'s* warning.**");
      // }else {
      // Util.sendMessage(event.getChannel(), "**>Error: warning no existo!**");
      // }
    } else if (warnId == -1) {
      int cl = 0;
      for(int i = 0; i < w.size(); i++){
        try {
          if(!w.get(i).cleared) {
            DataManager.clearWarns(event.getGuild().getLongID(), w.get(i).victim, event.getAuthor().getLongID(), i);
            cl++;
          }
        } catch (InvalidWarningException e) {
          e.printStackTrace();
        }
      }
//      Util.sendMessage(event.getChannel(), "Removing all warnings is currently disabled :/");
      if(cl == w.size() && cl != 0)
        Util.sendMessage(event, "**>Removed all of *" + event.getGuild().getUserByID(user).getName() + "'s* warnings.**");
      else if(cl == 0)
        Util.sendMessage(event, "**>All warnings allready cleared.**");
      else
        Util.sendMessage(event, "**>Removed " + cl + " warning(s) for *" + event.getGuild().getUserByID(user).getName() + "*.**");
    } else {
      Util.sendMessage(event, "**>Error: bad warning!**");
    }
    // }else {
    // Util.sendMessage(event, "**>Error: user has no warnings!**");
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
      Util.sendMessage(event, builder.build());
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
      Util.sendMessage(event, builder.build());
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
      Util.sendMessage(event, m);
    }
    

  }

  public static void setWarnRole(IRole role, MessageReceivedEvent event) {
    boolean authorMod = false;

    if(!DataManager.getModrole(event.getGuild().getLongID()).isEmpty()){
      for(Long l : DataManager.getModrole(event.getGuild().getLongID())){
        if(event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(l)))
          authorMod = true;
      }
    }else{
      Util.sendMessage(event,
          "**>Error: bot editing permission has not been set up. Please run j.modr roleName**");
      return;
    }

    if (authorMod) {
      if(!DataManager.getWarnrole(event.getGuild().getLongID()).contains(role.getLongID())) {
        DataManager.setWarnrole(event.getGuild().getLongID(), role.getLongID());
        Util.sendMessage(event, "**>Set warning role to: *" + role.getName() + "*.**");
      }else{
        DataManager.removeWarnr(event.getGuild().getLongID(), role.getLongID());
        Util.sendMessage(event, "**>Removed warning role: *" + role.getName() + "*.**");
      }
    } else {
      Util.sendMessage(event,
          "**>Error: you dont have permission to edit the bot settings.**");
    }
  }

  public static void setModRole(IRole role, MessageReceivedEvent event) {
    boolean authorMod = false;

    if(!DataManager.getModrole(event.getGuild().getLongID()).isEmpty()){
      for(Long l : DataManager.getModrole(event.getGuild().getLongID())){
        if(event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(l)))
          authorMod = true;
      }
    }else{
      authorMod = true;
    }

    if (authorMod) {
      if(!DataManager.getModrole(event.getGuild().getLongID()).contains(role.getLongID())) {
        DataManager.setModrole(event.getGuild().getLongID(), role.getLongID());
        Util.sendMessage(event, "**>Added bot moderator role: *" + role.getName() + "*.**");
      }else{
        DataManager.removeModr(event.getGuild().getLongID(), role.getLongID());
        Util.sendMessage(event, "**>Removed bot moderator role: *" + role.getName() + "*.**");
      }
    } else {
      Util.sendMessage(event,
          "**>Error: you dont have permission to edit the bot settings.**");
    }
  }

  private static void checkStuff(Long user, String reason, MessageReceivedEvent event) {
    List<Warning> warnings = DataManager.getWarns(event.getGuild().getLongID(), user);
    warnings.removeIf(a -> a.cleared);
    if (DataManager.getBanp(event.getGuild().getLongID()) != -1
        && warnings.size() >= DataManager.getBanp(event.getGuild().getLongID())) {
      Util.sendMessage(event, "**Banned *" + event.getGuild().getUserByID(user)
          + "* because they reached the warning limit.**");
      event.getGuild().banUser(event.getGuild().getUserByID(user), "Banned '" + event.getGuild().getUserByID(user)
        + "' because they reached the warning limit.");
    } else if (DataManager.getKickp(event.getGuild().getLongID()) != -1
        && warnings.size() >= DataManager.getKickp(event.getGuild().getLongID())) {
      Util.sendMessage(event, "**Kicked *" + event.getGuild().getUserByID(user)
          + "* because they reached the warning limit.**");
      event.getGuild().kickUser(event.getGuild().getUserByID(user), "Kicked '" + event.getGuild().getUserByID(user)
        + "' because they reached the warning limit.");
    }
  }

  public static boolean hasPermission(String command, IUser user, Long guildid, Long channelid){
    boolean has = true;
    for(Permission p : DataManager.getPerms(guildid)){
      if(p.command.equals(command) || (Util.catnames.contains(p.command) && Util.cats.get(p.command).containsKey(command))) {
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
