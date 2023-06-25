package visualizationUtils;

import javafx.scene.image.Image;
import mapUtils.locationPin.AgentType;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AgentIconProvider {
    private static final Map<AgentType, Image> iconMap = new HashMap<>();

    static {
        iconMap.put(AgentType.AGENT_RETAILER, loadImage("retailer_icon.png"));
        iconMap.put(AgentType.AGENT_TRUCK, loadImage("truck_icon.png"));
        iconMap.put(AgentType.AGENT_WAREHOUSE, loadImage("warehouse_icon.png"));
        iconMap.put(AgentType.AGENT_MAIN_HUB, loadImage("main_hub_icon.png"));
        iconMap.put(AgentType.AGENT_MECHANIC, loadImage("mechanic_icon.png"));
        iconMap.put(AgentType.AGENT_TRUCK_BROKEN, loadImage("truck_broken_icon.png"));
    }

    private static Image loadImage(String iconName) {
        String imagePath = Paths.get(Constants.RESOURCE_ROOT_PATH, iconName).toString();
        return new Image(imagePath);
    }

    public static Image getIcon(AgentType agentType) {
        return iconMap.getOrDefault(agentType, new Image("empty_icon.png"));
    }
}
