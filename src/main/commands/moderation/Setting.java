package main.commands.moderation;

public class Setting {
  
  public Long guild, warnrole, modrole;
  public int kickp, banp, look;
  public static final String KICKP = "kw", BANP = "bw", MODR = "mr", WARNR = "wr", PINCHAN = "pc", SETTINGSF = "settings", PERMSF = "perms", BOTCHAN = "botchan", CHANID="channelid",
  EXCP = "exceptions";

  public Setting(Long guild, Long warnrole, Long modrole, int kickp, int banp, int look) {
    super();
    this.guild = guild;
    this.warnrole = warnrole;
    this.modrole = modrole;
    this.kickp = kickp;
    this.banp = banp;
    this.look = look;
  }
}
