package services;

import java.io.*;

/**
 * Cette classe gère l'application tftpd32.exe
 * qui permet d'attribuer dynamiquement une ip à la carte.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class TFTPD {

	/** processus exécutant tftpd32.exe. */
	private static Process tftpd;

	/**
	 * Lance tftpd32 qui permet de lancer un serveur dhcp.
	 */
	public static void run() throws FileNotFoundException {
		try {
			tftpd = Runtime.getRuntime().exec(new String[] {
					"../outils/tftpd32.exe"
			});
		} catch (IOException ex) {
			System.out.println("Impossible de lancer tftpd");
			ex.printStackTrace();
		}
	}

	/**
	 * Arrête le serveur tftpd.
	 */
	public static void stop() {
		if (tftpd != null) {
			tftpd.destroy();
		}
	}

	/**
	 * Copie le contenue du fichier a dans le fichier b.
	 * @param a Le fichier source.
	 * @param b Le fichier cible.
	 * @throws Exception
	 */
	private static void copyContent(File a, File b)
			throws Exception
	{
		FileInputStream in = new FileInputStream(a);
		FileOutputStream out = new FileOutputStream(b);

		try {

			int n;
			while ((n = in.read()) != -1) {
				out.write(n);
			}
		}
		finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * Néttoie le fichier d'initialisation de tftpd32.
	 */
	public static void reinitialiserInit() {
		File init = new File("../outils/tftpd32.ini");
		File initCopie = new File("../outils/tftpd32copie.ini");
		
		try {
			copyContent(initCopie, init);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
