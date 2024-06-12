package uk.co.nstauthority.fieldconsents.epmqmessage;

import org.junit.jupiter.api.Test;

class FieldConsentsEpmqMessagesJarTest {

  @Test
  void classesExist() throws ClassNotFoundException {
    Class.forName("uk.co.nstauthority.fieldconsents.epmqmessage.ApplicationSubmissionType");
    Class.forName("uk.co.nstauthority.fieldconsents.epmqmessage.ApplicationSubmittedFieldConsentsEpmqMessage");
    Class.forName("uk.co.nstauthority.fieldconsents.epmqmessage.FieldConsentsEpmqMessage");
    Class.forName("uk.co.nstauthority.fieldconsents.epmqmessage.FieldConsentsEpmqTopics");
  }
}
