package main;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class Util {

  public static String prefix = "j.";
  public static double version = 0.15;
  private static int cnum = 10;
  public static String[][] cmdinfo = new String[cnum][2];
  public static int totcom = 0;
  public static String link =
      "https://discordapp.com/oauth2/authorize?client_id=334665490612092929&scope=bot&permissions=335932502";
  public static boolean gmode = true;
  public static Long jarza = 0l;
  public static Long testserver = 0l;

  public static void init() {
    cmdinfo[0][0] = "modr roleName";
    cmdinfo[0][1] = "This will set the bot editing permission role (example: j.modr Staff)";

    cmdinfo[1][0] = "warnr roleName";
    cmdinfo[1][1] = "This will set the warning permission role (ex: j.warnr Staff)";

    cmdinfo[2][0] = "warnp kick/ban numberOfWarnings";
    cmdinfo[2][1] = "This will set the punishment for a certain ammount of warnings (ex: j.warnp kick 3)";

    cmdinfo[3][0] = "warn @user reason";
    cmdinfo[3][1] = "For example j.warn @jarza#1289 spams hentai in general chat";

    cmdinfo[4][0] = "warnl [optional @user]";
    cmdinfo[4][1] = "Lists all warnings for a user if one is specified, otherwise it lists all warnings on the server (ex: j.warnl or j.warnl @jarza#1289)";

    cmdinfo[5][0] = "warnc @user [optional warnId]";
    cmdinfo[5][1] = "If you specify which warning from the warn list you want, it will be cleared. Otherwise all of that users warnings will be cleared.";

    cmdinfo[6][0] = "meme [optional @user]";
    cmdinfo[6][1] = "Gets a random meme that the bot has saved.";

    cmdinfo[7][0] = "dating";
    cmdinfo[7][1] = "Generates a custom dating ad!";

    cmdinfo[8][0] = "say text";
    cmdinfo[8][1] = "Makes the bot say something and delete your message (ex: j.say hehe i am alive)";

    cmdinfo[9][0] = "rule";
    cmdinfo[9][1] = "Miscellaneous commands.";

    cmdinfo[9][0] = "info, help, ping, invite";
    cmdinfo[9][1] = "Miscellaneous commands.";
  }

  static IDiscordClient getBuiltDiscordClient(String token) {
    return new ClientBuilder().withToken(token).build();

  }

  public static void sendMessage(IChannel channel, String message) {
    RequestBuffer.request(() -> {
      try {
        channel.sendMessage(message);
      } catch (DiscordException e) {
        System.err.println("Hmmm shit went sideways... Here's why: ");
        e.printStackTrace();
      }
    });
  }

  public static boolean botCommand(String t) {
    boolean r = false;

    if (t.startsWith("j.") || t.startsWith("+") || t.startsWith("!") || t.startsWith("?")
        || t.startsWith("-") || t.startsWith("b.") || t.startsWith("f'") || t.startsWith("p!")
        || t.startsWith("=") || t.startsWith(".")) {
      r = true;
    }

    return r;
  }

  public static void kickUser(String user, String reason) {

  }

  public static Long userToID(String user) {
    Long id = 0L;
    try {
      id = Long.parseLong(user.substring(2, user.length() - 1));
    } catch (Exception e) {
      id = Long.parseLong(user.substring(3, user.length() - 1));
    }
    return id;
  }

}
