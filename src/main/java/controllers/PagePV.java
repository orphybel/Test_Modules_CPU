package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modele.Testable;
import services.CreationPV;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controleur de la page pagePV.
 * Permet de gérer la génération du PV de test.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class PagePV implements Initializable {

	/** Barre de progression de la génération du PV. */
	@FXML
	ProgressBar progress;

	/** Bouutton permettant d'enregistrer le PV. */
	@FXML
	Button bouttonEnregistrer;

	/** Boutton permettant d'accèder à la page précédente. */
	@FXML
	Button bouttonGauche;

	/** Boutton permettant de débuter le test d'une nouvelle carte. */
	@FXML
	Button btnNouvelleCarte;

	/** Label indiquant que l'enregistrement a été réussi. */
	@FXML
	Label labelEnregistrement;

	/** Label indiquant l'état de la génération (en cours / terminée). */
	@FXML
	Label labelCreation;

	/** Générateur de PV. */
	private CreationPV pv;

	/** Control permettant de choisir l'emplacement du fichier à sauvegarder. */
	FileChooser fileChooser;


	/**
	 * Initialisation de la page.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word (*.docx)", "*.docx"));
		fileChooser.setInitialFileName(InfoPV.numModule);
		try {
			pv = new CreationPV(PageTest.carteChoisie,progress);
		} catch (IOException e) {
		}
		pv.remplacer();
		labelCreation.setText("Pv en cours de création ...");
		bouttonEnregistrer.setOnAction((event) -> enregistrer());
	}


	/**
	 * Enregistre le PV.
	 * @param e
	 */
	@FXML
	private void enregistrer() {
		Stage stage = (Stage) bouttonEnregistrer.getScene().getWindow();
		File file = fileChooser.showSaveDialog(stage);
		if (file != null) {
			if (!file.getName().contains(".docx")) {
				pv.save(file.getAbsolutePath() + ".docx");
			} else {
				pv.save(file.getAbsolutePath());
			}
			labelEnregistrement.setVisible(true);
		}
		btnNouvelleCarte.setVisible(true);
	}

	/**
	 * Affiche la page précédente.
	 * @param e
	 * @throws IOException
	 */
	@FXML
	private void stagePrecedent(ActionEvent e) throws IOException {
		Stage stage = (Stage) bouttonGauche.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/pageTest.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Tests Unitaires");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Démarre la procédure de test d'une nouvelle carte.
	 * @param e
	 * @throws IOException
	 */
	@FXML
	private void nouvelleCarte(ActionEvent e) throws IOException {
		Stage stage = (Stage) btnNouvelleCarte.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/choixCarte.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Choix de la carte");
		stage.setScene(scene);
		stage.show();
	}

	public void clickOnSave() {
		bouttonEnregistrer.fire();
	}
}
