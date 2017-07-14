package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import commands.ChatCommands;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

public class ChatEvents {
	
	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) {
		MainBot.cli.changePlayingText("With gay people");
		int in = new Random().nextInt(10);
		if(!ChatCommands.memes.containsKey(event.getAuthor().getStringID())){
			List<String> ini =  new ArrayList<>();
			ChatCommands.memes.put(event.getAuthor().getStringID(), ini);
			System.out.println("Initialized meme DB for user " + event.getAuthor().getStringID());
		}
		if(in < 3) {
			if(!Util.botCommand(event.getMessage().getContent())) {
//				RequestBuffer.request(() -> event.getChannel().sendMessage("Added ***" + event.getMessage().getContent() + "*** to ***" + event.getAuthor().getName() + "***."));
				System.out.println("Put " + event.getMessage().getContent() + " meme into DB");
				ChatCommands.memes.get(event.getAuthor().getStringID()).add(event.getMessage().getContent());
			}
			
		
		}

		if (!event.getMessage().getContent().startsWith(Util.prefix))
			return;
		
		String[] msg = event.getMessage().getContent().split(" ");
		String command = "";
		
		command = msg[0].substring(2);


		ArrayList<String> args = new ArrayList<String>(Arrays.asList(msg));
        args.remove(0); 
        
        
		if (msg.length == 0)
			return;

		if(ChatCommands.commandMap.containsKey(command)) {
			ChatCommands.commandMap.get(command).run(event, args);
			Util.totcom += 1;
		}else {
//			IUser username = MainBot.cli.getUserByID("67305365253783553");
//			System.out.println(command.substring(3, command.length()-1));
////			System.out.println("Command: " + username.getName() + "/" + username.getStringID() + " with " + args.size() + " arguments");
//			if(ChatCommands.memes.containsKey("67305365253783553")) {  //18547672462721024 - username.getStringID().substring(1)
//				int ind = new Random().nextInt(ChatCommands.memes.get(username.getStringID()).size());
//				System.out.println("Trying to print meme for " + username.getName() + " with index " + ind + ".");
//				String rndmsg = ChatCommands.memes.get(username.getStringID()).get(ind); //new Random().nextInt(ChatCommands.memes.get(w).size());
//				RequestBuffer.request(() -> event.getChannel().sendMessage("\"" + rndmsg + "\"\n\t\t\t\t\t *-" + username.getName() + "*"));
//////				
//////				for(String m : ChatCommands.memes.get(username.getStringID())) {
//////					RequestBuffer.request(() -> event.getChannel().sendMessage(m));
//////				}
////				
//				Util.totcom += 1;
//			}
		}
	}

}
