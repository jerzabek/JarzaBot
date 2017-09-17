package main;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;

import javax.imageio.ImageIO;

import commands.ChatCommands;
import commands.memes.Meme;
import commands.moderation.Moderation;
import dataStore.DataStore;
import db.DataManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessagePinEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class ChatEvents {

  @EventSubscriber public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot ())
      return;
    if(!MainBot.cli.isReady())
      return;

    int in = new Random().nextInt(100);
    // if (!MemeCL.memes.containsKey(event.getAuthor().getLongID())) {
    // List<String> ini = new ArrayList<>();
    // MemeCL.memes.put(event.getAuthor().getLongID(), ini);
    // System.out.println("Initialized meme DB for user " + event.getAuthor().getName() + " ("
    // + event.getAuthor().getStringID() + ")");
    // if (in >= 3) {
    // if (!Util.botCommand(event.getMessage().getContent())) {
    // // RequestBuffer.request(() -> event.getChannel().sendMessage("Added ***" +
    // // event.getMessage().getContent() + "*** to ***" + event.getAuthor().getName() +
    // // "***."));
    // System.out.println("Saved '" + event.getMessage().getContent() + "' for "
    // + event.getAuthor().getName() + " (" + event.getAuthor().getStringID() + ")");
    // MemeCL.memes.get(event.getAuthor().getLongID())
    // .add(event.getMessage().getContent());
    // }
    // }
    // }
    // List<Object> memes = Database.getData(Table.memes, event);

    //    if (in > 11) {
    //      if (!Util.botCommand(event.getMessage().getContent())) {
    //        // RequestBuffer.request(() -> event.getChannel().sendMessage("Added ***" +
    //        // event.getMessage().getContent() + "*** to ***" + event.getAuthor().getName() + "***."));
    //        System.out.println("Saved '" + event.getMessage().getContent() + "' for "
    //            + event.getAuthor().getName() + " (" + event.getAuthor().getStringID() + ")");
    //
    //
    //        // MemeCL.memes.get(event.getAuthor().getLongID()).add(event.getMessage().getContent());
    ////        System.out.println("insert into memes (guild, user, text) values ("
    ////            + event.getGuild().getLongID() + ", " + event.getAuthor().getLongID() + ", "
    ////            + event.getMessage().getFormattedContent() + ")");
    //
    //
    ////        Database.putData("insert into memes (guild, user, text) values ("
    ////            + event.getGuild().getLongID() + ", " + event.getAuthor().getLongID() + ", q'["
    ////            + event.getMessage().getFormattedContent() + "]');", event);
    //      }
    //    }

    if (in <= 8) {
      if (!Util.botCommand(event.getMessage().getContent()) && event.getMessage().getFormattedContent().length() > 0) {
        //event.getAuthor().getLongID(), event.getGuild().getLongID(), event.getMessage().getFormattedContent()
        String[] atts = new String[event.getMessage().getAttachments().size()];
        for(int i = 0; i <  event.getMessage().getAttachments().size(); i++){
          atts[i] =  event.getMessage().getAttachments().get(i).getUrl();
        }
        DataManager.saveMeme(new Meme(event.getMessage().getFormattedContent(),event.getAuthor().getLongID(), event.getGuild().getLongID(), Util.toTimeStamp(event.getMessage().getTimestamp()), atts));
      }
    }
    if (!event.getMessage().getContent().startsWith(Util.prefix))
      return;

    String[] msg = event.getMessage().getContent().split(" ");
    String command = msg[0].substring(2);


    ArrayList<String> args = new ArrayList<String>(Arrays.asList(msg));
    args.remove(0);


    if (msg.length == 0)
      return;

    if (ChatCommands.commandMap.containsKey(command) && Moderation.hasPermission(command, event.getAuthor(), event.getGuild().getLongID(), event.getChannel().getLongID())) {
      if (!Util.gmode) {
        ChatCommands.commandMap.get(command).run(event, args);
      } else if (event.getAuthor().getLongID() == Util.jarza) {
        ChatCommands.commandMap.get(command).run(event, args);
      }
      Util.totcom++;
    }

  }

  @EventSubscriber public void onReady(ReadyEvent event) {
    DataManager.init();
  }

  @EventSubscriber public void onGuildCreate(GuildCreateEvent event) {

  }

  @EventSubscriber public void onLeave(DisconnectedEvent event) {
    System.out.println("offline :<");
  }

  @EventSubscriber public void onMessagePinned(MessagePinEvent event) {
    if(!MainBot.cli.isReady())
      return;
    if(!DataManager.getPinbu(event.getGuild().getLongID()).equals(0)) {
      IMessage last = event.getChannel().getPinnedMessages().get(event.getChannel().getPinnedMessages().size() - 1);
      if (event.getChannel().getPinnedMessages().size() >= 45) {
        if (Util.gmode) {
          if (event.getGuild().getLongID() == Util.testserver) {
            pinClear(last, event);
          }
        } else {
          pinClear(last, event);
        }
      }
    }
  }

  private void pinClear(IMessage last, MessagePinEvent event){
    event.getChannel().unpin(last);

    String pin = last.getFormattedContent();
    //    msg = (pin.equals("") ? "" : "\"" + pin + "\" ") + tabs + " *-" + event.getAuthor().getDisplayName(event.getGuild()) + "*"
    EmbedBuilder bub = new EmbedBuilder();
    bub.withThumbnail(last.getAuthor().getAvatarURL());
    bub.appendField((pin.equals("") ? "-" : "\"" + pin + "\" "), " *-" + last.getAuthor().getName() + "*", false);
    if (last.getAttachments().size() > 0) {
      for (IMessage.Attachment a : last.getAttachments()) {
        bub.withImage(a.getUrl());
      }
    }
    RequestBuffer.request(() -> {
      try {
        event.getGuild().getChannelByID(DataManager.getPinbu(event.getGuild().getLongID())).sendMessage(bub.build());
      } catch (DiscordException e) {
        System.err.println("Hmmm shit went sideways... Here's why: ");
        e.printStackTrace();
      }
    });
  }


}
