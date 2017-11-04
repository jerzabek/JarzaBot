package exceptions;

/**
 * Created by Ivan on 21.9.2017..
 */
public class InvalidMemeException extends Exception{

  public InvalidMemeException(String reason){
    super(reason);
  }

}
