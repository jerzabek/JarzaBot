package main;

import commands.ChatCommands;
import sx.blah.discord.api.IDiscordClient;

public class MainBot {
	public static IDiscordClient cli;

	public static void main(String[] args) {
		// //In case you build, uncomment dis
		// if (args.length != 1) {
		// System.out.println("Pls token: java -jar thisjar.jar tokenhere");
		// return;
		// }
		String token = "MzM0NjY1NDkwNjEyMDkyOTI5.DEfAOw.9QVmBk2DFdKgKu_GsmJTWvZ-rHk";

		cli = Util.getBuiltDiscordClient(token); // args[0]

		cli.getDispatcher().registerListener(new ChatEvents());

		// Only login after all events are registered otherwise some may be missed.
		Util.init();
		ChatCommands.init();
		cli.login();
		cli.changePlayingText("my own creation");
	}

}
