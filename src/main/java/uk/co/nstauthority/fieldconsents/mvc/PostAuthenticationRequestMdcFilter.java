package uk.co.nstauthority.fieldconsents.mvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;

@Component
public class PostAuthenticationRequestMdcFilter extends OncePerRequestFilter {

  private final UserDetailService userDetailService;

  PostAuthenticationRequestMdcFilter(UserDetailService userDetailService) {
    this.userDetailService = userDetailService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    userDetailService.findUserDetail()
        .map(ServiceUserDetail::wuaId)
        .map(String::valueOf)
        .ifPresent(wuaId -> MDC.put(RequestLogFilter.MDC_WUA_ID, wuaId));

    filterChain.doFilter(request, response);
  }

}
