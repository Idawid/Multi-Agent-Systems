import containers.RetailerContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.Constants;
import utils.Location;
import utils.LocationInitializer;

import java.util.List;

public class AgentSystem {
    public static void main(String[] args) {

        List<Location> retailerLocations = LocationInitializer.generateRandomLocations(3, 0, 100, 0, 100);
        RetailerContainer retailerContainer = new RetailerContainer(Constants.CONTAINER_RETAIL, retailerLocations);

        
    }
}
