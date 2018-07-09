package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Datenzeile VT ohne Stress. Enspricht einer Zeile des Excel-Blattes VT o.Stress.
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
public class VtOStressZeile {
	/** A. */
	private final String lob;
	/** B. */
	private final int zeit;
	/** C. */
	private final int zinsGeneration;
	/** D. */
	private final String altNeuBestand;
	/** E. */
	private final double sueaf;
	/** F. */
	private final double zzr;
	/** G. */
	private final double zzrJeBasis;
	/** H. */
	private final double korrekturZzr;
	/** I. */
	private final double startWertRefZins;

	/**
	 * Erstelle die Daten aus einer aufbereiteten Zeile der csv-Datei.
	 * 
	 * @param zeile
	 *            die Zeile der csv-Datei
	 * @throws LineFormatException
	 *             bei Formatfehlern
	 */
	public VtOStressZeile(final CsvZeile zeile) throws LineFormatException {
		lob = zeile.getString(0);
		zeit = zeile.getInt(1);
		zinsGeneration = zeile.getInt(2);
		altNeuBestand = zeile.getString(3);
		sueaf = zeile.getDouble(4);
		zzr = zeile.getDouble(5);
		zzrJeBasis = zeile.getDouble(6);
		korrekturZzr = zeile.getDouble(7);
		startWertRefZins = zeile.getDouble(8);
	}

	/**
	 * LOB. Spalte A.
	 * 
	 * @return the lob
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * Zeit. Spalte B.
	 * 
	 * @return the zeit
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Rechnungszinsgeneration. Spalte C.
	 * 
	 * @return the zinsGeneration
	 */
	public int getZinsGeneration() {
		return zinsGeneration;
	}

	/**
	 * Alt- / Neubestand (a / n). Spalte D.
	 * 
	 * @return der Wert
	 */
	public String getAltNeuBestand() {
		return altNeuBestand;
	}

	/**
	 * S�AF nur als Startwert zum Stichtag (zeitpunkt 0). Spalte E.
	 * 
	 * @return the sueaf
	 */
	public double getSueaf() {
		return sueaf;
	}

	/**
	 * ZZR als Startwert. Spalte F.
	 * 
	 * @return the zzr
	 */
	public double getZzr() {
		return zzr;
	}

	/**
	 * ZZR Aufwand je Basispunkt je Euro Deckungsr�ckstellung. Spalte G.
	 * 
	 * @return the zzrJeBasis
	 */
	public double getZzrJeBasis() {
		return zzrJeBasis;
	}

	/**
	 * Korrekturterm ZZR wegen Rechnungsgrundlagen. Spalte H.
	 * 
	 * @return der Wert
	 */
	public double getKorrekturZzr() {
		return korrekturZzr;
	}

	/**
	 * Startwert Referenzzins. Spalte I.
	 * 
	 * @return der Wert
	 */
	public double getStartWertRefZins() {
		return startWertRefZins;
	}

}
