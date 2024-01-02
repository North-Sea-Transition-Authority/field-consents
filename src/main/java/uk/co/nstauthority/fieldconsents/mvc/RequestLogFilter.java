package uk.co.nstauthority.fieldconsents.mvc;

import com.google.common.base.Stopwatch;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.fieldconsents.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.fieldconsents.metrics.QueryCounter;

@Component
public class RequestLogFilter extends OncePerRequestFilter {

  static final String MDC_WUA_ID = RequestLogFilter.class.getName() + ".WUA_ID";
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogFilter.class);

  private final QueryCounter queryCounter;

  RequestLogFilter(QueryCounter queryCounter) {
    this.queryCounter = queryCounter;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    var correlationId = CorrelationIdUtil.setCorrelationIdOnMdcFromRequest(request);
    var stopwatch = Stopwatch.createStarted();

    try {
      filterChain.doFilter(request, response);
    } finally {
      var elapsedMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);
      var queryString = Optional.ofNullable(request.getQueryString()).map("?"::concat).orElse("");
      var pattern = Optional.ofNullable(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).orElse("unknown");
      var wuaId = MDC.get(MDC_WUA_ID);

      LOGGER.info(
          "[{}] {}ms {} {}{} ({}) logCorrelationId:{} wuaId:{} {}",
          response.getStatus(),
          elapsedMs,
          request.getMethod(),
          request.getRequestURI(),
          queryString,
          pattern,
          correlationId,
          wuaId,
          getQueryCounts()
      );
    }
  }

  private String getQueryCounts() {
    return "queryCounts[hibernate:%s jooq:%s epa:%s]".formatted(
        queryCounter.getAndResetHibernate(),
        queryCounter.getAndResetJooq(),
        queryCounter.getAndResetEpa()
    );
  }

}
