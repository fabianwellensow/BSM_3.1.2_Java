package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Zeile eines Satzes in der {@link VUHistorie}.
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
public class VUHistorieZeile {

	private final int zeit;
	/** RfB Zuf�hrung B. */
	private final double rfB_Zuf�hrung;
	/** Roh�berschuss C. */
	private final double rohueberschuss;
	/** HGB DRSt inkl. Ansammlungs-guthaben ohne ZZR D. */
	private final double hGB_DRSt_InklAnsammlungsguthaben_ohne_ZZR;

	/**
	 * Erstelle eine Zeile aus einer aufbereiteten csv-Zeile
	 * 
	 * @param zeile
	 *            die Zeile
	 * @throws LineFormatException
	 *             bei csv-Formatfehlern
	 * @throws EmptyLineException
	 *             wenn die Zeile leer ist
	 */
	public VUHistorieZeile(final CsvZeile zeile) throws LineFormatException, EmptyLineException {
		if (zeile.getString(0) == null || zeile.getString(0).isEmpty())
			throw new EmptyLineException();
		zeit = zeile.getInt(0);
		rfB_Zuf�hrung = zeile.getDouble(1);
		rohueberschuss = zeile.getDouble(2);
		hGB_DRSt_InklAnsammlungsguthaben_ohne_ZZR = zeile.getDouble(3);
	}

	/**
	 * Die Zeit. Spalte A.
	 * 
	 * @return der Wert
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Die RfB Zuf�hrung. Spalte B.
	 * 
	 * @return der Wert
	 */
	public double getRfBZufuehrung() {
		return rfB_Zuf�hrung;
	}

	/**
	 * Der Roh�berschuss. Spalte C.
	 * 
	 * @return der Wert
	 */
	public double getRohueberschuss() {
		return rohueberschuss;
	}

	/**
	 * Die HGB DRSt inkl. Ansammlungsguthaben ohne ZZR. Spalte D.
	 * 
	 * @return der Wert
	 */
	public double getHgbDrst() {
		return hGB_DRSt_InklAnsammlungsguthaben_ohne_ZZR;
	}

}
