package uk.co.nstauthority.fieldconsents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;

@AutoConfigureMockMvc
public abstract class AbstractActuatorControllerTest extends AbstractIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;
}
