package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * VU-Historie f�r chronologisch zur�ckliegende Zeitschritte. Abbild des Blattes <code>VU Historie</code>.
 * 
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
public class VUHistorie {
	private final List<VUHistorieZeile> zeilen = new ArrayList<>();

	/**
	 * Erstelle die Tabelle aus der exportierten csv-Datei.
	 * 
	 * @param dataFile
	 *            Name der Eingabedatei
	 * @throws IOException
	 *             Dateifehler
	 * @throws LineFormatException
	 *             Formatfehler in der csv-Datei
	 */

	public VUHistorie(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			csv.readLine();
			CsvZeile line;
			int zeitPrev = Integer.MAX_VALUE;
			while ((line = csv.readLine()) != null) {
				try {
					final VUHistorieZeile z = new VUHistorieZeile(line);
					zeilen.add(z);

					if (zeitPrev != Integer.MAX_VALUE && z.getZeit() != zeitPrev + 1) {
						throw new IllegalArgumentException("Zeit nicht chronologisch fortlaufend.");
					}
					zeitPrev = z.getZeit();
				} catch (EmptyLineException e) {
					// leere Zeilen werden ignoriert
				}
			}
		}
		Collections.reverse(zeilen);
	}

	/**
	 * Ermittle die Werte f�r einen Zeitschritt
	 * 
	 * @param zeit
	 *            der Zeitschritt <= 0
	 * @return die Zeile
	 */
	public VUHistorieZeile get(final int zeit) {
		return zeilen.get(-zeit);
	}

}
