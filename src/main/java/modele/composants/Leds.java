package modele.composants;

import com.jcraft.jsch.JSchException;
import modele.Testable;
import services.CommunicationSSH;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
@XmlAccessorType(XmlAccessType.FIELD)
public class Leds {
    public Leds() {
        isWorking = false;
    }

    @XmlAttribute(name = "nom")
    private String nom;
    @XmlAttribute(name = "numero")
    private int num;
    public boolean isWorking;

    public Leds(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void turnState(CommunicationSSH connexion,boolean state) {
        String commande = "gpio-write -gpio="+ nom + " " + (state ? "1" : "0");
        connexion.execCmd(commande,400);

    }

    public String[][] toBalise() {
        String[][] balise = new String[1][2];
        balise[0][0] = "Led";
        balise[0][1] = nom;
        return balise;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Leds && ((Leds) obj).nom.equals(nom);
    }
}
