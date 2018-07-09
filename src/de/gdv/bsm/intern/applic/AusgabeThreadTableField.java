package de.gdv.bsm.intern.applic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.gdv.bsm.intern.rechnung.RechenThread;

/**
 * Ausgabethreads f�r Daten, die mit der Annotation {@link TableField} die Felder markiert haben. Es werden genau die
 * markierten Felder in die Datei ausgegeben.
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
public class AusgabeThreadTableField implements AusgabeThread {
	private final RechenThread master;
	private final RechenFortschrittInterface rechenFortschritt;
	private final Class<?> klasse;
	private final List<?> data;
	private final File fileName;
	private final List<Field> dataFields;

	private Optional<Throwable> error = Optional.empty();

	/**
	 * Erstelle ein Runnable.
	 * 
	 * @param master
	 *            kontrollierender Prozess
	 * @param rechenFortschritt
	 *            f�r die Ausgabe von Prozentangaben
	 * @param klasse
	 *            Basisklasse f�r die {@link TableField}-Annotationen
	 * @param data
	 *            die Liste mit den auszugebenden Datens�tzen
	 * @param fileName
	 *            die Ausgabedatei
	 */
	public AusgabeThreadTableField(final RechenThread master, final RechenFortschrittInterface rechenFortschritt,
			final Class<?> klasse, final List<?> data, final File fileName) {
		this.master = master;
		this.rechenFortschritt = rechenFortschritt;
		this.klasse = klasse;
		this.data = data;
		this.fileName = fileName;

		final List<Field> dataFields = new ArrayList<>();
		for (Field f : klasse.getDeclaredFields()) {
			final TableField tf = f.getAnnotation(TableField.class);
			if (tf != null) {
				dataFields.add(f);
			}
		}
		this.dataFields = Collections.unmodifiableList(dataFields);
	}

	@Override
	public void run() {
		try (final PrintStream out = new PrintStream(
				new BufferedOutputStream(new FileOutputStream(fileName), 8 * 1024))) {
			final List<Boolean> accessible = new ArrayList<>();

			boolean first = true;
			for (Field f : dataFields) {
				accessible.add(f.isAccessible());
				f.setAccessible(true);
				out.print((first ? "" : ";") + f.getName());
				first = false;
			}
			out.println();

			final DecimalFormat df = new DecimalFormat("#.##############################");

			int lastPercent = 0;
			int anzahl = 0;
			for (Object zeile : data) {
				first = true;
				for (Field f : dataFields) {
					if (!first)
						out.print(';');
					final TableField tf = f.getAnnotation(TableField.class);
					if (tf == null || !tf.suppress()) {
						final Object data = f.get(zeile);
						first = false;
						if (data instanceof Double) {
							double d = ((Double) data).doubleValue();
							if (Double.isFinite(d))
								out.print(df.format(d));
							else
								out.print("0");
						} else {
							out.print(data.toString());
						}
					}
				}
				out.println();
				++anzahl;
				final int percent = (anzahl * 100) / data.size();
				if (lastPercent != percent) {
					rechenFortschritt.setAusgabePercent(klasse, percent);
					lastPercent = percent;
				}
			}

			// zugriffsm�glichkeit wieder zur�cksetzen:
			for (int i = 0; i < dataFields.size(); ++i) {
				dataFields.get(i).setAccessible(accessible.get(i));
			}

		} catch (

		Throwable e)

		{
			error = Optional.of(e);
		} finally

		{
			master.ausgabeReady(this);
		}

	}

	/**
	 * Ist bei der Berechnung ein Fehler aufgetreten?
	 * 
	 * @return wenn nicht empty, ist der Wert der Fehlern; andernfalls ist kein Fehler aufgetreten
	 */
	public Optional<Throwable> getError() {
		return error;
	}
}
