package uk.co.nstauthority.fieldconsents.application;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApplicationVersionNotFoundException extends EntityNotFoundException {

  public ApplicationVersionNotFoundException(String message) {
    super(message);
  }
}
