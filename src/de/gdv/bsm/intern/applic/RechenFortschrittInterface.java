package de.gdv.bsm.intern.applic;

/**
 * Interface zur Protokollierung des Rechenfortschritts.
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
public interface RechenFortschrittInterface {
	/**
	 * Die Berechnung wurd fehlerfrei beendet.
	 * 
	 * @param resultat
	 *            Resultate der Berechnung
	 */
	public void berechnungBeendet(final BerechnungResultat resultat);

	/**
	 * Die Berechnung ist mit der angegebenen Exception gecrasht.
	 * 
	 * @param reason
	 *            die Exception
	 */
	public void berechnungGechrashed(final Throwable reason);

	/**
	 * Setze den Fortschritt f�r das Laden der Zinskurve.
	 * 
	 * @param id
	 *            die Zinskurve
	 * @param i
	 *            der Prozentsatz
	 */
	public void setSznrPercent(final int id, final int i);

	/**
	 * Setze den Fortschritt f�r das Rechnen eines Szenarios.
	 * 
	 * @param id
	 *            das Szenario
	 * @param lastPercent
	 *            der Prozentsatz
	 */
	public void setBerechnungPercent(final int id, final int lastPercent);

	/**
	 * Soll die Berechnung abgebrochen werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isAbbruch();

	/**
	 * Setze den Fortschrittsbalken f�r die angegebene Klasse.
	 * 
	 * @param klasse
	 *            die Klasse
	 * @param percent
	 */
	public void setAusgabePercent(Class<?> klasse, int percent);
}
