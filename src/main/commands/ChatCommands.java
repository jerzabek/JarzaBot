package main.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.vdurmont.emoji.EmojiManager;
import main.MainBot;
import main.Util;
import main.commands.moderation.Permission;
import main.commands.music.GuildMusicManager;
import main.db.DataManager;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.audit.ActionType;
import sx.blah.discord.handle.audit.entry.AuditLogEntry;
import sx.blah.discord.handle.audit.entry.DiscordObjectEntry;
import sx.blah.discord.handle.audit.entry.TargetedEntry;
import sx.blah.discord.handle.audit.entry.change.ChangeKey;
import sx.blah.discord.handle.audit.entry.option.OptionKey;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatCommands {

  public static Map<String, ChatCommand> commandMap = new HashMap<>();
  public static Map<String, ChatCommand> adminMap = new HashMap<>();
  private static AudioPlayerManager playerManager;
  public static Map<Long, AfkObject> afkusers = new HashMap<>();
  private static Map<Long, GuildMusicManager> musicManagers;
  private static String[] exampleAfk = {"AFK", "Gone fishin'", "Not here atm", "oof user temporarily unavailiable", "currently dabbing on my haters, will be right back", "ded af rn, gimme a minute"};


  public static synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild) {
    long guildId = Long.parseLong(guild.getLongID() + "");
    GuildMusicManager musicManager = musicManagers.get(guildId);

    if (musicManager == null) {
      musicManager = new GuildMusicManager(playerManager);
      musicManagers.put(guildId, musicManager);
    }

    guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

    return musicManager;
  }

  public static void loadAndPlay(MessageReceivedEvent event, final String trackUrl) {
    GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());

    playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
//        sendMessageToChannel(channel, "Adding to queue " + track.getInfo().title);
        Util.sendMessage(event, ">Added " + track.getInfo().title + " to queue.");
        play(musicManager, track, event);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();
        if (firstTrack == null) {
          firstTrack = playlist.getTracks().get(0);
        }

        Util.sendMessage(event, ">Added " + firstTrack.getInfo().title + " to queue.");
