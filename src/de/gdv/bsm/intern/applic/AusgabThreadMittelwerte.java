package de.gdv.bsm.intern.applic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import de.gdv.bsm.intern.rechnung.Mittelwerte;
import de.gdv.bsm.intern.rechnung.RechenThread;

/**
 * Spezieller Thread f�r die Ausgabe von {@link Mittelwerte}.
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
public class AusgabThreadMittelwerte implements AusgabeThread {
	private final RechenThread master;
	private final RechenFortschrittInterface rechenFortschritt;
	private final List<Mittelwerte> mittelwerte;
	private final File fileName;
	private Optional<Throwable> error = Optional.empty();

	/**
	 * Erstelle ein Runnable.
	 * 
	 * @param master
	 *            kontrollierender Prozess
	 * @param rechenFortschritt
	 *            f�r die Ausgabe von Prozentangaben
	 * @param mittelwerte
	 *            die auszugebenden Datens�tze
	 * @param fileName
	 *            die Ausgabedatei
	 */
	public AusgabThreadMittelwerte(final RechenThread master, final RechenFortschrittInterface rechenFortschritt,
			final List<Mittelwerte> mittelwerte, final File fileName) {
		this.master = master;
		this.rechenFortschritt = rechenFortschritt;
		this.mittelwerte = mittelwerte;
		this.fileName = fileName;
	}

	@Override
	public void run() {
		if (mittelwerte.size() == 0) {
			// ohne Daten keine Ausgabe!!
			master.ausgabeReady(this);
			return;
		}
		try (final PrintStream out = new PrintStream(
				new BufferedOutputStream(new FileOutputStream(fileName), 8 * 1024))) {
			boolean first = true;
			for (String f : mittelwerte.get(0).getTitleDruckZeile1()) {
				out.print((first ? "" : ";") + f);
				first = false;
			}
			first = true;
			out.println();
			for (String f : mittelwerte.get(0).getTitleDruckZeile2()) {
				out.print((first ? "" : ";") + f);
				first = false;
			}
			out.println();

			int lastPercent = 0;
			int anzahl = 0;
			for (Mittelwerte mw : mittelwerte) {
				mw.writeZeile(out);
				++anzahl;
				final int percent = (anzahl * 100) / mittelwerte.size();
				if (lastPercent != percent) {
					rechenFortschritt.setAusgabePercent(Mittelwerte.class, percent);
					lastPercent = percent;
				}
			}
		} catch (Throwable e) {
			error = Optional.of(e);
		} finally {
			master.ausgabeReady(this);
		}
	}

	@Override
	public Optional<Throwable> getError() {
		return error;
	}

}
