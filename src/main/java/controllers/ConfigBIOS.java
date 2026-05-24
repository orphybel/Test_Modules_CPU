package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controleur de la page de configuration du BIOS.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class ConfigBIOS implements Initializable {

	/** Nombre d'objet à afficher sur la page. */
	private final int NB_CONTROLS = 10;

	/** Le textfield où l'utilisateur rentre la version du BIOS. */
	@FXML
	TextField tfVersion;

	/** Boutton permettant d'accèder à la page précédente. */
	@FXML
	Button bouttonGauche;

	/** Boutton permettant d'accèder à la page suivante. */
	@FXML
	Button bouttonDroite;

	/** Racine de la page. */
	@FXML
	VBox tasks;

	public static String versionBIOS;

	private String[] actions = {
			"Configurer la variable 'User I2C Support' \n Variable = Controller-based PCI mode",
			"Configuration de la variable 'SD-Card/GPIO selection' \n Variable = SD-Card",
			"Configuration de la variable 'HD-Audio Support' \n Variable = Disabled",
			"Configuration de la variable 'QuietBoot' \n Variable = Enabled"
	};


	/** Tableau contenant les objets de la page. */
	private Control[] controls = new Control[NB_CONTROLS];
	private ArrayList<CheckBox> checkBoxes = new ArrayList<>();

	/**
	 * Initialise les informations de la page.
	 * Cette méthode est exécuté à chaque fois que la page
	 * est chargée par l'application.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		HBox hbox = new HBox();
		tfVersion = new TextField();
		Label dqs = new Label("Version du BIOS : ");
		hbox.getChildren().add(dqs);
		hbox.getChildren().add(tfVersion);
		tasks.getChildren().add(hbox);
		dqs.widthProperty().addListener((observable, oldValue, newValue) -> {
			double availableSpace = hbox.getWidth() - newValue.intValue() - 125;
			dqs.setMinWidth(newValue.intValue());
			hbox.setSpacing(availableSpace);
			hbox.layout();
		});
		makeCheckBox(tasks,actions);
		/* On initialise avec les valeurs que l'on a déjà */
		if (versionBIOS != null && !versionBIOS.isEmpty()) {
			tfVersion.setText(versionBIOS);
		}
	}
	private void makeCheckBox(VBox root ,String[] text) {
		for (String s : text) {
			makeCheckBox(root,s);
		}
	}
	private void makeCheckBox(VBox root,String text) {
		HBox hbox = new HBox();
		Label label = new Label(text);
		CheckBox cb = new CheckBox();
		hbox.getChildren().addAll(label,cb);
		root.getChildren().add(hbox);
		checkBoxes.add(cb);
		label.getStyleClass().add("label");
		label.widthProperty().addListener((observable, oldValue, newValue) -> {
			double availableSpace  = hbox.getWidth() - newValue.intValue() ;
			label.setMinWidth(newValue.intValue());
			hbox.setSpacing(availableSpace);
			hbox.layout();
		});

	}



	/**
	 * Affiche la page précédente (infoPV).
	 * @throws IOException
	 */
	@FXML
	private void stagePrecedent() throws IOException {
		/* On affiche la page précédente */
		Stage stage = (Stage) bouttonGauche.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/infoPV.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Information pour le PV");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Affiche la page suivante (connexionCarte).
	 * @throws IOException
	 */
	@FXML
	private void stageSuivant() throws IOException {
		/* On met à jour le PV avec les infos de la page */
		if (isAllChecked() && isVersionBIOSRight()) {
			versionBIOS = tfVersion.getText();
			Stage stage = (Stage) bouttonDroite.getScene().getWindow();
			Parent root = FXMLLoader.load(getClass().getResource("/fxml/connexionCarte.fxml"));
			Scene scene = new Scene(root);
			stage.setTitle("Connexion à la carte");
			stage.setScene(scene);
			stage.show();
		}
	}

	private boolean isVersionBIOSRight() {
		String versionTampon = tfVersion.getText();
		boolean isRight = !versionTampon.isEmpty();
		if (isRight) versionBIOS = versionTampon;
		else tfVersion.setStyle("-fx-border-color: red");
		return isRight;
	}

	private boolean isAllChecked() {
		boolean allChecked = true;
		for (CheckBox cb : checkBoxes) {
			boolean checked = cb.isSelected();
			if (!checked) cb.setStyle("-fx-border-color: red");
			allChecked &= checked;
		}
		return allChecked;
	}
}
