package de.gdv.bsm.intern.applic;

import java.io.File;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.gdv.bsm.intern.params.Eingabe;
import de.gdv.bsm.intern.params.VuParameter;

/**
 * Start der Applikation.
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zust�ndigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschlie�en, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende R�ckmeldungen w�rde der GDV
 * begr��en. Der GDV �bernimmt aber keine Haftung f�r die fehlerfreie Funktionalit�t des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschlie�lich aller Tabellen, Grafiken und Erl�uterungen sind urheberrechtlich
 * gesch�tzt. Die ausschlie�lichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell � GDV 2016</b>
 */
public class Applic {
	/** Maximal zu berechnender Zeitschritt. */
	public static final int PROJEKTIONS_HORIZONT = 100;

	/**
	 * Zentraler Aufruf der Applikation.
	 * <p/>
	 * Folgende Parameter sind moglich:
	 * <ul>
	 * <li><b>--batch</b> (optional): Batch-Aufruf aus Excel ohne Eingabedialog.</li>
	 * <li><b>Basisverzeichnis</b> (optional): Verzeichnis, in dem die Vorgaben als csv-Dateien liegen.</li>
	 * </ul>
	 * Wird im Batch kein Basisverzeichnis angegeben, so wird das aktuelle Verzeichnis verwendet. Im Dialog wird
	 * hingegen ein Auswahldialog f�r ein Verzeichnis ge�ffnet.
	 * 
	 * @param args
	 *            die Kommandozeilenargumente
	 */
	public static void main(String[] args) {
		setLookAndFeel();

		final File transferDir;
		boolean batch = false;

		int dirIndex = 0;
		if (args.length > 0 && args[0].equals("--batch")) {
			batch = true;
			++dirIndex;
		}
		if (dirIndex >= args.length && batch) {
			// im Batch w�hlen wir das aktuelle Verzeichnis
			transferDir = new File(".");
		} else if (dirIndex < args.length) {
			transferDir = new File(args[dirIndex]);
			if (!transferDir.exists() || !transferDir.isDirectory()) {
				JOptionPane.showMessageDialog(null,
						"Das angegebene Verzeichnis\n" + transferDir.getAbsolutePath()
								+ "\nexistiert nicht oder ist kein Verzeichnis - ABBRUCH",
						"Parameterfehler", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		} else {
			// suche ein Verzeichnis aus:
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Verzeichnis mit Eingabedaten angeben");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			switch (fileChooser.showOpenDialog(null)) {
			case JFileChooser.APPROVE_OPTION:
				transferDir = fileChooser.getSelectedFile();
				break;
			default:
				System.exit(1);
				return;
			}
		}

		try {
			final VuParameter vuParameter = new VuParameter(transferDir);

			final File eingabeFile = new File(transferDir, VuParameter.EINGABE_FILENAME);
			final Eingabe eingabe;
			if (eingabeFile.exists() && eingabeFile.isFile() && eingabeFile.canRead()) {
				eingabe = new Eingabe(vuParameter, eingabeFile);
			} else {
				eingabe = new Eingabe(vuParameter);
			}

			if (!batch) {
				new ApplicFrame(vuParameter, eingabe);
			} else {
				final RechenFortschritt rf = new RechenFortschritt(vuParameter, eingabe);
				switch (rf.getExitCode()) {
				case ABBRUCH:
					System.exit(2);
					break;
				case FEHLER:
					throw rf.getCrashReason();
				case OK:
					break;
				default:
					throw new IllegalArgumentException("Unbekannter Exit-Code: " + rf.getExitCode());
				}
				System.exit(0);
			}
		} catch (Throwable e) {
			new ErrorMessageBox(e);
			System.exit(1);
		}

	}

	/**
	 * setzt das System-LookAndFeel der verwendeten Plattform, falls dies nicht klappt wird nur eine Error-Message auf
	 * StdErr geschickt und der dialog macht mir dem Standard-Java-LookAndFeel weiter.
	 */
	private static void setLookAndFeel() {
		Locale.setDefault(new Locale("de", "DE"));
		// Setze f�r den UIManager das look-and-feel passend zum aktuellen Betriebssystem
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
	}

}
