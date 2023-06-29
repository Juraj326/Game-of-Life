package project;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Scanner;

public class GameOfLife extends Application {
    private final int x;    // pocet stlpcov
    private final int y;    // pocet riadkov
    private final double interval;  // casovy interval pre timer
    private final Color c1; // farba mrtvych buniek
    private final Color c2; // farba zivych buniek
    private AnimationTimer timer;   // timer pre animate()
    private Rectangle[][] cells;    // 2D pole uchovavajuce vsetky bunky
    private int[][] generation; // 2D pole uchovavajuce najviac recent snapshot generacie
    private Stage stage;

    public GameOfLife(int x, int y, double interval, Color c1, Color c2) {
        this.x = x;
        this.y = y;
        this.interval = interval * 1_000_000;
        this.c1 = c1;
        this.c2 = c2;
        cells = new Rectangle[this.y + 2][this.x + 2];  // +2 kvoli ohraniceniu
        generation = new int[this.y + 2][this.x + 2];   // +2 kvoli ohraniceniu
        start(new Stage());
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        // sidebar s kontrolnymi prvkami
        VBox sidebar = new VBox();
        Button sim = new Button("Krok");
        Button begin = new Button("Spusti");
        Button stop = new Button("Zastav");
        Button clear = new Button("Zmaž");
        stop.setDisable(true);

        // ozivenie buttonov
        sim.setOnAction(actionEvent -> simulate());
        begin.setOnAction(actionEvent -> {
            begin.setDisable(true);
            stop.setDisable(false);
            clear.setDisable(true);
            animate();
        });
        stop.setOnAction(actionEvent -> {
            timer.stop();
            begin.setDisable(false);
            stop.setDisable(true);
            clear.setDisable(false);
        });
        clear.setOnAction(actionEvent -> {
            for (int i = 1; i < cells.length - 1; i++) {
                for (int j = 1; j < cells[i].length - 1; j++) {
                    cells[i][j].setFill(c1);
                }
            }
            generationSnapshot();
        });

        sidebar.getChildren().addAll(sim, begin, stop, clear);


        // samotna hra
        TilePane gameArea = new TilePane();
        gameArea.setPrefColumns(x);
        gameArea.setPrefRows(y);

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Rectangle cell;
                if (i == 0 || i == cells.length - 1 || j == 0 || j == cells[0].length - 1) {
                    cell = new Rectangle(10, 10, null);
                } else {
                    cell = new Rectangle(10, 10, c1);
                    cell.setOnMouseClicked(mouseEvent -> {
                            if (stop.isDisabled()) {
                                if (cell.getFill().equals(c1)) {
                                    cell.setFill(c2);
                                } else {
                                    cell.setFill(c1);
                                }
                                generationSnapshot();   // spravi snapshot, aby sa z neho dala vypocitat dalsia
                            }
                        });
                    gameArea.getChildren().add(cell);
                }
                cells[i][j] = cell;
            }
        }
        generationSnapshot();   // kvoli pripadu kedy by chcel uzivatel ulozit prazdnu plochu


        // Menu
        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Súbor");
        MenuItem newGame = new MenuItem("Nový");
        MenuItem loadGame = new MenuItem("Otvoriť");
        MenuItem saveGameAs = new MenuItem("Uložiť ako");
        MenuItem quitGame = new MenuItem("Ukončiť");
        menuBar.getMenus().addAll(file);
        file.getItems().addAll(newGame, loadGame, saveGameAs, new SeparatorMenuItem(), quitGame);
        newGame.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)); // Ctrl + N
        loadGame.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));    // Ctrl + O
        saveGameAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));    // Ctrl + S
        quitGame.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));    // Ctrl + Q

        newGame.setOnAction(actionEvent -> {
            new Main().start(new Stage());
            stage.close();
        });
        loadGame.setOnAction(actionEvent -> loadAction());
        saveGameAs.setOnAction(actionEvent -> saveAsAction());
        quitGame.setOnAction(actionEvent -> stage.close());

        // cela aplikacia spojena do seba
        HBox game = new HBox();
        game.getChildren().addAll(gameArea, sidebar);
        BorderPane app = new BorderPane(game);
        app.setTop(menuBar);
        Scene scene = new Scene(app);
        scene.getStylesheets().add("project/styles.css");
        stage.setTitle("Game of Life");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // spravi snapshot generacie
    private void generationSnapshot() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j].getFill() == null) {    // ohranicenie hracej plochy (nachadza sa mimo)
                    generation[i][j] = -1;
                } else if (cells[i][j].getFill().equals(c1)) {  // mrta bunka
                    generation[i][j] = 0;
                } else {    // ziva bunka
                    generation[i][j] = 1;
                }
            }
        }
    }

    // simuluje 1 kolo hry
    private void simulate() {
        for (int i = 1; i < cells.length - 1; i++) {
            for (int j = 1; j < cells[0].length - 1; j++) {
                int n = countNeighbors(i, j);
                if (generation[i][j] == 1) {
                    if (!(n == 2) && !(n == 3)) {
                        cells[i][j].setFill(c1);
                    }
                } else {
                    if (n == 3) {
                        cells[i][j].setFill(c2);
                    }
                }
            }
        }
        generationSnapshot();
    }

    // simuluje kazdych [interval] milisekund
    private void animate() {
        final long[] last = {0};
        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (l - last[0] >= interval) {
                    simulate();
                    last[0] =  l;
                }
            }
        };
        timer.start();
    }

    // spocita kolko zivych susedov ma dana bunka
    private int countNeighbors(int i, int j) {
        int res = 0;
        int[] dirV = new int[] {i - 1, i, i + 1};   // pole moznych pohybov vertikalne
        int[] dirH = new int[] {j - 1, j, j + 1};   // pole moznych pohybov horizontalne

        for (int a = 0; a < dirV.length; a++) {
            for (int b = 0; b < dirH.length; b++) {
                if (!(a == 1 && b == 1)) {  // aby nekontroloval sameho seba
                    if (isAlive(dirV[a], dirH[b])) {
                        res++;
                    }
                }
            }
        }
        return res;
    }

    // zisti ci je dana bunka ziva
    private boolean isAlive(int i, int j) {
        if (generation[i][j] < 1) {
            return false;
        } else {
            return true;
        }
    }

    // nastavenia FileChoosera
    private FileChooser initFileChooser() {
        FileChooser fileChooser = new FileChooser();
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));    na mojom pocitaci mi to nefungovalo
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Textový súbor", "*.txt"));
        return fileChooser;
    }

    private File chooseLoadFile() {
        FileChooser fileChooser = initFileChooser();
        fileChooser.setTitle("Otvoriť");
        return fileChooser.showOpenDialog(stage);
    }

    private File chooseSaveFile() {
        FileChooser fileChooser = initFileChooser();
        fileChooser.setTitle("Uložiť ako");
        return fileChooser.showSaveDialog(stage);
    }

    // loadovanie saveu zo suboru
    private int[][] loadFromFile(File file) throws IOException {
        Scanner scanner = new Scanner(new File(String.valueOf((Paths.get(file.getPath())))));
        int r = scanner.nextInt();
        int c = scanner.nextInt();
        if (c != x || r != y) {
            throw new IOException();
        }
        int[][] gameState = new int[y + 2][x + 2];
        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState[i].length; j++) {
                gameState[i][j] = scanner.nextInt();
            }
        }
        return gameState;
    }

    // nacitanie loadu na plochu
    private boolean loadAction() {
        File file = chooseLoadFile();
        if (file != null) {
            try {
                generation = loadFromFile(file);
                for (int i = 0; i < generation.length; i++) {
                    for (int j = 0; j < generation[i].length; j++) {
                        if (generation[i][j] == -1) {
                            cells[i][j].setFill(null);
                        } else if (generation[i][j] == 0) {
                            cells[i][j].setFill(c1);
                        } else {
                            cells[i][j].setFill(c2);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Nekompatibilný súbor.");
            }
            return true;
        } else {
            return false;
        }
    }

    // ukladanie stavu hry
    private boolean saveAsAction() {
        File file = chooseSaveFile();
        if (file != null) {
            try {
                PrintStream printStream = new PrintStream(file);
                printStream.println(y + " " + x);
                for (int i = 0; i < generation.length; i++) {
                    for (int j = 0; j < generation[i].length; j++) {
                        if (generation[i][j] == -1) {
                            if (j != 0) {
                                printStream.print(" ");
                            }
                            printStream.print("-1");
                        } else {
                            printStream.print("  " + generation[i][j]);
                        }
                    }
                    printStream.println();
                }
            } catch (IOException e) {
                System.err.println("Uloženie zlyhalo.");
            }
            return true;
        } else {
            return false;
        }
    }
}