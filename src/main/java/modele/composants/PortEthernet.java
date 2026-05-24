package modele.composants;

import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import modele.Balise;
import services.CommunicationSSH;
import modele.Testable;
import services.PopUpMaker;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class PortEthernet implements Testable {

    public PortEthernet() {
        popUpMaker = new PopUpMaker();
    }

    @XmlAttribute(name = "nom")
    private String nom;
    @XmlAttribute(name = "popUp")
    private String popUpLed;
    @XmlAttribute(name = "numero")
    private int num;
    public boolean isledWorking;

    public String getNom() {
        return nom;
    }

    public String getPopUp() {
        return popUpLed;
    }

    public String getText() {
        return text;
    }

    // En Mbits/s
    public float debit;
    // En pourcentage
    public float perte;
    @XmlAttribute(name = "text")
    private String text;

    private PopUpMaker popUpMaker;

    private boolean userHasClicked = false;

    public PortEthernet(String nom) {
        this.nom = nom;
        popUpMaker = new PopUpMaker();
        isledWorking = false;
    }
    public PortEthernet(String nom, String popUpLed, String text) {
        this.nom = nom;
        this.popUpLed = popUpLed;
        this.text = text;
        isledWorking = false;
        popUpMaker = new PopUpMaker();
    }

    public String toString() {
        return nom;
    }

    @Override
    public boolean isTestOk() {
        return  debit >= 200 && perte < 1 && (!hasLed() || isledWorking);
    }

    @Override
    public void run(CommunicationSSH connexion) {
        Platform.runLater(() -> {
            popUpMaker.makeAlert("Brancher le cable LAN sur le port (" + nom + ").", Alert.AlertType.CONFIRMATION).showAndWait();
            userHasClicked = true;
        });
        try {
            while (!userHasClicked) {
                Thread.sleep(100);
            }
            userHasClicked = false;
            Process p = connexion.callIperf();
            Thread.sleep(22000);
            p.destroy();
            String reponse = connexion.lireEntree();
            System.out.println(reponse);
            float[] result = parseOutput(reponse);
            debit = result[0];
            perte = result[1];
            if (hasLed()) {
                Platform.runLater(() ->  {
                    Alert alert = popUpMaker.makeAlert("Confirmation", popUpLed, text);
                    ButtonType a = alert.showAndWait().get();
                    isledWorking = a.getButtonData().name() == "OK_DONE";
                    userHasClicked = true;
                });
                while (!userHasClicked) {
                    Thread.sleep(100);
                }
             } else isledWorking = true;
        } catch (ConnexionDejaUtiliseException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
        }
    }

    public float[] parseOutput(String outPutMessage) {
        String[] entree = outPutMessage.split("\\s+");
        float debitMoyen = 0,perte= 0;
        int nbDebit = 0,nbPerte = 0;
        
        for (int i = 0; i < entree.length; i++) {
            String motCourant = entree[i];
            // Si le mot courant est "MBytes", on regarde le mot précédent et le mot suivant
            if (motCourant.equals("MBytes")) {
                String quantiteBytesTransfere = entree[i-1];
                String debit = entree[i+1];
                // Au démarrage le débit peut mettre un peut de temp a monter et sera plus faible que la moyenne.
                // Pour des résultats plus significatifs on ne prend en compte que les débits supérieurs à 25 MBytes
                // Donc si on n'est pas sur le premier débit et que le nombre de MBytes est supérieur à 25
                if (Float.parseFloat(quantiteBytesTransfere) > 25) {
                    // On ajoute le débit au débit total
                    debitMoyen += Float.parseFloat(debit);
                    nbDebit++;
                }
                // Si la longueur de la chaîne de caractères "motCourant" est supérieure à 2 et
                // Si le caractère situé à l'avant-dernière position de la chaîne de caractères "motCourant" est égal à "%".
            }
            if (motCourant.length() > 2 && motCourant.charAt(motCourant.length() - 2) == '%') {
                perte += Float.parseFloat(motCourant.substring(1, motCourant.length() - 2));
                nbPerte++;
            }
        }
        if (nbDebit != 0 && nbPerte != 0) {
            debitMoyen = debitMoyen / nbDebit;
            perte = perte / nbPerte;
        } else {
            debitMoyen = 0;
            perte = 100;
        }
        return new float[] {debitMoyen,perte};
    }



    @Override
    public String getDetails() {
        String message = nom + " :\n";
        message += "\t- Debit : " + debit + " Mbits/s\n";
        message += "\t- Perte : " + perte + " %\n";
        if (hasLed()) message += "\t- Led : " + (isledWorking ? "OK" : "KO") + "\n";
        return message;
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> data = new ArrayList<>();
        Balise balise = new Balise("[debit" + num + "]", debit + " Mbits/s");
        Balise balise2 = new Balise("[perte" + num + "]", perte + " %");
        data.add(balise);
        data.add(balise2);
        if (hasLed()) {
            Balise balise3 = new Balise("[ledPort" + num + "]", isledWorking ? "OK" : "KO");
            data.add(balise3);
        }
        return data;
    }

    @Override
    public int getTempAlancer() {
        return 30000;
    }

    @Override
    public String getNomTest() {
        return "Test Ethernet : " + nom;
    }
    public boolean hasLed() {
        return popUpLed != null && text != null && !popUpLed.isEmpty() && !text.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj != null && obj instanceof PortEthernet) {
            PortEthernet port = (PortEthernet) obj;
            isEqual = port.nom.equals(nom);
            isEqual &= port.hasLed() == hasLed();
            if(hasLed()) isEqual &= port.popUpLed.equals(popUpLed) && port.text.equals(text);

        }
        return isEqual;
    }
}
