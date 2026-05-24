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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fram implements Testable {

    private boolean isTestOk;
    public Fram() {
        isTestOk = false;
    }
    @Override
    public void run(CommunicationSSH connexion) {
        try {
            connexion.etablirConnexion();
            connexion.execCmd("fram -autotest", 5000);
            Pattern p = Pattern.compile("AUTOTEST FRAM : OK");
            String reponse = connexion.lireEntree();
            Matcher m = p.matcher(reponse);
            this.isTestOk = m.find();
            connexion.fermerConnexion();
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
        res.add(new Balise("[fram]",isTestOk ? "OK" : "KO"));
        return res;
    }

    @Override
    public int getTempAlancer() {
        return 6000;
    }

    @Override
    public String getNomTest() {
        return "Fram";
    }
}
