package commands.moderation;

public class Setting {
  
  public Long guild, warnrole, modrole;
  public int kickp, banp, look;
  
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
