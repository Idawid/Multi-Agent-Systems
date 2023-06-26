package mapUtils;

import java.rmi.RemoteException;
import java.util.function.Supplier;

public class RemoteUtils {
    @FunctionalInterface
    public interface RemoteSupplier<R> {
        R get() throws RemoteException;
    }

    public static <R> Supplier<R> wrapRemoteExceptionSupplier(RemoteSupplier<R> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (RemoteException e) {
                System.err.println("RemoteException captured: " + e.getMessage());
                throw new RuntimeException(e);
            }
        };
    }
}


