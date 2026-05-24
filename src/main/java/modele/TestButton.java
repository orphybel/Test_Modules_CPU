package modele;

import com.gluonhq.charm.glisten.control.ProgressIndicator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import services.Chrono;
import services.CommunicationSSH;
import services.PopUpMaker;

/**
 *  Permet de créer un enssemble d'objet qui permettent à l'utilisateur d'intéragir avec un test donné.
 *
 *
 */
public class TestButton {

    private ProgressIndicator progress;
    private Button button;
    private Hyperlink hyperlink;
    private Testable testable;
    private PopUpMaker popUpMaker;




    /**
     * @param testable   Test à exécuter lors du clic sur le bouton
     */
    public TestButton(Testable testable) {
        button = new Button(testable.getNomTest());
        this.testable = testable;
        init();
    }

    /**
     * Initialise les composants graphiques et lie l'action du bouton à la fonction run() du test
     */
    private void init() {
        popUpMaker = new PopUpMaker();
        hyperlink = new Hyperlink("Détails");
        progress = new ProgressIndicator();
        progress.setPrefHeight(148);
        progress.setPrefWidth(169);
        button.setGraphic(progress);
        progress.setRadius(60);
        button.getStyleClass().add("btnTest");
        progress.getStyleClass().add("progress");
        progress.setVisible(false);
        progress.setFocusTraversable(false);
        progress.setMouseTransparent(true);
        button.getStyleClass().add("button");
        hyperlink.setOnAction((event) -> popUpMaker.popupDetail(testable.getDetails()));
    }


    public void prepareTest() {
        button.setMouseTransparent(true);
        button.setFocusTraversable(false);
        progress.setProgress(-1.0);
        progress.setStyle("-fx-color: #2AA5FF");
        progress.setVisible(true);

    }
    public void endTest() {
        progress.progressProperty().unbind();
        progress.setProgress(1.0);
        hyperlink.setVisible(true);
        button.setMouseTransparent(false);
        button.setFocusTraversable(true);
    }

    /**
     * @return  Retourne un VBox contenant le bouton et le lien hypertexte
     */
    public VBox getVBox() {
        VBox vBox = new VBox();
        vBox.getChildren().addAll(button, hyperlink);
        vBox.setLayoutX(200);
        vBox.setLayoutY(200);
        return vBox;
    }

    public String getNomTest() {
        return testable.getNomTest();
    }


    public void fire() {
    	button.fire();
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
    	button.setOnAction(value);
    }
    public void run(CommunicationSSH communicationSSH) {
        Chrono.newTimer(testable.getTempAlancer(), progress).start();
        testable.run(communicationSSH);
        if (testable.isTestOk()) Platform.runLater(() -> progress.setStyle("-fx-color: #00ae4e"));
        else Platform.runLater(() -> progress.setStyle("-fx-color: #FF3715"));
    }
    public boolean isTestOk() {
        return testable.isTestOk();
    }





}
