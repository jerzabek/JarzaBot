package db;

import commands.memes.Meme;
import commands.moderation.Permission;
import commands.moderation.Setting;
import commands.moderation.Warning;
import exceptions.InvalidMemeException;
import exceptions.InvalidWarningException;
import main.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.omg.CORBA.DynAnyPackage.Invalid;
import sx.blah.discord.handle.obj.IChannel;

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
 * &#07;finish translation (done)
 * <br>
 * &#07;document all files and fix this whole class
 * <br>
 * &#07;move old things to new format
 */
public class DataManager {

  public final static String SETTINGS = "settings.json", MEMES = "memes.json", WARNS = "warnings.json";
  public static JSONObject settings, warns;
  public static JSONArray memes;
  public static JSONObject newSettingsObj;

  /**
   * Initializes the data arrays and default objects
   * @throws Throwable if shit goes sideways ig idfk
   */
  public static void init() {
    try {
      settings = (JSONObject) (new JSONParser().parse(new FileReader(SETTINGS)));
      memes = (JSONArray) (new JSONParser().parse(new FileReader(MEMES)));
      warns = (JSONObject) (new JSONParser().parse(new FileReader(WARNS)));

      newSettingsObj = new JSONObject();

      JSONObject sets = new JSONObject();
      sets.put(Setting.KICKP, -1);
      sets.put(Setting.BANP, -1);
      sets.put(Setting.WARNR, -1);
      sets.put(Setting.MODR, -1);
      sets.put(Setting.PINCHAN, -1);

      JSONArray newperms = new JSONArray();
      newperms.add("default;false;0;0;0");

      newSettingsObj.put(Setting.SETTINGSF, sets);
      newSettingsObj.put(Setting.PERMSF, newperms);

    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * Saves a given meme in {@link JSONArray memes}
   * @param maymay a meme
   * @throws InvalidMemeException if meme is invalid
   */
  public static void saveMeme(Meme maymay) throws InvalidMemeException{
    if (maymay.text.length() > 255) {
      throw new InvalidMemeException("Meme too long, didn't save it.");
    }else if(maymay == null){
      throw new InvalidMemeException("Meme can not be null");
    }

    JSONObject obj = new JSONObject();
    JSONArray att;
    try {
      obj.put(Meme.USERF, maymay.user);
      obj.put(Meme.GUILDF, maymay.guild);
      obj.put(Meme.TEXTF, maymay.text);
      obj.put(Meme.TIMESTAMPF, maymay.timestamp);
      att = new JSONArray();
      if (maymay.attachments.length > 0) {
        for (String a : maymay.attachments) {
          att.add(att.size(), a);
        }
      }
    }catch(NullPointerException e){
      throw new InvalidMemeException("Meme atribute can not be null");
    }

    obj.put("attachments", att);
    memes.add(memes.size() - 1, obj);
  }

  /**
   * Returns a random meme from the specified guild
   * @param guildid the Guild's Long ID
   * @return a random meme with all info filled in
   */
  public static Meme getMeme(Long guildid) {
    Meme fin;

    ArrayList<JSONObject> l = new ArrayList<>();
    for (Object a : memes) {
      if (((JSONObject) a).get(Meme.GUILDF).equals(guildid.toString())) {
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

  /**
   * Returns a random meme for a specified user in a guild
   * @param userid the Long user ID
   * @param guildid the Long guild ID
   * @return a random meme from that user in that guild
   */
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

  /**
   * Returns the ammount of warnings it takes to kick a guild member.
   * @param guildid the guild id
   * @return the number of warnings as an int
   */
  public static int getKickp(Long guildid) {
    Long p;
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }

    JSONObject olds = ((JSONObject) settings.get(guildid.toString()));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));

    p = (Long) (mod.get(Setting.KICKP));

    return Math.round(p);
  }

  /**
   * Sets the number of warnings it takes to kick a member in that guild
   * @param guildid guild id
   * @param p new number of warnings
   */
  public static void setKickp(Long guildid, int p) {
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONObject olds = ((JSONObject) settings.get(guildid.toString()));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.KICKP, p);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  /**
   * gets the number of warings to ban a member
   * @param guildid the guild id
   * @return the number of warnings
   */
  public static int getBanp(Long guildid) {
    Long p;
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }
    p = (Long)  ((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).get(Setting.BANP);
    return Math.round(p);
  }

  /**
   * sets number of warnings to ban a guild member
   * @param guildid guld id
   * @param p new number of warnings
   */
  public static void setBanp(Long guildid, int p) {
     if(!settings.containsKey(guildid.toString())){
       settings.put(guildid.toString(), newSettingsObj);
     }
    JSONObject olds = ((JSONObject) settings.get(guildid.toString()));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.BANP, p);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  /**
   * returns a list of role IDs whos members can edit  the bot settings
   * @param guildid guild id
   * @return list of Role IDs
   */
  public static List<Long> getModrole(Long guildid) {
    List<Long> p = new ArrayList<>();
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONArray l = (JSONArray) ((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).get(Setting.MODR);
    for(Object a : l){
      p.add((Long) a);
    }
    return p;
  }

  /**
   * sets a new bot moderator role in a guild
   * @param guildid guild id
   * @param id the role's id
   */
  public static void setModrole(Long guildid, Long id) {
    JSONObject olds = ((JSONObject) settings.get(guildid.toString()));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.MODR, id);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  /**
   * returns the roles that can warn on a guild
   * @param guildid guild id
   * @return list of role IDs
   */
  public static List<Long> getWarnrole(Long guildid) {
    List<Long> p = new ArrayList<>();
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONArray l = (JSONArray) (((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).get(Setting.WARNR));
    for(Object a : l){
      p.add((Long) a);
    }

    return p;
  }

  /**
   * sets a new role which can warn people
   * @param guildid guild id
   * @param id id of the role
   */
  public static void setWarnrole(Long guildid, Long id) {
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONObject olds = ((JSONObject) settings.get(guildid.toString()));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    mod.put(Setting.WARNR, id);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  /**
   * warns a user by a specified warning
   * @param w the warning
   * @throws InvalidWarningException if warning is invalid
   */
  public static void warn(Warning w) throws InvalidWarningException{
    JSONObject guild;
    JSONArray userw = new JSONArray();

    JSONObject warnin = new JSONObject();
    try {
      warnin.put(Warning.MOD, w.user);
      warnin.put(Warning.REASON, w.reason);
      warnin.put(Warning.CLEARED, w.cleared);
      warnin.put(Warning.CLEAREDBY, w.clearedby);
      warnin.put(Warning.TIMESTAMP, w.timestamp);

      if (!warns.containsKey(w.guild.toString())) {
        userw.add(warnin);
        warns.put(w.guild.toString(), userw);
      } else {
        guild = (JSONObject) warns.get(w.guild.toString());
        userw = ((JSONArray) guild.get(w.victim.toString()));
        userw.add(warnin);
        guild.put(w.victim.toString(), userw);
        warns.put(w.guild.toString(), guild);
      }
    }catch(NullPointerException e){
      throw new InvalidWarningException("Warning atribute can not be null: " + e.getMessage());
    }
  }

  /**
   * This method returns all warnings on the specified server.
   *
   * @param guildid the guild
   * @return A list of all the warnings on the server
   */
  public static List<Warning> getWarns(Long guildid) {
    List<Warning> l = new ArrayList<>();
    if(warns.containsKey(guildid.toString())) {
      JSONObject warns4g = ((JSONObject) warns.get(guildid.toString()));
      for (Object uk : warns4g.keySet()) { //uk - user key
        for (Object warns4uo : (JSONArray) warns4g.get(uk)) {
          JSONObject warn4u = (JSONObject) warns4uo; //the warning object for that user in that guild

          Object a = warn4u.get(Warning.MOD);
          Object b = warn4u.get(Warning.REASON);
          Object c = warn4u.get(Warning.CLEARED);
          Object d = warn4u.get(Warning.CLEAREDBY);
          Object e = warn4u.get(Warning.TIMESTAMP);

          //        System.out.println(a.toString() + " " + b.toString() + " " + c.toString() + " " + d.toString() + " " + e.toString());
          //Debbuging

          l.add(new Warning((Long) a, Long.parseLong((String) uk), guildid, (String) b, (boolean) c, (Long) d, (String) e));
        }
      }
    }
    return l;
  }

  /**
   * returns all the warnings for a user in a guild
   * @param guildid guild id
   * @param userid user id
   * @return list of warnings
   */
  public static List<Warning> getWarns(Long guildid, Long userid) {
    List<Warning> l = new ArrayList<>();
    if(warns.containsKey(guildid.toString())) {
      JSONObject warns4g = ((JSONObject) warns.get(guildid.toString()));
      JSONArray warns4u;
      if (warns4g.containsKey(userid.toString())) {
        warns4u = (JSONArray) warns4g.get(userid.toString());
        if (!warns4u.isEmpty())
          for (Object a : warns4u) {
            JSONObject warn4u = (JSONObject) a;
            l.add(new Warning((Long) warn4u.get(Warning.MOD), userid, guildid, (String) warn4u.get(Warning.REASON), (boolean) warn4u.get(Warning.CLEARED), (Long) warn4u.get(Warning.CLEAREDBY), (String) warn4u.get(Warning.TIMESTAMP)));
          }
      }
    }
    return l;
  }

  /**
   * clears specified warning for a user in a guild
   * @param guildid guild id
   * @param userid user id
   * @param modid id of the user who cleared this warning
   * @param id the ordinal number of the warning
   * @throws InvalidWarningException if warning info is invalid
   */
  public static void clearWarns(Long guildid, Long userid, Long modid, int id) throws InvalidWarningException{
    if(warns.containsKey(guildid.toString())) {
      JSONObject warns4g = ((JSONObject) warns.get(guildid.toString()));
      JSONArray warns4u;
      if (warns4g.containsKey(userid.toString())) {
        warns4u = (JSONArray) warns4g.get(userid.toString());
        if (warns4u.get(id) != null) {
          ((JSONObject) warns4u.get(id)).put(Warning.CLEARED, true);
          ((JSONObject) warns4u.get(id)).put(Warning.CLEAREDBY, modid);
          warns4g.put(userid.toString(), warns4u);
          warns.put(guildid, warns4g);
        } else {
          throw new InvalidWarningException("Specified warning doesn't exist.");
        }
      } else {
        throw new InvalidWarningException("User doesnt have any warnings.");
      }
    }else{
      throw new InvalidWarningException("User doesnt have any warnings.");
    }
  }

  /**
   * returns all command permissions in the guild
   * @param guildid guild id
   * @return list of {@link Permission permissions} in that guild
   */
  public static List<Permission> getPerms(Long guildid) {
    List<Permission> res = new ArrayList<>();

    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }

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

  /**
   * adds a new {@link Permission permission} to the guild
   * @param p the permission
   * @param guildid guild id
   */
  public static void setPermission(Permission p, Long guildid) {
    String perm = "";
    perm += p.command + ";" + p.value + ";" + p.user + ";" + p.channel + ";" + p.role;
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONObject sets4g = ((JSONObject) settings.get(guildid));
    JSONArray perms = (JSONArray) sets4g.get(Setting.PERMSF);
    perms.add(perm);
    sets4g.put(Setting.PERMSF, perms);
    settings.put(guildid + "", perms);
  }

  /**
   * returns pin channel ID. if none was specified or the feature is disabled it returns -1, otherwise it returns the channel id
   * @param guildid guild id
   * @return {@link IChannel channel} id
   */
  public static Long getPinbu(Long guildid) {
    Long c;

    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }

    JSONObject sets4g = ((JSONObject) settings.get(guildid));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);

    c = (Long) sets.get(Setting.PINCHAN);

    return c;
  }

  /**
   * sets the pin channel to the specified id
   * @param guildid guild id
   * @param id {@link IChannel channel} id
   */
  public static void setPinbu(Long guildid, Long id) {
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONObject sets4g = ((JSONObject) settings.get(guildid));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);
    sets.put(Setting.PINCHAN, id);
    sets4g.put(Setting.SETTINGSF, sets);
    settings.put(guildid, sets4g);
  }

  /**
   * Saves all data to the JSON files.
   */
  public static void finish() {
    try {
      FileWriter fw = new FileWriter(MEMES);
      try {
        fw.write(memes.toJSONString());
        fw.flush();
        fw.close();
        fw = new FileWriter(SETTINGS);
        fw.write(settings.toJSONString());
        fw.flush();
        fw.close();
        fw = new FileWriter(WARNS);
        fw.write(warns.toJSONString());
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
