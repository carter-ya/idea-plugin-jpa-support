package com.ifengxue.plugin.util;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class MyLogChute implements LogChute {

  private final Logger log = Logger.getInstance(getClass());

  @Override
  public void init(RuntimeServices runtimeServices) {

  }

  @Override
  public void log(int level, String message) {
    logInternal(level, message, null);
  }

  @Override
  public void log(int level, String message, Throwable ex) {
    logInternal(level, message, ex);
  }

  private void logInternal(int level, String message, Throwable ex) {
    switch (level) {
      case LogChute.TRACE_ID:
        log.trace(message);
        return;
      case LogChute.DEBUG_ID:
        log.debug(message, ex);
        return;
      case LogChute.INFO_ID:
        log.info(message, ex);
        return;
      case LogChute.WARN_ID:
        log.warn(message, ex);
        return;
      case LogChute.ERROR_ID:
        log.error(message, ex);
        return;
      default:
        log.error("unknown log level " + level + ", raw log message is " + level, ex);
    }
  }

  @Override
  public boolean isLevelEnabled(int level) {
    switch (level) {
      case LogChute.TRACE_ID:
        return log.isTraceEnabled();
      case LogChute.DEBUG_ID:
        return log.isDebugEnabled();
      case LogChute.INFO_ID:
      case LogChute.WARN_ID:
      case LogChute.ERROR_ID:
      default:
        return true;
    }
  }
}
