package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Dateinzeile der Struktur {@link BwAktivaFi}.
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
 *
 */
public class BwAktivaFiZeile {
	private final String risikoKategorie;
	private final int zeit;
	private final double cashflowFi;
	private final double ertrag;

	/**
	 * Erstelle eine Datenzeile.
	 * 
	 * @param zeile
	 *            die aufbereitete Zeile der csv-Datei
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Zeile
	 */
	public BwAktivaFiZeile(final CsvZeile zeile) throws LineFormatException {
		risikoKategorie = zeile.getString(0);
		zeit = zeile.getInt(1);
		final double c = zeile.getDouble(2);
		cashflowFi = Double.isNaN(c) ? 0.0 : c;
		final double e = zeile.getDouble(3);
		ertrag = Double.isNaN(e) ? 0.0 : e;
	}

	/**
	 * Die Risikokategorie. Spalte A.
	 * 
	 * @return die Kategorie
	 */
	public String getRisikoKategorie() {
		return risikoKategorie;
	}

	/**
	 * Die Zeit dieser Zeile. Spalte B.
	 * 
	 * @return die Zeit
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Der Cashflow. Spalte C.
	 * 
	 * @return der Wert
	 */
	public double getCashflowFi() {
		return cashflowFi;
	}

	/**
	 * Der Ertrag. Spalte D.
	 * 
	 * @return der Wert
	 */
	public double getErtrag() {
		return ertrag;
	}

}
