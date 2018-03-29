package main.db;

import exceptions.InvalidMemeException;
import exceptions.InvalidWarningException;
import main.MainBot;
import main.UserPremiumObject;
import main.Util;
import main.commands.ChatCommands;
import main.commands.memes.Meme;
import main.commands.moderation.Permission;
import main.commands.moderation.Setting;
import main.commands.moderation.Warning;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import sx.blah.discord.handle.obj.IChannel;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class DataManager {

  public final static String SETTINGS = "settings.json", MEMES = "memes.json", WARNS = "warnings.json", GLMEMES = "globalmemes.json", PREMIUMS = "premiumusers.json";
  public static JSONObject settings, warns, premiums;
  public static JSONArray memes, globalmemes;
  public static JSONObject newSettingsObj, botchanObj;

  /**
   * Initializes the data arrays and default objects
   * @throws Throwable if shit goes sideways ig idfk
   */
  public static void init() {
    try {
      settings = (JSONObject) (new JSONParser().parse(new FileReader(SETTINGS)));
      memes = (JSONArray) (new JSONParser().parse(new FileReader(MEMES)));
      globalmemes = (JSONArray) (new JSONParser().parse(new FileReader(GLMEMES)));
      warns = (JSONObject) (new JSONParser().parse(new FileReader(WARNS)));
      premiums = (JSONObject) (new JSONParser().parse(new FileReader(PREMIUMS)));

      newSettingsObj = new JSONObject();

      JSONObject sets = new JSONObject();
      sets.put(Setting.KICKP, -1);
      sets.put(Setting.BANP, -1);
      sets.put(Setting.WARNR, new JSONArray());
      sets.put(Setting.MODR,  new JSONArray());
      sets.put(Setting.PINCHAN, -1);
      sets.put(Setting.LOGGINGCHANNEL, -1L);

      botchanObj = new JSONObject();
      botchanObj.put(Setting.CHANID, -2);
      botchanObj.put(Setting.EXCP, new JSONArray());

      sets.put(Setting.BOTCHAN, botchanObj);

      JSONArray newperms = new JSONArray();
//      newperms.add("default;allow;0;0;0");

      newSettingsObj.put(Setting.SETTINGSF, sets);
      newSettingsObj.put(Setting.PERMSF, newperms);
      newSettingsObj.put(Setting.PREMIUM, false);

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
        for (Object a : maymay.attachments) {
          att.add(att.size(), a.toString());
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

    JSONObject memeobj;

    Object[] obja = memes.stream().filter(a -> ((JSONObject) a).get(Meme.GUILDF).equals(guildid)).toArray();

    if (obja.length > 0){
      memeobj = (JSONObject) obja[new Random().nextInt(obja.length)];
    }else {
      return new Meme("No maymays found /shrug", -1L, 0L, "");
    }

    Object[] atts = {};
    if (!((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).isEmpty()) {
      atts = ((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).toArray();
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
    JSONObject memeobj;
    if(!l.isEmpty())
      memeobj = l.get(new Random().nextInt(l.size()));
    else
      return new Meme("No maymays found /shrug", 0L, 0L, "");

//    System.out.println(memeobj.get(Meme.TEXTF));
    List<String> attssl = new ArrayList<>();
    if (!((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).isEmpty()) {
      for(Object a : ((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).toArray()){
        attssl.add(a.toString());
      }
    }


    fin = new Meme(memeobj.get(Meme.TEXTF).toString(), (Long) memeobj.get(Meme.USERF), (Long) memeobj.get(Meme.GUILDF), memeobj.get(Meme.TIMESTAMPF).toString(), attssl.toArray());

    return fin;
  }

  /**
   * Returns the ammount of warnings it takes to kick a guild member.
   * @param guildid the guild id
   * @return the number of warnings as an int
   */
  public static int getKickp(Long guildid) {
    Number p;
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }

    JSONObject olds = ((JSONObject) settings.get(guildid.toString()));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    p = (Number) (mod.get(Setting.KICKP));
    Integer.parseInt(String.valueOf(p));
    return Integer.parseInt(String.valueOf(p));
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
    Number p;
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }
    p = (Number)  ((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).get(Setting.BANP);
    Integer.parseInt(String.valueOf(p));
    return Integer.parseInt(String.valueOf(p));
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
//      System.out.println(a);
//      System.out.println((Long) a + " dis thing");
    }
//    System.out.println(l);
//    p.add(l);
    return p;
  }

  public static void removeModr(Long guildid, Long role) {
    List<Long> p = new ArrayList<>();
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONArray l = (JSONArray) ((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).get(Setting.MODR);
    l.remove(role);
    l.remove(role.toString());
    ((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).put(Setting.MODR, l);
  }

  /**
   * sets a new bot moderator role in a guild
   * @param guildid guild id
   * @param id the role's id
   */
  public static void setModrole(Long guildid, Long id) {
    JSONObject olds = ((JSONObject) settings.get(guildid.toString()));
    JSONObject mod = ((JSONObject) olds.get(Setting.SETTINGSF));
    JSONArray nnew = (JSONArray) mod.get(Setting.MODR);

    if(mod.containsValue(id))
      nnew.remove(id);
    else
      nnew.add(id);

//    nnew.add(id);
    mod.put(Setting.MODR, nnew);
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
    JSONArray nnew = (JSONArray) mod.get(Setting.WARNR);
    if(mod.containsValue(id))
      nnew.remove(id);
    else
      nnew.add(id);

    mod.put(Setting.WARNR, nnew);
    olds.put(Setting.SETTINGSF, mod);
    settings.put(guildid + "", olds);
  }

  public static void removeWarnr(Long guildid, Long role) {
    if(!settings.containsKey(guildid.toString())) {
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONArray l = (JSONArray) ((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).get(Setting.WARNR);
    l.remove(role);
    l.remove(role.toString());
    ((JSONObject) ((JSONObject) settings.get(guildid.toString())).get(Setting.SETTINGSF)).put(Setting.WARNR, l);
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
        warns.put(w.guild.toString(), new JSONObject());
        warns.put(w.victim.toString(), userw);
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

    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
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
    if(getPerms(guildid).size() >= 25)
      return;
//    System.out.println(ChatCommands.commandMap.keySet().contains(p.command) );
    if(!(ChatCommands.commandMap.keySet().contains(p.command) || ChatCommands.adminMap.keySet().contains(p.command) || Util.catnames.contains(p.command)))
      return;

    String perm = "";
//    System.out.println(p.role + "< someone added dis");
    perm += p.command + ";" + (p.value ? "allow" : "deny") + ";" + p.role + ";" + p.channel;
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONArray perms = (JSONArray) sets4g.get(Setting.PERMSF);
    if(!perms.contains(perm))
      perms.add(perm);

    sets4g.put(Setting.PERMSF, perms);
    settings.put(guildid + "", sets4g);
  }

  public static void setPermissions(List<Permission> p, Long guildid) {
    if(getPerms(guildid).size() >= 25)
      return;
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONArray perms = new JSONArray();
    if(p.isEmpty())
      return;
    for(Permission tp : p) {
      String perm = "";
      perm += tp.command + ";" + (tp.value ? "allow" : "deny") + ";" + tp.role + ";" + tp.channel;
      if (!settings.containsKey(guildid.toString())) {
        settings.put(guildid.toString(), newSettingsObj);
      }
      if (!perms.contains(perm))
        perms.add(perm);
    }
    sets4g.put(Setting.PERMSF, perms);
    settings.put(guildid + "", sets4g);

  }

  /**
   * returns pin channel ID. if none was specified or the feature is disabled it returns -1, otherwise it returns the channel id
   * @param guildid guild id
   * @return {@link IChannel channel} id
   */
  public static Long getPinbu(Long guildid) {
    Long c = -1L;

    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }

    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);

    try {
      c = Long.parseLong(sets.get(Setting.PINCHAN).toString());
    }catch (NumberFormatException e){
      
    }
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
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);
    sets.put(Setting.PINCHAN, id);
    sets4g.put(Setting.SETTINGSF, sets);
    settings.put(guildid, sets4g);
  }

  public static Long getBotComChan(Long guildid){
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }
    Long ret = -1L;
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);
    JSONObject botchan = (JSONObject) sets.get(Setting.BOTCHAN);
    try {
      ret = Long.parseLong(botchan.get(Setting.CHANID).toString());
    }catch(NumberFormatException e){

    }
    return ret;
  }

  public static Long getBotComChan(Long guildid, Long userid){
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }
    Long ret = -1L;
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);
    JSONObject botchan = (JSONObject) sets.get(Setting.BOTCHAN);
    try {
      ret = Long.parseLong(botchan.get(Setting.CHANID).toString());
    }catch(NumberFormatException e){}

    if(ret == -1L){
      JSONArray l = (JSONArray) botchan.get(Setting.EXCP);
      ret = userid;
      for(Object a : l){
        if(a.equals(userid))
          ret = -1L;

//        System.out.println(a + " & " + userid);
      }
    }

    System.out.println("dis is " + ret);
    return ret;
  }

  public static void setBotComChan(Long guildid, Long channelid){
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }

    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);
    JSONObject botch = (JSONObject) sets.get(Setting.BOTCHAN);

    botch.put(Setting.CHANID, channelid);
    sets.put(Setting.BOTCHAN, botch);
    sets4g.put(Setting.SETTINGSF, sets);
    settings.put(guildid.toString(), sets4g);
  }

  public static void setUserNotifi(Long guildid, Long userid){
    if(!settings.containsKey(guildid.toString())){
      settings.put(guildid.toString(), newSettingsObj);
    }

    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    JSONObject sets = (JSONObject) sets4g.get(Setting.SETTINGSF);
    JSONObject botch = (JSONObject) sets.get(Setting.BOTCHAN);
    JSONArray l = (JSONArray) botch.get(Setting.EXCP);

    if(!l.contains(userid))
      l.add(userid);
    else
      l.remove(userid);

    botch.put(Setting.EXCP, l);
    sets.put(Setting.BOTCHAN, botch);
    sets4g.put(Setting.SETTINGSF, sets);
    settings.put(guildid.toString(), sets4g);
  }

  public static void saveGlobalMeme(Meme maymay) throws InvalidMemeException{
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
        for (Object a : maymay.attachments) {
          att.add(att.size(), a.toString());
        }
      }
    }catch(NullPointerException e){
      throw new InvalidMemeException("Meme atribute can not be null");
    }

    obj.put("attachments", att);
    globalmemes.add(obj);
  }

  /**
   * Returns a random meme from the specified guild
   * @return a random meme with all info filled in
   */
  public static Meme getGlobalMeme() {
    Meme fin;

    ArrayList<JSONObject> l = new ArrayList<>();
    for (Object a : globalmemes) {
      l.add((JSONObject) a);
    }
    JSONObject memeobj;

    if(!l.isEmpty()) {
      //      System.out.println("only meme random chose");
      memeobj = l.get(new Random().nextInt(l.size()));
    }else {
      //      System.out.println("no meme found");
      return new Meme("No maymays found /shrug", -1L, 0L, "");
    }
    Object[] atts = {};
    if (!((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).isEmpty()) {
      atts = ((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).toArray();
    }
    fin = new Meme((String) memeobj.get(Meme.TEXTF), (Long) memeobj.get(Meme.USERF), (Long) memeobj.get(Meme.GUILDF), (String) memeobj.get(Meme.TIMESTAMPF), atts);

    return fin;
  }

  /**
   * Returns a random meme for a specified user in a guild
   * @param userid the Long user ID
   * @return a random meme from that user in that guild
   */
  public static Meme getGlobalMemes(Long userid) {
    Meme fin;

    ArrayList<JSONObject> l = new ArrayList<>();
    for (Object a : globalmemes) {
      if (((JSONObject) a).get(Meme.USERF).equals(userid)) {
        l.add((JSONObject) a);
      }
    }
    JSONObject memeobj;
    if(!l.isEmpty())
      memeobj = l.get(new Random().nextInt(l.size()));
    else
      return new Meme("No maymays found /shrug", 0L, 0L, "");

    //    System.out.println(memeobj.get(Meme.TEXTF));
    List<String> attssl = new ArrayList<>();
    if (!((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).isEmpty()) {
      for(Object a : ((JSONArray) memeobj.get(Meme.ATTACHMENTSF)).toArray()){
        attssl.add(a.toString());
      }
    }


    fin = new Meme(memeobj.get(Meme.TEXTF).toString(), (Long) memeobj.get(Meme.USERF), (Long) memeobj.get(Meme.GUILDF), memeobj.get(Meme.TIMESTAMPF).toString(), attssl.toArray());

    return fin;
  }

  public static void setPremiumForGuild(Long guildid, boolean a){
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    if(sets4g == null){
      JSONObject temp = newSettingsObj;
      temp.put("premium", a);
      settings.put(guildid, temp);
    }else{
      sets4g.put("premium", a);
      settings.put(guildid.toString(), sets4g);
    }
  }

  public static boolean isGuildPremium(Long guildid){
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    if(sets4g == null){
      settings.put(guildid, newSettingsObj);
      return false;
    }
    if(sets4g.get(Setting.PREMIUM) != null){
      return ((boolean) sets4g.get(Setting.PREMIUM));
    }else{
      sets4g.put(Setting.PREMIUM, false);
      settings.put(guildid.toString(), sets4g);
      return false;
    }
  }

  public static boolean isUserPremium(Long userid){
    JSONObject userprem = ((JSONObject) premiums.get(userid.toString()));
    if(userprem == null) {
      System.out.println("1");
      return false;
    }else {
      if((boolean) userprem.get("premium")) {
        System.out.println("2");
        return true;
      }
    }
    System.out.println("3");
    return false;
  }

  public static void setUserPremium(Long userid, boolean a, String date) {
    JSONObject userprem = ((JSONObject) premiums.get(userid.toString()));
    if (userprem == null) {
      JSONObject prems = new JSONObject();

      prems.put("premium", a);
      prems.put("premium_since", date);
      premiums.put(userid.toString(), prems);
    } else {
      userprem.put("premium", a);
      userprem.put("premium_since", date);
      premiums.put(userid.toString(), userprem);
    }
  }

  public static Long hasLogging(Long guildid){
    Long res = -1L;
    JSONObject sets4g = ((JSONObject) settings.get(guildid.toString()));
    if(sets4g == null){
      settings.put(guildid, newSettingsObj);
      return res;
    }
    JSONObject sets = ((JSONObject) sets4g.get(Setting.SETTINGSF));
    if(sets.get(Setting.LOGGINGCHANNEL) == null){
      sets.put(Setting.LOGGINGCHANNEL, -1L);
      sets4g.put(Setting.SETTINGSF, sets);
      settings.put(sets4g, settings);
      return res;
    }else if(sets.getOrDefault(Setting.LOGGINGCHANNEL, -1).equals(-1L)){
      return res;
    }else{
      return (Long) sets.get(Setting.LOGGINGCHANNEL);
    }
  }

  public static int getPremiumGuildCount(){
    return (int) settings.values().stream().filter(n -> (boolean) ((JSONObject) n).getOrDefault("premium", false)).count();
  }

  public static UserPremiumObject getUserPremium(Long userid) {
    JSONObject userprem = ((JSONObject) premiums.get(userid.toString()));
    if (userprem == null)
      return null;
    else {
      return new UserPremiumObject(userid, userprem.get("premium_since").toString(), (boolean) userprem.get("premium"));
    }
  }

  public static int getPremiumUserCount() {
    return (int) premiums.values().stream().filter(n -> (boolean) ((JSONObject) n).getOrDefault("premium", false)).count();
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
        fw.flush();
        fw.close();
        fw = new FileWriter("config.json");
        MainBot.config.put("commands", Util.totcom);
        fw.write(MainBot.config.toJSONString());
        fw.flush();
        fw.close();
        fw = new FileWriter(GLMEMES);
        fw.write(globalmemes.toJSONString());
        fw.flush();
        fw.close();
        fw = new FileWriter(PREMIUMS);
        fw.write(premiums.toJSONString());
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
