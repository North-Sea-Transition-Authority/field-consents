package uk.co.nstauthority.fieldconsents.configuration;

import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
class AsyncConfiguration implements AsyncConfigurer {

  // https://moelholm.com/blog/2017/07/24/spring-43-using-a-taskdecorator-to-copy-mdc-data-to-async-threads
  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(new CorrelationIdTaskDecorator());
    executor.initialize();
    return executor;
  }

  private static class CorrelationIdTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
      Map<String, String> contextMap = MDC.getCopyOfContextMap();
      return () -> {
        try {
          MDC.setContextMap(contextMap);
          runnable.run();
        } finally {
          MDC.clear();
        }
      };
    }
  }
}
