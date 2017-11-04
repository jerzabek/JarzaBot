package main.commands.memes;

public class Meme {

  public static final String TEXTF = "content", USERF = "author", GUILDF = "guild", TIMESTAMPF = "timestamp", ATTACHMENTSF = "attachments";

  public String text;
  public Long user;
  public Long guild;
  public String timestamp;
  public Object[] attachments;

  public Meme(String text, Long user, Long guild, String timestamp, Object... attachments) {
    this.text = text;
    this.user = user;
    this.guild = guild;
    this.timestamp = timestamp;
    this.attachments = attachments;
  }
}
