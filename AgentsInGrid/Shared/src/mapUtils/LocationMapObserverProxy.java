package mapUtils;

import mapUtils.locationPin.LocationPin;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

public class LocationMapObserverProxy extends UnicastRemoteObject implements LocationMapObserver, Serializable {
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
