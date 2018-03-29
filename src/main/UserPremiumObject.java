package main;

public class UserPremiumObject {

  public Long userid;
  public String date;
  public boolean isPremium;

  public UserPremiumObject(Long userid, String date, boolean isPremium) {
    this.userid = userid;
    this.date = date;
    this.isPremium = isPremium;
  }
}
