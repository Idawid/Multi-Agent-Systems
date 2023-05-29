package simulationUtils;

import utils.LocationMap;
import utils.LocationPin;

import java.util.*;

public class LocationMapObserver implements LocationMap.Observer {
    private LocationMap locationMap;

    public LocationMapObserver(LocationMap locationMap) {
        this.locationMap = locationMap;
    }

    @Override
    public void update(LocationMap locationMap) {

    }
}

