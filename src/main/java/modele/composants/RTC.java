package modele.composants;

import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import jakarta.xml.bind.annotation.XmlRootElement;
import modele.Balise;
import modele.Testable;
import services.CommunicationSSH;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RTC implements Testable {


    private boolean test1;
    private boolean test2;
    private CommunicationSSH connexion;
    public RTC() {
        connexion = CommunicationSSH.getInstance();
    }
    @Override
    public void run(CommunicationSSH connexion) {
        int nbTest = 0;
        try {
            connexion.etablirConnexion();
            do {
                connexion.execCmd("./t_rtc.sh", 14000);
                String reponse = connexion.lireEntree();
                Pattern p = Pattern.compile("RAM OK");
                Matcher m = p.matcher(reponse);
                test1 = m.find();
                p = Pattern.compile("test OK");
                m = p.matcher(reponse);
                test2 = m.find();
                nbTest++;
                System.out.println(reponse);
            } while (nbTest < 2 && !(test1 && test2));
            connexion.fermerConnexion();
        } catch (ConnexionDejaUtiliseException e) {
            e.printStackTrace();
        } catch (JSchException | IOException e) {}
    }

    @Override
    public boolean isTestOk() {
        return test1 && test2;
    }

    @Override
    public String getDetails() {
        String message = "Zone RAM : " + (test1 ? "OK" : "KO");
        message += "\nEcriture/Lecture heure " + (test2 ? "OK" : "KO");
        return message;
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> balises = new ArrayList<>();
        balises.add(new Balise("[rtc1]", test1 ? "OK" : "KO"));
        balises.add(new Balise("[rtc2]", test2 ? "OK" : "KO"));
        return balises;
    }

    @Override
    public int getTempAlancer() {
        return 28000;
    }

    @Override
    public String getNomTest() {
        return "Test RTC";
    }
}
