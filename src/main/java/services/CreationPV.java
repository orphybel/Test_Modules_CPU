package services;

import controllers.ConfigBIOS;
import controllers.InfoPV;
import javafx.scene.control.ProgressBar;
import modele.Balise;
import modele.Module;
import modele.Testable;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Cette classe permet de générer des PV de test à partir
 * des informations stockées dans la classe PV.
 * @author Loris Gaven
 * @version 0.0.0.1
 */
public class CreationPV {

	private ProgressBar progress;
	/** Image du document Word template. */
	private File template;
	private XSSFWorkbook xwb;
	private Module carteChoisie;
	private XWPFDocument doc;



	public CreationPV(Module carteChoisie, ProgressBar progress) throws IOException {
		this.progress = progress;
		this.carteChoisie = carteChoisie;
		template = new File(carteChoisie.getTemplate());
		try {
			OPCPackage opcPackage = OPCPackage.open(template);
			doc = new XWPFDocument(opcPackage);
		} catch (InvalidFormatException e) {
		}
	}

	public void setTemplate(File template) {
		try {
			doc = new XWPFDocument(OPCPackage.open(template));
		} catch (IOException | InvalidFormatException e) {
		}
	}

	public void remplacer(String aRemplacer, String valeur) {
		if (valeur != null && !valeur.isEmpty() && !valeur.isBlank()) {
			System.out.println(valeur);
			for (XWPFParagraph p : doc.getParagraphs()) {
				replaceRuns(p, aRemplacer, valeur);
			}

			for (XWPFTable tbl : doc.getTables()) {
				for (XWPFTableRow row : tbl.getRows()) {
					for (XWPFTableCell cell : row.getTableCells()) {
						for (XWPFParagraph p : cell.getParagraphs()) {
							replaceRuns(p, aRemplacer, valeur);

						}
					}
				}
			}
		}
	}

	public void remplacer(List<Balise> balises) {
		for (Balise balise : balises) {
			remplacer(balise.getKey(), balise.getValue());
		}
	}
	public void remplacer(Testable testable) {
		remplacer(testable.toBalise());
	}

	public void remplacer() {
		Chrono.newTimer(carteChoisie.getTestables().size(), progress).start();
		remplacer("[bios_version]", ConfigBIOS.versionBIOS);
		remplacer("[date]", InfoPV.date);
		remplacer("[operateur]",InfoPV.operateur);
		remplacer("[numero]",InfoPV.numModule);
		carteChoisie.forEachTestableCompenent(testable -> remplacer(testable));
	}

	public void replaceRuns(XWPFParagraph paragraph,String oldValue, String newValue) {
		String runText = "";
		List<XWPFRun> runs = paragraph.getRuns();
		if (runs != null) {
			Iterator<XWPFRun> iterator = runs.iterator();
			while (iterator.hasNext()) {
				XWPFRun run = iterator.next();
				runText += run.getText(0);
			}
			if (runText != null && runText.contains(oldValue)) {

				runText = runText.replace(oldValue, newValue);
				runText = runText.replace("null", "");
				runText = runText.replace("[", "");
				runText = runText.replace("]", "");
				for (int i = runs.size() - 1; i >= 0; i--) paragraph.removeRun(i);
				paragraph.createRun().setText(runText);
			}
		}
	}


	public void save(String chemin) {
		try {
			FileOutputStream out = new FileOutputStream(chemin);
			doc.write(out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




}
