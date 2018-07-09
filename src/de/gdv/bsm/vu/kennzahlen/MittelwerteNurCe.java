package de.gdv.bsm.vu.kennzahlen;

import static de.gdv.bsm.vu.module.DiskontFunktion.dfVu;

import java.util.List;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;
import de.gdv.bsm.vu.module.DiskontFunktion;

/**
 * Berechnung der Mittelwerte. Dies entspricht den Angaben im Blatt <code>StrgTab_Mittelwerte</code> in der Rubrik
 * <b>Nur CE</b>.
 * <p/>
 * Die Namen der Kenngr��en wurden �bernommen und nur nach Java-Konvention ver�ndert. Die Spaltennamen der einflie�enden
 * Gr��en wurden durch <code>get...</code> -Funktionen im Blatt <code>rzg</code> ersetzt. Die Java-Doc gibt Auskunft
 * �ber die Spalten. Sollen andere Werte aus <code>agg</code> verwendet werden, sind in der Klasse {@link RzgZeile}
 * eventuell passende <code>get...</code> -Funktionen zu erg�nzen. Die dortigen Angaben <code>DF</code> bzw.
 * <code>DFVU</code> wurden durch die Funktionsaufrufe {@link DiskontFunktion#df(double, AggZeile)} und
 * {@link DiskontFunktion#dfVu(double, AggZeile, int)} ersetzt. Bei Bedarf sind diese Anzupassen. Addition bzw.
 * Subtraktion wurden direkte in die Formeln �bernommen.
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
public class MittelwerteNurCe {
	// Schl�sselfelder d�rfen hier nicht Annotiert werden, da diese
	// sp�ter zur Zusammenfassung genutzt werden
	private final int pfad;
	private final String szenario;
	private final int szenarioId;
	private final String lob;
	private final int zeit;
	@TableField
	private final double aq;
	@TableField
	private final double l;
	@TableField
	private final double m;
	@TableField
	private final double aqOrig;

	/**
	 * Erstelle die Kennzahlen zu einem Pfad anhand der Agg-Zeilen.
	 * 
	 * @param pfad
	 *            der berechnete Pfad
	 * @param szenario
	 *            Name des berechneten Szenarios
	 * @param szenarioId
	 *            zu dem die Berechnung geh�rt
	 * @param lob
	 *            berechnete Line of Business
	 * @param zeit
	 *            Zeitpunkt (Jahr)
	 * @param rzgZeilen
	 *            zugeh�rige RZG-Zeilen
	 * @param aggZeile
	 *            zum passenden Zeitpunkt
	 * @param monat
	 *            der Zahlungsmonat aus den zeitunabh�ngigen Managementregeln
	 */
	public MittelwerteNurCe(final int pfad, final String szenario, final int szenarioId, final String lob,
			final int zeit, final List<RzgZeile> rzgZeilen, final AggZeile aggZeile, final double monat) {
		this.pfad = pfad;
		this.szenario = szenario;
		this.szenarioId = szenarioId;
		this.lob = lob;
		this.zeit = zeit;

		this.aq = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getLGarantiertDet(), aggZeile, monat)).sum();
		this.l = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getKosten(), aggZeile, monat)).sum();
		this.m = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getPraemien(), aggZeile, monat)).sum();
		this.aqOrig = rzgZeilen.stream().mapToDouble(z -> z.getLGarantiertDet()).sum();
	}

	/**
	 * @return the pfad
	 */
	public int getPfad() {
		return pfad;
	}

	/**
	 * @return the szenario
	 */
	public String getSzenario() {
		return szenario;
	}

	/**
	 * @return the szenarioId
	 */
	public int getSzenarioId() {
		return szenarioId;
	}

	/**
	 * @return the lob
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * @return the zeit
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * @return the aq
	 */
	public double getAq() {
		return aq;
	}

	/**
	 * @return the l
	 */
	public double getL() {
		return l;
	}

	/**
	 * @return the m
	 */
	public double getM() {
		return m;
	}

	/**
	 * @return the aqOrig
	 */
	public double getAqOrig() {
		return aqOrig;
	}

}
