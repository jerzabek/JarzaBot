package commands.moderation;

import commands.ChatCommands;
import db.DataManager;
import main.MainBot;
import main.Util;
import org.eclipse.jetty.client.api.Request;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ModerationCL {
  
  public static void init() {
    ChatCommands.commandMap.put("warnp", (event, args) -> {
      Punishments p;
      switch (args.get(1)) {
        case "kick":
          p = Punishments.kick;
          break;
        case "ban":
          p = Punishments.ban;
          break;
        default:
          Util.sendMessage(event.getChannel(), "Error: 'kick' or 'ban' only.");
          return;
      }
      Moderation.warnp(Integer.parseInt(args.get(0)), p, event);
    });

    ChatCommands.commandMap.put("warn", (event, args) -> {
      if (DataManager.getWarnrole(event.getGuild().getLongID()).size() == 1 && DataManager.getWarnrole(event.getGuild().getLongID()).get(0) > -1) {
        boolean cont = false;
        for(Long a : DataManager.getWarnrole(event.getGuild().getLongID())){
          if (event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(a))){
            cont = true;
          }
        }
        if (!cont) {
          Util.sendMessage(event.getChannel(), "**>Error: inssuficient permission**");
          return;
        }
      } else {
        Util.sendMessage(event.getChannel(), "**>Error: warning role not set. Please run j.warnr roleName**");
        return;
      }

      String text = "";
      // System.out.println(Util.userToID(args.get(0)));
      Long id = event.getGuild().getUserByID(Util.userToID(args.get(0))).getLongID();
      // Long id = 0L;
      args.remove(0);
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
//       System.out.println("id: " + id + " res: " + text);
      Moderation.warn(id, text, event);
    });

    ChatCommands.commandMap.put("warnc", (event, args) -> {
      // if (Database.loadSettings(event).warnrole > -1) {
      // System.out.println("warnrole: " + Database.loadSettings(event).warnrole);
      // if (!event.getAuthor().getRolesForGuild(event.getGuild())
      // .contains(event.getGuild().getRoleByID(Database.loadSettings(event).warnrole))) {
      // // for (IRole a : event.getAuthor().getRolesForGuild(event.getGuild())) {
      // // System.out.println(a.getName() + " < warnrole name loop");
      // // }
      // Util.sendMessage(event.getChannel(), "**>Error: inssuficient permission**");
      // return;
      // }
      // } else {
      // Util.sendMessage(event.getChannel(), "**>Error: warning role not set**");
      // return;
      // }

      Long id = event.getGuild().getUserByID(Util.userToID(args.get(0))).getLongID();

      if (args.size() > 1) {
        Moderation.clearWarn(Integer.parseInt(args.get(1)) - 1, id, event);
      } else {
        Moderation.clearWarn(-1, id, event);
      }


    });

    ChatCommands.commandMap.put("warnl", (event, args) -> {
      String mode = "a";
      Long id = 0L;
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
      if (args.size() == 1) {
        id = event.getGuild().getUserByID(Util.userToID(args.get(0))).getLongID();
        mode = "b";
      }

      Moderation.listWarns(id, mode, event);

    });

    ChatCommands.commandMap.put("modr", (event, args) -> {
      String role = args.get(0);
      IRole rol = event.getGuild().getRoles().get(0);
      boolean f = false;
      Long temp = 0L;
      try{
        temp = Long.parseLong(args.get(0));
      }catch(NumberFormatException e){
        e.printStackTrace();
      }

      for (IRole r : event.getGuild().getRoles()) {
        if (r.getName().equals(role) || r.getLongID() == temp) {
          rol = r;
          f = true;
        }
      }

      if (rol == event.getGuild().getRoles().get(0) && !f) {
        Util.sendMessage(event.getChannel(), "**>Error: bad role u dipshite**");
        return;
      }
      Moderation.setModRole(rol, event);
    });

    ChatCommands.commandMap.put("warnr", (event, args) -> {
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
      if(args.isEmpty())
        return;
      String role = args.get(0);
      IRole rol = event.getGuild().getRoles().get(0);
      boolean f = false;
      Long temp = 0L;
      try{
        temp = Long.parseLong(args.get(0));
      }catch(NumberFormatException e){
        e.printStackTrace();
      }
      for (IRole r : event.getGuild().getRoles()) {
        if (r.getName().equals(role) || r.getLongID() == temp) {
          rol = r;
          f = true;
        }
      }

      if (rol == event.getGuild().getRoles().get(0) && !f) {
        Util.sendMessage(event.getChannel(), "**>Error: bad role u dipshite**");
        return;
      }
      Moderation.setWarnRole(rol, event);
    });

    ChatCommands.commandMap.put("backuppins", (event, args) -> {
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
      if(args.size() > 0) {
        if (event.getGuild().getChannelsByName(args.get(0)).size() == 0) {
          Util.sendMessage(event.getChannel(), "*Error: Invalid channel*");
        }else{
          DataManager.setPinbu(event.getGuild().getLongID(), event.getGuild().getChannelsByName(args.get(0)).get(0).getLongID());
          Util.sendMessage(event.getChannel(), "*Success!*");
        }
      }else{
        if(DataManager.getPinbu(event.getGuild().getLongID()).equals(0L)){
          IChannel ps = event.getGuild().createChannel("pins");
          if(event.getGuild().getChannelsByName("pins").size() == 0){
            ps.overrideRolePermissions(event.getGuild().getEveryoneRole(), null, EnumSet.of(Permissions.SEND_MESSAGES));
            ps.overrideUserPermissions(MainBot.cli.getOurUser(), EnumSet.of(Permissions.SEND_MESSAGES), null);
            DataManager.setPinbu(event.getGuild().getLongID(), ps.getLongID());
          }else{
            DataManager.setPinbu(event.getGuild().getLongID(), ps.getLongID());
          }
          Util.sendMessage(event.getChannel(), "*Success! Pins will be stored in " + ps.getName() + "*");
        }else{
          DataManager.setPinbu(event.getGuild().getLongID(), 0l);
          Util.sendMessage(event.getChannel(), "*Success! Pins will no longer be backed up.");
        }
      }
    });

    ChatCommands.commandMap.put("roleids", (event, args) -> {
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
      String s = "";
      for(IRole r : event.getGuild().getRoles()){
        if(!r.isEveryoneRole())
          s += "@" + r.getName() + " - " + r.getLongID() + "\n";
      }
      String finalS = s;
      RequestBuffer.request(() -> {
        event.getChannel().sendMessage(finalS);
      });
    });

    ChatCommands.commandMap.put("ruler", (event, args) -> {
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
      EmbedBuilder builder = new EmbedBuilder();
      builder.withFooterText("Permissions for this guild");
      if(args.isEmpty()){
        int id = 0;
        for(Permission p : DataManager.getPerms(event.getGuild().getLongID())){
          String stuff = p.command + " is " + (p.value ? "allowed " : "denied ") +
            (p.channel == 0L ? " everywhere" : " in " + event.getGuild().getChannelByID(p.channel))
            + " for " + (p.role == 0L ? "everyone" : "@" + event.getGuild().getRoleByID(p.role).getName());
          builder.appendField("#" + id, stuff, false);
          id++;
        }

        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
      }else if(args.size() == 2){
        List<Permission> ls = new ArrayList<>();
        DataManager.getPerms(event.getGuild().getLongID()).forEach(ls::add);
        int t, tt;
        try{
          t = Integer.parseInt(args.get(1));
        }catch(NumberFormatException e){
          e.printStackTrace();
          return;
        }

        if(args.get(0).toLowerCase().equals("e")){
          ls.remove(t);
          DataManager.setPermissions(ls, event.getGuild().getLongID());
        }else{
          try{
            tt = Integer.parseInt(args.get(0));
          }catch(NumberFormatException e){
            e.printStackTrace();
            return;
          }
          if(tt >= t)
            return;

          Permission temprem = null;
          List<Permission> nls = new ArrayList<>();
          for(int i = 0; i < DataManager.getPerms(event.getGuild().getLongID()).size(); i++) {
            if(i == t) {
              temprem = ls.get(i);
              continue;
            }else if (i == tt){
              nls.add(temprem);
            }else {
              nls.add(ls.get(i));
            }
          }
          DataManager.setPermissions(nls, event.getGuild().getLongID());
        }
      }
    });
  }
  
}