//        sendMessageToChannel(channel, "Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")");
        play(musicManager, firstTrack, event);
      }

      @Override
      public void noMatches() {
//        sendMessageToChannel(channel, "Nothing found by " + trackUrl);
        Util.sendMessage(event, "Couldn't find anything for: " + trackUrl);
      }

      @Override
      public void loadFailed(FriendlyException exception) {
//        sendMessageToChannel(channel, "Could not play: " + exception.getMessage());
        Util.sendMessage(event, "Something went to heck");
      }
    });
  }

  public static void play(GuildMusicManager musicManager, AudioTrack track, MessageReceivedEvent event) {
//    connectToFirstVoiceChannel(guild.getAudioManager());
    track.setUserData(event);
    musicManager.scheduler.queue(track);
//    System.out.println("Sender response:\n" + Sender.sendTing(new String[]{track.getInfo().title, track.getInfo().author, event.getGuild().getLongID() + ""}) + "\n//END");
  }

  public static void init() {
    musicManagers = new HashMap<>();
//
    playerManager = new DefaultAudioPlayerManager();
    playerManager.setPlayerCleanupThreshold(20000L);

    AudioSourceManagers.registerRemoteSources(playerManager);
    AudioSourceManagers.registerLocalSource(playerManager);

    ChatCommands.commandMap.put("rule", (event, args) -> {
      //j.rule say deny general jarza
      // OR
      //j.rule say deny jarza genearl
      boolean cont = false;
      for(Long a : DataManager.getModrole(event.getGuild().getLongID())){
        if (event.getAuthor().getRolesForGuild(event.getGuild()).contains(event.getGuild().getRoleByID(a))){
          cont = true;
        }
      }
      if (!cont) {
        Util.sendMessage(event, "**>Error: inssuficient permission**");
        return;
      }
      if(DataManager.getPerms(event.getGuild().getLongID()).size() >= 25) {
        Util.sendMessage(event, "Error: you can only have up to 25 permissions.");
        return;
      }
      if(args.isEmpty())
        return;
      if(args.size() == 1)
        return;


      DataManager.setPermission(Permission.commandParse(args, event.getGuild().getLongID(), event), event.getGuild().getLongID());
    });

    ChatCommands.adminMap.put("logoff", (event, args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        Util.sendMessage(event, "Turning off...");
        RequestBuffer.request(() -> MainBot.cli.logout());
        DataManager.finish();
        System.exit(0);
      }
    });

    ChatCommands.adminMap.put("godmode", (event, args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        Util.gmode = !Util.gmode;
        if (Util.gmode) {
          Util.sendMessage(event, "GodMode On - Only jaja can do commands now hehehe");
        } else {
          Util.sendMessage(event, "GodMode Off - *just don't spam too much pls*");
        }
      }
    });

    ChatCommands.commandMap.put("invite", (event, args) -> {
      Util.sendMessage(event,  "Invite link for Hund: " + Util.link);
    });

    ChatCommands.adminMap.put("game", (MessageReceivedEvent event, List<String> args) -> {
      if (event.getAuthor().getLongID() == Util.jarza) {
        if (args.size() != 0) {
          String text = "";
          for (Object a : args.toArray()) {
            text += " " + a;
          }
          Util.sendMessage(event, "Changed game status to: " + text);
          MainBot.cli.changePlayingText(text);
        }
      }
    });

    commandMap.put("about", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      builder.appendField("Hund Bot!", String.format("Version %1$s", Util.version), true);
      builder.appendField("Commands used so far:", "" + Util.totcom, true);
      builder.appendField("How many servers have me?", MainBot.cli.getGuilds().size() + " so far!", true);
      String i = "";
      for(Long a : Util.botAdmins){
        i += MainBot.cli.getUserByID(a) + ", ";
      }
      i = i.substring(0, i.length()-2);
      builder.appendField("Bot admins: ", i, false);
      builder.withAuthorName(event.getAuthor().getName());
      builder.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
      builder.withAuthorIcon(event.getAuthor().getAvatarURL());
      builder.withFooterText("Still under development! Made by jarza#1289");
      builder.withThumbnail(MainBot.cli.getOurUser().getAvatarURL());

      Util.sendMessage(event, builder.build());
    });

    commandMap.put("help", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      if(args.isEmpty()){
        builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version),
          true);
        builder.withAuthorName("Hund Bot Manual!");
        builder.withAuthorIcon(event.getAuthor().getAvatarURL());
        builder.withFooterText("Still under development! uwu");
        builder.withColor(event.getAuthor().getColorForGuild(event.getGuild()));

        for (String a : Util.catnames) {
          String comms = "";
          for(String c : Util.cats.get(a).keySet()) {
            if(!c.equalsIgnoreCase(a)) {
              //              comms += "`" + c + "`, ";
              comms += (Util.premcoms.contains(c) ? "***`" + c + "`***, " : "`" + c + "`, ");
            }
          }
          comms = comms.substring(0, comms.length()-2);
          builder.appendField(a, comms, false);
        }
        Util.sendMessage(event, builder.build());
      }else if(args.size() == 1){
        builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version),
          true);
        builder.withAuthorName("Hund Bot Manual!");
        builder.withAuthorIcon(event.getAuthor().getAvatarURL());
        builder.withFooterText("Still under development! uwu");
        builder.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
        String cmd = null, info = null;
        for (String a : Util.catnames) {
          for(String c : Util.cats.get(a).keySet()) {
            if(c.equals(args.get(0).toLowerCase())){
              cmd = c;
              info = Util.cats.get(a).get(c);
            }
          }
        }

        if(cmd == null)
          return;

        builder.appendField(cmd, info, false);
        Util.sendMessage(event, builder.build());
      }
    });

    commandMap.put("say", (MessageReceivedEvent event, List<String> args) -> {
      String text = event.getMessage().getFormattedContent().substring((Util.prefix.length() + 4), event.getMessage().getFormattedContent().length());

//      if(!event.getMessage().getMentions().isEmpty() || !event.getMessage().getRoleMentions().isEmpty() || event.getMessage().getFormattedContent().contains("@here")){
//        Util.sendMessage(event, "`no heck you`");
//        System.out.println(event.getAuthor().getName() + " tried to say: " + text + " || " + event.getMessage().getFormattedContent());
//        return;
//      }
      System.out.println(event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator()
        + "(" + event.getAuthor().getLongID() + ") said: <" + text + "> in: " + event.getGuild().getName() + " (" + event.getGuild().getLongID() + ")");
      try {
        if (event.getGuild() != null)
          event.getMessage().delete();
      }catch (Throwable e){
        e.printStackTrace();
      }
      Object[] e = Arrays.stream(text.split(" ")).map(a -> {
        if(a.contains("@")) {
          return a.replace("@", " at ");
        }else
          return a;
      }).toArray();
      String doof = "";
      for(Object a : e){
        doof += (String) a + " ";
      }
      Util.sendMessage(event, doof);
    });

    commandMap.put("satansbae", (event, args) -> {
      String song =
          "*Archangel*\n*Darkangel*\n*Bring us elevation*\n*Through hell and through heaven*\n***Until we reach ascention***";
      Util.sendMessage(event, song);
    });

    commandMap.put("disable", (event, args) -> {
      if(args.isEmpty())
        return;

      Long g;
      try{
        g = Long.parseLong(args.get(0));
      }catch(NumberFormatException e){return;}

      DataManager.setUserNotifi(g, event.getAuthor().getLongID());
    });

    adminMap.put("write", (event, args) -> {
      MainBot.writer.run();
    });
