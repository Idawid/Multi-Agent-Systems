import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simulation.LocationMapVisualizer;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Button startSimulationButton = new Button();
		startSimulationButton.setText("Start simulation");
		startSimulationButton.setTranslateX(150);
		startSimulationButton.setTranslateY(60);
		EventHandler<ActionEvent> startSimulation = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				CoreEntry.main(new String[] { });
			}
		};
		startSimulationButton.setOnAction(startSimulation);

		Button startVisualizationButton = new Button();
		startVisualizationButton.setText("Start visualization");
		startVisualizationButton.setTranslateX(300);
		startVisualizationButton.setTranslateY(60);
		EventHandler<ActionEvent> startVisualization = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				LocationMapVisualizer.main(new String[] { });
			}
		};
		startVisualizationButton.setOnAction(startVisualization);
		Group root = new Group(startSimulationButton);
		root.getChildren().add(startVisualizationButton);
		Scene scene = new Scene(root, 595, 150, Color.BEIGE);
		stage.setTitle("Agents in grid");
		stage.setScene(scene);
		stage.show();
	}
}
