package main;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class Util {

	public static String prefix = "j.";
	public static double version = 0.2;
	private static int cnum = 4;
	public static String[][] cmdinfo = new String[cnum][2];
	public static int totcom = 0;
	public static String link = "https://discordapp.com/oauth2/authorize?client_id=334665490612092929&scope=bot";
	public static boolean gmode = true;
	
	public static void init() {
		cmdinfo[0][0] = "ayy lmao ping pong";
		cmdinfo[0][1] = "ping";

		cmdinfo[1][0] = "version";
		cmdinfo[1][1] = "ver";

		cmdinfo[2][0] = "advanced info";
		cmdinfo[2][1] = "info";

		cmdinfo[3][0] = "I wont update this help menu lol";
		cmdinfo[3][1] = "too boring to write down all the shit";
	}

	static IDiscordClient getBuiltDiscordClient(String token) {
		return new ClientBuilder().withToken(token).build();

	}

	public static void sendMessage(IChannel channel, String message) {
		RequestBuffer.request(() -> {
			try {
				channel.sendMessage(message);
			} catch (DiscordException e) {
				System.err.println("Hmmm shit went sideways... Here's why: ");
				e.printStackTrace();
			}
		});
	}

	public static boolean botCommand(String t) {
		boolean r = false;
		
		if(t.startsWith("j.") || t.startsWith("+") || t.startsWith("!") || t.startsWith("?") || t.startsWith("-") || t.startsWith("b.") ||
				t.startsWith("f'") || t.startsWith("p!") || t.startsWith("=") || t.startsWith(".")) {
			r = true;
		}
		
		return r;
	}

}
