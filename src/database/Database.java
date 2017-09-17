package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import commands.memes.Meme;
import commands.moderation.Setting;
import commands.moderation.Warning;
import main.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

@Deprecated
public class Database {

  private static Connection con;
  private static Statement st;
  private static ResultSet set;

  /**
   * 
   */
  public static void connect() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      con =
          DriverManager.getConnection("jdbc:mysql://localhost:3306/jajabot", "root", "babilon123");
      st = con.createStatement();
      set = st.executeQuery("select * from warnings");
    } catch (Exception ex) {
      System.out.println("Error: " + ex);

    }
  }

  public static List<Object> getData(Table t, MessageReceivedEvent event, String... query) {
    List<Object> res = new ArrayList<>();
    try {
      switch (t) {
        case warnings:
          set = st.executeQuery("select * from warnings where guild = " + event.getGuild().getLongID());
          while (set.next()) {
//            res.add(new Warning(set.getLong("moderator"), set.getLong("user"), set.getString("reason"), set.getBoolean("cleared")));
          }
          break;
        case memes:
          set = st.executeQuery("select * from memes where guild = " + event.getGuild());
          while (set.next()) {
//            res.add(new Meme(set.getString("text"), set.getLong("user"), set.getLong("guild")));
          }
          break;
        case settings:
          set = st.executeQuery(query[0]);
          set.next();
          res.add(new Setting(set.getLong("guild"), set.getLong("warnrole"), set.getLong("modrole"), set.getInt("kickp"), set.getInt("banp"), set.getInt("look")));
          break;
        default:
          Util.sendMessage(event.getChannel(), "**>Error**");
          break;
      }
    } catch (Exception ex) {
      System.out.println("Shit went sideways: " + ex);
    }
    return res;
  }
  
//  public static List<Object> getq(Table t, MessageReceivedEvent event, String query) {
//    List<Object> res = new ArrayList<>();
//    try {
//      switch (t) {
//        case warnings:
//          set = st.executeQuery(query);
//          while (set.next()) {
//            res.add(new Warning(set.getLong("moderator"), set.getLong("user"), set.getString("reason"), set.getBoolean("cleared")));
//          }
//          break;
//        case memes:
//          set = st.executeQuery(query);
//          while (set.next()) {
//            res.add(new Meme(set.getString("name"), set.getLong("user")));
//          }
//          break;
//        case settings:
//          set = st.executeQuery(query);
//          set.next();
//          res.add(new Setting(set.getLong("guild"), set.getLong("warnrole"), set.getLong("modrole"), set.getInt("kickp"), set.getInt("banp"), set.getInt("look")));
//          break;
//        default:
//          Util.sendMessage(event.getChannel(), "**>Error**");
//          System.out.println("Error");
//          break;
//      }
//    } catch (Exception ex) {
//      System.out.println("Shit went sideways: " + ex);
//    }
//    return res;
//  }
  
  public static void putData(String query) {
    try {
      st.execute(query);
    }catch(Exception e) {
      e.printStackTrace();
//      Util.sendMessage(event.getChannel(), "**>Error: while putting data into DB shit went sideways.**");
    }
  }
  
  public static void putData(String query, MessageReceivedEvent event) {
    try {
      st.execute(query);
    }catch(Exception e) {
      e.printStackTrace();
//      Util.sendMessage(event.getChannel(), "**>Error: while putting data into DB shit went sideways.**");
    }
  }
  
  public static void dodata(String query) {
    try {
      st.executeQuery(query);
    }catch(Exception e) {
      e.printStackTrace();
//      Util.sendMessage(event.getChannel(), "**>Error: while putting data into DB shit went sideways.**");
    }
  }
  
  public static void dodata(String query, MessageReceivedEvent event) {
    try {
      st.executeQuery(query);
    }catch(Exception e) {
      e.printStackTrace();
      Util.sendMessage(event.getChannel(), "**>Error: while putting data into DB shit went sideways.**");
    }
  }

  public static Setting loadSettings(MessageReceivedEvent event) {
    if(Database.getData(Table.settings, event, "select * from `settings` where guild = " + event.getGuild().getLongID() + ";").size() == 0) {
      String q = "insert into `settings` (guild) values (" + event.getGuild().getLongID() + ")";
      Database.putData(q, event);
    }
    
//    String q = "insert into `settings` (guild) values (" + event.getGuild().getLongID() + ")";
    List<Object> l = Database.getData(Table.settings, event, "select * from settings where guild = " + event.getGuild().getLongID());
    Setting a = (Setting) l.get(0);
    return a;
  }
  
}
