package db;

import commands.moderation.Permission;
import commands.moderation.Warning;
import main.MainBot;
import main.Util;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/*
 *
 * W.I.P.
 * Storage will go 
 *
 */
public class DataManager {

  private final static String SETTINGS = "settings.json", MEMES = "memes.json", PERMS = "perms.json";
  private static JSONObject settings, perms;
  private static JSONArray memes;

  public static void init(){
    try {
      settings = (JSONObject) (new JSONParser().parse(new FileReader(SETTINGS)));
      memes = (JSONArray) (new JSONParser().parse(new FileReader(MEMES)));
//      perms = (JSONObject) (new JSONParser().parse(new FileReader(SETTINGS)));
    }catch(Throwable e){
      e.printStackTrace();
    }
  }

  public static void saveMeme(Long userid, Long guildid, String text, String timestamp, String... attachments) {
//    RequestBuffer.request(() -> {
//      try {
//        MainBot.cli.getChannelByID(memesdb).sendMessage(userid + "\n" + guildid + "\n" + text + " ");
//
//      } catch (DiscordException e) {
//        System.err.println("Hmmm shit went sideways... Here's why: ");
//        e.printStackTrace();
//      }
//    });
//    m = c.getFullMessageHistory();
    JSONObject obj = new JSONObject();
    obj.put("author", userid);
    obj.put("guild", guildid);
    obj.put("content", text);
    obj.put("timestamp", timestamp);
    JSONArray att = new JSONArray();
    if(attachments.length > 0){
      for(String a : attachments){
        att.add(att.size(), a);
      }
    }
    obj.put("attachments", att);
    memes.add(memes.size()-1, obj);
  }

  public static IMessage getMeme(Long guildid) {
    m = c.getFullMessageHistory();
    List<IMessage> tempm = new ArrayList<>();
    for (IMessage mm : m) {
      if (mm.getFormattedContent().contains(guildid + "")) {
        tempm.add(mm);
      }
    }
    IMessage meme = tempm.get(new Random().nextInt(tempm.size()));
    return meme;
  }

  public static IMessage getMemes(Long userid, Long guildid) {
    List<IMessage> tempm = new ArrayList<>();
    for (IMessage mm : m) {
      if (mm.getFormattedContent().contains(userid + "\n" + guildid + "\n") || mm.getFormattedContent().contains(userid + "\n" + guildid + "\n ")) {
        tempm.add(mm);
      }
    }
    IMessage meme = null;
    if (!tempm.isEmpty()) {
      meme = tempm.get(new Random().nextInt(tempm.size()));
    }
    return meme;
  }

  public static int getKickp(Long guildid) {
    IMessage tempm = null;
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    int res = -1;
    if (tempm != null) {
      String[] settings = tempm.getFormattedContent().split("\n");
      res = Integer.parseInt(settings[1]);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n-1\n-1\n-1");
    }
    return res;
  }

  public static void setKickp(Long guildid, int p) {
    IMessage tempm = null;
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    if (tempm != null) {
      String[] sets = tempm.getFormattedContent().split("\n");
      sets[1] = "" + p;
      String fin = "";
      for (String a : sets) {
        fin += a + "\n";
      }
      tempm.edit(fin);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n" + p + "\n-1\n-1\n-1\n-1");
    }
    ms = cs.getFullMessageHistory();
  }

  public static int getBanp(Long guildid) {
    IMessage tempm = null;
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    int res = -1;
    if (tempm != null) {
      String[] settings = tempm.getFormattedContent().split("\n");
      res = Integer.parseInt(settings[2]);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n-1\n-1\n-1");
    }

    return res;
  }

  public static void setBanp(Long guildid, int p) {
    IMessage tempm = null;
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    if (tempm != null) {
      String[] sets = tempm.getFormattedContent().split("\n");
      sets[2] = "" + p;
      String fin = "";
      for (String a : sets) {
        fin += a + "\n";
      }

      tempm.edit(fin);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n" + p + " \n-1\n-1\n-1");
    }
    ms = cs.getFullMessageHistory();
  }

  public static Long getModrole(Long guildid) {
    IMessage tempm = null;
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    Long res = -1L;
    if (tempm != null) {
      String[] settings = tempm.getFormattedContent().split("\n");
      res = Long.parseLong(settings[3].replace(" ", ""));
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n-1\n-1\n-1");
    }

    return res;
  }

  public static void setModrole(Long guildid, Long id) {
    IMessage tempm = null;
    ms = cs.getFullMessageHistory();
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    if (tempm != null) {
      String[] sets = tempm.getFormattedContent().split("\n");
      sets[3] = "" + id;
      String fin = "";
      for (String a : sets) {
        fin += a + "\n";
      }
      tempm.edit(fin);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n" + id + " \n-1\n-1");
    }
  }

