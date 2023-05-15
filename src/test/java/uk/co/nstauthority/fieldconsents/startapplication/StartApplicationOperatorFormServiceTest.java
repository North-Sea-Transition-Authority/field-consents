package uk.co.nstauthority.fieldconsents.startapplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.startapplication.StartApplicationOperatorFormService.OPERATOR_SEARCH_PURPOSE;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

@ExtendWith(MockitoExtension.class)
class StartApplicationOperatorFormServiceTest {

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @InjectMocks
  private StartApplicationOperatorFormService startApplicationOperatorFormService;

  @Test
  void getPrefilledOperatorForField_nullFieldId() {
    when(fieldService.findFieldWithOperator(null, OPERATOR_SEARCH_PURPOSE))
        .thenReturn(Optional.empty());

    assertThat(startApplicationOperatorFormService.getPrefilledOperatorForField(null))
        .isEqualTo(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOperatorForField_nonNullFieldId() {
    when(fieldService.findFieldWithOperator(FIELD_ID_1, OPERATOR_SEARCH_PURPOSE))
        .thenReturn(Optional.of(field1JsonWithOperator));

    assertThat(startApplicationOperatorFormService.getPrefilledOperatorForField(FIELD_ID_1))
        .isEqualTo(new RestSearchItem(
            field1JsonWithOperator.getOperatorJson().getSelectionId(),
            field1JsonWithOperator.getOperatorJson().getSelectionText())
        );
  }

  @Test
  void getPrefilledOperatorForTerminal_nullTerminalId() {
    when(terminalService.findTerminalWithOperator(null, OPERATOR_SEARCH_PURPOSE))
        .thenReturn(Optional.empty());

    assertThat(startApplicationOperatorFormService.getPrefilledOperatorForTerminal(null))
        .isEqualTo(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOperatorForTerminal_nonNullTerminalId() {
    when(terminalService.findTerminalWithOperator(TERMINAL_ID_1, OPERATOR_SEARCH_PURPOSE))
        .thenReturn(Optional.of(terminal1JsonWithOperator));

    assertThat(startApplicationOperatorFormService.getPrefilledOperatorForTerminal(TERMINAL_ID_1))
        .isEqualTo(new RestSearchItem(
            terminal1JsonWithOperator.getOperatorJson().getSelectionId(),
            terminal1JsonWithOperator.getOperatorJson().getSelectionText())
        );
  }
}
