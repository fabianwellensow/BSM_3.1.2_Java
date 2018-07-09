package de.gdv.bsm.vu.module;

/**
 * Funktionen des Moduls <code>Esg</code>.
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
public class EsgFormeln {

	/**
	 * Berechnet den mittleren Zins. <br/>
	 * Funktionsname in Excel: jaehrl_Zins_ESG.
	 * 
	 * @param vgDiskontEsg
	 *            Diskontfunktion (Vorg�nger)
	 * @param diskontEsg
	 *            Diskontfunktion
	 * @return mittleren Zins
	 */
	public static double jaehrlZinsEsg(final double vgDiskontEsg, final double diskontEsg) {
		return vgDiskontEsg / diskontEsg - 1.0;
	}

}
