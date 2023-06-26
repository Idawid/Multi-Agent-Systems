package visualizationUtils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import mapUtils.locationPin.*;

import java.nio.file.Paths;

public class IconContainerBuilder {
    private VBox iconContainer = new VBox();

    public IconContainerBuilder locationPin(String name, LocationPin pin) {
        iconContainer.getProperties().put("pinId", name);
        iconContainer.setLayoutX(pin.getX());
        iconContainer.setLayoutY(pin.getY());
        return this;
    }
    public IconContainerBuilder alignment(Pos alignment) {
        iconContainer.setAlignment(alignment);
        iconContainer.setSpacing(5);
        return this;
    }
    public IconContainerBuilder child(Node child) {
        iconContainer.getChildren().add(child);
        return this;
    }
    public VBox build() {
        return iconContainer;
    }
    private static Label createNameLabel(String name) {
        Label nameLabel = new Label(name);
        nameLabel.setAlignment(Pos.TOP_CENTER);
        nameLabel.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, Font.getDefault().getSize()));
        return nameLabel;
    }

    private static ImageView createIcon(AgentType agentType) {
        ImageView icon = new ImageView(AgentIconProvider.getIcon(agentType));
        icon.setPreserveRatio(true);
        icon.setFitWidth(50);
        return icon;
    }

    private static ImageView createProgressBar(HasStock stockProvider) {
        String progressBarPath;
        double percentage = (double) stockProvider.getCurrentStock() * 100 / stockProvider.getMaxStock();
        if (percentage == 0) {
            progressBarPath = "progress-bar-empty.png";
        } else if (percentage < 25) {
            progressBarPath = "progress-bar-low.png";
        } else if (percentage < 50) {
            progressBarPath = "progress-bar-mid.png";
        } else if (percentage < 75) {
            progressBarPath = "progress-bar-high.png";
        } else {
            progressBarPath = "progress-bar-full.png";
        }

        ImageView progressBar = new ImageView(new Image(Paths.get(Constants.RESOURCE_ROOT_PATH, progressBarPath).toString()));
        progressBar.setPreserveRatio(true);
        progressBar.setFitWidth(50);

        return progressBar;
    }

    private static Label createLabel(HasLabel labelProvider) {
        Label label = new Label();
        String labelText = labelProvider.getLabel();
        label.setText(labelText);
        return label;
    }

    public static class createLocationPinView extends VBox {
        public static VBox createLocationPinView(LocationPin pin, String name) {
            IconContainerBuilder builder = new IconContainerBuilder()
                    .locationPin(name, pin)
                    .alignment(Pos.CENTER)
                    .child(createNameLabel(name))
                    .child(createIcon(pin.getAgentType()));

            if (pin.getAgentData() instanceof HasStock && pin.getAgentType() != AgentType.AGENT_RETAILER) {
                builder.child(createProgressBar((HasStock) pin.getAgentData()));
            }

            if (pin.getAgentData() instanceof HasLabel) {
                builder.child(createLabel((HasLabel) pin.getAgentData()));
            }

            return builder.build();
        }
    }
}
