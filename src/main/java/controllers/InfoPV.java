package controllers;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Controleur de la page demandant les informations de PV de test.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class InfoPV implements Initializable {

	/** labeld demandant d'entrer le numéro du module. */
	@FXML
	Label labelModule;

	/** Controleur permettant de selectionner une date. */
	@FXML
	DatePicker datePicker;

	/** Textfield où il faut rentrer les initiales de l'opérateur. */
	@FXML
	TextField operateurTextField;

	/** Textfield où il faut rentrer le numéro du module. */
	@FXML
	TextField moduleTextField;

	/** Boutton permettant d'accèder à la page précédente. */
	@FXML
	Button bouttonGauche;
	
	/** Boutton permettant d'accèder à la page suivante. */
	@FXML
	Button bouttonDroite;

	public static String date;

	public static String operateur;

	public static String numModule;

	/**
	 * Initialise la page avec les infos du 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (date == null || date.isBlank()) {
			Date dated = Calendar.getInstance().getTime();
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			date = formatter.format(dated);
		}
		datePicker.setValue(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")));

		if (operateur != null) {
			operateurTextField.setText(operateur);
		}
		if (numModule != null) {
			moduleTextField.setText(numModule);
		}
		if (PageTest.carteChoisie != null) {
			labelModule.setText("S/N Module (" + PageTest.carteChoisie.getNumero() + ")  :");
		}
	}

	/**
	 * Passe à la vue precedente (choixCarte).
	 * @throws IOException
	 */
	@FXML
	private void stagePrecedent() throws IOException {
		/* On met à jour le PV avec les infos de la page */
		date = datePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		operateur = operateurTextField.getText().trim();
		numModule = moduleTextField.getText().trim();
		/* On affiche la page précédente */
		Stage stage = (Stage) bouttonGauche.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/choixCarte.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Choix de la carte");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Passe à la vue suivante (configBIOS).
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@FXML
	private void stageSuivant() throws IOException {
		/* On vérifie que toutes les infos ont été saisies */
		boolean dateOk, operateurOk, moduleOk;
		dateOk = datePicker.getValue() != null;
		operateurOk = operateurTextField.getText().trim() != "";
		moduleOk = moduleTextField.getText().trim() != "";
		if (dateOk && operateurOk && moduleOk) {
			/* On met à jour le PV avec les infos de la page */
			date = datePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			operateur = operateurTextField.getText().trim();
			numModule = moduleTextField.getText().trim();
			/* On affiche la page suivante si tout est OK */
			Stage stage = (Stage) bouttonDroite.getScene().getWindow();
			Parent root = FXMLLoader.load(getClass().getResource("/fxml/configBIOS.fxml"));
			Scene scene = new Scene(root);
			stage.setTitle("Configuration du BIOS");
			stage.setScene(scene);
			stage.show();
		} else {
			if (!dateOk) {
				erreur(datePicker);
			} else {
				defaut(datePicker);
			}
			if (!operateurOk) {
				erreur(operateurTextField);
			} else {
				defaut(operateurTextField);
			}
			if (!moduleOk) {
				erreur(moduleTextField);
			} else {
				defaut(moduleTextField);
			}
		}
	}
	
	/**
	 * Vérifie l'entrée du textfield et encadre en vert
	 * si longueur > 2.
	 */
	@FXML
	private void checkOperateur(KeyEvent e) {
		if (operateurTextField.getText().trim().length() >= 2) {
			correct(operateurTextField);
		} else {
			defaut(operateurTextField);
		}
	}
	
	/**
	 * Vérifie l'entrée du textfield et encadre en vert
	 * si de la forme xxxxxx-xxxxxxx-xxxxx.
	 */
	@FXML
	private void checkModule(KeyEvent e) {
		Pattern pattern = Pattern.compile("\\d{6}-\\d{7}-\\d{5}");
		Matcher m = pattern.matcher(moduleTextField.getText().trim());
		if (m.matches()) {
			correct(moduleTextField);
		} else {
			defaut(moduleTextField);
		}
	}

	/**
	 * Encadre en rouge le textfield mal remplis.
	 * @param tf
	 */
	private void erreur(Control tf) {
		tf.setStyle("-fx-effect: dropshadow(three-pass-box, red, 5, 0.5, 0, 0);");
	}

	/**
	 * Supprime l'encadrement du textfield.
	 * @param tf
	 */
	private void defaut(Control tf) {
		tf.setStyle("-fx-effect: dropshadow(three-pass-box, white, 0, 0, 0, 0);");
	}

	/**
	 * Encadre en vert le textfield.
	 * @param tf
	 */
	private void correct(Control tf) {
		tf.setStyle("-fx-effect: dropshadow(three-pass-box, green, 5, 0.5, 0, 0);");
	}
}
