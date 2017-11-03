package commands.memes;

import commands.ChatCommands;
import db.DataManager;
import main.Util;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
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
import java.net.URLEncoder;
import java.util.*;

public class MemeCL {

  // public static Map<Long, List<String>> memes = new HashMap<>();

  public static void init() {

    ChatCommands.commandMap.put("meme", (MessageReceivedEvent event, java.util.List<String> args) -> {
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
          if(!event.getMessage().getMentions().isEmpty()){
            meme = DataManager.getMemes(event.getMessage().getMentions().get(0).getLongID(), event.getGuild().getLongID());
          }else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.appendField("No memes available for that user", "¯\\_(ツ)_/¯", false);
            Util.sendMessage(event, builder.build());
            return;
          }
        }
      }else{
        meme = DataManager.getMeme(event.getGuild().getLongID());
      }

      if(meme != null) {
        EmbedBuilder builder = new EmbedBuilder();
        String username, memetext, timestamp, avtrURL;
        if (meme.user != -1L) {
          if(event.getGuild().getUserByID(meme.user) == null) {
            builder.appendField("No memes available for that user",
              "¯\\_(ツ)_/¯", false);
            Util.sendMessage(event, builder.build());
            return;
          }else {
            username = event.getGuild().getUserByID(meme.user).getName();
            memetext = meme.text;
            timestamp = meme.timestamp;
            avtrURL = event.getGuild().getUserByID(meme.user).getAvatarURL();
          }
          //        System.out.println("beb");
        }else{
          builder.appendField("No memes available for that user",
            "¯\\_(ツ)_/¯", false);
          Util.sendMessage(event, builder.build());
          return;
        }

        builder.withThumbnail(avtrURL);
        builder.appendField("\"" + memetext + "\" ", " *-" + username + "*", false);
        builder.withFooterText(timestamp);
        builder.withColor(new Color(112, 137, 255));
        //      Util.sendMessage(event.getChannel(), msg);
        Util.sendMessage(event, builder.build());
      }else{
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField("No memes available",
          "¯\\_(ツ)_/¯", false);
        Util.sendMessage(event, builder.build());
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
      IUser us;
      if(!event.getMessage().getMentions().isEmpty()){
        us = event.getMessage().getMentions().get(0);
      }else{
        us = event.getAuthor();
      }

      Graphics2D g = img.createGraphics();
      BufferedImage i;
      BufferedInputStream in;
      try {
        URL url;
        URLConnection uc;
        url = new URL(us.getAvatarURL().replace("webp", "jpg"));
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
          g.drawString(us.getName(), lox[id][3], lox[id][4]);
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
      Util.sendMessage(event, "", image, "lol.jpg");
    });

    ChatCommands.commandMap.put("maymay", (event, args) -> {
      Util.sendMessage(event, "maymaysss #ripmira #neverforgetti");
    });

    ChatCommands.commandMap.put("ud", (event, args) -> {
      if(args.isEmpty())
        return;

      String tx = "";

      for(String a : args)
        tx += a;

      URL url = null;

      try {
        String stuff = "http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(tx, "UTF-8");
//        System.out.println(stuff + " / " + tx);
        url = new URL(stuff);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      URLConnection conn = null;
      try {
        conn = url.openConnection();
      } catch (IOException e) {
        e.printStackTrace();
      }

      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      } catch (IOException e) {
        e.printStackTrace();
      }

      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = null;
      try {
        jsonObject = (JSONObject) jsonParser.parse(reader);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      }

      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

      JSONArray listObject = (JSONArray) jsonObject.get("list");

      JSONObject firstResult;
      EmbedBuilder e = new EmbedBuilder();

      if (listObject.isEmpty()) {
        e.appendField("Sorry, couldn't find anything",
          "¯\\_(ツ)_/¯", false);
        e.withFooterText("UrbanDictionary brought to you by JarzaBot");
        Util.sendMessage(event, e.build());
        return;
      }

      firstResult = (JSONObject) listObject.get(0);



//      System.out.println(firstResult.toJSONString());

      e.appendField(firstResult.get("word").toString(),firstResult.get("definition").toString(), false);
      e.withFooterText("UrbanDictionary brought to you by JarzaBot");
      Util.sendMessage(event, e.build());
    });
  }

}
