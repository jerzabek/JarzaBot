package main;

import commands.ChatCommand;
import commands.ChatCommands;
import commands.memes.Meme;
import commands.moderation.Moderation;
import db.DataManager;
import exceptions.InvalidMemeException;
import org.json.simple.JSONObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessagePinEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ChatEvents {

  @EventSubscriber public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot())
      return;
    if(!MainBot.cli.isReady())
      return;

    int in = new Random().nextInt(100);

    if (in <= 9) {
      if (!Util.botCommand(event.getMessage().getContent()) && event.getMessage().getFormattedContent().length() > 0) {
        //event.getAuthor().getLongID(), event.getGuild().getLongID(), event.getMessage().getFormattedContent()
        String[] atts = new String[event.getMessage().getAttachments().size()];
        for(int i = 0; i <  event.getMessage().getAttachments().size(); i++){
          atts[i] =  event.getMessage().getAttachments().get(i).getUrl();
        }
        try {
          DataManager.saveMeme(new Meme(event.getMessage().getFormattedContent(),event.getAuthor().getLongID(), event.getGuild().getLongID(), Util.toTimeStamp(event.getMessage().getTimestamp()), atts));
        } catch (InvalidMemeException e) {
          e.printStackTrace();
        }
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
    try {
      if (ChatCommands.commandMap.containsKey(command)) {
        boolean t = true;
        if(event.getGuild() != null){
          if(!(Moderation.hasPermission(command, event.getAuthor(), event.getGuild().getLongID(), event.getChannel().getLongID()))){
            t = false;
          }
        }
        if(t) {
          if (!Util.gmode) {
            ChatCommands.commandMap.get(command).run(event, args);
          } else if (event.getAuthor().getLongID() == Util.jarza) {
            ChatCommands.commandMap.get(command).run(event, args);
          }
          Util.totcom++;
        }
      }else if(ChatCommands.adminMap.containsKey(command)){
        if (event.getAuthor().getLongID() == Util.jarza) {
          ChatCommands.adminMap.get(command).run(event, args);
          Util.totcom++;
        }
      }
    }catch (Throwable e){
      e.printStackTrace();
      Util.sendMessage(event, e.toString());
    }
  }

  @EventSubscriber public void onReady(ReadyEvent event) {

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
      if (event.getChannel().getPinnedMessages().size() >= 3) {
        pinClear(last, event);
      }
    }
  }

  private void pinClear(IMessage last, MessagePinEvent event){
    event.getChannel().unpin(last);
    String pin = last.getFormattedContent();
    //    msg = (pin.equals("") ? "" : "\"" + pin + "\" ") + tabs + " *-" + event.getAuthor().getDisplayName(event.getGuild()) + "*"
    EmbedBuilder bub = new EmbedBuilder();
    bub.withThumbnail(last.getAuthor().getAvatarURL());

    if(last.getEmbeds().isEmpty()) {
      if (pin.equals(""))
        bub.withFooterText("-" + last.getAuthor().getName());
      else
        bub.appendField("\"" + last.getFormattedContent() + "\" ", " *-" + last.getAuthor().getName() + "*", false);
    }else{
      last.getEmbeds().get(0).getEmbedFields().forEach(bub::appendField);
      bub.withAuthorIcon(last.getAuthor().getAvatarURL());
      bub.withAuthorName(last.getAuthor().getName());
      bub.withThumbnail(last.getEmbeds().get(0).getThumbnail().getUrl());
      bub.withFooterText("In #" + last.getChannel().getName());
    }
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
