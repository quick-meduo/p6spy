package com.p6spy.engine.spy.appender;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6ModuleManager;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.impl.LoggerSendSocket;

import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketLogger extends FormattedLogger{
  private LoggerSendSocket _instance = null;

  static {
    try {
      Class.forName("com.p6spy.engine.spy.P6ModuleManager");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private LoggerSendSocket getSender(){
    synchronized (this) {
      if( _instance == null || !_instance.isStarted()){
        try {
          final P6SpyOptions opts = P6ModuleManager.getInstance().getOptions(P6SpyOptions.class);
          String ip = opts.getRmoteLoggerAddress();
          int    port = opts.getRmoteLoggerPort();
          final InetSocketAddress resolvedAddress = new InetSocketAddress(ip,port);
          Socket socket = new Socket();
          socket.connect(resolvedAddress, 1000);
          _instance = new LoggerSendSocket(socket);
          _instance.start();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return _instance;
  }
  @Override
  public void logException(Exception e) {
    getSender().postText(e.getMessage());
  }

  @Override
  public void logText(String text) {
    getSender().postText(text);
  }

  @Override
  public boolean isCategoryEnabled(Category category) {
    return true;
  }
}
