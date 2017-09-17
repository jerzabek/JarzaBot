package commands.memes;

public class Meme {

  public static final String TEXTF = "content", USERF = "author", GUILDF = "guild", TIMESTAMPF = "timestamp", ATTACHMENTSF = "attachments";

  public String text;
  public Long user;
  public Long guild;
  public String timestamp;
  public String[] attachments;

  public Meme(String text, Long user, Long guild, String timestamp, String... attachments) {
    this.text = text;
    this.user = user;
    this.guild = guild;
    this.timestamp = timestamp;
    this.attachments = attachments;
  }
}
