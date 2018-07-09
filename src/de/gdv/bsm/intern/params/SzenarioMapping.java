package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.gdv.bsm.intern.applic.Pair;
import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Mapping der verschiedenen Szenarien auf die Eingabedaten. Abbild des Blattes <code>sznr-mapping</code>. Jede Zeile
 * enth�lt die Angaben f�r ein Szenario.
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
public class SzenarioMapping {
	private final List<String> header;
	private final Map<Integer, SzenarioMappingZeile> zeilen = new HashMap<>();

	/**
	 * Erstelle ein Szenario-Mapping aus einer Datei.
	 * 
	 * @param dataFile
	 *            der vollst�ndige Dateiname
	 * @throws IOException
	 *             bei Ein/Ausgabefehlern
	 * @throws LineFormatException
	 *             wenn die csv-Datei nicht korrekt formatiert ist.
	 */
	public SzenarioMapping(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			csv.readLine();
			header = csv.getTitel();

			CsvZeile zeile;
			while ((zeile = csv.readLine()) != null) {
				SzenarioMappingZeile smZeile = new SzenarioMappingZeile(zeile);
				if (zeilen.containsKey(smZeile.getId())) {
					throw new LineFormatException(
							"Doppelte Stressszenario ID in Zeile " + zeile.getZeilenNummer() + ": " + smZeile.getId());
				}
				zeilen.put(smZeile.getId(), smZeile);
			}
		}
	}

	/**
	 * Die Namen der Titelfelder.
	 * 
	 * @return die Namen
	 */
	public List<String> getHeader() {
		return Collections.unmodifiableList(header);
	}

	/**
	 * Ermittle zu einer Szenario-Id das passende Mapping.
	 * 
	 * @param szenarioId
	 *            die Id
	 * @return das Mapping
	 */
	public SzenarioMappingZeile getSzenarionMapping(final int szenarioId) {
		return zeilen.get(szenarioId);
	}

	/**
	 * Liste von Szenarien-ID's.
	 * 
	 * @return die liste
	 */
	public List<Pair<Integer, String>> getList() {
		final List<Pair<Integer, String>> list = new ArrayList<>();
		final Set<Integer> set = new TreeSet<>();
		set.addAll(zeilen.keySet());
		for (int id : set) {
			list.add(new Pair<Integer, String>(id, zeilen.get(id).getName()));
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Erstelle eine Liste aller als aktiv markierten Szenarien.
	 * 
	 * @return die Szenarien aufsteigend nach ID
	 */
	public List<SzenarioMappingZeile> getAktiveSzenarien() {
		final List<SzenarioMappingZeile> list = new ArrayList<>();
		for (int id : new TreeSet<>(zeilen.keySet())) {
			if (zeilen.get(id).isAktiv()) {
				list.add(zeilen.get(id));
			}
		}
		return list;
	}
}
