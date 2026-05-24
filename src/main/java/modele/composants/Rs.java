package modele.composants;

import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import jakarta.xml.bind.annotation.XmlRootElement;
import modele.Balise;
import modele.Testable;
import services.CommunicationSSH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Rs implements Testable {

    private boolean isTestOk;
    public Rs() {
        isTestOk = false;
    }
    @Override
    public void run(CommunicationSSH connexion) {
        try {
            connexion.etablirConnexion();
            connexion.execCmd("gpio-write -gpio=RS485_TX_EN 1", 500);
            connexion.execCmd("test_rs485_fdp", 12000);
            connexion.execCmd("gpio-write -gpio=RS485_TX_EN 0", 500);
            String reponse = connexion.lireEntree();
            //On regarde que le nombre de test KO est nul
            boolean testOk = true;
            String[] entree = reponse.split("\\s+");
            for (int i = 0; i < entree.length-1; i++) {
                if (entree[i].equals("KO")) {
                    testOk &= entree[i+1].equals("0");
                }
            }
            connexion.fermerConnexion();
            this.isTestOk = testOk;
        } catch (ConnexionDejaUtiliseException e) {
            e.printStackTrace();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isTestOk() {
        return isTestOk;
    }

    @Override
    public String getDetails() {

        return getNomTest().toUpperCase() + " : " + (isTestOk ? "OK" : "KO");
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> res = new ArrayList<>();
        res.add(new Balise("[rs]", isTestOk ? "OK" : "KO"));
        return res;
    }

    @Override
    public int getTempAlancer() {
        return 14000;
    }

    @Override
    public String getNomTest() {
        return "Liaison RS485";
    }
}