//
//    adminMap.put("join", (event, args) -> {
//      event.getGuild().getVoiceChannelByID(334657409178009600L).join();
//    });


    commandMap.put("p", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      if(args.isEmpty())
        return;
      IVoiceChannel c = null;
      for(IVoiceChannel a : event.getGuild().getVoiceChannels()) {
        if(a.getConnectedUsers().contains(event.getAuthor())){
          c = a;
        }
      }
      String q = event.getMessage().getFormattedContent().replace(Util.prefix + "p ", "");
      try{
        new URL(q);
      }catch(Throwable e){
        q = "ytsearch:".concat(q);
      }
      if(c != null) {
        c.join();
      }
      loadAndPlay(event, q);
      if(getGuildAudioPlayer(event.getGuild()).player.isPaused())
        Util.sendMessage(event, "*Hint: unpause the player with the `pause` command to hear your audio*");
    });

    commandMap.put("pause", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      musicManager.player.setPaused(!musicManager.player.isPaused());

    });

    commandMap.put("skip", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      musicManager.scheduler.nextTrack();
      EmbedBuilder e = new EmbedBuilder();
      e.withFooterText("Hund music player");
      e.withColor(0, 220, 0);
      e.withDescription("Track was skipped!");
      Util.sendMessage(event, e.build());
    });

    commandMap.put("join", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      IVoiceChannel c = null;
      for(IVoiceChannel a : event.getGuild().getVoiceChannels()) {
        if(a.getConnectedUsers().contains(event.getAuthor())){
          c = a;
        }
      }
      if(c != null){
        c.join();
//        GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      }else{
        Util.sendMessage(event, ">You must be in a voice channel first!");
      }
    });

    commandMap.put("leave", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      musicManager.player.getPlayingTrack().stop();
      event.getGuild().getConnectedVoiceChannel().leave();
    });

    commandMap.put("q", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      EmbedBuilder e = new EmbedBuilder();
      e.withFooterText("Hund music player");
//      String queuelist = "```css\n";

      if (musicManager.player.getPlayingTrack() != null) {
        e.appendField(":arrow_forward: Current track: ", musicManager.player.getPlayingTrack().getInfo().title + " by " + musicManager.player.getPlayingTrack().getInfo().author, false);
        //          queuelist += (":arrow_forward: Current track:\n" + musicManager.player.getPlayingTrack().getInfo().title + " {" + musicManager.player.getPlayingTrack().getInfo().author + "}");
      }
      //        queuelist += "\nNext up:";
      int cout;
      if (args.isEmpty())
        cout = 1;
      else {
        try {
          cout = Integer.parseInt(args.get(0));
          if (cout < 1 || cout > musicManager.scheduler.getQueuedTracks().size()) {
            e.withDescription("That page is not availiable.");

            Util.sendMessage(event, e.build());
            return;
          }
        } catch (NumberFormatException exc) {
          cout = 0;
        }
      }
      int oc = cout;
      String songs = "";
//      System.out.println(musicManager.scheduler.getQueuedTracks().size());
      if(musicManager.scheduler.getQueuedTracks().isEmpty()){
        e.withColor(220, 0, 0);
        e.withDescription("Queue is empty.");
        Util.sendMessage(event, e.build());
      }
      if (!(cout <= musicManager.scheduler.getQueuedTracks().size())) {

        return;
      }
      if(!args.isEmpty())
        try{
          Integer.parseInt(args.get(0));
        }catch(Throwable thr){
          return;
        }

      String temp;
      for (int i = cout - 1; i < oc + 5; i++) {
        if (i >= musicManager.scheduler.getQueuedTracks().size()) {
          break;
        }
        AudioTrack a = musicManager.scheduler.getQueuedTracks().get(i);
        //        e.appendField("#" + cout, a.getInfo().title + " by " + a.getInfo().author, false);
        String inser = "#" + (i+1) + ": " + a.getInfo().title + " {" + a.getInfo().author + "}\n";

        if(songs.length() + inser.length() > 1024)
          break;

        temp = songs + inser;
        //
        songs = temp;

        //          if(temp.length() < 256){
        //
        //          }else{
        //            break;
        //          }
        //          queuelist += ("\n#" + cout + ": " + a.getInfo().title + " {" + a.getInfo().author + "}");
        cout++;

      }

      if (e.getFieldCount() == 0) {
        e.withColor(220, 0, 0);
        e.withDescription("Queue is empty.");
        Util.sendMessage(event, e.build());
        return;
      } else {
        e.appendField("Next up:", songs.length() == 0 ? "." : songs, false);
        e.withColor(0, 220, 0);
      }

      Util.sendMessage(event, e.build());
      //      Util.sendMessage(event, queuelist + "\n```");
    });

    commandMap.put("qr", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      int i;
      try{
        i = Integer.parseInt(args.get(0));
      }catch(NumberFormatException ex){
        return;
      }
      i--;
      EmbedBuilder e = new EmbedBuilder();
      if(i < 0 || i >= musicManager.scheduler.getQueuedTracks().size()){
        e.withDescription("Not that many tracks in the queue.");
        e.withColor(220, 0, 0);
      }else {
        if (!musicManager.player.getPlayingTrack().equals(musicManager.scheduler.getQueuedTracks().get(i))) {
          String title = musicManager.scheduler.getQueuedTracks().get(i).getInfo().title;
          musicManager.scheduler.remove(i);
          e.withDescription("Successfully removed " + title);
          e.withColor(0, 220, 0);
        } else {
          e.withDescription("Can't remove current track from queue. But if you want you can skip it.");
          e.withColor(220, 0, 0);
        }
      }
      Util.sendMessage(event, e.build());
    });

    commandMap.put("playing", (event, args) -> {
      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      EmbedBuilder e = new EmbedBuilder();
      e.withFooterText("Hund music player");
      e.withColor(0, 220, 0);
      //      String queuelist = "```css\n";

      if (musicManager.player.getPlayingTrack() != null) {
        e.appendField(":arrow_forward: Current track: ", musicManager.player.getPlayingTrack().getInfo().title + " by " + musicManager.player.getPlayingTrack().getInfo().author, false);
        e.withDesc(String.format("%02d:%02d/%02d:%02d",
          TimeUnit.MILLISECONDS.toMinutes(musicManager.player.getPlayingTrack().getPosition()),
          TimeUnit.MILLISECONDS.toSeconds(musicManager.player.getPlayingTrack().getPosition()) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(musicManager.player.getPlayingTrack().getPosition())),
          TimeUnit.MILLISECONDS.toMinutes(musicManager.player.getPlayingTrack().getDuration()),
          TimeUnit.MILLISECONDS.toSeconds(musicManager.player.getPlayingTrack().getDuration()) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(musicManager.player.getPlayingTrack().getDuration()))
        ));
        //          queuelist += (":arrow_forward: Current track:\n" + musicManager.player.getPlayingTrack().getInfo().title + " {" + musicManager.player.getPlayingTrack().getInfo().author + "}");
      }else{
        e.withDescription("Nothing is playing right now :<");
      }

      Util.sendMessage(event, e.build());
    });


    commandMap.put("pause", (event, args) -> {
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
        musicManager.player.setPaused(!musicManager.player.isPaused());
    });

    commandMap.put("serverinfo", (event, args) -> {
      EmbedBuilder e = new EmbedBuilder();
      IGuild g = event.getGuild();
      e.withDescription(g.getName());
      e.withThumbnail(g.getIconURL());
      e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
      e.appendField("Members:", "" + g.getTotalMemberCount(), true);
      e.appendField("Owner:", g.getOwner() + "", true);
      e.appendField("Server created:", g.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")), false);
      e.appendField("Channels:", g.getChannels().size() + " text/" + g.getVoiceChannels().size() + " voice", true);
      e.appendField("Roles:", "" + (g.getRoles().size()-1), true);
      e.appendField("ID:", g.getLongID() + "", true);
      e.withFooterText("thank you for choosing Hund");
      Util.sendMessage(event, e.build());
    });

    commandMap.put("info", (event, args) -> {
      EmbedBuilder e = new EmbedBuilder();

      IUser g;

      if(event.getMessage().getMentions().isEmpty())
        g = event.getAuthor();
      else
        g = event.getMessage().getMentions().get(0);

      e.withDescription(g.getName() + "#" + g.getDiscriminator() + (g.getNicknameForGuild(event.getGuild()) != null ? (" (nickname: '" + g.getNicknameForGuild(event.getGuild()) + "')") : ""));
      e.withThumbnail(g.getAvatarURL());
      e.appendField("Joined server:", event.getGuild().getJoinTimeForUser(g).format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")), true);
      e.appendField("User created account:", g.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")), true);
      e.withColor(g.getColorForGuild(event.getGuild()));
      e.appendField("Roles:", g.getRolesForGuild(event.getGuild()).size() + "", true);
      e.appendField("ID:", g.getLongID() + "", true);
      Util.sendMessage(event, e.build());
    });

    Map<String, String> vals = new HashMap<>();
    vals.put("m", "-3");
    vals.put("d", "-1");
    vals.put("c", "-2");
    vals.put("k", "3");

    Map<String, String> imper = new HashMap<>();
    imper.put("foot", "12");
    imper.put("yard", "36");
    imper.put("mile", "63360");

    commandMap.put("convertdist", (MessageReceivedEvent event, List<String> args) -> {
      if(args.isEmpty() || args.size() != 3)
        return;
      try{
          double val = Double.parseDouble(args.get(0));
          double ret = val;
          String initial = args.get(1).toLowerCase();
          String wanted = args.get(2).toLowerCase();

//          Util.sendMessage(event, val + "/" + initial + "/" + wanted);
          int potenta = 0, potentb = 0;
          try {
            potenta = (initial.length() > 1 ?
              (initial.length() == 3 ? Integer.parseInt(vals.get(String.valueOf(initial.charAt(0)))) * Integer.parseInt(vals.get(String.valueOf(initial.charAt(2)))) : Integer.parseInt(vals.get(String.valueOf(initial.charAt(0))))) :
              0);
          }catch(Throwable ey){ }

          try {
            potentb = (wanted.length() > 1 ? (wanted.length() == 3 ?
              Integer.parseInt(vals.get(String.valueOf(wanted.charAt(0))))*Integer.parseInt(vals.get(String.valueOf(wanted.charAt(2))))
              : Integer.parseInt(vals.get(String.valueOf(wanted.charAt(0))))) : 0);
          }catch(Throwable ey){ }

          int dif = Math.abs(potentb-potenta);

          String unita = (initial.length() == 1 ? initial : String.valueOf(initial.charAt(1))).toLowerCase();
          String unitb = (wanted.length() == 1 ? wanted : String.valueOf(wanted.charAt(1))).toLowerCase();

          if(unita.equals(unitb) && !imper.containsKey(unita) && !imper.containsKey(unitb)){
            if (potenta > potentb){
              ret *= Math.pow(10, dif);
            }else if (potentb > potenta){
              ret /= Math.pow(10, dif);
            }
          } else {


            if(imper.containsKey(initial)){
              double ininches = val*Integer.parseInt(imper.get(initial));
              if(imper.containsKey(wanted)){
                ret = ininches/Double.parseDouble(imper.get(wanted));
              }else{
                 double incm = ininches*2.54d;

                if (potentb > 0){
                  ret *= Math.pow(10, dif);
                }else if (potentb < 0){
                  ret /= Math.pow(10, dif);
                }
              }
            }else {
              double incm = 2.54d;

              if (potenta > 0){
                incm = val/Math.pow(10, potenta);
              }else if (potenta < 0){
                incm = val*Math.pow(10, potenta);
              }

              if(imper.containsKey(wanted)){
                double ininches = incm/2.54d;
                ret = ininches*Double.parseDouble(imper.get(wanted));
              }else{
                if(Integer.parseInt(vals.get(unitb)) > 0){
                  ret = incm * Integer.parseInt(vals.get(unitb));
                }else if(Integer.parseInt(vals.get(unitb)) < 0){
                  ret = incm / Integer.parseInt(vals.get(unitb));
                }
              }
            }
          }

        Util.sendMessage(event, String.format("%.5f %s is equal to %.5f in %s", val, initial, ret, wanted).concat(" *(like maybe i am not 100% sure ¯\\_(ツ)_/¯)*"));
      } catch(Throwable e){
        e.printStackTrace();
      }
    });

    commandMap.put("convert", (MessageReceivedEvent event, List<String> args) -> {
      if (args.isEmpty() || args.size() != 3)
        return;

      if(!DataManager.isGuildPremium(event.getGuild().getLongID()) || !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), "If you want to use this and many other super spicy and exclusive features donate.", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }

      try
      {
        HttpPost httpPost = new HttpPost("https://neutrinoapi.com/convert");
        List<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("user-id", "jarzaowo"));
        postData.add(new BasicNameValuePair("api-key", "6sdRvZJLbwmAN9xjseUmsva7fAcoqedFVTfYoFAQz0kTKmru"));
        postData.add(new BasicNameValuePair("from-value", args.get(0)));
        postData.add(new BasicNameValuePair("from-type", args.get(1).toUpperCase()));
        postData.add(new BasicNameValuePair("to-type", args.get(2).toUpperCase()));

        httpPost.setEntity(new UrlEncodedFormEntity(postData));

        HttpResponse response = HttpClients.createDefault().execute(httpPost);
        String jsonStr = EntityUtils.toString(response.getEntity());

        JSONObject json = new JSONObject(jsonStr);
        EmbedBuilder e = new EmbedBuilder();
        e.withTitle(String.format("%s %s = %s %s",
          args.get(0), args.get(1).toUpperCase(),json.get("result").toString(), json.get("to-type").toString().toUpperCase()));
        e.withFooterText("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode());
        e.withColor(255,215,0);
        Util.sendMessage(event, e.build());
      }
      catch (Throwable ex)
      {
        ex.printStackTrace();
      }
    });

    commandMap.put("premium", (MessageReceivedEvent event, List<String> args) -> {
      EmbedBuilder e = new EmbedBuilder();
      e.appendField("What is Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(),
        "Premium is a package only for the richest of users that have donated money to keep me and jarza alive. thank."
          + " Premium allows you to use plenty of features regular peasants can only dream of.", true);
      if(event.getMessage().getMentions().size() == 1){
        IUser tag = event.getMessage().getMentions().get(0);
        e.appendField(tag.getName() + "#" + tag.getDiscriminator(),
          (DataManager.isUserPremium(tag.getLongID()) ? String.format("Yes, %s is a premium user! They are also super cool and better than you :)", tag.getName()) :
            String.format("Nope, unfortunately %s isn't premium :(", tag.getName())), true);
      }else{
        e.appendField("Is this server premium?",
          (DataManager.isGuildPremium(event.getGuild().getLongID()) ? String.format("Yes! %s is a Premium guild! Be very grateful to whoever paid for this shit", event.getGuild().getName())
            : "Nah, this server isn't ~~cool~~ premium. DM the owner and tell him to buy Hund premium" + EmojiManager.getForAlias("tm").getUnicode() + " thanks!"), true);
      }
      e.appendField("Premium is availiable for", String.format("%d users and %d servers!", DataManager.getPremiumUserCount(), DataManager.getPremiumGuildCount()), true);
      e.withColor(255, 215, 0);

      Util.sendMessage(event, e.build());
    });

    adminMap.put("makepremium",  (MessageReceivedEvent event, List<String> args) -> {
      EmbedBuilder e = new EmbedBuilder();
      e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
      if(event.getMessage().getMentions().isEmpty()){
        boolean des = !DataManager.isGuildPremium(event.getGuild().getLongID());
        DataManager.setPremiumForGuild(event.getGuild().getLongID(), des);
        if(des)
          e.appendField("Congratulations!" + EmojiManager.getForAlias("tada").getUnicode(), "This server is now a super dank one! wew\nThanks for contributing!", false);
        else
          e.appendField("Ouchies", "Lol you lost Hund Premium, get fucked LUL",false);
      }else{
        boolean des = !DataManager.isUserPremium(event.getMessage().getMentions().get(0).getLongID());
        DataManager.setUserPremium(event.getMessage().getMentions().get(0).getLongID(), des, event.getMessage().getTimestamp().toLocalDate().toString());
        if(des)
          e.appendField("Congratulations!" + EmojiManager.getForAlias("tada").getUnicode(), event.getMessage().getMentions().get(0) + " is now super cool! wew\nThanks for contributing!", false);
        else
          e.appendField("Ouchies", "Lol you lost Hund Premium, get fucked LUL",false);
      }

      e.withFooterText(event.getMessage().getTimestamp().toLocalDate().toString());
      Util.sendMessage(event, e.build());
    });

    commandMap.put("discrim",  (MessageReceivedEvent event, List<String> args) -> {
      String disc;
      Map<String, Long> tops = new HashMap<>();;
      int max = 0;
      if(!args.isEmpty()) {
        try {
          if (args.get(0).length() != 4)
            return;

          disc = args.get(0);

        } catch (Throwable ex) {
          ex.printStackTrace();
          return;
        }
        if (args.size() == 2) {
          try {
            max = Integer.parseInt(args.get(1));
            if (max <= 0) {
              return;
            }
          } catch (Throwable e) {
            e.printStackTrace();
            return;
          }
        }
      }else{
        disc = "oof";

        if(!DataManager.isGuildPremium(event.getGuild().getLongID()) && !DataManager.isUserPremium(event.getAuthor().getLongID())) {
          EmbedBuilder e = new EmbedBuilder();
          e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(),
            "You have to search a discriminator by including it at the end of the command!\nBut if you wanted to see what the most used discriminator is "
              + "you should get Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(), false);
          e.withColor(255, 215, 0);
          Util.sendMessage(event, e.build());
          return;
        }

        // asdf
      }
      EmbedBuilder e = new EmbedBuilder();
      final String[] des = {""};
      final int maxf = max;
      final int[] c = {0, 0};
      event.getGuild().getUsers().forEach(a -> {
        if(disc.equals("oof")){
          if(tops.containsKey(a.getDiscriminator())){
            tops.put(a.getDiscriminator(), tops.get(a.getDiscriminator()) + 1L);
          }else{
            tops.put(a.getDiscriminator(), 1L);
          }
        }else if(a.getDiscriminator().equals(disc)){
          if((des[0] + a + "\n").length() < 2048 && (maxf == 0 || ((maxf > 0) && (c[1] < maxf)))) {
            des[0] += a + " - (" + a.getName() + "#" + a.getDiscriminator() + ")\n";
            c[1]++;
          }
          c[0]++;
        }
      });
      if(!disc.equals("oof")) {
        e.withTitle(String.format("Found %d %s with the %s discriminator. Here %s %d of them.", c[0], (c[0] > 1 ? "users" : "user"), disc, (c[1] > 1 ? "are" : "is"), c[1]));
      }else{
        Long maxd = 0L;
        String maxdis = "";
        for(Map.Entry<String, Long> t : tops.entrySet()){
          if(t.getValue() > maxd) {
            maxd = t.getValue();
            maxdis = t.getKey().toString();
          }
        }

        if(maxd > 1)
          e.appendField(String.format("%s is the most common discriminator, used by %d people", maxdis, maxd),
            String.format("That is actually %1.4f%2s of all of the users in this server", maxd/(double)event.getGuild().getTotalMemberCount(), "%"), false);
        else
          e.withTitle("Everyone has a unique discriminator. You're all special little snowflakes.");
      }
      if(c[0] > 0)
        e.withDesc(des[0]);
      e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
      //gold - 255,215,0
      Util.sendMessage(event, e.build());
    });

    commandMap.put("roll",  (MessageReceivedEvent event, List<String> args) -> {
      int n = 0;
      if(args.isEmpty()) {
        n = new Random().nextInt(6) + 1;
      }else{
        try {
          if(Integer.parseInt(args.get(0)) <= 0)
            return;
          n = new Random().nextInt(Integer.parseInt(args.get(0)) - 1) + 1;
        }catch(Throwable e){
          e.printStackTrace();

          n = new Random().nextInt(6) + 1;
        }
      }

      EmbedBuilder e = new EmbedBuilder();
      e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
      e.withTitle(String.format("You rolled a %d!", n)); // <a:dice:410144743373406213>
      e.withThumbnail("http://moziru.com/images/dice-clipart-animated-gif-19.gif");
      RequestBuffer.request(() -> MainBot.cli.changeUsername("hund"));
      Util.sendMessage(event, e.build());
    });

    commandMap.put("ar", (MessageReceivedEvent event, List<String> args) -> {
      if (!DataManager.isGuildPremium(event.getGuild().getLongID()) && !DataManager.isUserPremium(event.getAuthor().getLongID())) {
        EmbedBuilder e = new EmbedBuilder();
        e.appendField("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode(),
          "This is a Premium" + EmojiManager.getForAlias("tm").getUnicode() + " command, and it seems that neither you nor this server have the Premium package."
            + " oof pls buy it for more exclusive and cool stuff like this", false);
        e.withColor(255, 215, 0);
        Util.sendMessage(event, e.build());
        return;
      }

      if(afkusers.containsKey(event.getAuthor().getLongID())){
        afkusers.remove(event.getAuthor().getLongID());
        EmbedBuilder e = new EmbedBuilder();
        e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
        e.withTitle("Autoresponder off " + EmojiManager.getForAlias("thumbsup").getUnicode());
        e.withFooterText("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode() + " feature. type '" + Util.prefix + "premium' for more info");
        Util.sendMessage(event, e.build());
      }else{
        String t;
        if(args.isEmpty()){
          t = exampleAfk[new Random().nextInt(exampleAfk.length)];
        }else{
          t = event.getMessage().getContent().substring((Util.prefix + "ar ").length());
        }
        afkusers.put(event.getAuthor().getLongID(), new AfkObject(event.getMessage().getTimestamp(), t));
        EmbedBuilder e = new EmbedBuilder();
        e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
        e.withTitle("Set autoresponder status to: \"" + t + "\". Whenever someone tags you I will respond with that message. Just use "
          + Util.prefix + "ar to turn off this feature!");
        e.withFooterText("Hund Premium" + EmojiManager.getForAlias("tm").getUnicode() + " feature. type '" + Util.prefix + "premium' for more info");
        Util.sendMessage(event, e.build());

      }
    });

    adminMap.put("memeinfo", (MessageReceivedEvent event, List<String> args) -> {
      int mc = DataManager.memes.size();
      
      int gmc = DataManager.globalmemes.size();
      Long ggmc = DataManager.memes.stream().filter(n -> ((org.json.simple.JSONObject) n).get("guild").equals(event.getGuild().getLongID() )).count();

      EmbedBuilder e = new EmbedBuilder();

      e.appendField("Total number of memes:", mc + "", false);
      e.appendField("Number of memes in this guild:", ggmc + "", false);
      e.appendField("Total number of global memes:", gmc + "", false);

      e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
      Util.sendMessage(event, e.build());
    });

    adminMap.put("listservers",  (MessageReceivedEvent event, List<String> args) -> {
      EmbedBuilder e = new EmbedBuilder();
      String n = "";
      int c = 0;

      for(IGuild x : MainBot.cli.getGuilds()){
        if(n.length() + ("`" + x.getName() + "`, ").length() < 1024)
          n += "`" + x.getName() + "`, ";
        c++;
      }
      e.appendField("Guild names (" + c + "): ", n.substring(0, n.length()-2), false);

      e.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
      Util.sendMessage(event, e.build());
    });

    commandMap.put("testi", (MessageReceivedEvent event, List<String> args) ->{
      Util.sendMessage(event, event.getGuild().getAuditLog().getDiscordObjectEntries().size() + " owo");
      for(DiscordObjectEntry e : event.getGuild().getAuditLog().getDiscordObjectEntries()) {
        if (e.getActionType().equals(ActionType.MEMBER_KICK)) {
          //e.getTargetID()
        }
      }
//      System.out.println("awwwies");
    });
  }

}
