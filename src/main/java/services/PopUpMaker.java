package services;

import controllers.PageTest;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PopUpMaker {
    public static void disableAll(Control... controls) {
        for (Control c : controls) {
            c.setDisable(true);
        }
    }

    public static void enableAll(Control... controls) {
        for (Control c : controls) {
            c.setDisable(false);
        }
    }

    public static void hideAll(Control... controls) {
        for (Control c : controls) {
            c.setVisible(false);
        }
    }
    public static void showAll(Control... controls) {
        for (Control c : controls) {
            c.setVisible(true);
        }
    }




    /**
     * Popup qui affiche les details du test.
     */
    public void popupDetail(String message) {
        Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
        detailAlert.setContentText(message);
        detailAlert.show();
    }
    public Alert makeAlert(String title, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setResizable(false);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(PageTest.class.getResourceAsStream("/images/icone.png")));
        return alert;
    }

    public Alert makeAlert(String title,String pathPopUp,String messages) {
        ButtonType okButton = new ButtonType("OUI", ButtonBar.ButtonData.OK_DONE);
        ButtonType notOkButton = new ButtonType("NON", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().setAll(okButton, notOkButton);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(messages);

        ImageView imageView = new ImageView(new Image(PopUpMaker.class.getResourceAsStream(pathPopUp)));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        alert.getDialogPane().lookupButton(okButton).setId("btnOui");
        alert.getDialogPane().lookupButton(notOkButton).setId("btnNon");
        alert.setGraphic(imageView);
        return alert;
    }


}
