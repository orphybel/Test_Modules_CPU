package controllers;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.JSchException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import services.CommunicationSSH;
import services.PopUpMaker;

/**
 * Controleur de la pge de connexion à la carte.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class ConnexionCarte implements Initializable {

	/** Textfield où il faut entrer l'identfiant de connexion. */
	@FXML
	private TextField identifiant;

	/** Textfield où il faut entrer le numéro de COM. */
	@FXML
	private TextField textFieldCom;

	/** Textfield où il faut entrer le mot de passe. */
	@FXML
	private PasswordField mdp;

	/** Message qui est affiché en cas d'erreur de connexion. */
	@FXML
	private Label labelErreur;

	/** Boutton permettant d'accèder à la page précédente. */
	@FXML
	private Button bouttonGauche;

	/** Boutton permettant d'accèder à la page suivante. */
	@FXML
	private Button btnConnexion;

	/** Un indicateur qui est affiché lors du chargement de la connexion à la carte. */
	@FXML
	private ProgressIndicator progress;

	private PopUpMaker popUpMaker;


	/**
	 * Passe à la vue suivante (pageTest).
	 * @param stage
	 * @throws IOException
	 */
	private void stageSuivant(Stage stage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/pageTest.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Tests unitaires");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Affiche la page précédente (configBIOS).
	 * @param e
	 * @throws IOException
	 */
	@FXML
	private void stagePrecedent(ActionEvent e) throws IOException {
		/* On met à jour le PV avec les infos de la page */
		/* On affiche la page précédente */
		Stage stage = (Stage) btnConnexion.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/configBIOS.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Configuration du BIOS");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Essaye de se connecter à la carte avec les informations saisies.
	 * Affiche les indicateur correspondant au résultat de la connexion.
	 */
	@FXML
	private void connexion(ActionEvent event) {
		if (identifiant.getText().equals("test")) {
			try {
				stageSuivant((Stage) btnConnexion.getScene().getWindow());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return;
		}
		String login = identifiant.getText();
		String mdpText = mdp.getText();
		String comText = textFieldCom.getText().trim();
		Pattern pattern = Pattern.compile("\\d");
		Matcher m = pattern.matcher(comText);
		int noCom = Integer.parseInt(comText);
		CommunicationSSH connection = null;
		try {
			CommunicationSSH.connecter(login, mdpText,noCom);
			connection = CommunicationSSH.getInstance();
			PopUpMaker.disableAll(btnConnexion,bouttonGauche);
			progress.setVisible(true);
			PopUpMaker.enableAll(btnConnexion,bouttonGauche);
			connection.etablirConnexion();
			progress.setVisible(false);
			connection.fermerConnexion();
			Stage stage = (Stage) mdp.getScene().getWindow();
			Platform.runLater(() -> { try {
				stageSuivant(stage);
			} catch (IOException e1) {
			} });
		} catch (PortUnreachableException e) {
			popUpMaker.makeAlert(e.getMessage(), Alert.AlertType.ERROR).showAndWait();
		} catch (RuntimeException | JSchException | IOException ex) {
			Alert alert = popUpMaker.makeAlert("Erreur de connexion", Alert.AlertType.WARNING);
			alert.setContentText("La connexion à la carte a échoué\n"
					             + "Vérifier qu'elle soit bien connectée à votre ordinateur");
			alert.showAndWait();
		}
	}

	/** Si la touche ENTREE est pressée, clique sur le boutton connexion. */
	@FXML
	private void declancherBoutton(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			btnConnexion.fire();
		}
	}

	/**
	 * Initialise la page avec les infos du PV.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			popUpMaker = new PopUpMaker();
			CommunicationSSH connexion = CommunicationSSH.getInstance();
			identifiant.setText(connexion.getUser());
			mdp.setText(connexion.getPassword());
			textFieldCom.setText(""+connexion.getNoCom());
			connexion.fermerConnexion();
		} catch (RuntimeException e) {}


		final Alert verifCable = popUpMaker.makeAlert("Vérification des cables", Alert.AlertType.INFORMATION);
		verifCable.setHeaderText("Vérification du branchement des cables");
		verifCable.setContentText("- Cable LAN branché sur le port RJ45 J20 carte FDP.\n"
				+ "- Cable USB branché sur le port J18.");
		verifCable.show();
	}
}
