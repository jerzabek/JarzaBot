package main;

import com.vdurmont.emoji.EmojiManager;
import exceptions.InvalidMemeException;
import main.commands.ChatCommand;
import main.commands.ChatCommands;
import main.commands.memes.Meme;
import main.commands.moderation.Moderation;
import main.db.DataManager;
import org.json.simple.JSONObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.audit.ActionType;
import sx.blah.discord.handle.audit.entry.DiscordObjectEntry;
import sx.blah.discord.handle.audit.entry.change.ChangeKey;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessagePinEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.impl.events.guild.member.*;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RequestBuffer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChatEvents {

  @EventSubscriber public void onMessageReceived(MessageReceivedEvent event) {
    Long timea = 0L;
    if(Util.botAdmins.contains(event.getAuthor().getLongID()) && event.getAuthor().getDisplayName(event.getGuild()).endsWith("plsdebug"))
      timea = System.currentTimeMillis();
    if (event.getAuthor().isBot())
      return;
    if(!MainBot.cli.isReady())
      return;

    if(event.getMessage().getMentions().size() == 1){
      if(ChatCommands.afkusers.containsKey(event.getMessage().getMentions().get(0).getLongID())){
        EmbedBuilder e = new EmbedBuilder();
        e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
        e.withTitle(
          ChatCommands.afkusers.get(event.getMessage().getMentions().get(0).getLongID()).t);
        e.withFooterText("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode() + " feature. type '" + Util.prefix + "premium' for more info");
        e.withTimestamp(ChatCommands.afkusers.get(event.getMessage().getMentions().get(0).getLongID()).ldt);
        e.withAuthorIcon(event.getMessage().getMentions().get(0).getAvatarURL());
        e.withAuthorName("This is an automated response by "
          + event.getMessage().getMentions().get(0).getName() + "#" + event.getMessage().getMentions().get(0).getDiscriminator() + ":");
        Util.sendMessage(event, e.build());
      }
    }else{
      for(IUser x : event.getMessage().getMentions()){
        if(ChatCommands.afkusers.containsKey(x.getLongID())){
          EmbedBuilder e = new EmbedBuilder();
          e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
          e.withTitle(
            ChatCommands.afkusers.get(x.getLongID()).t);
          e.withFooterText("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode() + " feature. type '" + Util.prefix + "premium' for more info");
          e.withTimestamp(ChatCommands.afkusers.get(x.getLongID()).ldt);
          e.withAuthorIcon(x.getAvatarURL());
          e.withAuthorName("This is an automated response by "
            + event.getMessage().getMentions().get(0).getName() + "#" + event.getMessage().getMentions().get(0).getDiscriminator() + ":");
          Util.sendMessage(event, e.build());
        }
      }
    }

    int in = new Random().nextInt(1000);

    if (in <= 9) {
      if (!Util.botCommand(event.getMessage().getContent()) && event.getMessage().getFormattedContent().length() > 0) {
        //event.getAuthor().getLongID(), event.getGuild().getLongID(), event.getMessage().getFormattedContent()
        String[] atts = new String[event.getMessage().getAttachments().size()];
        for(int i = 0; i <  event.getMessage().getAttachments().size(); i++){
          atts[i] =  event.getMessage().getAttachments().get(i).getUrl();
        }
        try {
          DataManager.saveMeme(new Meme(event.getMessage().getFormattedContent(),event.getAuthor().getLongID(), event.getGuild().getLongID(), Util.toTimeStamp(event.getMessage().getTimestamp()), atts));

          System.out.println("saved `" + event.getMessage().getFormattedContent() + "` in: " + event.getGuild().getName() + " (" + event.getGuild().getLongID() + ")\n"
          + "by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getLongID() + ");\n");
        } catch (InvalidMemeException e) {
          e.printStackTrace();
        }
      }
    }
    if (!event.getMessage().getContent().toLowerCase().startsWith(Util.prefix))
      return;

    String[] msg = event.getMessage().getContent().split(" ");
    String command = msg[0].substring(Util.prefix.length());


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
      if(event.getGuild().getLongID() == 334657301862547456L)
        Util.sendMessage(event, "```\n" + e.toString()+"\n```");
    }
    if(Util.botAdmins.contains(event.getAuthor().getLongID()) && event.getAuthor().getDisplayName(event.getGuild()).endsWith("plsdebug"))
      Util.sendMessage(event, "`Took " + (System.currentTimeMillis() - timea) + "ms to complete this operation.`");
  }

  @EventSubscriber public void onReady(ReadyEvent event) {
    MainBot.cli.changePlayingText(Util.prefix + "help | " + MainBot.cli.getUsers().size() + " users");
    //Only change the nickname when the server gets premium
//    MainBot.cli.changeUsername("hund");
//    RequestBuffer.request(() -> MainBot.cli.changeUsername("hund"));
//    MainBot.cli.changeAvatar(Image.forUrl("jpg","https://pbs.twimg.com/profile_images/722564351417143296/XSCLFJPH_400x400.jpg"));
    MainBot.cli.getGuilds().forEach(i -> {
      if(DataManager.isGuildPremium(i.getLongID())) {
        if(!i.getUserByID(MainBot.cli.getOurUser().getLongID()).getDisplayName(i).equals("Hund [premium]"))
          RequestBuffer.request(() -> i.setUserNickname(MainBot.cli.getOurUser(), "Hund [premium]"));
      }else{
        RequestBuffer.request(() -> i.setUserNickname(MainBot.cli.getOurUser(), "Hund"));
      }
    });
  }

  @EventSubscriber public void onUserJoin(UserJoinEvent event){
    if(!DataManager.hasLogging(event.getGuild().getLongID()).equals(-1L)){
      EmbedBuilder e = new EmbedBuilder();
      e.withAuthorIcon(event.getUser().getAvatarURL());
      e.withAuthorName("ID: " + event.getUser().getLongID());
      e.withColor(255, 215, 0);
      e.appendField("User joined:", event.getUser() + " (" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + ")", false);
      e.withTimestamp(event.getJoinTime());
      e.withFooterText(event.getGuild().getTotalMemberCount() + " members in total.");
      Util.sendLog(event.getGuild().getChannelByID(DataManager.hasLogging(event.getGuild().getLongID())), e.build());
    }
  }

  @EventSubscriber public void onUserLeave(UserLeaveEvent event){
    if(!DataManager.hasLogging(event.getGuild().getLongID()).equals(-1L)){
      EmbedBuilder e = new EmbedBuilder();
      boolean y = false;
      IUser yu = null;
      for(DiscordObjectEntry ent : event.getGuild().getAuditLog().getDiscordObjectEntries()) {
        if (ent.getActionType().equals(ActionType.MEMBER_KICK)) {
          //ent.getTargetID()
          if(event.getUser().getLongID() == ent.getTargetID()){
            y = true;
            yu = ent.getResponsibleUser();
            break;
          }
        }
      }


      e.withAuthorIcon(event.getUser().getAvatarURL());
      e.withAuthorName((y ? "User kicked" : "User left"));
      e.withColor(255, 0, 0);
      e.appendField("User:", event.getUser() + " (" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + ")", false);
      if(y){
        e.appendField("Kicked by:", yu + " (" + yu.getName() + "#" + yu.getDiscriminator() + ")", false);
      }
      e.withTimestamp(LocalDateTime.now());
      e.withFooterText(event.getGuild().getTotalMemberCount() + " members in total. ID: " + event.getUser().getLongID());
      Util.sendLog(event.getGuild().getChannelByID(DataManager.hasLogging(event.getGuild().getLongID())), e.build());
    }
  }

  @EventSubscriber public void onUserBan(UserBanEvent event){
    if(!DataManager.hasLogging(event.getGuild().getLongID()).equals(-1L)){
      EmbedBuilder e = new EmbedBuilder();
      IUser yu = null;
      for(DiscordObjectEntry ent : event.getGuild().getAuditLog().getDiscordObjectEntries()) {
        if (ent.getActionType().equals(ActionType.MEMBER_BAN_ADD)) {
          //ent.getTargetID()
          if(event.getUser().getLongID() == ent.getTargetID()){
            yu = ent.getResponsibleUser();
            break;
          }
        }
      }
      e.withAuthorIcon(event.getUser().getAvatarURL());
      e.withAuthorName("User banned");
      e.withColor(255, 0, 0);
      e.appendField("User:", event.getUser() + " (" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + ")", false);
      e.appendField("Banned by:", yu + " (" + yu.getName() + "#" + yu.getDiscriminator() + ")", false);
      e.withTimestamp(LocalDateTime.now());
      e.withFooterText(event.getGuild().getTotalMemberCount() + " members in total.");
      Util.sendLog(event.getGuild().getChannelByID(DataManager.hasLogging(event.getGuild().getLongID())), e.build());
    }
  }

  @EventSubscriber public void onMessageEdit(MessageUpdateEvent event){
    if(event.getOldMessage() == null || event.getNewMessage() == null)
      return;
    else if(event.getOldMessage().getContent().equals(event.getNewMessage().getContent()))
      return;

    if(!DataManager.hasLogging(event.getGuild().getLongID()).equals(-1L)){
      EmbedBuilder e = new EmbedBuilder();
      e.withAuthorIcon(event.getOldMessage().getAuthor().getAvatarURL());
      e.withAuthorName("Message edited in #" + event.getOldMessage().getChannel().getName() + ":");
      e.withColor(255, 0, 0);
      e.withDescription("Message author: " + event.getOldMessage().getAuthor() + " (" + event.getOldMessage().getAuthor().getName() + "#" + event.getOldMessage().getAuthor().getDiscriminator() + ")");

      e.appendField("before:", "\"" + (event.getOldMessage().getContent().length() == 0 ? "-" : event.getOldMessage().getContent()) + "\"", false);
      e.appendField("after:", "\"" + (event.getNewMessage().getContent().length() == 0 ? "-" : event.getNewMessage().getContent()) + "\"", false);
      e.withTimestamp(LocalDateTime.now());
      Util.sendLog(event.getGuild().getChannelByID(DataManager.hasLogging(event.getGuild().getLongID())), e.build());
    }
  }

  @EventSubscriber public void onMessageDelete(MessageDeleteEvent event){
    if(!DataManager.hasLogging(event.getGuild().getLongID()).equals(-1L)){
      EmbedBuilder e = new EmbedBuilder();
      e.withAuthorIcon(event.getAuthor().getAvatarURL());
      e.withAuthorName("Message by: " + event.getMessage().getAuthor().getName() + "#" + event.getMessage().getAuthor().getDiscriminator() + " (" + event.getMessage().getAuthor().getLongID() + ")");
      e.withColor(255, 0, 0);
      e.withDescription("Deleted in: " + event.getMessage().getChannel());
      e.appendField("Message content:", (event.getMessage().getContent().length() == 0 ? "-" : event.getMessage().getContent()), false);
      e.withTimestamp(LocalDateTime.now());
      e.withFooterText("Message ID:" + event.getMessage().getLongID());
      Util.sendLog(event.getGuild().getChannelByID(DataManager.hasLogging(event.getGuild().getLongID())), e.build());
    }
  }

  @EventSubscriber public void onNickChange(NicknameChangedEvent event){
    if(!DataManager.hasLogging(event.getGuild().getLongID()).equals(-1L)){
      EmbedBuilder e = new EmbedBuilder();
      e.withTimestamp(LocalDateTime.now());
      e.withAuthorName(event.getUser().getName() + "#" + event.getUser().getDiscriminator());
      e.withAuthorIcon(event.getUser().getAvatarURL());
      e.withDescription("Old nickname: " + event.getOldNickname().orElse(event.getUser().getName()) + "\nNew nickname: " + event.getNewNickname().orElse(event.getUser().getName()));
      e.withColor(255, 215, 0);
      e.withFooterText("ID: " + event.getUser().getLongID());
      Util.sendLog(event.getGuild().getChannelByID(DataManager.hasLogging(event.getGuild().getLongID())), e.build());
    }
  }

  @EventSubscriber public void onRoleUpdate(UserRoleUpdateEvent event){
    if(!DataManager.hasLogging(event.getGuild().getLongID()).equals(-1L)){
      ArrayList<IRole> diff = new ArrayList<>();
      List<IRole> smol;
      List<IRole> big;

      boolean added;
      if(event.getNewRoles().size() > event.getOldRoles().size()){
        added = true;
        big = event.getNewRoles();
        smol = event.getOldRoles();
      }else{
        added = false;
        big = event.getOldRoles();
        smol = event.getNewRoles();
      }

      for(IRole x : big){
        if(!smol.contains(x)){
          diff.add(x);
        }
      }

      String t = "";
      if (added){
        t = "Received role" + (diff.size() > 1 ? "s: " : ": ");
      }else{
        t = "Lost role" + (diff.size() > 1 ? "s: " : ": ");
      }

      for(IRole x : diff){
        t += x.getName() + ", ";
      }
      t = t.substring(0, t.length()-2);


      EmbedBuilder e = new EmbedBuilder();
      e.withTimestamp(LocalDateTime.now());
      e.withAuthorName(event.getUser().getName() + "#" + event.getUser().getDiscriminator());
      e.withAuthorIcon(event.getUser().getAvatarURL());
      e.withColor(255, (added ? 215 : 125), 0);
      e.withDescription(t);
      e.withTimestamp(LocalDateTime.now());
      e.withFooterText("ID: " + event.getUser().getLongID());
      Util.sendLog(event.getGuild().getChannelByID(DataManager.hasLogging(event.getGuild().getLongID())), e.build());
    }

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
      IMessage last = RequestBuffer.request(() -> {
      return event.getChannel().getPinnedMessages().get(event.getChannel().getPinnedMessages().size() - 1);}).get();
      if (event.getChannel().getPinnedMessages().size() >= 3) {
        pinClear(last, event);
      }
    }
  }

  @EventSubscriber public void onReactAdd(ReactionEvent event){
    if(event.getReaction().getEmoji().getName().equals(EmojiManager.getForAlias("b").getUnicode())) {
      if (!event.getMessage().getEmbeds().isEmpty()) {
        if (!event.getReaction().getUsers().isEmpty() && event.getReaction().getUsers().size() > 2) {
          new Thread(() -> {
            //         System.out.println("doing remov");
            if (DataManager.memes.removeIf(a -> {
              if (!event.getMessage().getEmbeds().get(0).getDescription().equals("memes uwu"))
                return false;

              String nem = event.getMessage().getEmbeds().get(0).getEmbedFields().get(0).getName();
              //           System.out.println(nem.substring(1, nem.length()-1) + " -- " + ((JSONObject) a).get(Meme.TEXTF));
              return ((JSONObject) a).get(Meme.GUILDF).equals(event.getGuild().getLongID()) && ((JSONObject) a).get(Meme.TEXTF).equals(nem.substring(1, nem.length() - 1));

            })) {
              EmbedBuilder e = new EmbedBuilder();
              e.withColor(event.getMessage().getEmbeds().get(0).getColor());
              e.withDescription("meme removed ;-;");
              e.appendField(event.getMessage().getEmbeds().get(0).getEmbedFields().get(0));
              e.withThumbnail(event.getMessage().getEmbeds().get(0).getThumbnail().getUrl());
              e.withFooterText(event.getMessage().getEmbeds().get(0).getFooter().getText());
              event.getMessage().edit(e.build()).addReaction(EmojiManager.getForAlias("ok_hand"));
              //           System.out.println("done remov");
            }
            //         System.out.println("finished remov");
            //       event.getMessage().getEmbeds().get(0).getEmbedFields().get(0).getName()
          }).start();
        }
      }
    } else if(event.getReaction().getEmoji().getName().equals(EmojiManager.getForAlias("star").getUnicode())) {
      if (!event.getMessage().getEmbeds().get(0).getDescription().equals("memes uwu"))
        return;

      if (!event.getMessage().getEmbeds().isEmpty()) {
        if (!event.getReaction().getUsers().isEmpty() && event.getReaction().getUsers().size() > 5) {
          new Thread(() -> {
            EmbedBuilder e = new EmbedBuilder();
            e.withColor(event.getMessage().getEmbeds().get(0).getColor());
            e.withDescription("global meme! :tada:");
            e.appendField(event.getMessage().getEmbeds().get(0).getEmbedFields().get(0));
            e.withThumbnail(event.getMessage().getEmbeds().get(0).getThumbnail().getUrl());
            e.withFooterText(event.getMessage().getEmbeds().get(0).getFooter().getText());
            String[] atts = new String[event.getMessage().getAttachments().size()];
            for(int i = 0; i <  event.getMessage().getAttachments().size(); i++){
              atts[i] =  event.getMessage().getEmbeds().get(0).getImage().getUrl();
            }
            try {
              IEmbed emf = event.getMessage().getEmbeds().get(0);
              DataManager.saveGlobalMeme(
                new Meme(emf.getEmbedFields().get(0).getName().substring(1, emf.getEmbedFields().get(0).getName().length()-1),
                  MainBot.cli.getUsersByName(emf.getEmbedFields().get(0).getValue().substring(2, emf.getEmbedFields().get(0).getValue().length()-6), true).get(0).getLongID(),
                  event.getGuild().getLongID(), emf.getFooter().getText(), atts));

            }catch (InvalidMemeException ex){
              ex.printStackTrace();
            }
            event.getMessage().edit(e.build());
          }).start();
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
