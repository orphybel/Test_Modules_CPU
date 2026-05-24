package exceptions;

public class ConnexionDejaUtiliseException extends RuntimeException {
    public ConnexionDejaUtiliseException(String message) {
        super(message);
    }

    public ConnexionDejaUtiliseException() {
        super("La connexion est déjà utiliser !");
    }
}
