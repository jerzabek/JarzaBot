package commands.moderation;

import com.sun.media.jfxmedia.logging.Logger;
import main.MainBot;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Permission {

  public String command;
  public boolean value;
  public Long user;
  public Long guild;
  public Long channel;
  public Long role;

  public Permission(String command, boolean value, Long user, Long role, Long channel, Long guild) {
    this.command = command;
    this.value = value;
    this.user = user;
    this.guild = guild;
    this.channel = channel;
    this.role = role;
  }

  public static List<Permission> toPerms(IMessage m) {
    List<Permission> res = new ArrayList<>();
    ArrayList<String> fields = new ArrayList<>();
    for(String a : m.getFormattedContent().split("\n")){
      fields.add(a);
    }
    //commandname - allow/deny (1/0) - userid/roleid/channelid or 0
    //say-1-0-0-0
    Long g = Long.parseLong(fields.get(0));
    fields.remove(0);
    for(String a : fields){
      String[] temp = a.split("-");
      res.add(new Permission(temp[0], (temp[1].equals("1") ? true : false), Long.parseLong(temp[2]), Long.parseLong(temp[3]), Long.parseLong(temp[4]), g));
    }
    return res;
  }
  //j.rule deny say general
  //j.rule allow say staff general
  //j.rule deny meme jarza
  //j.rule allow meme botcommands
  //j.rule deny satansbae mod bot
  //[deny or allow] [command name] (user default: everyone) (channel default: all) (role default: everyone)
  public static Permission commandParse(List<String> args, Long guild){
    Permission p = new Permission("-", false, 0l, 0l, 0l, 0l);
    if(!(args.get(0).equals("allow") || args.get(0).equals("deny"))){
      System.out.println("Perm keked");
      return p;
    }
    for(String a : args){
      System.out.println(a + " / ");
    }
    if(args.size() == 2){
      p.value = args.get(0).equals("allow") ? true : false;
      p.channel = 0L;
      p.user = 0L;
      p.role = 0L;
      p.guild = guild;
      p.command = args.get(1);
    }else if(args.size() == 3){
//      System.out.println(args.get(0));
      p.value = args.get(0).equals("allow") ? true : false;
      if(MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(2)).size() != 0){
        p.channel =  MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(2)).get(0).getLongID();
      }else{
        p.channel = 0L;
      }

      if(MainBot.cli.getGuildByID(guild).getUsersByName(args.get(2), true).size() != 0){
        p.user =  MainBot.cli.getGuildByID(guild).getUsersByName(args.get(2), true).get(0).getLongID();
      }else{
        p.user = 0L;
      }

      if(MainBot.cli.getGuildByID(guild).getRolesByName(args.get(2)).size() != 0){
        p.role =  MainBot.cli.getGuildByID(guild).getRolesByName(args.get(2)).get(0).getLongID();
      }else{
        p.role = 0L;
      }

      p.guild = guild;
      p.command = args.get(1);
    }else if(args.size() == 4) {
      p.value = args.get(0).equals("allow") ? true : false;
      if(MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(2)).size() != 0){
        p.channel =  MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(2)).get(0).getLongID();
      }else if(MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(3)).size() != 0){
        p.channel =  MainBot.cli.getGuildByID(guild).getChannelsByName(args.get(3)).get(0).getLongID();
      }else{
        p.channel = 0L;
      }

      if(MainBot.cli.getGuildByID(guild).getUsersByName(args.get(2), true).size() != 0){
        p.user =  MainBot.cli.getGuildByID(guild).getUsersByName(args.get(2), true).get(0).getLongID();
      }else if(MainBot.cli.getGuildByID(guild).getUsersByName(args.get(3), true).size() != 0){
        p.user =  MainBot.cli.getGuildByID(guild).getUsersByName(args.get(3), true).get(0).getLongID();
      }else{
        p.user = 0L;
      }

      if(MainBot.cli.getGuildByID(guild).getRolesByName(args.get(2)).size() != 0){
        p.role =  MainBot.cli.getGuildByID(guild).getRolesByName(args.get(2)).get(0).getLongID();
      }else if(MainBot.cli.getGuildByID(guild).getRolesByName(args.get(3)).size() != 0){
        p.role =  MainBot.cli.getGuildByID(guild).getRolesByName(args.get(3)).get(0).getLongID();
      }else{
        p.role = 0L;
      }

      p.guild = guild;
      p.command = args.get(1);
    }else if(args.size() == 5){
      Logger.logMsg(0, "Someone tried to kek me lel");
      return null;
    }
    return p;
  }

  public static String toString(Permission p){
    String res;
    res = p.command + "-" + (p.value ? "1" : "0") + "-" + (p.user == 0L ? "0" : p.user) + "-" + (p.role == 0L ? "0" : p.role) + "-" + (p.channel == 0L ? "0" : p.channel);
    //System.out.println(res);
    return res;
  }
}
