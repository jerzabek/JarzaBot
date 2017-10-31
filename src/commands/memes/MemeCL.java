package commands.memes;

import commands.ChatCommands;
import db.DataManager;
import main.MainBot;
import main.Util;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class MemeCL {

  // public static Map<Long, List<String>> memes = new HashMap<>();

  public static void init() {

    ChatCommands.commandMap.put("meme", (event, args) -> {
      Meme meme;
      if (args.size() > 0) {
        String name = "";
        int c = 0;
        for(String a : args){
          c++;
          if(c == args.size()){
            name += a;
          }else {
            name += a + " ";
          }
        }
        if(!event.getGuild().getUsersByName(name, true).isEmpty())
         meme = DataManager.getMemes(event.getGuild().getUsersByName(name, true).get(0).getLongID(), event.getGuild().getLongID());
        else if(!event.getGuild().getUsersByName(name, false).isEmpty()) {
          meme = DataManager.getMemes(event.getGuild().getUsersByName(name, false).get(0).getLongID(), event.getGuild().getLongID());
        }else {
          EmbedBuilder builder = new EmbedBuilder();
          builder.appendField("No memes available for that user", "¯\\_(ツ)_/¯", false);
          RequestBuffer.request(() -> {
            try {
              event.getChannel().sendMessage(builder.build());
            } catch (DiscordException e) {
              System.err.println("Hmmm shit went sideways... Here's why: ");
              e.printStackTrace();
            }
          });
          return;
        }
      }else{
        meme = DataManager.getMeme(event.getGuild().getLongID());
//        System.out.println("meme only");
      }

//      Long crt = Long.parseLong(meme.getFormattedContent().split("\n")[1]);
//      Long crt = Long.parseLong(meme.text.substring(0, 18));
//      Long guild = Long.parseLong(meme.getFormattedContent().split("\n")[0]);
//      String msg = meme.text.substring(38);
//      Long guild = Long.parseLong(meme.getFormattedContent().substring(19, 37));
      String tabs;
      tabs = "\n";
      if(meme != null) {
        for (int x = 0; x < meme.text.length() * 2; x++) {
          tabs += " ";
        }
        //      msg = "\"" + msg + "\" " + tabs + " *-" + MainBot.cli.getUserByID(crt).getName() + "*";
        EmbedBuilder builder = new EmbedBuilder();
        String username = "1337", memetext = "not available /shrug", timestamp = "n/a", avtrURL = "https://cdnjs.cloudflare.com/ajax/libs/emojione/2.2.6/assets/png/1f914.png";
        //      System.out.println(meme.user);
        if (meme.user != -1L) {
          username = event.getGuild().getUserByID(meme.user).getName();
          memetext = meme.text;
          timestamp = meme.timestamp;
          avtrURL = event.getGuild().getUserByID(meme.user).getAvatarURL();
          //        System.out.println("beb");
        } else {
          //        System.out.println("stuff");
        }

        builder.withThumbnail(avtrURL);
        builder.appendField("\"" + memetext + "\" ", " *-" + username + "*", false);
        builder.withFooterText(timestamp);
        builder.withColor(new Color(112, 137, 255));
        //      Util.sendMessage(event.getChannel(), msg);
        RequestBuffer.request(() -> {
          try {
            event.getChannel().sendMessage(builder.build());
          } catch (DiscordException e) {
            System.err.println("Hmmm shit went sideways... Here's why: ");
            e.printStackTrace();
          }
        });
      }else{
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField("No warnings available",
          "¯\\_(ツ)_/¯", false);
        RequestBuffer.request(() -> {
          try {
            event.getChannel().sendMessage(builder.build());
          } catch (DiscordException e) {
            System.err.println("Hmmm shit went sideways... Here's why: ");
            e.printStackTrace();
          }
        });
      }
    });

    ChatCommands.commandMap.put("dating", (event, args) -> {
      BufferedImage img = null;
      int[][] lox = {{4, 0, 157, -1, -1}, {307, 51, 241, -1, -1}, {6, 35, 266, -1, -1},
          {47, 190, 184, 150, 125}};
      int id = new Random().nextInt(4);
      // int id = 3;
      try {
        img = ImageIO.read(new File("gfx/d" + id + ".jpg"));
      } catch (IOException e) {
        e.printStackTrace();
      }

      Graphics2D g = img.createGraphics();
      BufferedImage i = null;
      BufferedInputStream in = null;
      try {
        URL url;
        URLConnection uc;
        url = new URL(event.getAuthor().getAvatarURL().replace("webp", "jpg"));
        uc = url.openConnection();
        uc.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:44.0) Gecko/20100101 Firefox/44.0");
        uc.connect();
        in = new BufferedInputStream(uc.getInputStream());
        i = ImageIO.read(in);
        g.drawImage(i, lox[id][0], lox[id][1], lox[id][2], lox[id][2], null);
        if (lox[3][3] > 0) {
          g.setFont(new Font("Arial", Font.BOLD, 25));
          g.setColor(new Color(105, 102, 180));
          g.drawString(event.getAuthor().getName(), lox[id][3], lox[id][4]);
        }

      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }


      // Clean up -- dispose the graphics context that was created.
      g.dispose();
      //
      InputStream is = null;
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", os);
        is = new ByteArrayInputStream(os.toByteArray());
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      final InputStream image = is;
      RequestBuffer.request(() -> {
        try {
          event.getChannel().sendFile("", image, "lol.jpg");
        } catch (DiscordException e) {
          System.err.println("Hmmm shit went sideways... Here's why: ");
          e.printStackTrace();
        }
      });
    });

    ChatCommands.commandMap.put("maymay", (event, args) -> {
      Util.sendMessage(event.getChannel(), "maymaysss #ripmira #neverforgetti");
    });
  }

}
