package de.gdv.bsm.intern.csv;

/**
 * Exception f�r Formatfehler beim Lesen einer Zeile.
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
@SuppressWarnings("serial")
public class LineFormatException extends Exception {
	/**
	 * Erstelle eine neue Exception.
	 * 
	 * @param message
	 *            die zugeh�rige Meldung.
	 */
	public LineFormatException(String message) {
		super(message);
	}

	/**
	 * Erstelle eine Exception mit einem Ursprungsgrund.
	 * 
	 * @param message
	 *            die Meldung
	 * @param causedBy
	 *            die eigentliche Ursache
	 */
	public LineFormatException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
