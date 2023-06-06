package utils;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class LocationMapObserverProxy extends UnicastRemoteObject implements Serializable, LocationMapObserver {
    private LocationMapObserver observer;

    public LocationMapObserverProxy(LocationMapObserver observer) throws RemoteException {
        super();
        this.observer = observer;
    }

    @Override
    public void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException {
        observer.locationUpdated(agentName, newLocationPin);
    }
}
