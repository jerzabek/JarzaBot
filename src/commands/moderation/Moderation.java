package commands.moderation;

import dataStore.DataStore;
import main.MainBot;
import main.Util;
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
      if(DataStore.getKickp(event.getGuild().getLongID()) > 0 || DataStore.getBanp(event.getGuild().getLongID()) > 0){
        DataStore.warn(new Warning(event.getAuthor().getLongID(), user, event.getGuild().getLongID(), reason, false, 0L));
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
        DataStore.setBanp(event.getGuild().getLongID(), warnings);
        Util.sendMessage(event.getChannel(),
            "**>Set punishment for " + warnings + " warnings to ban.**");
        break;
      case kick:
        DataStore.setKickp(event.getGuild().getLongID(), warnings);
        Util.sendMessage(event.getChannel(),
            "**>Set punishment for " + warnings + " warnings to kick.**");
        break;
    }
  }

  public static void clearWarn(int warnId, Long user, MessageReceivedEvent event) {
    List<Warning> w = (warnId == -1 ?
        DataStore.getWarns(event.getGuild().getLongID()) :
        DataStore.getWarns(event.getGuild().getLongID(), user));
    if (warnId > -1) {
      if (w.get(warnId) != null) {
        DataStore.clearWarns(w.get(warnId).msgid);
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
        DataStore.clearWarns(wt.msgid);
      }
      Util.sendMessage(event.getChannel(),
          "**>Removed all of *" + event.getGuild().getUserByID(user).getName() + "'s* warnings.**");
    } else {
      Util.sendMessage(event.getChannel(), "**>Error: bad warning!**");
    }
    // }else {
    // Util.sendMessage(event.getChannel(), "**>Error: user has no warnings!**");
    // }
  }

  public static void listWarns(Long user, String mode, MessageReceivedEvent event) {
    List<Warning> group = (mode.equals("a") ?
        DataStore.getWarns(event.getGuild().getLongID()) :
        DataStore.getWarns(event.getGuild().getLongID(), user));

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
              "Warning for: " + event.getGuild().getUserByID(a.victim).getName() + " - by *"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "*",
              false);
        } else {
          builder.appendField("**~~'" + a.reason + "'~~**",
              "Warning for: " + event.getGuild().getUserByID(a.victim).getName() + " - by *~~"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "~~* (cleared)",
              false);
        }
      }
      RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
    } else {
      String m = "";
      for (Warning a : group) {
        if (!a.cleared) {
          m += "**'" + a.reason + "'**\n^Warning for: " + event.getGuild().getUserByID(a.victim).getName() + " - by *"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "*\n";
        } else {
          m += "**~~'" + a.reason + "'~~**\n^Warning for: " + event.getGuild().getUserByID(a.victim).getName() + " - by *~~"
                  + event.getGuild().getUserByID(a.user).getName() + "#"
                  + event.getGuild().getUserByID(a.user).getDiscriminator() + "~~* (cleared)\n";
        }
      }
      Util.sendMessage(event.getChannel(), m);
    }
    

  }

  public static void setWarnRole(IRole role, MessageReceivedEvent event) {
    boolean authorMod = false;

    if(!DataStore.getModrole(event.getGuild().getLongID()).equals(-1L)){
      authorMod = event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(DataStore.getModrole(event.getGuild().getLongID())));
      for(IRole a : event.getAuthor().getRolesForGuild(event.getGuild())){
        System.out.println(a.getLongID());
      }
      System.out.println(DataStore.getModrole(event.getGuild().getLongID()));
    }else{
      Util.sendMessage(event.getChannel(),
          "**>Error: bot editing permission has not been set up. Please run j.modr roleName**");
      authorMod = true;
//      return;
    }

    if (authorMod) {
      DataStore.setWarnrole(event.getGuild().getLongID(), role.getLongID());
      Util.sendMessage(event.getChannel(), "**>Set warning role to: *" + role.getName() + "*.**");
    } else {
      Util.sendMessage(event.getChannel(),
          "**>Error: you dont have permission to edit the bot settings.**");
    }
  }

  public static void setModRole(IRole role, MessageReceivedEvent event) {
    boolean authorMod;

    if(!DataStore.getModrole(event.getGuild().getLongID()).equals(-1L)){
      authorMod = event.getAuthor().getRolesForGuild(event.getGuild()) .contains(DataStore.getModrole(event.getGuild().getLongID()));
    }else{
      authorMod = true;
    }

    if (authorMod) {
      DataStore.setModrole(event.getGuild().getLongID(), role.getLongID());
      Util.sendMessage(event.getChannel(), "**>Set bot moderator role to: *" + role.getName() + "*.**");
    } else {
      Util.sendMessage(event.getChannel(),
          "**>Error: you dont have permission to edit the bot settings.**");
    }
  }

  private static void checkStuff(Long user, String reason, MessageReceivedEvent event) {
    List<Warning> warnings = DataStore.getWarns(event.getGuild().getLongID(), user);
    warnings.removeIf(a -> a.cleared);
    if (DataStore.getBanp(event.getGuild().getLongID()) != -1
        && warnings.size() >= DataStore.getBanp(event.getGuild().getLongID())) {
      Util.sendMessage(event.getChannel(), "**Banned *" + event.getGuild().getUserByID(user)
          + "* because they reached the warning limit.**");
      event.getGuild().banUser(event.getGuild().getUserByID(user), reason);
    } else if (DataStore.getKickp(event.getGuild().getLongID()) != -1
        && warnings.size() >= DataStore.getKickp(event.getGuild().getLongID())) {
      Util.sendMessage(event.getChannel(), "**Kicked *" + event.getGuild().getUserByID(user)
          + "* because they reached the warning limit.**");
      event.getGuild().kickUser(event.getGuild().getUserByID(user), reason);
    }
  }

  public static boolean hasPermission(String command, IUser user, Long guildid, Long channelid){
    boolean has = true;
    for(Permission p : DataStore.getPerms(guildid)){
//      has = p.value;
      if(p.command.equals(command)) {
        if(p.channel == 0L || p.channel.equals(channelid)){
          has = p.value;
          if(p.role == 0L || user.getRolesForGuild(MainBot.cli.getGuildByID(guildid)).contains(MainBot.cli.getRoleByID(p.role))){
            has = p.value;
            if(p.user == 0L || p.user.equals(user.getLongID())){
              System.out.println("3");
              has = p.value;
            }
            System.out.println("2");
          }
          System.out.println("1");
        }



      }
    }
    return has;
  }

  public static void setPermission(IMessage m){
    DataStore.setPermission(Permission.toPerms(m).get(1), m.getGuild().getLongID());
  }
}