package mapUtils;

import mapUtils.locationPin.LocationPin;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static mapUtils.RemoteUtils.wrapRemoteExceptionSupplier;

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

    public int getMapBoundX() {
        return mapBoundX;
    }

    public int getMapBoundY() {
        return mapBoundY;
    }

    public ConcurrentMap<String, LocationPin> getLocationPins() {
        return locationPins;
    }

    public LocationPin getLocationPin(String agentName) {
        return locationPins.get(agentName);
    }

    public void addLocationPin(String agentName, LocationPin pin) {
        locationPins.put(agentName, pin);
        notifyObservers(agentName, pin);
    }

    public void removeLocationPin(String agentName) {
        LocationPin pin = locationPins.get(agentName);
        locationPins.remove(agentName);
        notifyObservers(agentName, null);
    }


    private final List<LocationMapObserver>observers = new ArrayList<>();

    public void registerObserver(LocationMapObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(LocationMapObserver observer) {
        String idToRemove = wrapRemoteExceptionSupplier(observer::getUniqueId).get();
        observers.removeIf(o -> wrapRemoteExceptionSupplier(o::getUniqueId).get().equals(idToRemove));
    }

    private void notifyObservers(String agentName, LocationPin newLocationPin) {
        List<LocationMapObserver> observersCopy = new ArrayList<>(observers);

        for (LocationMapObserver observer : observersCopy) {
            try {
                observer.locationUpdated(agentName, newLocationPin);
            } catch (Exception e) { }
        }
    }

    public void updateLocationPin(String agentName, LocationPin pin) {
        locationPins.put(agentName, pin);
        notifyObservers(agentName, pin);
    }
}
