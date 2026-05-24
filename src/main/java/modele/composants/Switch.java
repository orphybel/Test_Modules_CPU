package modele.composants;

import com.fazecast.jSerialComm.SerialPort;
import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import modele.Balise;
import modele.Testable;
import services.CommunicationSSH;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Liste des switchs existantes sur les différentes carte, toutes les switchs
 * ne sont pas utilisé sur toutes les cartes
 * Le nom de la carte est utilisé pour envoyer des commande SSH sur la carte.
 * Le numéro de la carte est utilisé pour identifier les différentes carte dans un autre programme codé en c++.
 * Ils sont donc arbitraire et ne sont pas forcément dans l'ordre.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Switch implements Testable {
    public Switch() {
    }
    @XmlAttribute(name = "nom")
    private String nom;
    @XmlAttribute(name = "numero")
    private int num;
    private boolean isTestOk;

    private CommunicationSSH connexion;

    public Switch(int num, String nom) {
        this.num = num;
        this.nom = nom;
    }

    public int getNum() {
        return num;
    }
    public String getNom() {
        return nom;
    }

    @Override
    public void run(CommunicationSSH connexion) {
        this.connexion = connexion;

        isTestOk = test(true) && test(false);

    }

    private boolean test(boolean on) {
        bougerSwitch(on);
        connexion.execCmd("gpio-read -gpio=" + nom, 1000);
        String reponse = connexion.lireEntree();
        System.out.println(reponse);
        return parSeOutPut(reponse,on);
    }
    /**
     * Change l'état des switchs par l'intérmédiaire d'un programme en c++.
     * @param switchs les switchs à changer voir enum SWITCH.
     * @param on position à 1 ou à 0.
     * @throws InterruptedException
     */
    private void bougerSwitch(boolean on) {
        SerialPort comPort = connexion.getPort();
        if (comPort == null) throw new RuntimeException("Port null");
        comPort.setBaudRate(115200);
        comPort.openPort();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        byte data = (byte) (192 + (num << 1) + (on ? 1 : 0));
        try {
            byte[] msg = {(byte) 0x02, data};
            comPort.writeBytes(msg, msg.length);
        } catch (Exception e) { e.printStackTrace(); }
        comPort.closePort();
    }

    public boolean parSeOutPut(String outPut,boolean on) {
        Pattern p;
        p = Pattern.compile(": " +(on ? "1" : "0"));
        System.out.println(outPut);
        System.out.println(p.pattern());
        Matcher m = p.matcher(outPut);
        return m.find();
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
        balises.add(new Balise("[switch" + num + "]", isTestOk ? "OK" : "KO"));
        return balises;
    }

    @Override
    public int getTempAlancer() {
        return 40000;
    }

    @Override
    public String getNomTest() {
        return "Test switch";
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof Switch) {
            Switch s = (Switch) obj;
            isEqual = s.nom.equals(nom) && s.num == num;
        }
        return isEqual;
    }
}
