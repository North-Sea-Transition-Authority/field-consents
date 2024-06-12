package uk.co.nstauthority.fieldconsents.epmqmessage;

import java.time.Instant;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;

public abstract class FieldConsentsEpmqMessage extends EpmqMessage {

  protected FieldConsentsEpmqMessage(String type, String correlationId, Instant createdInstant) {
    super("FIELD_CONSENTS", type, correlationId, createdInstant);
  }
}
