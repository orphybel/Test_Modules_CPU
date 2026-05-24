package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import modele.Module;
import services.TFTPD;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Controleur de la page qui demande à l'utilisateur la carte
 * qu'il souhaite tester.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class ChoixCarte implements Initializable {

	@FXML
	ComboBox<String> listTAV;
	
	@FXML
	ComboBox<String> listHubview;

	List<Module> cartes;

	private String folderConfig = "./modulesdef";

	
	/**
	 * Passe à la vue suivante (infoPV).
	 * @param stage
	 * @throws IOException
	 */
	private void stageSuivant(Stage stage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/infoPV.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Information pour le PV");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Cette méthode est lancée dès que la page est chargée par l'application.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		/* redémarre tftpd32 pour la nouvelle carte */
		try {
			TFTPD.stop();
			TFTPD.reinitialiserInit();
			TFTPD.run();
		} catch (FileNotFoundException e) {}
		cartes = loadCartesConfig(new File(folderConfig));

		for (Module carte : cartes) {
			if (carte.getType().equals("Tav")) {
				listTAV.getItems().add(carte.toString());
			}
			if (carte.getType().equals("Hub")) {
				listHubview.getItems().add(carte.toString());
			}
		}

	}

	private List<Module> loadCartesConfig(File folder) {
		List<Module> cartes = new ArrayList<>();
		try {
			for (File f : folder.listFiles()) {
				JAXBContext jaxbContext = JAXBContext.newInstance(Module.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Module module = (Module) jaxbUnmarshaller.unmarshal(f);
				cartes.add(module);
			}

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return cartes;
	}

	/**
	 * Récupère le numéro de serie du module choisie.
	 * @param choix
	 */
	private String recupSN(String choix) {
		return choix.split("\\(|\\)")[1];
	}
	
	@FXML
	private void choixTAV(ActionEvent e) throws IOException {
		PageTest.carteChoisie = getHashMapCarte().get(recupSN(listTAV.getValue()));
		Stage stage = (Stage) listTAV.getScene().getWindow();
		stageSuivant(stage);
	}
	
	@FXML
	private void choixHubview(ActionEvent e) throws IOException {
		PageTest.carteChoisie = getHashMapCarte().get(recupSN(listHubview.getValue()));
		Stage stage = (Stage) listHubview.getScene().getWindow();
		stageSuivant(stage);
	}

	public HashMap<String, Module> getHashMapCarte() {
		HashMap<String, Module> map = new HashMap<String, Module>();
		for (Module carte : cartes) {
			map.put(carte.getNumero(), carte);
		}
		return map;
	}
}
