package de.gdv.bsm.intern.szenario;

/**
 * Lesen von double-Werten f�r Szenarien.
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
public class Parse {
	static double parseDouble(final String data, final int semikolon) {
		if (semikolon == 1 && data.charAt(0) == '0')
			return 0.0;
		final String s = (semikolon < 0 ? data : data.substring(0, semikolon)).trim();
		if (s.isEmpty())
			return Double.NaN;
		if (s.equals("-"))
			return 0.0;
		return Double.parseDouble(s);
	}

	static double parseDouble2(final StringBuilder buffer, final int start, final int semikolon) {
		if ((semikolon - start) == 1 && (buffer.charAt(start) == '0' || buffer.charAt(start) == '-'))
			return 0.0;
		if (semikolon == start)
			return Double.NaN;
		return Double.parseDouble(buffer.substring(start, semikolon));
	}

}
