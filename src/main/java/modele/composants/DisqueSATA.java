package modele.composants;

import com.jcraft.jsch.JSchException;
import exceptions.ConnexionDejaUtiliseException;
import modele.Balise;
import modele.Testable;
import services.CommunicationSSH;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DisqueSATA implements Testable {


    public DisqueSATA() {
    }
    public boolean isWorking;
    @XmlAttribute(name = "nom")
    private String nom;
    @XmlAttribute(name = "lettre")
    private String lettre;
    @XmlAttribute(name = "numero")
    private int numero;

    public DisqueSATA(String nom, String lettre,int numero) {
        this.nom = nom;
        this.lettre = lettre;
        this.numero = numero;
    }


    @Override
    public void run(CommunicationSSH connexion) {
        try {

            int nbOk;
            connexion.execCmd("./t_hdd"+ lettre +".sh", 16000);
            String reponse = connexion.lireEntree();
            System.out.println(reponse);
            /* On compte le nombre de Transfert réussi */
            nbOk = 0;
            for (String mot : reponse.split("\\s+")) {
                if (mot.equals("OK,"))nbOk++;
            }
            isWorking = nbOk >= 5;
        } catch (ConnexionDejaUtiliseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isTestOk() {
        return isWorking;
    }

    public String getDetails() {
        return "SATA Port " + numero + " : " + (isWorking ? "OK" : "KO");
    }

    @Override
    public List<Balise> toBalise() {
        List<Balise> balise = new ArrayList<>();
        balise.add(new Balise("[sata" + numero + "]",isWorking ? "OK" : "KO"));
        return balise;
    }

    @Override
    public int getTempAlancer() {
        return 32000;
    }

    @Override
    public String getNomTest() {
        return "Test : " + nom;
    }


    public String getNom() {
        return nom;
    }

    public String getLettre() {
        return String.valueOf(lettre);
    }

    public String getNumero() {
        return String.valueOf(numero);
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEquals = false;
        if (obj instanceof DisqueSATA) {
            DisqueSATA disqueSATA = (DisqueSATA) obj;
            isEquals = disqueSATA.nom.equals(nom) && disqueSATA.numero == numero && disqueSATA.lettre.equals(lettre);
        }
        return isEquals;
    }
}
