package de.gdv.bsm.intern.szenario;

import java.util.List;

/**
 * Daten eines Pfades.
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
public class Pfad {
	private final int pfadNummer;
	private final List<PfadZeile> zeilen;

	/**
	 * Konstruiere einen Pfad aus den Zeilen.
	 * 
	 * @param pfadNummer
	 *            des Pfades
	 * @param zeilen
	 *            des Pfades
	 */
	public Pfad(final int pfadNummer, final List<PfadZeile> zeilen) {
		this.pfadNummer = pfadNummer;
		this.zeilen = zeilen;
	}

	/**
	 * Die Pfadnummer.
	 * 
	 * @return die Zahl
	 */
	public int getPfadNummer() {
		return pfadNummer;
	}

	/**
	 * Die Zeile.
	 * 
	 * @param zeilenNummer
	 *            entspricht der Zeit
	 * @return die Zeile
	 */
	public PfadZeile getPfadZeile(final int zeilenNummer) {
		return zeilen.get(zeilenNummer);
	}
}
