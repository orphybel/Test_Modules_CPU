package modele.composants;

import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import modele.Balise;
import modele.Testable;
import services.CommunicationSSH;
import services.PopUpMaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@XmlRootElement(name = "Watchdog")
public class Watchdog implements Testable {
    private int temp;
    private PopUpMaker popUpMaker;
    public Watchdog() {
        popUpMaker = new PopUpMaker();
    }
    @Override
    public void run(CommunicationSSH connexion) {
        try {
            connexion.etablirConnexion();
            Platform.runLater(() -> {
                Alert alert = popUpMaker.makeAlert("Test du Watchdog", Alert.AlertType.INFORMATION);
                alert.setHeaderText("Ne pas débrancher le cable LAN");
                alert.setContentText("La carte doit rester connectée jusqu'à\n" + "la fin du test du Watchdog.");
                alert.showAndWait();
            });
            temp = connexion.testRemoveWatchdog();
            connexion.fermerConnexion();
        } catch (ConnexionDejaUtiliseException e) {
            e.printStackTrace();
        } catch (JSchException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isTestOk() {
        return temp < 165;
    }

    @Override
    public String getDetails() {
        return "Watchdog : " + (isTestOk() ? temp + " s" : "KO");
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> balises = new ArrayList<>();
        balises.add(new Balise("watchdog", isTestOk() ? temp + " s" : "KO"));
        return balises;
    }

    @Override
    public int getTempAlancer() {
        return 160000;
    }

    @Override
    public String getNomTest() {
        return "Test du Watchdog";
    }
}
