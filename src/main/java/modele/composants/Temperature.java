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

public class Temperature implements Testable {
    private CommunicationSSH connexion;
    public Temperature() {
        connexion = CommunicationSSH.getInstance();
    }
    public Temperature(CommunicationSSH connexion) {
        this.connexion = connexion;
    }
    private float tempCPU;
    private float tempDS1621;
    @Override
    public void run(CommunicationSSH connexion) {
        try {
            connexion.etablirConnexion();
            connexion.execCmd("temperature -cpu -ds1621", 1500);
            String reponse = connexion.lireEntree();
            connexion.fermerConnexion();
            String[] entree = reponse.split("\\s+");
            for (int i = 0; i < entree.length - 1; i++) {
                if (entree[i].equals("DS1621:")) {
                    tempDS1621 = Float.parseFloat(entree[i+1]);
                } else if (entree[i].equals("CPU:")) {
                    tempCPU = Float.parseFloat(entree[i+1]);
                }
            }

        } catch (ConnexionDejaUtiliseException e) {

            e.printStackTrace();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isTestOk() {
        return 20 <= tempDS1621 && tempDS1621 <= 40 && 20 <= tempCPU && tempCPU <= 55 && tempCPU >= tempDS1621;
    }

    @Override
    public String getDetails() {
        String message = "Température DS1621 : " + tempDS1621 + " °C";
        message += "\nTempérature CPU : " + tempCPU + " °C";
        return message;
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> balises = new ArrayList<>();
        balises.add(new Balise("[ds]", String.valueOf(tempDS1621 + " °C")));
        balises.add(new Balise("[cpu]", String.valueOf(tempCPU + " °C")));
        return balises;
    }

    @Override
    public int getTempAlancer() {
        return 2500;
    }

    @Override
    public String getNomTest() {
        return "Test Température";
    }
}
