package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Modelliert eine Zeile des Blattes des Blattes <code>HGB Bilanzdaten</code>.
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
public class HgbBilanzdatenZeile {
	final String aktivaPassiva;
	final String position;
	final String produktKategorie;
	final double buchwert;
	final double vorDirektgutschriftAnpassung;

	/**
	 * Erstelle eine Zeile aus den Rohdaten der csv-Datei.
	 * 
	 * @param zeile
	 *            der csv-Datei
	 * @throws LineFormatException
	 *             bei Formatfehlern
	 */
	public HgbBilanzdatenZeile(final CsvZeile zeile) throws LineFormatException {
		aktivaPassiva = zeile.getString(0).trim();
		position = zeile.getString(1).trim();
		produktKategorie = zeile.getString(2).trim();
		buchwert = zeile.getDouble(3);
		vorDirektgutschriftAnpassung = zeile.getDouble(4);
	}

}
