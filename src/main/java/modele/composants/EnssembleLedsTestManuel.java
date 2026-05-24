package modele.composants;

import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import modele.Balise;
import modele.Testable;
import services.CommunicationSSH;
import services.PopUpMaker;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class EnssembleLedsTestManuel implements Testable {
    public EnssembleLedsTestManuel() {
        isTestOk = false;
        popUpMaker = new PopUpMaker();
    }

    @XmlElement(name = "Led")
    private List<Leds> leds = new ArrayList<>();
    private boolean isTestOk;
    @XmlAttribute(name = "nom")
    private String nom;
    private CommunicationSSH connexion;

    @XmlAttribute(name = "popUp")
    private String pathPopUp;
    @XmlAttribute(name = "text")
    private String text;
    private boolean userHasConfirmed;

    private PopUpMaker popUpMaker;
    public EnssembleLedsTestManuel(String nom, String pathPopUp, String text, Leds... leds) {
        popUpMaker = new PopUpMaker();
        this.nom = nom;
        this.leds = List.of(leds);
        this.pathPopUp = pathPopUp;
        this.text = text;

    }



    @Override
    public void run(CommunicationSSH connexion) {
        this.connexion = connexion;
        // On lance le test dans le thread JavaFX pour interagir avec l'utilisateur
        Platform.runLater(() -> test());
        while (!userHasConfirmed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void test() {
        isTestOk = true;
        Alert aAfficher = popUpMaker.makeAlert("Leds opérationelle ?",pathPopUp,text);
        changerEtatLed(true);
        // On demande à l'utilisateur si les leds marchent correctement
        ButtonType a = aAfficher.showAndWait().get();
        System.out.println(a.getButtonData());
        // On récupère la réponse de l'utilisateur
        isTestOk &= a.getButtonData().name() == "OK_DONE";
        System.out.println(isTestOk);
        // On éteint les leds
        changerEtatLed(false);
        // On ferme la connexion
        connexion.lireEntree();
        // On réveille le thread principal
        userHasConfirmed = true;
    }

    private void changerEtatLed(boolean onOff) {
        leds.forEach(l -> l.turnState(connexion,onOff));
    }

    @Override
    public boolean isTestOk() {
        return isTestOk;
    }

    @Override
    public String getDetails() {
        return nom + " : " + (isTestOk ? "OK" : "KO");
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> balises = new ArrayList<>();
        Balise balise = new Balise(
        "["+nom.replace(" ", "_").toLowerCase()+"]",
        isTestOk ? "OK" : "KO");
        balises.add(balise);
        return balises;
    }

    @Override
    public int getTempAlancer() {
        return 0;
    }

    public List<Leds> getLeds() {
        return leds;
    }

    @Override
    public String getNomTest() {
        return "Test : " + nom ;
    }

    public String getNom() {
        return nom;
    }

    public String getPopUp() {
        return pathPopUp;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof EnssembleLedsTestManuel) {
            EnssembleLedsTestManuel en = (EnssembleLedsTestManuel) obj;
            isEqual = en.getNom().equals(nom);
            isEqual &= en.getPopUp().equals(pathPopUp);
            isEqual &= en.getText().equals(text);
            isEqual &= en.getLeds().size() == leds.size();
            for (int i = 0; i < leds.size(); i++) {
                isEqual &= leds.get(i).equals(en.getLeds().get(i));
            }
        }
        return isEqual;
    }
}
