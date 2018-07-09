package de.gdv.bsm.intern.applic;

import javax.swing.table.TableModel;

/**
 * Spezielles TableModel mit Zusatzangaben.
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
public interface SizedTableModel extends TableModel {

	/**
	 * Ist die Spalte eine Prozent-Spalte?
	 * 
	 * @param column
	 *            die Spalte
	 * @return ja oder nein
	 */
	public boolean isPercent(final int column);

	/**
	 * Anzahl der anzuzeigenden Nachkommastellen einer Spalte.
	 * 
	 * @param column
	 *            die Spalte
	 * @return die Nachkommastellen
	 */
	public int getNachkommaStellen(final int column);

	/**
	 * Breite einer Spalte in Buchstaben.
	 * 
	 * @param column
	 *            die Spalte
	 * @return die Anzahl Buchstaben
	 */
	public int getWidth(final int column);

	/**
	 * ToolTip f�r eine Zelle.
	 * 
	 * @param row
	 *            die Zeile
	 * @param column
	 *            die Spalte
	 * @return der ToolTip
	 */
	public String getToolTip(final int row, final int column);
}
