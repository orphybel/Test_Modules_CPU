import controllers.PageTest;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import services.CommunicationSSH;
import services.TFTPD;

import java.io.File;

/**
 * Classe de chargement de la vue de l'application.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class App extends Application {

	/**
	 * Chargement de la vue.
	 */
	@Override
	public void start(Stage stage) throws Exception {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/choixCarte.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone.png")));
		stage.setTitle("Choix de la carte");
		stage.setScene(scene);
		stage.setResizable(true);
		stage.show();


	}





	/**
	 * Fonction exécutée lors de la fermeture de l'application.
	 */
	@SuppressWarnings("removal")
	@Override
	public void stop(){
		PageTest.executorService.shutdownNow();
		/* Stoppe l'application permettant d'attribuer une ip dynamiquement à la carte */
		TFTPD.stop();

		if (CommunicationSSH.getInstance() != null) {
			/* fermeture de la communication SSH */
			CommunicationSSH.getInstance().fermerConnexion();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
