package commands.moderation;

import main.MainBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Permission {

  public String command;
  public boolean value;
  public Long guild;
  public Long channel;
  public Long role;

  public Permission(String command, boolean value, Long role, Long channel, Long guild) {
    this.command = command;
    this.value = value;
    this.guild = guild;
    this.channel = channel;
    this.role = role;
  }

  public static List<Permission> toPerms(IMessage m) {
    List<Permission> res = new ArrayList<>();
    ArrayList<String> fields = new ArrayList<>();
    for (String a : m.getFormattedContent().split("\n")) {
      fields.add(a);
    }
    //commandname - allow/deny (1/0) - userid/roleid/channelid or 0
    //say-1-0-0-0
    Long g = Long.parseLong(fields.get(0));
    fields.remove(0);
    for (String a : fields) {
      String[] temp = a.split("-");
      res.add(new Permission(temp[0], (temp[1].equals("1") ? true : false), Long.parseLong(temp[2]), Long.parseLong(temp[3]), g));
    }
    return res;
  }

  public static Permission commandParse(List<String> args, Long guild, MessageReceivedEvent event) {
    Permission p = new Permission("-", false, 0l, 0l, 0l);
    if (!(args.get(1).equals("deny") || args.get(1).equals("allow"))) {
      //      System.out.println("Perm keked");
      //      System.out.println(args.get(0));
      return p;
    }
    //    }

    p.command = args.get(0);
    p.value = args.get(1).equals("allow") ? true : false;
    p.guild = guild;
    for (int i = 2; i < args.size(); i++) {
      if (!event.getMessage().getChannelMentions().isEmpty()) {
        p.channel = event.getMessage().getChannelMentions().get(0).getLongID();
        break;
      }
      if(!event.getMessage().getRoleMentions().isEmpty()) {
        p.role = event.getMessage().getRoleMentions().get(0).getLongID();
        break;
      }

      if (p.channel == 0L)
        if (MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(i)).size() != 0) {
          p.channel = MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(i)).get(0).getLongID();
        }
      if (p.role == 0L)
        if (MainBot.cli.getGuildByID(guild).getRolesByName(args.get(i)).size() != 0) {
          p.role = MainBot.cli.getGuildByID(guild).getRolesByName(args.get(i)).get(0).getLongID();
        }
      if (p.channel == 0L)
        try {
          if (MainBot.cli.getGuildByID(guild).getChannelByID(Long.parseLong(args.get(i))) != null) {
            p.channel = MainBot.cli.getGuildByID(guild).getChannelByID(Long.parseLong(args.get(i))).getLongID();
          }
        } catch (Throwable e) {
        }

      if (p.role == 0L)
        try {
          if (MainBot.cli.getGuildByID(guild).getRoleByID(Long.parseLong(args.get(i))) != null) {
            p.role = MainBot.cli.getGuildByID(guild).getRoleByID(Long.parseLong(args.get(i))).getLongID();
          }
        } catch (Throwable e) {
        }

      if (p.channel == 0L)
        try {
          String temp = args.get(i).substring(2, args.get(i).length() - 1);
          Long templ = Long.parseLong(temp);

          if (MainBot.cli.getGuildByID(guild).getChannelByID(templ) != null) {
            p.channel = MainBot.cli.getGuildByID(guild).getChannelByID(templ).getLongID();
          }
        } catch (Throwable e) {
        }

      if (p.role == 0L)
        try {
          String temp = args.get(i).substring(3, args.get(i).length() - 1);
          Long templ = Long.parseLong(temp);

          if (MainBot.cli.getGuildByID(guild).getRoleByID(templ) != null) {
            p.role = MainBot.cli.getGuildByID(guild).getRoleByID(templ).getLongID();
          }
        } catch (Throwable e) {
        }
    }
    return p;
  }

  public static Permission commandParse(List<String> args, Long guild) {
    Permission p = new Permission("-", false, 0l, 0l, 0l);
    if (!(args.get(1).equals("deny") || args.get(1).equals("allow"))) {
      //      System.out.println("Perm keked");
      //      System.out.println(args.get(0));
      return p;
    }
    //    }

    p.command = args.get(0);
    p.value = args.get(1).equals("allow") ? true : false;
    p.guild = guild;
    for (int i = 2; i < args.size(); i++) {
      if (p.channel == 0L)
        if (MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(i)).size() != 0) {
          p.channel = MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(i)).get(0).getLongID();
        }
      if (p.role == 0L)
        if (MainBot.cli.getGuildByID(guild).getRolesByName(args.get(i)).size() != 0) {
          p.role = MainBot.cli.getGuildByID(guild).getRolesByName(args.get(i)).get(0).getLongID();
        }
      if (p.channel == 0L)
        try {
          if (MainBot.cli.getGuildByID(guild).getChannelByID(Long.parseLong(args.get(i))) != null) {
            p.channel = MainBot.cli.getGuildByID(guild).getChannelByID(Long.parseLong(args.get(i))).getLongID();
          }
        } catch (Throwable e) {
        }

      if (p.role == 0L)
        try {
          if (MainBot.cli.getGuildByID(guild).getRoleByID(Long.parseLong(args.get(i))) != null) {
            p.role = MainBot.cli.getGuildByID(guild).getRoleByID(Long.parseLong(args.get(i))).getLongID();
          }
        } catch (Throwable e) {
        }

      if (p.channel == 0L)
        try {
          String temp = args.get(i).substring(2, args.get(i).length() - 1);
          Long templ = Long.parseLong(temp);

          if (MainBot.cli.getGuildByID(guild).getChannelByID(templ) != null) {
            p.channel = MainBot.cli.getGuildByID(guild).getChannelByID(templ).getLongID();
          }
        } catch (Throwable e) {
        }

      if (p.role == 0L)
        try {
          String temp = args.get(i).substring(3, args.get(i).length() - 1);
          Long templ = Long.parseLong(temp);

          if (MainBot.cli.getGuildByID(guild).getRoleByID(templ) != null) {
            p.role = MainBot.cli.getGuildByID(guild).getRoleByID(templ).getLongID();
          }
        } catch (Throwable e) {
        }
    }
    return p;
  }

  public static String toString(Permission p) {
    String res;
    res = p.command + "-" + (p.value ? "1" : "0") + "-" + (p.role == 0L ? "0" : p.role) + "-" + (p.channel == 0L ? "0" : p.channel);
    //System.out.println(res);
    return res;
  }
}
