package uk.co.nstauthority.fieldconsents.util;

import static org.mockito.Mockito.RETURNS_DEFAULTS;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SelfReturningAnswer implements Answer<Object> {

  @Override
  public Object answer(InvocationOnMock invocation) throws Throwable {
    var mock = invocation.getMock();
    if (invocation.getMethod().getReturnType().isInstance(mock)) {
      return mock;
    }
    return RETURNS_DEFAULTS.answer(invocation);
  }
}
