package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Das Mapping f�r ein LoB.
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
public class LobMappingZeile {
	private final String kuerzel;
	private final String beschreibung;
	private final String kategorie;
	private final String uebNueb;
	private final String zinsSensitiv;
	private final int kaKostenstressDerLob;

	/**
	 * Erstelle ein Mapping aus der Zeile der csv-Datei.
	 * 
	 * @param zeile
	 *            die Zeile
	 * @throws LineFormatException
	 *             bei Formatfehlern
	 */
	public LobMappingZeile(final CsvZeile zeile) throws LineFormatException {
		kuerzel = zeile.getString(0).trim();
		beschreibung = zeile.getString(1);
		kategorie = zeile.getString(2).trim();
		uebNueb = zeile.getString(3).trim();
		zinsSensitiv = zeile.getString(4);
		kaKostenstressDerLob = zeile.getInt(5);

	}

	/**
	 * K�rzel des LOB. Spalte A
	 * 
	 * @return das K�rzel
	 */
	public String getKuerzel() {
		return kuerzel;
	}

	/**
	 * Informelle Beschreibung dieses LOB. Spalte B
	 * 
	 * @return die Beschreibung
	 */
	public String getBeschreibung() {
		return beschreibung;
	}

	/**
	 * Kategorie des LOB. Spalte C
	 * 
	 * @return die Kategorie
	 */
	public String getKategorie() {
		return kategorie;
	}

	/**
	 * Das Ueb/Nueb dieses Lob. Spalte D
	 * 
	 * @return die Information
	 */
	public String getUebNueb() {
		return uebNueb;
	}

	/**
	 * Zinssensitiv (j/n) Spalte E
	 *
	 * @return istZinssensitiv
	 */
	public String getZinsSensitiv() {
		return zinsSensitiv;
	}

	/**
	 * KA-Kostenstress der LoB Spalte F
	 * 
	 * @return KA-Kostenstress
	 */
	public int getKaKostenstressDerLob() {
		return kaKostenstressDerLob;
	}

}
