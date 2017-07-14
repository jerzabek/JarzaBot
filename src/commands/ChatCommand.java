package commands;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface ChatCommand {
	
    void run(MessageReceivedEvent event, List<String> args);
}