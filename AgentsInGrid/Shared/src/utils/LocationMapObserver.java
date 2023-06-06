package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LocationMapObserver extends Remote {
    void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException;
}
