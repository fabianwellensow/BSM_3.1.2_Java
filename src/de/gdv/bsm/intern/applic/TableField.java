package de.gdv.bsm.intern.applic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;

/**
 * Annotation f�r Felder, die in Tabellen angezeigt werden sollen.
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
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
	/** Optionen f�r den Test von csv-Dateien mit berechneten Werten. */
	public static enum TestOption {
		/** Dieses Feld wird nicht gepr�ft. */
		NO,
		/** F�r dieses Feld werden nur die Vorgegebenen Zeitschritte gepr�ft. */
		START,
		/** Dieses Feld wird insgesamt gepr�ft. */
		FULL
	}

	/**
	 * Anzahl der gew�nschten Nachkommastellen.
	 * 
	 * @return die Stellen
	 */
	public int nachKomma() default 1;

	/**
	 * Soll die Zahl als Prozentzahl angezeigt werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean percent() default false;

	/**
	 * Soll das Feld angezeigt werden? Wird benutzt, um Spalten zu unterdr�cken!
	 * 
	 * @return ja oder nein
	 */
	public boolean suppress() default false;

	/**
	 * Soll dieses Feld bei der Endlichkeitspr�fung ber�cksichtigt werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean checkFinite() default true;

	/**
	 * Feldname in {@link KennzahlenPfadweise} des CV Feldes. F�r Felder aus {@link KennzahlenPfadweise} und
	 * {@link KennzahlenPfadweiseLoB} wird dies als CV verwendet (siehe die Definition im Excel-Blatt
	 * StrgTab_Mittelwerte).
	 * 
	 * @return der Feldname
	 */
	public String cvKennzahlen() default "mwPassiva";

	/**
	 * Soll dieses Feld getestet werden?
	 * 
	 * @return eine der Optionen
	 */
	public TestOption testOption() default TestOption.FULL;

	/**
	 * Column-Bezeichnung in Excel, falls von Reihenfolge in File abweichend.
	 * 
	 * @return die Bezeichnung
	 */
	public String testColumn() default "";
}
