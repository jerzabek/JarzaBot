package db;

import commands.memes.Meme;
import commands.moderation.Permission;
import commands.moderation.Setting;
import commands.moderation.Warning;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * W.I.P.
 * <br>
 * Storage will go to JSON format.
 * <br>
 * TODO:
 * <br>
 * &#07;finish translation (finished, I guess it works we'll see xdd)
 * <br>
 * &#07;move old things to new format
 */
public class DataManager {

  private final static String SETTINGS = "settings.json", MEMES = "memes.json", WARNS = "warnings.json";
  private static JSONObject settings, warns;
  private static JSONArray memes;

  public static void init() {
    try {
      settings = (JSONObject) (new JSONParser().parse(new FileReader(SETTINGS)));
      memes = (JSONArray) (new JSONParser().parse(new FileReader(MEMES)));
      warns = (JSONObject) (new JSONParser().parse(new FileReader(WARNS)));
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  public static void saveMeme(Meme maymay) {
    if (maymay.text.length() > 255)
      return;

    JSONObject obj = new JSONObject();
    obj.put(Meme.USERF, maymay.user);
    obj.put(Meme.GUILDF, maymay.guild);
    obj.put(Meme.TEXTF, maymay.text);
    obj.put(Meme.TIMESTAMPF, maymay.timestamp);
    JSONArray att = new JSONArray();
    if (maymay.attachments.length > 0) {
      for (String a : maymay.attachments) {
        att.add(att.size(), a);
      }
    }
    obj.put("attachments", att);
    memes.add(memes.size() - 1, obj);
  }

  public static Meme getMeme(Long guildid) {
    Meme fin;

    ArrayList<JSONObject> l = new ArrayList<>();
    for (Object a : memes) {
      if (((JSONObject) a).get(Meme.GUILDF).equals(guildid)) {
        l.add((JSONObject) a);
      }
    }

    JSONObject memeobj = l.get(new Random().nextInt(l.size()));
    String[] atts = {};
    if (!((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).isEmpty()) {
      atts = (String[]) ((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).toArray();
    }
    fin = new Meme((String) memeobj.get(Meme.TEXTF), (Long) memeobj.get(Meme.USERF), (Long) memeobj.get(Meme.GUILDF), (String) memeobj.get(Meme.TIMESTAMPF), atts);

    return fin;
  }

  public static Meme getMemes(Long userid, Long guildid) {
    Meme fin;

    ArrayList<JSONObject> l = new ArrayList<>();
    for (Object a : memes) {
      if (((JSONObject) a).get(Meme.GUILDF).equals(guildid) && ((JSONObject) a).get(Meme.USERF).equals(userid)) {
        l.add((JSONObject) a);
      }
    }
    JSONObject memeobj = l.get(new Random().nextInt(l.size()));
    String[] atts = {};
    if (!((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).isEmpty()) {
      atts = (String[]) ((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).toArray();
    }
    fin = new Meme((String) memeobj.get(Meme.TEXTF), (Long) memeobj.get(Meme.USERF), (Long) memeobj.get(Meme.GUILDF), (String) memeobj.get(Meme.TIMESTAMPF), atts);

    return fin;
  }

  public static int getKickp(Long guildid) {
    int p;
    p = (int) ((JSONObject) ((JSONObject) settings.get(guildid + "")).get(Setting.SETTINGSF)).get(Setting.KICKP);
    return p;
  }

  public static void setKickp(Long guildid, int p) {
    JSONObject olds = ((JSONObject) settings.get(guildid + ""));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.KICKP, p);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  public static int getBanp(Long guildid) {
    int p;
    p = (int) ((JSONObject) ((JSONObject) settings.get(guildid + "")).get(Setting.SETTINGSF)).get(Setting.BANP);
    return p;
  }

  public static void setBanp(Long guildid, int p) {
    JSONObject olds = ((JSONObject) settings.get(guildid + ""));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.BANP, p);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  public static Long getModrole(Long guildid) {
    Long p;
    p = (Long) ((JSONObject) ((JSONObject) settings.get(guildid + "")).get(Setting.SETTINGSF)).get(Setting.MODR);
    return p;
  }

  public static void setModrole(Long guildid, Long id) {
    JSONObject olds = ((JSONObject) settings.get(guildid + ""));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.MODR, id);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  public static Long getWarnrole(Long guildid) {
    Long p;
    p = (Long) ((JSONObject) ((JSONObject) settings.get(guildid + "")).get(Setting.SETTINGSF)).get(Setting.WARNR);
    return p;
  }

  public static void setWarnrole(Long guildid, Long id) {
    JSONObject olds = ((JSONObject) settings.get(guildid + ""));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.WARNR, id);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  public static void warn(Warning w) {
    JSONObject guild = (JSONObject) warns.get(w.guild + "");
    JSONArray userw = (JSONArray) guild.get(w.victim + "");
    JSONObject warnin = new JSONObject();
    warnin.put(Warning.MOD, w.user);
    warnin.put(Warning.REASON, w.reason);
    warnin.put(Warning.CLEARED, w.cleared);
    warnin.put(Warning.CLEAREDBY, w.clearedby);
    warnin.put(Warning.TIMESTAMP, w.timestamp);
    userw.add(warnin);
    guild.put(w.victim + "", userw);
    warns.put(w.guild + "", guild);
  }

  /**
   * This method returns all warnings on the specified server.
   *
   * @param guildid the guild
   * @return A list of all the warnings on the server
   */
  public static List<Warning> getWarns(Long guildid) {
    List<Warning> l = new ArrayList<>();
    JSONObject warns4g = ((JSONObject) warns.get(guildid));
    for (Object uk : warns4g.keySet()) { //uk - user key
      for (Object warns4uo : (JSONArray) warns4g.get(uk)) {
        JSONObject warn4u = (JSONObject) warns4uo; //the warning object for that user in that guild
        l.add(new Warning((Long) warn4u.get(Warning.MOD), (Long) uk, guildid, (String) warn4u.get(Warning.REASON), (boolean) warn4u.get("cleared"), (Long) warn4u.get(Warning.CLEAREDBY),
          (String) warn4u.get(Warning.TIMESTAMP)));
      }
    }
    return l;
  }

  public static List<Warning> getWarns(Long guildid, Long userid) {
    List<Warning> l = new ArrayList<>();
    JSONObject warns4g = ((JSONObject) warns.get(guildid));
    JSONArray warns4u = (JSONArray) warns4g.get(userid + "");
    for (Object a : warns4u) {
      JSONObject warn4u = (JSONObject) a;
      l.add(new Warning((Long) warn4u.get(Warning.MOD), userid, guildid, (String) warn4u.get(Warning.REASON), (boolean) warn4u.get(Warning.CLEARED), (Long) warn4u.get(Warning.CLEAREDBY),
        (String) warn4u.get(Warning.TIMESTAMP)));
    }
    return l;
  }

  @Deprecated public static void clearWarns(Long msgid) {
    //    IMessage i = MainBot.cli.getMessageByID(msgid);
    //    String[] fin = i.getFormattedContent().split("\n");
    //    fin[4] = "1";
    //    String res = "";
    //    for (String f : fin) {
    //      res += f + "\n";
    //    }
    //    i.edit(res);
  }

  public static void clearWarns(Long guildid, Long userid, Long modid, int id) {
    JSONObject warns4g = ((JSONObject) warns.get(guildid));
    JSONArray warns4u = (JSONArray) warns4g.get(userid + "");
    ((JSONObject) warns4u.get(id)).put(Warning.CLEARED, true);
    ((JSONObject) warns4u.get(id)).put(Warning.CLEAREDBY, modid);
    warns4g.put(userid + "", warns4u);
    warns.put(guildid, warns4g);
  }

  public static List<Permission> getPerms(Long guildid) {
    List<Permission> res = new ArrayList<>();

    JSONObject sets4g = ((JSONObject) settings.get(guildid));
    JSONArray perms = (JSONArray) sets4g.get(Setting.PERMSF);
    for (Object a : perms) {
      List<String> temp = new ArrayList<>();
      for (String tempa : ((String) a).split(";")) {
        temp.add(tempa);
      }
      res.add(Permission.commandParse(temp, guildid));
    }
    return res;
  }

  public static void setPermission(Permission p, Long guildid) {
    String perm = "";
    perm += p.command + ";" + p.value + ";" + p.user + ";" + p.channel + ";" + p.role;
    JSONObject sets4g = ((JSONObject) settings.get(guildid));
    JSONArray perms = (JSONArray) sets4g.get(Setting.PERMSF);
    perms.add(perm);
    sets4g.put(Setting.PERMSF, perms);
    settings.put(guildid + "", perms);
  }

  public static Long getPinbu(Long guildid) {
    Long c;

    JSONObject sets4g = ((JSONObject) settings.get(guildid));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);

    c = (Long) sets.get(Setting.PINCHAN);

    return c;
  }

  public static void setPinbu(Long guildid, Long id) {
    JSONObject sets4g = ((JSONObject) settings.get(guildid));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);
    sets.put(Setting.PINCHAN, id);
    sets4g.put(Setting.SETTINGSF, sets);
    settings.put(guildid, sets4g);
  }

  public static void finish() {
    try {
      FileWriter fw = new FileWriter(MEMES);
      try {
        fw.write(memes.toJSONString());
        fw.flush();
        fw.close();
        fw = new FileWriter(SETTINGS);
        fw.write(settings.toJSONString());
      } catch (Throwable e) {
        e.printStackTrace();
      } finally {
        fw.flush();
        fw.close();
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
