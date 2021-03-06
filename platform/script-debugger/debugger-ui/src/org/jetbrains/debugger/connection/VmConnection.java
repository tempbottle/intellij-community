package org.jetbrains.debugger.connection;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.EventDispatcher;
import com.intellij.util.io.socketConnection.ConnectionState;
import com.intellij.util.io.socketConnection.ConnectionStatus;
import com.intellij.util.io.socketConnection.SocketConnectionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.DebugEventListener;
import org.jetbrains.debugger.Vm;

import javax.swing.event.HyperlinkListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class VmConnection<T extends Vm> implements Disposable, BrowserConnection {
  private final AtomicReference<ConnectionState> state = new AtomicReference<ConnectionState>(new ConnectionState(ConnectionStatus.NOT_CONNECTED));
  private final EventDispatcher<DebugEventListener> dispatcher = EventDispatcher.create(DebugEventListener.class);
  private final EventDispatcher<SocketConnectionListener> connectionDispatcher = EventDispatcher.create(SocketConnectionListener.class);

  protected volatile T vm;

  private final ActionCallback started = new ActionCallback();

  private final AtomicBoolean closed = new AtomicBoolean();

  public final Vm getVm() {
    return vm;
  }

  @NotNull
  @Override
  public ConnectionState getState() {
    return state.get();
  }

  public void addDebugListener(@NotNull DebugEventListener listener, @NotNull Disposable parentDisposable) {
    dispatcher.addListener(listener, parentDisposable);
  }

  @Override
  public void executeOnStart(@NotNull Runnable runnable) {
    started.doWhenDone(runnable);
  }

  protected void setState(@NotNull ConnectionStatus status, @Nullable String message) {
    setState(status, message, null);
  }

  protected void setState(@NotNull ConnectionStatus status, @Nullable String message, @Nullable HyperlinkListener messageLinkListener) {
    ConnectionState oldState = state.getAndSet(new ConnectionState(status, message, messageLinkListener));
    if (oldState == null || oldState.getStatus() != status) {
      connectionDispatcher.getMulticaster().statusChanged(status);
    }
  }

  @Override
  public void addListener(@NotNull SocketConnectionListener listener, @NotNull Disposable parentDisposable) {
    connectionDispatcher.addListener(listener, parentDisposable);
  }

  public DebugEventListener getDebugEventListener() {
    return dispatcher.getMulticaster();
  }

  protected void startProcessing() {
    started.setDone();
  }

  public final void close(@Nullable String message) {
    if (!closed.compareAndSet(false, true)) {
      return;
    }

    vm = null;
    if (!started.isProcessed()) {
      started.setRejected();
    }
    setState(ConnectionStatus.DISCONNECTED, message);
    Disposer.dispose(this, false);
  }

  @Override
  public void dispose() {
  }

  public ActionCallback detachAndClose() {
    if (!started.isProcessed()) {
      started.setRejected();
    }

    Vm currentVm = vm;
    ActionCallback callback;
    if (currentVm == null) {
      callback = new ActionCallback.Done();
    }
    else {
      vm = null;
      callback = currentVm.detach();
    }
    close(null);
    return callback;
  }
}