package mapUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocationMapImpl extends UnicastRemoteObject implements LocationMap {
    private int mapBoundX;
    private int mapBoundY;
    private ConcurrentMap<String, LocationPin> locationPins;

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

    private List<LocationMapObserver> observers = new ArrayList<>();

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
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateLocationPin(String agentName, LocationPin pin) throws RemoteException {
        locationPins.put(agentName, pin);
        notifyObservers(agentName, pin);
    }
}
