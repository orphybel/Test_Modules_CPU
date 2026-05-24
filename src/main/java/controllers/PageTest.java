package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modele.Module;
import modele.TestButton;
import services.CommunicationSSH;
import services.PopUpMaker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controleur de la page pageTest.
 * Permet d'éxécuter les tests sur la carte.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class PageTest implements Initializable {

	/** Boutton permettant d'accèder à la page précédente. */
	@FXML
	public Button bouttonGauche;
	/** Boutton permettant d'accèder à la page suivante. */
	@FXML
	public Button bouttonDroite;
	/** Boutton permettant d'exécuter tous les tests. */
	@FXML
	public Button bouttonTous;
	@FXML
	public ScrollPane scroll;


	public static Module carteChoisie;
	/** Vrai si l'action a été effectuée. */

	public ArrayList<TestButton> listeTests;
	private PopUpMaker popUpMaker;

	public static ExecutorService executorService;



	/**
	 * Affiche la page précédente (connexionCarte).
	 * @param e
	 * @throws IOException
	 */
	@FXML
	private void stagePrecedent(ActionEvent e) throws IOException {
		/* On met à jour le PV avec les infos de la page */

		/* On affiche la page précédente */
		Stage stage = (Stage) bouttonGauche.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/connexionCarte.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Connexion à la carte");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Affiche la page suivante (pagePV).
	 * @param e
	 * @throws IOException
	 */
	@FXML
	private void stageSuivant(ActionEvent e) throws IOException {
		Stage stage = (Stage) bouttonDroite.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/pagePV.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Génération du PV");
		stage.setScene(scene);
		stage.show();
	}


	/**
	 * Initialise les infos de la page.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		executorService = Executors.newSingleThreadExecutor();
		popUpMaker = new PopUpMaker();
		listeTests = new ArrayList<>();
		carteChoisie.forEachTestableCompenent((k) -> {
			listeTests.add(new TestButton(k));
		});
		listeTests.forEach((k) -> k.setOnAction(event -> {
			PopUpMaker.disableAll(bouttonDroite, bouttonGauche, bouttonTous);
			k.prepareTest();
			executorService.submit(() -> {
				k.run(CommunicationSSH.getInstance());
				k.endTest();
				PopUpMaker.enableAll(bouttonDroite, bouttonGauche, bouttonTous);
			});
		}));
		placeButtonTest(listeTests, scroll);
	}

	public void placeButtonTest(ArrayList<TestButton> listeTests, ScrollPane scroll) {
		double x = 50;
		double y = 50;
		int i = 1;
		AnchorPane anchorPane = new AnchorPane();
		for (TestButton t : listeTests) {
			VBox vBox = t.getVBox();
			anchorPane.getChildren().add(vBox);
			AnchorPane.setLeftAnchor(vBox,  x);
			AnchorPane.setTopAnchor(vBox,  y);
			x += 400;
			if (i % 3 == 0) {
				x = 50;
				y += 300;
			}
			i++;
		}
		scroll.setContent(anchorPane);
	}

	public void testTous() {
		Alert alert = popUpMaker.makeAlert("Voulez vous éxécuter le test du watchdog ?", Alert.AlertType.CONFIRMATION);
		ButtonType result = alert.showAndWait().get();
		boolean réponseUtilisateur = result == ButtonType.OK;
		for (TestButton t : listeTests) {
			if (!t.getNomTest().equalsIgnoreCase("Test du watchdog")) {
				t.fire();
			} else if (réponseUtilisateur) {
				t.fire();
			}
		}
	}

}
