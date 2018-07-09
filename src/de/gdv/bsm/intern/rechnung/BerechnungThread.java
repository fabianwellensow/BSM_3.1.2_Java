package de.gdv.bsm.intern.rechnung;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.gdv.bsm.vu.berechnung.Berechnung;

/**
 * Thread f�r die Berechnung von Pfaden.
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
public class BerechnungThread implements Runnable {
	private final BlockingQueue<Optional<Integer>> nextPfad = new LinkedBlockingQueue<>();
	private final Berechnung berechnung;
	private final RechenThread rechenThread;

	/**
	 * Erstelle einen Thread f�r die Berechnung. Die eigentliche Berechnung muss jedoch durch die Funktion
	 * {@link #berechne(int)} angesto�en werden.
	 * 
	 * @param berechnung
	 *            die Basis-Berechnung
	 * @param rechenThread
	 *            der �bergeordnete Thread
	 */
	public BerechnungThread(final Berechnung berechnung, final RechenThread rechenThread) {
		this.berechnung = berechnung;
		this.rechenThread = rechenThread;
	}

	@Override
	public void run() {
		try {
			while (true) {
				try {
					final Optional<Integer> letzterPfad = nextPfad.take();
					if (letzterPfad.isPresent()) {
						if (!berechnung.isPfadKorrekt(letzterPfad.get())) {
							throw new IllegalArgumentException("Fehler in Szenariensatz f�r Stressszenario "
									+ berechnung.getSzenarioId()
									+ ": Anzahl der Pfade im Szenariensatz ist kleiner als letzter zu rechnender Pfad");
						}
						// berechne den n�chsten Pfad:
						berechnung.berechnung(letzterPfad.get());
						rechenThread.done(new BerechnungReady(this, letzterPfad, berechnung.getKennzahlenPfadweise(),
								berechnung.getKennzahlenPfadweiseLoB(), berechnung.getMittelwerteUndCe(),
								berechnung.getMittelwerteNurCe()));
					} else {
						// alle Pfade sind berechnet, Signalisiere das Ende:
						rechenThread.done(new BerechnungReady(this, letzterPfad, null, null, null, null));
						break;
					}
				} catch (InterruptedException e) {
				}
			}
		} catch (Throwable t) {
			rechenThread.done(new BerechnungReady(this, Optional.empty(), null, null, null, null, Optional.of(t)));
		}
	}

	/**
	 * Berechne einen vorgegebenen Pfad.
	 * 
	 * @param pfad
	 *            der Pfad
	 */
	public synchronized void berechne(final int pfad) {
		while (true) {
			try {
				nextPfad.put(Optional.of(pfad));
				return;
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Beende diesen Rechenthread.
	 */
	public synchronized void stop() {
		while (true) {
			try {
				nextPfad.put(Optional.empty());
				return;
			} catch (InterruptedException e) {
			}
		}

	}

	/**
	 * @return the berechnung
	 */
	public Berechnung getBerechnung() {
		return berechnung;
	}

}
