package modele;

import com.jcraft.jsch.JSchException;
import services.CommunicationSSH;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Permet de rendre un composant testable et intégrable facilement dans le programme
 * @method            run() , définit la procédure de test pour
 *                            cet élément
 * @method       isTestOk() , doit retourner le résultat du test
 * @method     getDetails() , doit retourner les détails du test
 * @method       toBalise() , doit retourner un tableau de String[][] transportant
 *                            les informations nécessaire a la génération des pv
 * @method getTempAlancer() , doit retourner le temps que devrait prendre le test a être éxécuter,
 *                            ce temps est utilisé pour afficher une barre de progression
 */
public interface Testable {
    public void run(CommunicationSSH connexion);
    public boolean isTestOk();
    public String getDetails();
    public List<Balise> toBalise();
    public int getTempAlancer();
    public String getNomTest();

}
