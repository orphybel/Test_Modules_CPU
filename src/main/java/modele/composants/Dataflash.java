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
public class Dataflash implements Testable {

    private boolean isTestOk;
    public Dataflash() {
        isTestOk = false;
    }
    @Override
    public void run(CommunicationSSH connexion) {
        try {
            boolean OK = false;
            connexion.etablirConnexion();
            connexion.execCmd("dataflash -autotest", 30000);
            String reponse = connexion.lireEntree();
            connexion.fermerConnexion();
            OK = false;
            for (String mot : reponse.split("\\s+")) {
                if (mot.equals("OK")) { OK = true; }
            }
            this.isTestOk = OK;
        } catch (ConnexionDejaUtiliseException e) {
            e.printStackTrace();
        } catch (JSchException | IOException e) {
            throw new RuntimeException(e);
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
        res.add(new Balise("[dataflash]",isTestOk ? "OK" : "KO"));
        return res;
    }

    @Override
    public int getTempAlancer() {
        return 14000;
    }

    @Override
    public String getNomTest() {
        return "Dataflash";
    }
}
