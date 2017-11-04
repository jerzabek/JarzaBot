package main.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import main.commands.moderation.Permission;
import main.commands.music.GuildMusicManager;
import main.db.DataManager;
import main.MainBot;
import main.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatCommands {

  public static Map<String, ChatCommand> commandMap = new HashMap<>();
  public static Map<String, ChatCommand> adminMap = new HashMap<>();
  private static AudioPlayerManager playerManager;
  private static Map<Long, GuildMusicManager> musicManagers;

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
        play(musicManager, track);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();
        if (firstTrack == null) {
          firstTrack = playlist.getTracks().get(0);
        }

//        sendMessageToChannel(channel, "Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")");

        play(musicManager, firstTrack);
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

  public static void play(GuildMusicManager musicManager, AudioTrack track) {
//    connectToFirstVoiceChannel(guild.getAudioManager());
    musicManager.scheduler.queue(track);
    List<AudioTrack> s = new ArrayList<>();
    s.addAll(musicManager.scheduler.queue);
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
      Util.sendMessage(event, "Invite link for Jarzu botto: " + Util.link);
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

    commandMap.put("info", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      builder.appendField("Jarza Bot!", String.format("Version %1$s", Util.version), true);
      builder.appendField("Commands used so far:", "" + Util.totcom, true);
      builder.appendField("How many servers have me?", MainBot.cli.getGuilds().size() + " so far!", true);
      builder.withAuthorName(event.getAuthor().getName());
      builder.withColor(new Color(112, 137, 255));
      builder.withAuthorIcon(event.getAuthor().getAvatarURL());
      builder.withFooterText("Still under development! uwu");
      builder.withThumbnail(MainBot.cli.getOurUser().getAvatarURL());

      Util.sendMessage(event, builder.build());
    });

    commandMap.put("help", (event, args) -> {
      EmbedBuilder builder = new EmbedBuilder();

      if(args.isEmpty()){
        builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version),
          true);
        builder.withAuthorName("Jarza Bot Manual!");
        builder.withAuthorIcon(event.getAuthor().getAvatarURL());
        builder.withFooterText("Still under development! uwu");
        builder.withColor(new Color(112, 137, 255));


        for (String a : Util.catnames) {
          String comms = "";
          for(String c : Util.cats.get(a).keySet()) {
            comms += c + ", ";
          }
          builder.appendField(a, comms, false);
        }
        Util.sendMessage(event, builder.build());
      }else if(args.size() == 1){
        builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version),
          true);
        builder.withAuthorName("Jarza Bot Manual!");
        builder.withAuthorIcon(event.getAuthor().getAvatarURL());
        builder.withFooterText("Still under development! uwu");
        builder.withColor(new Color(112, 137, 255));
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
      String text = "";
      {
        String t;
        int counter = 0;
        for (Object a : args.toArray()) {
          counter++;
          if (!(counter == args.size())) {
            t = " ";
          } else {
            t = "";
          }
          text += a + t;
        }
      }
      System.out.println(event.getAuthor().getName() + " said: " + text);
      if(event.getGuild() != null)
        event.getMessage().delete();

      Util.sendMessage(event, text);
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


    commandMap.put("play", (event, args) -> {
      loadAndPlay(event, args.get(0));
    });

    commandMap.put("pause", (event, args) -> {
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      musicManager.player.setPaused(!musicManager.player.isPaused());

    });

    commandMap.put("skip", (event, args) -> {
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      musicManager.scheduler.nextTrack();
    });

    commandMap.put("join", (event, args) -> {
      IVoiceChannel c = null;
      for(IVoiceChannel a : event.getGuild().getVoiceChannels()) {
        if(a.getConnectedUsers().contains(event.getAuthor())){
          c = a;
        }
      }
      if(c != null){
        c.join();
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
//        musicManager.player.setPaused(false);
      }else{
        Util.sendMessage(event, ">You must be in a voice channel first!");
      }
    });

    commandMap.put("leave", (event, args) -> {
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
      musicManager.player.stopTrack();
      event.getGuild().getConnectedVoiceChannel().leave();
    });

    commandMap.put("queue", (event, args) -> {
      GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());

    });
  }

}
