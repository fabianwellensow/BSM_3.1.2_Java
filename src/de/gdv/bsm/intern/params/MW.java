package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Mapping der verschiedenen Marktwerte. Abbild des Blattes <code>MW</code>. Die Daten zu den Szenarien erh�lt man mit
 * den String-Konstanten
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
public class MW {
	/** Konstante f�r den Zugriff. */
	public static final String EQ_RE_FI = "EQ_RE_FI";
	/** Konstante f�r den Zugriff. */
	public static final String EQ = "EQ";
	/** Konstante f�r den Zugriff. */
	public static final String RE = "RE";
	/** Konstante f�r den Zugriff. */
	public static final String FI = "FI";
	/** Konstante f�r den Zugriff. */
	public static final String SA_F_VV = "SA + F_VV";
	/** Konstante f�r den Zugriff. */
	public static final String SP = "SP";

	private final Map<Integer, Map<String, MWZeile>> daten;

	/**
	 * Erzeuge die Mapping-Struktur aus einer csv-Datei.
	 * 
	 * @param dataFile
	 *            vollst�ndiger Name der Eingabedatei
	 * @throws IOException
	 *             bei Dateifehlern
	 * @throws LineFormatException
	 *             bei Formatfehlern in der csv-Datei
	 */
	public MW(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// Header weglesen
			csv.readLine();

			final Map<Integer, Map<String, MWZeile>> map = new HashMap<>();

			CsvZeile line;
			while ((line = csv.readLine()) != null) {
				final MWZeile z = new MWZeile(line);

				if (!map.containsKey(z.getStressSzenarioId())) {
					map.put(z.getStressSzenarioId(), new HashMap<>());
				}

				final Map<String, MWZeile> idMap = map.get(z.getStressSzenarioId());
				idMap.put(z.getKapitalAnlageKlasse(), z);
			}
			for (int id : map.keySet()) {
				map.put(id, Collections.unmodifiableMap(map.get(id)));
			}
			daten = Collections.unmodifiableMap(map);
		}

	}

	/**
	 * Ermittle die Zeile zu einem Szenario und einer Anlageklasse.
	 * 
	 * @param szenarioId
	 *            des Szenarios
	 * @param kapitalAnlageKlasse
	 *            der Klasse
	 * @return der Satz
	 */
	public MWZeile get(final int szenarioId, final String kapitalAnlageKlasse) {
		return daten.get(szenarioId).get(kapitalAnlageKlasse);
	}

}
