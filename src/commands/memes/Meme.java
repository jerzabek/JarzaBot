package commands.memes;

public class Meme {
  
  public String text;
  public Long user;
  public Long guild;
  
  public Meme(String text, Long user, Long guild) {
    this.text = text;
    this.user = user;
    this.guild = guild;
  }
}