  public static Long getWarnrole(Long guildid) {
    IMessage tempm = null;
    ms = cs.getFullMessageHistory();
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }

    }
    Long res = -1L;
    if (tempm != null) {
      String[] settings = tempm.getFormattedContent().split("\n");
      res = Long.parseLong(settings[4]);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n-1\n-1\n-1");
    }

    return res;
  }

  public static void setWarnrole(Long guildid, Long id) {
    IMessage tempm = null;
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    if (tempm != null) {
      String[] sets = tempm.getFormattedContent().split("\n");
      sets[4] = "" + id;
      String fin = "";
      for (String a : sets) {
        fin += a + "\n";
      }
      tempm.edit(fin);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n-1\n-1\n" + id);
    }
    ms = cs.getFullMessageHistory();
  }

  public static void warn(Warning w) {
    String warning = w.user + "\n" + w.victim + "\n" + w.guild + "\n" + w.reason + "\n" + "0\n";
    IMessage ret = cw.sendMessage(warning);
    ret.edit(warning + ret.getLongID());
    mw = cw.getFullMessageHistory();
  }

  public static List<Warning> getWarns(Long guildid) {
    mw = cw.getFullMessageHistory();
    List<Warning> warn = new ArrayList<>();
    for (IMessage m : mw) {
      if (m.getFormattedContent().contains("\n" + guildid + "\n")) {
        warn.add(Warning.toWarning(m));
      }
    }
    return warn;
  }

  public static List<Warning> getWarns(Long guildid, Long userid) {
    List<Warning> warn = new ArrayList<>();
    for (IMessage m : mw) {
      if (m.getFormattedContent().contains("\n" + guildid + "\n") && m.getFormattedContent().contains(userid + "\n")) {
        warn.add(Warning.toWarning(m));
      }
    }
    return warn;
  }

  public static void clearWarns(Long msgid) {
    IMessage i = MainBot.cli.getMessageByID(msgid);
    String[] fin = i.getFormattedContent().split("\n");
    fin[4] = "1";
    String res = "";
    for (String f : fin) {
      res += f + "\n";
    }
    i.edit(res);
  }

  public static List<Permission> getPerms(Long guild){
    List<Permission> res = new ArrayList<>();

    try {
      mp = cp.getFullMessageHistory();
    }catch(Throwable e){
      String msg;
      msg = guild + "\n" + "default-0-0-0-0";
      final IMessage[] ms = new IMessage[1];
      RequestBuffer.request(() -> ms[0] = cp.sendMessage(msg));
      res = Permission.toPerms(ms[0]);
      e.printStackTrace();
    }
    for(IMessage m : mp){
      if (m.getFormattedContent().contains(guild + "\n")){
        //System.out.println(m.getFormattedContent());
        res = Permission.toPerms(m);
      }
    }

    return res;
  }

  public static void setPermission(Permission p, Long guild){
    IMessage gperms = null;
    for(IMessage m : mp) {
      if (m.getFormattedContent().contains(guild + "\n")) {
        gperms = m;
        break;
      }
    }
    if(gperms == null){
      String msg;
      msg = guild + "\n" + "default-0-0-0-0" + "\n" + Permission.toString(p);
      final IMessage[] ms = new IMessage[1];
      RequestBuffer.request(() -> ms[0] = cp.sendMessage(msg));
      gperms = ms[0];
    }
    //    String full = "";
    //    String[] splits = gperms.getFormattedContent().split("\n");
    //    if(gperms.getFormattedContent().contains(p.command)){
    //      for(String a : splits){
    //        if(a.startsWith(p.command) && ((!a.split("-")[4].equals("0") && !p.channel.equals("0")) || (!a.split("-")[3].equals("0")
    //          && !p.role.equals("0")) || (!a.split("-")[2].equals("0") && !p.user.equals("0")))){
    //          full += Permission.toString(p) + "\n";
    //        }else{
    //          full += a + "\n";
    //        }
    //      }
    //      gperms.edit(full);
    //    }else{
    gperms.edit(gperms.getFormattedContent() + "\n" + Permission.toString(p));
    //    }
  }

  public static Long getPinbu(Long guildid) {
    IMessage tempm = null;
    ms = cs.getFullMessageHistory();
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }

    }
    Long res = -1L;
    if (tempm != null) {
      String[] settings = tempm.getFormattedContent().split("\n");
      res = Long.parseLong(settings[5]);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n-1\n-1\n-1");
    }

    return res;
  }

  public static void setPinbu(Long guildid, Long id) {
    IMessage tempm = null;
    for (IMessage mm : ms) {
      if (mm.getFormattedContent().contains(guildid + "\n")) {
        tempm = mm;
      }
    }
    if (tempm != null) {
      String[] sets = tempm.getFormattedContent().split("\n");
      sets[5] = "" + id;
      String fin = "";
      for (String a : sets) {
        fin += a + "\n";
      }
      tempm.edit(fin);
    } else {
      Util.sendMessage(MainBot.cli.getChannelByID(settingsdb), guildid + "\n-1\n-1\n-1\n-1\n" + id);
    }
    ms = cs.getFullMessageHistory();
  }

}
