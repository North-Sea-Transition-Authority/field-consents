package uk.co.nstauthority.fieldconsents.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The item could not be found")
public class FcsEntityNotFoundException extends EntityNotFoundException {
  public FcsEntityNotFoundException(String message) {
    super(message);
  }
}
