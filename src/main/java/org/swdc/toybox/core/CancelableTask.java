package org.swdc.toybox.core;

import java.util.concurrent.*;
import java.util.function.Function;

public class CancelableTask<T> implements RunnableFuture<T> {

    private boolean cancelled;

    private FutureTask<T> task;

    public CancelableTask() {
        this.task = new FutureTask<>(this::call);
    }

    public T call() {
        return null;
    }

    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.cancel();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return task.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return task.get();
    }

    @Override
    public T get(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return task.get(timeout,unit);
    }

    @Override
    public void run() {
        task.run();
    }
}
