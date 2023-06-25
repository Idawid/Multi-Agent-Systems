package mapUtils;

import mapUtils.locationPin.LocationPin;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class LocationMapObserverProxy extends UnicastRemoteObject implements LocationMapObserver, Serializable {
    public LocationMapObserver observer;

    public LocationMapObserverProxy(LocationMapObserver observer) throws RemoteException {
        this.observer = observer;
    }

    @Override
    public void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException {
        observer.locationUpdated(agentName, newLocationPin);
    }

    @Override
    public String getUniqueId() throws RemoteException {
        return observer.getUniqueId();
    }
}
