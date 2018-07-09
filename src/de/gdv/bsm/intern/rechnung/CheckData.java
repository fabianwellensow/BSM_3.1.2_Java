package de.gdv.bsm.intern.rechnung;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gdv.bsm.intern.applic.TableField;

/**
 * �berpr�fung von Double-Feldern auf illegale Werte.
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
public class CheckData {
	// Map mit allen zu pr�fenden Feldern pro Klasse
	private static final Map<Class<?>, List<Field>> doubleFields = new HashMap<>();

	/**
	 * Pr�fe alle als {@link TableField} markierten double-Felder auf g�ltige Werte.
	 * <p/>
	 * Alle diese Werte werden gepr�ft, ob sie finit sind. Wenn mindestens ein Feld nicht finit ist, wird eine Liste
	 * aller falschen Felder zur�ckgeliefert. Ansonsten wird eine leere Liste zur�ckgeliefert.
	 * <p/>
	 * Durch setzen von {@link TableField#checkFinite()} auf false k�nnen einzelne Felder von dieser Pr�fung
	 * ausgeschlossen werden.
	 * 
	 * @param data
	 *            die zu pr�fenden Daten
	 * @return die Liste der fehlerhaften Felder, oder eine leere Liste
	 */
	public synchronized static List<String> checkFinite(final Object data) {
		if (!doubleFields.containsKey(data.getClass())) {
			cashFields(data);
		}
		final List<String> errors = new ArrayList<>();
		for (Field field : doubleFields.get(data.getClass())) {
			final boolean accessible = field.isAccessible();
			field.setAccessible(true);
			try {
				final double value = field.getDouble(data);
				if (!Double.isFinite(value)) {
					errors.add(data.getClass().getSimpleName() + "." + field.getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException("Fehler beim Zugriff", e);
			}
			field.setAccessible(accessible);
		}
		return errors;
	}

	private static void cashFields(Object data) {
		final Class<?> c = data.getClass();
		final List<Field> fields = new ArrayList<>();
		for (Field field : c.getDeclaredFields()) {
			final TableField tf = field.getAnnotation(TableField.class);
			if (tf != null && tf.checkFinite()) {
				if (field.getType() == double.class) {
					fields.add(field);
				}
			}
		}
		doubleFields.put(c, fields);
	}
}
