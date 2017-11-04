package main.commands.moderation;

import sx.blah.discord.handle.obj.IMessage;

public class Warning {
	
	public Long user, victim;
	public Long guild;
	public String reason;
	public boolean cleared;
  public Long clearedby;
  public String timestamp;

	public static final String REASON = "reason", MOD = "by", TIMESTAMP = "timestamp", CLEARED = "cleared", CLEAREDBY = "clearedby";

	public Warning(Long user, Long victim, Long guild, String reason, boolean cleared, Long clearedby, String timestamp) {
		this.user = user;
		this.victim = victim;
		this.guild = guild;
		this.reason = reason;
		this.cleared = cleared;
    this.clearedby = clearedby;
    this.timestamp = timestamp;
	}

	public static Warning toWarning(IMessage m){
		String[] t = m.getFormattedContent().split("\n");
//		return new Warning(Long.parseLong(t[0]), Long.parseLong(t[1]), Long.parseLong(t[2]), t[3], (t[4].equals("1") ? true : false), m.getLongID());
    return null;
	}
	
}
