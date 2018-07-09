package de.gdv.bsm.intern.rechnung;

/**
 * Fehlersignalisierung f�r unendliche oder NaN Ergebnisse.
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
@SuppressWarnings("serial")
public class ResultNotFinite extends Error {
	private final String header;
	private final String felder;

	/**
	 * Erstelle eine Fehlermeldung.
	 * 
	 * @param message
	 *            die Meldung insgesamt
	 * @param header
	 *            der Beginn der Meldung
	 * @param felder
	 *            die betroffenen Felder
	 */
	public ResultNotFinite(final String message, final String header, final String felder) {
		super(message);
		this.header = header;
		this.felder = felder;
	}

	/**
	 * Liste der fehlerhaften Felder
	 * 
	 * @return die Felder
	 */
	public String getFelder() {
		return felder;
	}

	/**
	 * Header-Text-
	 * 
	 * @return der Text
	 */
	public String getHeader() {
		return header;
	}
}
