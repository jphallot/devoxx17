package bk.devoxx17.ui;

import java.io.File;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import bk.devoxx17.front.ApplicationScope;
import bk.devoxx17.front.Front;
import bk.devoxx17.utils.DownloadTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	private static final Logger log = Logger.getLogger(Application.class);

	private final static KeyCodeCombination ENTER_FULLSCREEN_CODE = new KeyCodeCombination(KeyCode.F,
			KeyCombination.CONTROL_DOWN);
	private final static KeyCodeCombination EXIT_FULLSCREEN_CODE = new KeyCodeCombination(KeyCode.G,
			KeyCombination.CONTROL_DOWN);
	private final static KeyCodeCombination SHOWHIDE_MENU = new KeyCodeCombination(KeyCode.M,
            KeyCombination.CONTROL_DOWN);
    private final static KeyCodeCombination PRINT_METHODTOFIND = new KeyCodeCombination(KeyCode.P,
			KeyCombination.CONTROL_DOWN);
	private final static KeyCodeCombination CHANGE_METHODTOFIND = new KeyCodeCombination(KeyCode.O,
			KeyCombination.CONTROL_DOWN);
	private final static KeyCodeCombination RESET_GAME = new KeyCodeCombination(KeyCode.N,
			KeyCombination.CONTROL_DOWN);

	private static Label resultLabel;
    private static Label timerLabel;

    DownloadTimer downloadTimer = new DownloadTimer(5, 0);

	/**
	 * Keyloggers
     */
	private Queue<String> konamiCode = new CircularFifoQueue<String>(12);
	private Queue<String> dernieresTouches = new CircularFifoQueue<String>(12);

	private int intClose = 0;

	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setTitle("Big Kahuna Log Hack Game");
		primaryStage.setFullScreenExitKeyCombination(EXIT_FULLSCREEN_CODE);
		primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event -> logKey(event));

		initKonamiCode();

		/**
		 * Create a Menu.
		 */
		final MenuBar menuBar = new MenuBar();
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
		Menu mainMenu = new Menu("File");
		MenuItem fullscreenCmd = new MenuItem("Fullscreen");

		MenuItem menuCmd = new MenuItem("Show/Hide menus");
		MenuItem menuReset = new MenuItem("Reset Game");

		MenuItem menuPrintMethodToFind = new MenuItem("Print MethodToFind");
		MenuItem menuChangeMethodToFind = new MenuItem("Change MethodToFind");

		mainMenu.getItems().addAll(fullscreenCmd, menuCmd, menuReset, menuPrintMethodToFind, menuChangeMethodToFind);
		menuBar.getMenus().add(mainMenu);

		/**
		 * Setup MenuItem Listeners.
		 */
		fullscreenCmd.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				primaryStage.setFullScreen(true);
			}
		});
		fullscreenCmd.setAccelerator(ENTER_FULLSCREEN_CODE);

		menuCmd.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				menuBar.setVisible(!menuBar.isVisible());
			}
		});
		menuCmd.setAccelerator(SHOWHIDE_MENU);

		menuPrintMethodToFind.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				log.info(ApplicationScope.getInstance().getMethodToFind());
			}
		});
		menuPrintMethodToFind.setAccelerator(PRINT_METHODTOFIND);

		menuReset.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				log.info("Reset Game");
                initStopWatch();

                final Timer timer = new Timer();
                TimerTask timerTask = new TimerTask(){
                    @Override
                    public void run(){
                        if (downloadTimer.getIsActive() == false){
                            timer.cancel();
                            timer.purge();
                            System.out.println("GUI timer DONE");
							Platform.exit();
                        } else {
							Platform.runLater(new Runnable() {
								public void run() {
									timerLabel.setText(downloadTimer.getTime());
								}
							});
                        }
                    }
                };
                timer.scheduleAtFixedRate(timerTask, 0, 1000);
			}
		});
		menuReset.setAccelerator(RESET_GAME);

		menuChangeMethodToFind.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ApplicationScope.getInstance().chooseNewMethodToFind();
				log.info("Methode changed");
			}
		});
		menuChangeMethodToFind.setAccelerator(CHANGE_METHODTOFIND);


		/**
		 *
         */
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (intClose++ < 1) {

					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Nice try");
					alert.setHeaderText("Alt + F4 detected");
					alert.setContentText("You know better than that");

					alert.showAndWait();

					event.consume();
					primaryStage.show();
				}
				else {
					try {
						stop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		/**
		 * Create, fill a Grid and package it into a Group
		 */
		GridPane grid = new GridPane();
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(5, 5, 5, 5));
		final TextField loginTxt = new TextField();
		grid.add(new Label("Login: "), 0, 0);
		grid.add(loginTxt, 1, 0);
		
		final CheckBox checkbox = new javafx.scene.control.CheckBox();
		
		final PasswordField  passwordTxt = new PasswordField ();
		grid.add(new Label("Password: "), 0, 1);
		grid.add(passwordTxt, 1, 1);
		final TextField dispPwd = new TextField();
		passwordTxt.managedProperty().bind(checkbox.selectedProperty().not());
		passwordTxt.visibleProperty().bind(checkbox.selectedProperty().not());
		dispPwd.managedProperty().bind(checkbox.selectedProperty());
		dispPwd.visibleProperty().bind(checkbox.selectedProperty());
		dispPwd.textProperty().bindBidirectional(passwordTxt.textProperty());
		grid.add(dispPwd, 1, 1);
		grid.add(checkbox, 2, 1);
		
		Button connectBtn = new Button();
		connectBtn.setText("Connect");
		grid.add(connectBtn, 1, 2);

        timerLabel = new Label("05:00:00");
        timerLabel.setVisible(true);
        grid.add(timerLabel, 0, 4, 2, 1);

		resultLabel = new Label("ErrorText");
		resultLabel.setTextFill(Color.web("#EE0000"));
		resultLabel.setVisible(false);
		grid.add(resultLabel, 0, 3, 2, 1);
		connectBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				log.info("Typed login/password: " + loginTxt.getText() + "/" + passwordTxt.getText());
				ApplicationScope.getInstance().setErrorMessage(null);
				boolean result = Front.check(loginTxt.getText(), passwordTxt.getText());
				if (ApplicationScope.getInstance().getErrorMessage()!=null) {
					resultLabel.setVisible(true);
					resultLabel.setText(ApplicationScope.getInstance().getErrorMessage());
				}else{
					resultLabel.setVisible(false);
				}

				log.info("Result:" + (result ? "OK" : "KO"));
			}
		});
		Group group = new Group();
		group.getChildren().add(grid);

		BorderPane root = new BorderPane();
		root.setTop(menuBar);
		root.setCenter(group);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.show();
	}
	
	private void logKey(KeyEvent event) {
		dernieresTouches.add(event.getCode().toString());
		if (dernieresTouches.toString().equals(konamiCode.toString())) {
			System.out.println("KONAMI");
			String pwd = Front.getKonamiCode();
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Nice try");
			alert.setHeaderText("Here is the Konami password : " + pwd);
			alert.setContentText("You know better than that");
			alert.showAndWait();
		}
	}

	private void initKonamiCode() {
		konamiCode.add("UP");
		konamiCode.add("UP");
		konamiCode.add("DOWN");
		konamiCode.add("DOWN");
		konamiCode.add("LEFT");
		konamiCode.add("RIGHT");
		konamiCode.add("LEFT");
		konamiCode.add("RIGHT");
		konamiCode.add("B");
		konamiCode.add("A");
		konamiCode.add("ENTER");
		konamiCode.add("ENTER");

		for(int i = 0 ; i < 12 ; i++) {
			dernieresTouches.add("");
		}
	}

	private void initStopWatch(){
        downloadTimer.start();
	}
	
	private static boolean resetDb() {
		File file = new File("./test.db");
		log.debug("Reset db:" + file.getAbsolutePath());
		if (file.exists()) {
			return file.delete();
		}
		return true;
	}
	
	public static void main(String[] args) {
		if (resetDb()) {
			Front.init();
		} else {
			System.exit(0);
		}
		launch(args);
	}

	@Override
	public void stop() throws Exception {
	    downloadTimer.stop();
		Front.terminateDb();
		resetDb();
		super.stop();
		System.exit(0);
	}
}
