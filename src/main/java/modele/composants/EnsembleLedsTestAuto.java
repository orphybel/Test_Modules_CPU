package modele.composants;

import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import modele.Balise;
import modele.Testable;
import services.CommunicationSSH;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class EnsembleLedsTestAuto implements Testable {
    public EnsembleLedsTestAuto() {
    }

    @XmlElement(name = "Led")
    private List<Leds> leds = new ArrayList<>();
    @XmlAttribute(name = "nom")
    private String nom;
    public EnsembleLedsTestAuto(String nom, Leds... leds) {
        this.nom = nom;
        this.leds = Arrays.asList(leds);
    }
    @Override
    public void run(CommunicationSSH connexion) {
        try {
            connexion.etablirConnexion();
            boolean[] testLeds = connexion.testSeqRecorder();
            for (int i = 0; i < leds.size(); i++) {
                leds.get(i).isWorking = testLeds[i];
            }
            connexion.fermerConnexion();
        } catch (ConnexionDejaUtiliseException e) {
            e.printStackTrace();
        } catch (JSchException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isTestOk() {
        boolean isOk = true;
        for (Leds l : leds) {
            isOk &= l.isWorking;
        }
        return isOk;
    }

    @Override
    public String getDetails() {
        String message = "";
        for (Leds l : leds ) {
            message += l.getNom() + " ";
            message += l.isWorking ? " : OK\n" : " : KO\n";
        }
        return message;
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> balise = new ArrayList<>();
        for (Leds l : leds) {
            Balise ligne = new Balise(
                    "["+ l.getNom().replace(" ", "_").toLowerCase() + "]",
                    l.isWorking ? "OK" : "KO");
            balise.add(ligne);
        }
        return balise;
    }

    @Override
    public int getTempAlancer() {
        return 10000;
    }

    @Override
    public String getNomTest() {
        return "Test :\n" + nom;
    }

    public String getNom() {
        return nom;
    }

    public List<Leds> getLeds() {
        return leds;
    }
    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof EnsembleLedsTestAuto) {
            EnsembleLedsTestAuto en = (EnsembleLedsTestAuto) obj;
            isEqual = en.getNom().equals(nom);
            isEqual &= en.getLeds().size() == leds.size();
            for (int i = 0; i < leds.size(); i++) {
                isEqual &= leds.get(i).equals(en.getLeds().get(i));
            }
        }
        return isEqual;
    }
}
