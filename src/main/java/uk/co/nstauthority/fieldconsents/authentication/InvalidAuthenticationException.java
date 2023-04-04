package uk.co.nstauthority.fieldconsents.authentication;

public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException(String message) {
    super(message);
  }
}
