package commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import main.MainBot;
import main.Util;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class ChatCommands {

	public static Map<String, ChatCommand> commandMap = new HashMap<>();
	
    public static Map<String, List<String>> memes = new HashMap<>();
	
	@SuppressWarnings("deprecation")
	public static void init() {
		ChatCommands.commandMap.put("logoff", (event, args) -> {
			Util.sendMessage(event.getChannel(), "Turning off...");
			MainBot.cli.logout();
			System.out.println("Bot going offline.");
			System.exit(0);
		});
		
		ChatCommands.commandMap.put("meme", (event, args) -> {
			int n = 2;
			String tabs = "\n";
			String id = "";
			try {
				System.out.println("one");
				id = args.get(0).substring(n, args.get(0).length()-1);
			
			}catch (NumberFormatException e) {
				try {
					int s = Integer.parseInt(id);
				}catch(Exception es) {
					if(id.length() != 18) {
						id = args.get(0).substring(3, args.get(0).length()-1);
					}
				}
			}
			
			try {
				int s = Integer.parseInt(id);
			}catch(Exception es) {
				if(id.length() != 18) {
					id = args.get(0).substring(3, args.get(0).length()-1);
				}
			}
			
			IUser username = MainBot.cli.getUserByID(id);
			System.out.println(args.get(0) + "/" + id);
//			System.out.println("Command: " + username.getName() + "/" + username.getStringID() + " with " + args.size() + " arguments");
			if(ChatCommands.memes.containsKey(id)) { 
				System.out.println("command activated");
				int ind = new Random().nextInt(ChatCommands.memes.get(id).size());
				System.out.println("Trying to print meme for " + username.getName() + " with index " + ind + ".");
				String rndmsg = ChatCommands.memes.get(username.getStringID()).get(ind); 
				tabs = "\n";
				for(int x = 0; x < rndmsg.length()/2; x++) {
					tabs += "\t";
				}
				final String tabss = tabs;
				RequestBuffer.request(() -> event.getChannel().sendMessage("\"" + rndmsg + "\" " + tabss + " *-" + username.getName() + "*"));
////				
////				for(String m : ChatCommands.memes.get(username.getStringID())) {
////					RequestBuffer.request(() -> event.getChannel().sendMessage(m));
////				}
//				
				Util.totcom += 1;
			}
		});
		
		ChatCommands.commandMap.put("godmode", (event, args) -> {
			if(event.getAuthor().getName() == "jarza") {
				Util.gmode = !Util.gmode;
				if(Util.gmode) {
					Util.sendMessage(event.getChannel(), "GodMode On - Only jaja can do commands now hehehe");
				}else {
					Util.sendMessage(event.getChannel(), "GodMode Off - *just don't spam too much pls*");
				}
			}
		});
		
		ChatCommands.commandMap.put("invite", (event, args) -> {
			Util.sendMessage(event.getChannel(), "Invite link for Jarzu botto: " + Util.link);
		});
		
		ChatCommands.commandMap.put("game", (event, args) -> {
			Util.sendMessage(event.getChannel(), "Changed game status to: " + args.get(0));
			MainBot.cli.changePlayingText(args.get(0));
		});
		
		commandMap.put("ping", (event, args) -> {
			Util.sendMessage(event.getChannel(), "pong");
		});

		commandMap.put("ver", (event, args) -> {
			Util.sendMessage(event.getChannel(), String.format("Made by Jarza. v%1$s", Util.version));
		});

		commandMap.put("info", (event, args) -> {
			EmbedBuilder builder = new EmbedBuilder();

			builder.appendField("Jarza Bot!", String.format("Version %1$s", Util.version), true);
			builder.appendField("Commands used:", Util.totcom + "", false);
			builder.withAuthorName(event.getAuthor().getName());
			builder.withAuthorIcon(event.getAuthor().getAvatarURL());
			builder.withFooterText("Still under development! uwu");
			builder.withThumbnail(
					"https://cdn.discordapp.com/avatars/218787234910961665/34d21653c66436b94c2af78b30156e91.webp?size=64");

			RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
		});

		commandMap.put("help", (event, args) -> {
			EmbedBuilder builder = new EmbedBuilder();

			builder.appendField("Prefix: " + Util.prefix, String.format("Version %1$s", Util.version), true);
			builder.withAuthorName("Jarza Bot Manual!");
			builder.withAuthorIcon(
					"https://cdn.discordapp.com/avatars/218787234910961665/34d21653c66436b94c2af78b30156e91.webp?size=64");
			builder.withFooterText("Still under development! uwu");

			for (int x = 0; x < Util.cmdinfo.length; x++) {
				System.out.println(Util.cmdinfo[2][1]);
				builder.appendField(Util.cmdinfo[Util.cmdinfo.length - 1 - x][1] + "",
						Util.cmdinfo[Util.cmdinfo.length - 1 - x][0], false);
			}

			builder.appendField("Commands used:", Util.totcom + "", false);

			RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
		});
		
//		commandMap.put("printMsg", (event, args) -> {
//			
//		});
		
	}

}
