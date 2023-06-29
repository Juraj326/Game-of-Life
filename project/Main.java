package project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        GridPane options = new GridPane();
        Scene scene = new Scene(options);
        scene.getStylesheets().add("project/styles.css");
        Label lbRows =  new Label("Počet riadkov mriežky: ");
        Spinner<Integer> rows = new Spinner<>(10, 60, 30);
        Label lbColumns =  new Label("Počet stĺpcov mriežky: ");
        Spinner<Integer> columns = new Spinner<>(10, 60, 30);
        Label lbInterval = new Label("Takt simulovania (ms): ");
        TextField interval = new TextField("500");
        Label lbColor1 = new Label("Farba mŕtvych buniek: ");
        ColorPicker color1 = new ColorPicker();
        Label lbColor2 = new Label("Farba živých buniek: ");
        ColorPicker color2 = new ColorPicker();
        Button ok = new Button("OK");

        options.add(lbRows, 0, 0);
        options.add(rows, 1, 0);
        options.add(lbColumns, 0, 1);
        options.add(columns, 1, 1);
        options.add(lbInterval, 0, 2);
        options.add(interval, 1, 2);
        options.add(lbColor1, 0, 3);
        options.add(color1, 1, 3);
        options.add(lbColor2, 0, 4);
        options.add(color2, 1, 4);
        options.add(ok, 0, 5, 2, 1);

        interval.textProperty().addListener((observableValue, s, t1) -> {   // dovoluje iba numericke vstupy
            if (!t1.matches("\\d*")) {
                interval.setText(t1.replaceAll("[^\\d]", ""));
            }
        });
        color1.setValue(Color.valueOf("#101010"));
        color2.setValue(Color.valueOf("#FCFC3D"));

        ok.setOnAction(actionEvent -> {
            int x = columns.getValue();
            int y = rows.getValue();
            new GameOfLife(x, y, Integer.valueOf(interval.getText()), color1.getValue(), color2.getValue());
            stage.close();
        });
        stage.setTitle("Nastavenia hry");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}