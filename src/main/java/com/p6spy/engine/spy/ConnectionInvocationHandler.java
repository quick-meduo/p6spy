package com.p6spy.engine.spy;

import com.p6spy.engine.common.P6Util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Properties;
import java.util.UUID;

public class ConnectionInvocationHandler implements InvocationHandler {
  private final Connection wrappedConnection;

  ConnectionInvocationHandler(final Connection wrappedConnection) {
    this.wrappedConnection = wrappedConnection;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    final Object result = P6Util.invokeUnwrapException(wrappedConnection, method, args);
    return result;
  }
}
