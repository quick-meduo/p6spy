package com.p6spy.engine.spy.appender.impl;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggerSendSocket implements Runnable{
    private Socket socket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<String> logsToSend = new LinkedBlockingQueue<String>(10000);
    private final AtomicBoolean queueFull = new AtomicBoolean();
    private Thread worker = null;

    public LoggerSendSocket(final Socket socket) throws SocketException {
      this.socket = socket;
      socket.setKeepAlive(true);
      socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(10));
    }

    public void start() {
      if(running.compareAndSet(false, true)){
        worker = new Thread(this);
        worker.start();
      }
    }

    public void stop() {
      running.set(false);
    }

    public void interrupt() {
      running.set(false);
      if(worker != null) {
        worker.interrupt();
      }
    }

    public void postText(final String log) {
      final boolean posted = logsToSend.offer(log);
      if (!posted) {
        queueFull.set(true);
      }
    }

    public boolean isStarted() {
    return running.get();
  }

    @Override
    public void run() {
      ObjectOutputStream oos = null;
      try {
        oos = new ObjectOutputStream(socket.getOutputStream());
        int cnt = 0;
        while (running.get()) {
          try {
            if (queueFull.compareAndSet(true, false)) {
            }

            final String message = logsToSend.poll(10, TimeUnit.SECONDS);
            if (message != null) {
              oos.writeUnshared(message);
            } else {
              if (socket.isClosed() || !socket.isConnected()) {
                break;
              }
            }
            cnt = (cnt + 1) % 10;
            if (cnt == 0) {
              oos.reset();
            }
          } catch (final InterruptedException e) {
            break;
          }
        }
        running.set(false);
      }
      catch (final Exception e) {}
      finally {
        if (oos != null) {
          try {
            oos.close();
          } catch (final Exception ignored) {}
        }
        try {
          socket.close();
        } catch (final Exception e) {}
      }
      running.set(false);
    }
}
