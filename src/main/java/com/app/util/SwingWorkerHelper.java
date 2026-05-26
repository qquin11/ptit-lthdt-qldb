package com.app.util;

import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Helper rút gọn SwingWorker boilerplate.
 *
 * <p>Cách dùng:
 * <pre>{@code
 * SwingWorkerHelper.run(
 *     () -> monAnDAO.findAll(),
 *     result -> table.setData(result),
 *     ex -> Toast.error(parent, "Lỗi: " + ex.getMessage())
 * );
 * }</pre>
 */
public final class SwingWorkerHelper {

    private SwingWorkerHelper() {}

    /** Chạy background task + callback success/error trên EDT. */
    public static <T> SwingWorker<T, Void> run(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() {
                return backgroundTask.get();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) onSuccess.accept(result);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    if (onError != null) onError.accept(ie);
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
                    if (onError != null) onError.accept(cause);
                }
            }
        };
        worker.execute();
        return worker;
    }
}
