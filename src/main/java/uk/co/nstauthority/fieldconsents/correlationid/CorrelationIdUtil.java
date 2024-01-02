package uk.co.nstauthority.fieldconsents.correlationid;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class CorrelationIdUtil {

  public static final String HTTP_CORRELATION_ID_HEADER = "energy-portal-correlation-id";
  public static final String MDC_CORRELATION_ID_ATTR = "CORRELATION_ID";
  private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdUtil.class);

  CorrelationIdUtil() {
    throw new IllegalStateException("Cannot instantiate static helper");
  }

  public static void setCorrelationIdOnMdc(String value) {
    var existingCorrelationId = getCorrelationIdFromMdc();
    if (existingCorrelationId != null) {
      LOGGER.debug("Overwriting existing correlationId - {}", existingCorrelationId);
    }

    MDC.put(MDC_CORRELATION_ID_ATTR, value);
  }

  public static String setCorrelationIdOnMdcFromRequest(HttpServletRequest request) {
    var newCorrelationId = Optional
        .ofNullable(request.getHeader(HTTP_CORRELATION_ID_HEADER))
        .orElseGet(() -> UUID.randomUUID().toString());

    setCorrelationIdOnMdc(newCorrelationId);

    return newCorrelationId;
  }


  public static String getCorrelationIdFromMdc() {
    return MDC.get(MDC_CORRELATION_ID_ATTR);
  }

}
