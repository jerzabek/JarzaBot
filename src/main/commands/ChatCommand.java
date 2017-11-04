package main.commands;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface ChatCommand{
  
  // Interface for a command to be implemented in the command map
  void run(MessageReceivedEvent event, List<String> args);
}
