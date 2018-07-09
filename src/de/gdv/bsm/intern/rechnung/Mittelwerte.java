package de.gdv.bsm.intern.rechnung;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.vu.kennzahlen.MittelwerteNurCe;
import de.gdv.bsm.vu.kennzahlen.MittelwerteUndCe;

/**
 * Ausgabe und Anzeige der berechneten Mittelwerte. Dies sind die {@link MittelwerteUndCe} und {@link MittelwerteNurCe},
 * wobei die ersteren auch �ber alle Pfade gemittelt werden.
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
public class Mittelwerte {
	private final String szenario;
	private final int szenarioId;
	private final String lob;
	private final int zeit;

	private int anzahl = 0;
	private List<Field> sumFields = new ArrayList<>();
	private List<String> title = new ArrayList<>();
	private List<String> titleDrZeile1 = new ArrayList<>();
	private List<String> titleDrZeile2 = new ArrayList<>();
	private List<Double> values = new ArrayList<>();

	/**
	 * Erstelle die Grunddaten einer Zeile.
	 * 
	 * @param mwUndCe
	 *            die Grunddaten
	 * @param addierePfad0
	 *            sollen die Daten f�r Pfad 0 mit addiert werden?
	 * @throws IllegalArgumentException
	 *             bei Systemfehlern
	 * @throws IllegalAccessException
	 *             bei Systemfehlern
	 */
	public Mittelwerte(final MittelwerteUndCe mwUndCe, final boolean addierePfad0)
			throws IllegalArgumentException, IllegalAccessException {
		this.szenario = mwUndCe.getSzenario();
		this.szenarioId = mwUndCe.getSzenarioId();
		this.lob = mwUndCe.getLob();
		this.zeit = mwUndCe.getZeit();

		for (Field field : mwUndCe.getClass().getDeclaredFields()) {
			final TableField tf = field.getAnnotation(TableField.class);
			if (tf != null) {
				sumFields.add(field);
				title.add("<html><p>" + field.getName() + "</p><p>Mittelwert</p></html>");
				titleDrZeile1.add(field.getName());
				titleDrZeile2.add("Mittelwert");
				values.add(0.0);
			}
		}
		addValues(mwUndCe, addierePfad0);
	}

	/**
	 * Addiere den Grunddaten die Daten f�r einen weiteren Pfad hinzu.
	 * 
	 * @param mwUndCe
	 *            die Daten von Pfad 0.
	 * @param addierePfad0
	 *            sollen die Daten f�r Pfad 0 mit addiert werden?
	 * @throws IllegalArgumentException
	 *             bei Systemfehlern
	 * @throws IllegalAccessException
	 *             bei Systemfehlern
	 */
	public void addValues(final MittelwerteUndCe mwUndCe, final boolean addierePfad0)
			throws IllegalArgumentException, IllegalAccessException {
		if (mwUndCe.getPfad() == 0 && !addierePfad0)
			return;
		++anzahl;
		for (int i = 0; i < sumFields.size(); ++i) {
			final Field f = sumFields.get(i);
			final boolean accessible = f.isAccessible();
			f.setAccessible(true);
			final Object value = f.get(mwUndCe);
			if (value instanceof Double) {
				values.set(i, values.get(i) + ((double) value));
			} else {
				throw new IllegalArgumentException(
						"Mit TableField annotierte Felder in Mittelwerte und Ce m�ssen vom Typ double sein!");
			}
			f.setAccessible(accessible);
		}
	}

	/**
	 * F�ge die Daten hinzu von Pfad 0.
	 * 
	 * @param mwCe
	 *            die gerechneten Daten
	 * @throws IllegalArgumentException
	 *             bei Systemfehlern
	 * @throws IllegalAccessException
	 *             bei Systemfehlern
	 */
	public void setValues(final MittelwerteUndCe mwCe) throws IllegalArgumentException, IllegalAccessException {
		for (int i = 0; i < sumFields.size(); ++i) {
			final Field f = sumFields.get(i);
			final boolean accessible = f.isAccessible();
			f.setAccessible(true);
			final Object value = f.get(mwCe);
			title.add("<html><p>" + f.getName() + "</p><p>CE</p></html>");
			titleDrZeile1.add(f.getName());
			titleDrZeile2.add("CE");
			if (value instanceof Double) {
				values.add((double) value);
			} else {
				throw new IllegalArgumentException(
						"Mit TableField annotierte Felder in Mittelwerte und Ce m�ssen vom Typ double sein!");
			}
			f.setAccessible(accessible);
		}

	}

	/**
	 * F�ge die Daten hinzu, die nur auf Pfad 0 gerechnet werden.
	 * 
	 * @param mwNurCe
	 *            die gerechneten Daten
	 * @throws IllegalArgumentException
	 *             bei Systemfehlern
	 * @throws IllegalAccessException
	 *             bei Systemfehlern
	 */
	public void setValues(final MittelwerteNurCe mwNurCe) throws IllegalArgumentException, IllegalAccessException {
		for (Field f : mwNurCe.getClass().getDeclaredFields()) {
			final TableField tf = f.getAnnotation(TableField.class);
			if (tf != null) {
				final boolean accessible = f.isAccessible();
				f.setAccessible(true);
				final Object value = f.get(mwNurCe);
				title.add("<html><p>" + f.getName() + "</p><p>CE</p></html>");
				titleDrZeile1.add(f.getName());
				titleDrZeile2.add("CE");
				if (value instanceof Double) {
					values.add((double) value);
				} else {
					throw new IllegalArgumentException(
							"Mit TableField annotierte Felder in Mittelwerte und Ce m�ssen vom Typ double sein!");
				}
				f.setAccessible(accessible);
			}
		}
	}

	private final DecimalFormat df = new DecimalFormat("#.##############################");

	/**
	 * Gebe die Daten dieser Zeile im csv-Format aus.
	 * 
	 * @param printStream
	 *            der Ausgabestrom
	 */
	public void writeZeile(final PrintStream printStream) {
		printStream.print(szenario + ";" + szenarioId + ";" + lob + ";" + zeit);
		int i = 0;
		for (double v : values) {
			if (i < sumFields.size()) {
				printStream.print(";" + df.format(v / anzahl));
			} else {
				printStream.print(";" + df.format(v));
			}
			++i;
		}
		printStream.println();
	}

	/**
	 * @return the title f�r das Tablemodel
	 */
	public List<String> getTitle() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("<html><p>Szenario</p><br/><p></p></html>");
		alleTitel.add("<html><p>Szenario ID</p><br/><p></p></html>");
		alleTitel.add("<html><p>LoB</p><br/><p></p></html>");
		alleTitel.add("<html><p>Zeit</p><br/><p></p></html>");
		alleTitel.addAll(title);

		return alleTitel;
	}

	/**
	 * @return the title f�r die csv_Datei - Zeile 1
	 */
	public List<String> getTitleDruckZeile1() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("Szenario");
		alleTitel.add("Szenario ID");
		alleTitel.add("LoB");
		alleTitel.add("Zeit");
		alleTitel.addAll(titleDrZeile1);

		return alleTitel;
	}

	/**
	 * @return the title f�r die csv_Datei - Zeile 2
	 */
	public List<String> getTitleDruckZeile2() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("");
		alleTitel.add("");
		alleTitel.add("");
		alleTitel.add("");
		alleTitel.addAll(titleDrZeile2);

		return alleTitel;
	}

	/**
	 * Gebe den Wert eines Feldes zur�ck.
	 * 
	 * @param index
	 *            des Feldes
	 * @return der Wert
	 */
	public double getValue(final int index) {
		if (index < sumFields.size()) {
			return values.get(index) / anzahl;
		} else {
			return values.get(index);
		}
	}

	/**
	 * Das zugrunde liegende Szenario.
	 * 
	 * @return the szenario
	 */
	public String getSzenario() {
		return szenario;
	}

	/**
	 * ID des zugrunde liegenden Szenarios.
	 * 
	 * @return the szenarioId
	 */
	public int getSzenarioId() {
		return szenarioId;
	}

	/**
	 * Line of Business.
	 * 
	 * @return the lob
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * Der Zeitpunkt.
	 * 
	 * @return the zeit
	 */
	public int getZeit() {
		return zeit;
	}

}
