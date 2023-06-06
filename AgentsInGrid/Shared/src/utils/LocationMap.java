package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public interface LocationMap extends Remote {
    int getMapBoundX() throws RemoteException;
    int getMapBoundY() throws RemoteException;
    ConcurrentMap<String, LocationPin> getLocationPins() throws RemoteException;
    LocationPin getLocationPin(String agentName) throws RemoteException;
    void addLocationPin(String agentName, LocationPin pin) throws RemoteException;
    void removeLocationPin(String agentName) throws RemoteException;
    void updateLocationPin(String agentName, LocationPin pin) throws RemoteException;


    void registerObserver(LocationMapObserver observer) throws RemoteException;
    void unregisterObserver(LocationMapObserver observer) throws RemoteException;
}
