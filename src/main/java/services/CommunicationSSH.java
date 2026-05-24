package services;


import com.fazecast.jSerialComm.SerialPort;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import exceptions.ConnexionDejaUtiliseException;
import modele.Testable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cette classe permet d'ouvrir une connexion SSH avec la carte de test.
 * Elle permet de lancer des tests sur la carte.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class CommunicationSSH {
	private final static String  defaultIpAdress = "10.15.150.61";
	private final static int defaultPort = 22;
	private final static String pathToIperf = "../outils/iperf3.exe";

	/** Session de connexion à la carte. */
	private Session session;

	/** Canal de communication avec la carte. */
	private ChannelShell channel;

	/** Entrée de la carte venant du processus. */
	private BufferedReader cardOutPut;

	/** Sortie du processus vers la carte. */
	private PrintStream cardInPut;

	/** Le login de connexion. */
	private String user;

	/** host L'adresse IP de connexion (10.15.150.61). */
	private String host;

	/** Le port de connexion (22). */
	private int port;

	/** Le mot de passe de connexion. */
	private String password;
	private int noCom;

	private JSch jsch;

	/** Vrai si les tests ont été initialisé. */
	private boolean testInit;

	private static CommunicationSSH instance;

	public static CommunicationSSH getInstance() {
		return instance;
	}


	public static void connecter(String user, String host, int port,
								 String password,int noCom) throws PortUnreachableException {
		instance = new CommunicationSSH(user, host, port, password, noCom);
	}

	public static void connecter(String user, String password,int noCom) throws PortUnreachableException {
		connecter(user, defaultIpAdress, defaultPort, password, noCom);
	}

	public int getNoCom() {
		return noCom;
	}

	/**
	 * Initialisation des informations nécessaire à la connexion.
	 * @param user
	 * @param host
	 * @param port
	 * @param password
	 */
    private CommunicationSSH(String user, String host, int port,
							 String password, int noCom) throws PortUnreachableException {
		this.user = user;
		this.host = host;
		this.port = port;
		this.password = password;
		this.testInit = false;
		this.noCom = noCom;
		jsch = new JSch();
		getPort(noCom);

	}

	// Constructeur pour les tests unitaires (ne pas utiliser)
    public CommunicationSSH(JSch jsch) {

		this.jsch = jsch;
	}


	/**
	 * Ouvre une connexion avec le serveur distant.
	 * @throws JSchException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void etablirConnexion() throws JSchException , IOException{
		session = jsch.getSession(user, host, port);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		channel = (ChannelShell) session.openChannel("shell");
		cardOutPut = buildCardOutPut(channel);
		cardInPut = buildCardInput();
		channel.connect();

		if (cardOutPut == null) throw new IOException("reader null");
		execCmd("su", 300);
		execCmd("actia", 300);
		if (!testInit) {
			initialisationDesTests();
		}

	}

	public Process callIperf() {
		String[] cmd = {pathToIperf, "-c", host, "-u", "-b", "250M", "-t", "20"};
		execCmd("iptables -F", 500);
		execCmd("iptables -X", 500);
		execCmd("iptables -P INPUT ACCEPT", 500);
		execCmd("iperf3 -s -i 1 -1", 2000);
		Process p = getProcess(cmd); // Lance un processus exécutant iperf
		return p;
	}


	/** Fermeture de la connexion avec la carte.
	 * @throws IOException
	 * @throws InterruptedException */
	public void fermerConnexion()  {
		try {
			if (session != null) {
				session.disconnect();
				session = null;
			}
			if (channel != null) {
				channel.disconnect();
				channel = null;
			}
			if (cardOutPut != null) {
				cardOutPut.close();
				cardOutPut = null;
			}
			if (cardInPut != null) {
				cardInPut.close();
				cardInPut = null;
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Exécution de commandes préliminaires,
	 * nécessaires au bon fonctionnement des tests.
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws JSchException
	 */
	public void initialisationDesTests()  {

		execCmd("killall principal", 300);
		execCmd("principal -killall", 300);
		execCmd("killall wdogSw", 300);
		execCmd("./wdog.sh &", 600);
		lireEntree();
		testInit = true;

	}


	/**
	 * Test du fonctionnement des leds D1 et D4.
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws JSchException
	 */
	public boolean[] testSeqRecorder() {
		boolean d1Ok, d4Ok;
		byte etat_led;
		/* Les deux leds à 0 */
		execCmd("gpio-write -gpio=LED_POWER_SEQ 0", 1000);
		execCmd("gpio-write -gpio=CMD_POWER_RECORDER 0", 1000);
		etat_led = demander_etat_leds();
		d1Ok = (etat_led & ((byte) 0x01)) == 0;
		d4Ok = (etat_led & ((byte) 0x02)) == 0;
		/* D1 à 1 et D4 à 0 */
		execCmd("gpio-write -gpio=LED_POWER_SEQ 1", 1000);
		etat_led = demander_etat_leds();
		d1Ok = d1Ok && (etat_led & ((byte) 0x01)) == 1;
		d4Ok = d4Ok && (etat_led & ((byte) 0x02)) == 0;
		/* D1 à 0 et D4 à 1 */
		execCmd("gpio-write -gpio=LED_POWER_SEQ 0", 1000);
		execCmd("gpio-write -gpio=CMD_POWER_RECORDER 1", 1000);
		etat_led = demander_etat_leds();
		d1Ok = d1Ok && (etat_led & ((byte) 0x01)) == 0;
		d4Ok = d4Ok && (etat_led & ((byte) 0x02)) == 2;
		/* Les deux à 1 */
		execCmd("gpio-write -gpio=LED_POWER_SEQ 1", 1000);
		etat_led = demander_etat_leds();
		d1Ok = d1Ok && (etat_led & ((byte) 0x01)) == 1;
		d4Ok = d4Ok && (etat_led & ((byte) 0x02)) == 2;

		lireEntree(); // Vide le cache

		return new boolean[] {d1Ok, d4Ok};
	}

	public Process getProcess(String[] cmd)  {
		try {
			return Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Test du redémarrage automatique de la carte.
	 * @throws JSchException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public int testRemoveWatchdog() {
		int i = 0;
		try {
			InetAddress address = InetAddress.getByName(host);
			execCmd("killall wdog.sh", 200);
			final int ATTENTE_MAX = 165;    // Temps maximal à attendre le redémarrage
			/* Attente de l'extinction */
			i = 0;
			while (i < ATTENTE_MAX && address.isReachable(1000)) {
				i++;
				Thread.sleep(1000);
			}

			String message = lireEntree(); // Vide le cache
		} catch (IOException | InterruptedException e) {}

		testInit = false;

		return i;
	}


	/* -------------- METHODES UTILES --------------- */

	private static final int BUFF_SIZE = 1024;

	/**
	 * Lit tout le contenu de l'entrée .
	 * @return Une chaine contenant l'entrée.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String lireEntree() {
		String entree = "";
		try {
			char[] buff = new char[BUFF_SIZE];
			while(cardOutPut.ready() && (cardOutPut.read(buff, 0, BUFF_SIZE) != -1)) {
				entree += new String(buff);
			}
		} catch (IOException e){}
		return entree;
	}

	/**
	 * Exécute une commande et attend la réponse.
	 * @param cmd La commande à exécuter.
	 * @param attente Le temps à attendre après exécution de la commande en ms.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void execCmd(String cmd, int attente) {
		try {
			cardInPut.println(cmd);
			Thread.sleep(attente);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	public SerialPort getPort() {
		SerialPort port = null;
		try {
			port = getPort(noCom);
		} catch (PortUnreachableException e) {
			e.printStackTrace();
		}
		return port;
	}

	private SerialPort getPort(int noCom) throws PortUnreachableException {
		SerialPort[] portList = SerialPort.getCommPorts();
		SerialPort comPort = null;
		Pattern pattern = Pattern.compile("COM" + noCom);
		Matcher m;
		for (SerialPort port : portList) {
			String portName = port.getDescriptivePortName();
			m = pattern.matcher(portName);
			if (m.find()) {
				comPort = port;
			}
		}
		if (comPort == null) throw new PortUnreachableException("Le port n°" + noCom + " n'a pas été trouver !");
		return comPort;
	}



	/**
	 * Envoie un message en serie au microprocesseur pour lui demander
	 * l'etat (0 ou 1) des leds D1 et D4.
	 * Note : On communique bien avec le microprocesseur mais par l'intermédiaire
	 * d'un programme qui tourne en c++
	 */
	public byte demander_etat_leds() {
		final int DELAI_MAX = 200;
		byte[] readBuffer = new byte[1];
		SerialPort comPort = getPort();
		if (comPort != null) {
			comPort.setBaudRate(115200);
			comPort.openPort();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			try {
				/* Demande des etats */
				byte[] msg = {(byte) 0x04};
				comPort.writeBytes(msg, msg.length);
				/* Attente de réponse */
				int delai = 0;
				while (comPort.bytesAvailable() == 0 && delai++ < DELAI_MAX) {
					Thread.sleep(20);
				}
				/* Lecture de la réponse */
				if (delai < DELAI_MAX) {
					comPort.readBytes(readBuffer, 1);
				}
			} catch (Exception e) { e.printStackTrace(); }
			comPort.closePort();
		}
		return readBuffer[0];
	}

	private BufferedReader buildCardOutPut(ChannelShell channel) throws IOException {
		BufferedReader reader = null;
		try {

			reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

		} catch (Exception e) {
			e.printStackTrace();
		}
			return reader;
	}
	private PrintStream buildCardInput() throws IOException {
		return new PrintStream(channel.getOutputStream(), true);
	}



	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public boolean isConnected() {
		return session != null && session.isConnected();
	}



}