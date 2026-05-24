package services;

import com.gluonhq.charm.glisten.control.ProgressIndicator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import java.util.concurrent.TimeUnit;

public class Chrono extends Task<Void> {

    private int temps;

    public Chrono(int temps) {
        this.temps = temps;
    }

    @Override
    protected Void call() throws Exception {
        for (int i = 0; i < temps/2; i++) {
            TimeUnit.MILLISECONDS.sleep(1);
            updateProgress(i, temps/2);
        }
        return null;
    }

    public static Thread newTimer(int temps, ProgressIndicator progress) {
        Chrono chrono = new Chrono(temps);
        Platform.runLater(() -> progress.progressProperty().bind(chrono.progressProperty()));
        return new Thread(chrono);
    }
    public static Thread newTimer(int temps, ProgressBar progress) {
        Chrono chrono = new Chrono(temps);
        Platform.runLater(() -> progress.progressProperty().bind(chrono.progressProperty()));
        return new Thread(chrono);
    }

    public void linkProgress(ProgressIndicator progress) {
        Platform.runLater(() -> progress.progressProperty().bind(this.progressProperty()));
    }

}