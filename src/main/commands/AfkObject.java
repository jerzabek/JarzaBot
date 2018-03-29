package main.commands;

import java.time.LocalDateTime;

public class AfkObject {

  public LocalDateTime ldt;
  public String t;

  public AfkObject(LocalDateTime ldt, String t){
    this.ldt = ldt;
    this.t = t;
  }
}
