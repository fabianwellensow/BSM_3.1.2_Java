package de.gdv.bsm.vu.module;

/**
 * Allgemeine Funktionen.
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
public class Functions {
	/**
	 * Wandelt eine Zahl in einen Prozentwert um.
	 * 
	 * @param bp
	 *            der Wert
	 * @return der umgewandelte Wert
	 */
	public static double inProzent(final double bp) {
		return bp / 10000.0;
	}

	/**
	 * Mache aus einem NaN eine echte Null.
	 * 
	 * @param x
	 *            die Zahl
	 * @return die Zahl, oder 0.0, wenn x NaN ist.
	 */
	public static double nanZero(final double x) {
		return Double.isNaN(x) ? 0.0 : x;
	}

	/**
	 * Summiere die Zahlen eines Arrays.
	 * 
	 * @param array
	 *            das Array
	 * @return die Summe
	 */
	public static double sum(final double[] array) {
		double r = 0.0;
		for (double d : array)
			r += Functions.nanZero(d);
		return r;
	}
}
