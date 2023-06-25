package mapUtils;

import java.rmi.RemoteException;

@FunctionalInterface
public interface RemoteFunction<T, R> {
    R apply(T t) throws RemoteException;
}


