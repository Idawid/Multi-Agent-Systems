package utils;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public interface LocationMapObserver extends Remote {
    void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException;
}
