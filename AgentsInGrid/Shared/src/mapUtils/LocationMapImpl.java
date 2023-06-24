package mapUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocationMapImpl extends UnicastRemoteObject implements LocationMap {
    // TODO [2] event type (add / delete)
    //  - maybe pass the null? somehow let the observer know what happened,
    //          so he doesn't need to sync the whole map, but just this changed pin

    private final int mapBoundX;
    private final int mapBoundY;
    private final ConcurrentMap<String, LocationPin> locationPins;

    public LocationMapImpl() throws RemoteException {
        this.mapBoundX = MapConfig.MAP_BOUND_X;
        this.mapBoundY = MapConfig.MAP_BOUND_Y;
        this.locationPins = new ConcurrentHashMap<>();
    }

    public int getMapBoundX() throws RemoteException {
        return mapBoundX;
    }

    public int getMapBoundY() throws RemoteException {
        return mapBoundY;
    }

    public ConcurrentMap<String, LocationPin> getLocationPins() throws RemoteException {
        return locationPins;
    }

    public LocationPin getLocationPin(String agentName) throws RemoteException {
        return locationPins.get(agentName);
    }

    public void addLocationPin(String agentName, LocationPin pin) throws RemoteException {
        locationPins.put(agentName, pin);
        notifyObservers(agentName, pin);
    }

    public void removeLocationPin(String agentName) throws RemoteException {
        LocationPin pin = locationPins.get(agentName);
        locationPins.remove(agentName);
        notifyObservers(agentName, pin);
    }

    private final List<LocationMapObserver> observers = new ArrayList<>();

    public void registerObserver(LocationMapObserver observer) throws RemoteException {
        observers.add(observer);
    }

    public void unregisterObserver(LocationMapObserver observer) throws RemoteException {
        observers.remove(observer);
    }

    private void notifyObservers(String agentName, LocationPin newLocationPin) {
        List<LocationMapObserver> observersCopy = new ArrayList<>(observers);

        for (LocationMapObserver observer : observersCopy) {
            try {
                observer.locationUpdated(agentName, newLocationPin);
            } catch (Exception e) { }
        }
    }

    public void updateLocationPin(String agentName, LocationPin pin) throws RemoteException {
        locationPins.put(agentName, pin);
        notifyObservers(agentName, pin);
    }
}