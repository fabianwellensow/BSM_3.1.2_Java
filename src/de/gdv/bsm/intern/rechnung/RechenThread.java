package de.gdv.bsm.intern.rechnung;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.gdv.bsm.intern.applic.AusgabThreadMittelwerte;
import de.gdv.bsm.intern.applic.AusgabeThread;
import de.gdv.bsm.intern.applic.AusgabeThreadTableField;
import de.gdv.bsm.intern.applic.BerechnungResultat;
import de.gdv.bsm.intern.applic.RechenFortschrittInterface;
import de.gdv.bsm.intern.params.Eingabe;
import de.gdv.bsm.intern.params.SzenarioMapping;
import de.gdv.bsm.intern.params.SzenarioMappingZeile;
import de.gdv.bsm.intern.params.VuParameter;
import de.gdv.bsm.intern.szenario.Szenario;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.Berechnung;
import de.gdv.bsm.vu.berechnung.RzgZeile;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;
import de.gdv.bsm.vu.kennzahlen.MittelwerteNurCe;
import de.gdv.bsm.vu.kennzahlen.MittelwerteUndCe;

/**
 * Rechenkern für die komplette Ausführung von Berechnungen.
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zuständigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschließen, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende Rückmeldungen würde der GDV
 * begrüßen. Der GDV übernimmt aber keine Haftung für die fehlerfreie Funktionalität des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschließlich aller Tabellen, Grafiken und Erläuterungen sind urheberrechtlich
 * geschützt. Die ausschließlichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell © GDV 2016</b>
 */
public class RechenThread implements Runnable {
	private final RechenFortschrittInterface fortschritt;
	private final Eingabe eingabe;
	private final VuParameter vuParameter;
	// alle zu berechnenden Pfade:
	private List<Integer> pfade = new ArrayList<>();
	// nächster zu berechnender Pfad:
	private int nextPfad = 0;
	// Alle asymchronen Berechnung-Threads
	private final Set<BerechnungThread> berechnungThreads = new HashSet<>();
	// Queue mit den Resultaten
	private final BlockingQueue<BerechnungReady> resultat = new LinkedBlockingQueue<>();
	// Queue mit den Ausgaben
	private final BlockingQueue<AusgabeThread> ausgaben = new LinkedBlockingQueue<>();
	// bereits berechnete Pfade:
	private final Set<Integer> berechnetePfade = new HashSet<>();
	// die gesammelten berechneten Kennzahlen
	private final TreeMap<Integer, TreeMap<Integer, KennzahlenPfadweise>> kennzahlenPfadweise = new TreeMap<>();
	private final TreeMap<Integer, TreeMap<Integer, List<KennzahlenPfadweiseLoB>>> kennzahlenPfadweiseLoB = new TreeMap<>();
	private final List<Mittelwerte> mittelwerteList = new ArrayList<>();

	/**
	 * Erstelle den Rechenkern.
	 * 
	 * @param fortschritt
	 *            zur Fortschrittsanzeige
	 * @param eingabe
	 *            Vorgaben
	 * @param vuParameter
	 *            Parameter des VU
	 * @param sznrCache
	 *            Cache für die Zinskurven
	 * @param baseDir
	 *            Basispfad des Verzeichnisses
	 */
	public RechenThread(final RechenFortschrittInterface fortschritt, final Eingabe eingabe,
			final VuParameter vuParameter) {
		this.fortschritt = fortschritt;
		this.eingabe = eingabe;
		this.vuParameter = vuParameter;
	}

	/**
	 * Signalisiere eine durchgeführte Berechnung.
	 * 
	 * @param berechnungReady
	 *            die Signalisierung
	 */
	public void done(final BerechnungReady berechnungReady) {
		while (true) {
			try {
				resultat.put(berechnungReady);
				return;
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void run() {
		final File ausgabeFile = new File(vuParameter.getTransferDir(), VuParameter.AUSGABE);
		try (final PrintStream ausgabe = new PrintStream(new FileOutputStream(ausgabeFile))) {
			// Startzeit der Simulation
			final Calendar start = new GregorianCalendar();
			final Map<Integer, List<String>> sznrHeader = new TreeMap<>();

			// erster Teil: Laden der benötigten Zinskurven

			final SzenarioMapping szenarioMapping = vuParameter.getSzenarioMapping();
			final Set<Integer> sznrAlleIdSet = new TreeSet<>();
			if (eingabe.isAlleSzenarien()) {
				for (SzenarioMappingZeile smz : szenarioMapping.getAktiveSzenarien()) {
					sznrAlleIdSet.add(smz.getZinskurve());
				}
			} else {
				int sznrId = szenarioMapping.getSzenarionMapping(eingabe.getSzenario()).getZinskurve();
				sznrAlleIdSet.add(sznrId);
			}

			if (eingabe.getPfadVon() > eingabe.getPfadBis() && eingabe.getPfadBis() != 0) {
				throw new IllegalArgumentException("Pfad von größer als Pfad bis.");
			}
			// Pfad bis kann null sein, dann nur Pfad von rechnen:
			final int pfadBis = Math.max(eingabe.getPfadVon(), eingabe.getPfadBis());

			if (fortschritt.isAbbruch()) {
				// Benutzerabbruch:
				fortschritt.berechnungBeendet(null);
				return;
			}

			final List<SzenarioMappingZeile> szenarien = new ArrayList<>();
			if (eingabe.isAlleSzenarien()) {
				szenarien.addAll(szenarioMapping.getAktiveSzenarien());
			} else {
				szenarien.add(szenarioMapping.getSzenarionMapping(eingabe.getSzenario()));
			}

			// nacheinander alle Szenarien berechnen:
			int lastPercent = 0;
			final boolean addierePfad0;
			if (eingabe.getPfadVon() > 0) {
				// Pfad 0 muss immer berechnet werden (für CE-Werte)
				pfade.add(0);
				addierePfad0 = false;
			} else {
				addierePfad0 = true;
			}
			for (int pfad = eingabe.getPfadVon(); pfad <= pfadBis; ++pfad) {
				pfade.add(pfad);
			}

			Berechnung letzteBerechnung = null;

			ausgabe.println(
					"Stressszenario ID;Stressszenarion;Modifizierte Duration Zinstitel-Portfolio;FI-Ausfall-Wahrscheinlichkeit");
			final DecimalFormat df = new DecimalFormat("#.##############################");

			Szenario szenario = null;
			int szenarioId = 0;

			for (SzenarioMappingZeile sz : szenarien) {
				boolean ausgabeGeschrieben = false;

				// Mittelwerte gesammelt zunächst für das aktuelle Szenario:
				final Map<String, Map<Integer, List<MittelwerteUndCe>>> mittelwerteUndCe = new HashMap<>();
				final Map<String, Map<Integer, List<MittelwerteNurCe>>> mittelwerteNurCe = new HashMap<>();

				fortschritt.setBerechnungPercent(sz.getId(), 0);
				nextPfad = 0;
				berechnungThreads.clear();
				berechnetePfade.clear();

				// setze parallele Threads auf, maximal einer weniger als Prozessoren
				// und zu berechnende Pfade
				final int threadCount = Math.max(1,
						Math.min(Runtime.getRuntime().availableProcessors() - 1, pfade.size()));
				for (int i = 0; i < threadCount; ++i) {
					if (szenarioId != sz.getZinskurve()) {
						szenario = new Szenario(new File(eingabe.getPfadSzenariensatz()), sz.getZinskurve(), pfadBis,
								fortschritt);
						szenarioId = sz.getZinskurve();
						sznrHeader.put(sz.getZinskurve(), szenario.getHeader());
					}
					final Berechnung berechnung = new Berechnung(sz.getId(), eingabe.isFlvRechnen(),
							eingabe.isNegAusfallwk(), eingabe.isAusgabe(), vuParameter, szenario);

					if (!ausgabeGeschrieben) {
						ausgabe.println(
								sz.getId() + ";" + sz.getName() + ";" + df.format(berechnung.getDurationKaBestand())
										+ ";" + df.format(berechnung.getAusfallWahrscheinlichkeitQ()));
						ausgabeGeschrieben = true;
					}

					final BerechnungThread berechnungThread = new BerechnungThread(berechnung, this);
					berechnungThreads.add(berechnungThread);
					new Thread(berechnungThread).start();
				}

				for (BerechnungThread bt : berechnungThreads) {
					bt.berechne(pfade.get(nextPfad));
					if (pfade.get(nextPfad) == pfadBis) {
						letzteBerechnung = bt.getBerechnung();
					}
					++nextPfad;
				}

				while (true) {
					try {
						final BerechnungReady br = resultat.take();
						if (br.berechneterPfad.isPresent()) {
							int pfad = br.berechneterPfad.get();
							if (berechnetePfade.contains(pfad)) {
								throw new IllegalStateException("Pfad doppelt berechnet: " + pfad);
							}
							berechnetePfade.add(pfad);

							if (!kennzahlenPfadweise.containsKey(sz.getId())) {
								kennzahlenPfadweise.put(sz.getId(), new TreeMap<>());
							}
							kennzahlenPfadweise.get(sz.getId()).put(pfad, br.kennzahlenPfadweise);

							if (!kennzahlenPfadweiseLoB.containsKey(sz.getId())) {
								kennzahlenPfadweiseLoB.put(sz.getId(), new TreeMap<>());
							}
							kennzahlenPfadweiseLoB.get(sz.getId()).put(pfad, br.kennzahlenPfadweiseLoB);

							for (String lob : br.mittelwerteUndCe.keySet()) {
								if (!mittelwerteUndCe.containsKey(lob)) {
									mittelwerteUndCe.put(lob, new HashMap<>());
								}
								final Map<Integer, MittelwerteUndCe> map = br.mittelwerteUndCe.get(lob);
								for (int zeit : map.keySet()) {
									if (!mittelwerteUndCe.get(lob).containsKey(zeit)) {
										mittelwerteUndCe.get(lob).put(zeit, new ArrayList<>());
									}
									mittelwerteUndCe.get(lob).get(zeit).add(map.get(zeit));
								}
							}

							for (String lob : br.mittelwerteNurCe.keySet()) {
								if (!mittelwerteNurCe.containsKey(lob)) {
									mittelwerteNurCe.put(lob, new HashMap<>());
								}
								final Map<Integer, MittelwerteNurCe> map = br.mittelwerteNurCe.get(lob);
								for (int zeit : map.keySet()) {
									if (!mittelwerteNurCe.get(lob).containsKey(zeit)) {
										mittelwerteNurCe.get(lob).put(zeit, new ArrayList<>());
									}
									mittelwerteNurCe.get(lob).get(zeit).add(map.get(zeit));
								}
							}

							final int percent = berechnetePfade.size() * 100 / pfade.size();
							if (percent != lastPercent) {
								fortschritt.setBerechnungPercent(sz.getId(), percent);
								lastPercent = percent;
							}

							if (nextPfad < pfade.size() && !fortschritt.isAbbruch()) {
								br.doer.berechne(pfade.get(nextPfad));
								if (pfade.get(nextPfad) == pfadBis) {
									letzteBerechnung = br.doer.getBerechnung();
								}
								++nextPfad;
							} else {
								br.doer.stop();
							}

						} else {
							// an error occured:
							if (br.error.isPresent()) {
								throw br.error.get();
							}
							// doer has stopped:
							berechnungThreads.remove(br.doer);
							if (berechnungThreads.isEmpty()) {
								// all threads ready: stop
								break;
							}
						}
					} catch (InterruptedException e) {
					}
				}

				if (berechnetePfade.size() != pfade.size() && !fortschritt.isAbbruch()) {
					throw new IllegalStateException("es wurden einige Pfade nicht berechnet!");
				}

				fortschritt.setBerechnungPercent(sz.getId(), 100);

				// alle Lobs, die in den Daten gefunden wurden
				final Set<String> lobs = new HashSet<>();
				lobs.addAll(mittelwerteUndCe.keySet());
				lobs.addAll(mittelwerteNurCe.keySet());

				for (String lob : vuParameter.getLobs()) {
					lobs.remove(lob);

					if (!mittelwerteUndCe.containsKey(lob)) {
						continue;
					}

					// Alle Zeiten in der korrekten Sortierung
					final TreeSet<Integer> zeiten = new TreeSet<>();
					zeiten.addAll(mittelwerteUndCe.get(lob).keySet());

					for (int zeit : zeiten) {
						Mittelwerte mittelwerte = null;
						MittelwerteUndCe undCeNurCe = null;
						MittelwerteNurCe nurCe = null;
						for (MittelwerteUndCe mwCe : mittelwerteUndCe.get(lob).get(zeit)) {
							if (mittelwerte == null) {
								mittelwerte = new Mittelwerte(mwCe, addierePfad0);
							} else {
								mittelwerte.addValues(mwCe, addierePfad0);
							}
							if (mwCe.getPfad() == 0) {
								undCeNurCe = mwCe;
							}
						}
						for (MittelwerteNurCe nce : mittelwerteNurCe.get(lob).get(zeit)) {
							if (nce.getPfad() == 0) {
								nurCe = nce;
								break;
							}
						}
						mittelwerte.setValues(undCeNurCe);
						mittelwerte.setValues(nurCe);
						mittelwerteList.add(mittelwerte);
					}
				}
			}

			final Set<AusgabeThread> ausgabeThreads = new HashSet<>();

			final File rzgFileName = new File(vuParameter.getTransferDir(), VuParameter.RZG);
			final AusgabeThreadTableField rzgT = new AusgabeThreadTableField(this, fortschritt, RzgZeile.class,
					letzteBerechnung.getRzgZeilen(), rzgFileName);
			ausgabeThreads.add(rzgT);
			new Thread(rzgT).start();

			final File aggFileName = new File(vuParameter.getTransferDir(), VuParameter.AGG);
			final AusgabeThreadTableField aggT = new AusgabeThreadTableField(this, fortschritt, AggZeile.class,
					letzteBerechnung.getAggZeilen(), aggFileName);
			ausgabeThreads.add(aggT);
			new Thread(aggT).start();

			// Ausgabe der Dateien
			final List<KennzahlenPfadweise> kp = new ArrayList<>();
			for (int i : kennzahlenPfadweise.keySet()) {
				Map<Integer, KennzahlenPfadweise> map = kennzahlenPfadweise.get(i);
				for (int j : map.keySet()) {
					final KennzahlenPfadweise kptmp = map.get(j);
					kp.add(map.get(j));
					if (kptmp.getPfad() == 0 && eingabe.getPfadVon() == 0) {
						kp.add(map.get(j));
					}
				}
			}

			final File kennzPfadFileName = new File(vuParameter.getTransferDir(), VuParameter.KENNZAHLEN_PFADWEISE);
			final AusgabeThreadTableField kpt = new AusgabeThreadTableField(this, fortschritt,
					KennzahlenPfadweise.class, kp, kennzPfadFileName);
			ausgabeThreads.add(kpt);
			new Thread(kpt).start();

			// Hier schon mmal die Header für Kennzahlen Pfadweise ausgeben:
			final File schaetzerMittelwerteFileName = new File(vuParameter.getTransferDir(),
					VuParameter.SCHAETZER_MITTELWERTE);
			KennzahlenPfadweise.writeSchaeterMittelwerte(schaetzerMittelwerteFileName, kp);

			// und auch den Header für die Stochastischen Kennzahlen:
			{
				final File stochastischeKennzahlen = new File(vuParameter.getTransferDir(),
						VuParameter.STOCHASTISCHE_KENNZAHLEN);
				try (final PrintStream ps = new PrintStream(new FileOutputStream(stochastischeKennzahlen))) {
					ps.println("Stressszenario;Stressszenario ID");
					for (SzenarioMappingZeile z : szenarien) {
						ps.println(z.getName() + ";" + z.getId());
					}
				}
			}

			final List<KennzahlenPfadweiseLoB> kpl = new ArrayList<>();
			for (int i : kennzahlenPfadweiseLoB.keySet()) {
				Map<Integer, List<KennzahlenPfadweiseLoB>> map = kennzahlenPfadweiseLoB.get(i);
				for (int j : map.keySet()) {
					kpl.addAll(map.get(j));
					if (j == 0 && eingabe.getPfadVon() == 0) {
						// pfad 0 doppelt ausgeben:
						kpl.addAll(map.get(j));
					}
				}
			}

			final File kennzPfadLobFileName = new File(vuParameter.getTransferDir(),
					VuParameter.KENNZAHLEN_PFADWEISE_LOB);
			final AusgabeThreadTableField kplt = new AusgabeThreadTableField(this, fortschritt,
					KennzahlenPfadweiseLoB.class, kpl, kennzPfadLobFileName);
			ausgabeThreads.add(kplt);
			new Thread(kplt).start();

			// Hier schon mmal die Header für Kennzahlen Pfadweise LoB ausgeben:
			final File schaetzerMittelwerteLobFileName = new File(vuParameter.getTransferDir(),
					VuParameter.SCHAETZER_MITTELWERTE_LOB);
			KennzahlenPfadweiseLoB.writeSchaeterMittelwerteLob(schaetzerMittelwerteLobFileName, kpl);

			{
				// und den Header für die Stochastischen Kennzahlen pro LoB
				final Map<Integer, Set<String>> szenarioNachLob = new HashMap<>();
				final File stochastischeKennzahlen = new File(vuParameter.getTransferDir(),
						VuParameter.STOCHASTISCHE_KENNZAHLEN_LOB);
				try (final PrintStream ps = new PrintStream(new FileOutputStream(stochastischeKennzahlen))) {
					ps.println("");
					for (KennzahlenPfadweiseLoB k : kpl) {
						if (!szenarioNachLob.containsKey(k.getSzenarioId())) {
							szenarioNachLob.put(k.getSzenarioId(), new HashSet<>());
						}
						if (!szenarioNachLob.get(k.getSzenarioId()).contains(k.getLob())) {
							szenarioNachLob.get(k.getSzenarioId()).add(k.getLob());
							final String name = vuParameter.getSzenarioMapping().getSzenarionMapping(k.getSzenarioId())
									.getName();
							ps.println(name + ";" + k.getSzenarioId() + ";" + k.getLob());
						}
					}
				}
			}

			final AusgabThreadMittelwerte awMw = new AusgabThreadMittelwerte(this, fortschritt, mittelwerteList,
					new File(vuParameter.getTransferDir(), VuParameter.KENNZAHLEN_MITTELWERTE));
			ausgabeThreads.add(awMw);
			new Thread(awMw).start();

			Optional<Throwable> error = Optional.empty();

			while (true) {
				final AusgabeThread at = ausgaben.take();
				if (at.getError().isPresent()) {
					error = at.getError();
				}
				ausgabeThreads.remove(at);
				if (ausgabeThreads.size() == 0) {
					break;
				}
			}

			// ein Fehler ist aufgetreten, einmal signalisieren!
			if (error.isPresent()) {
				throw error.get();
			}

			// Schreiben des Protokolls:
			final File ausfuehrungsLog = new File(vuParameter.getTransferDir(), VuParameter.AUSFUEHRUNGS_LOG);
			try (final PrintStream out = new PrintStream(new FileOutputStream(ausfuehrungsLog))) {
				// Endezeit der Simulation:
				final GregorianCalendar ende = new GregorianCalendar();

				final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

				out.println("Start der Simulation;" + sdf.format(start.getTime()));
				out.println("Ende der Simulation;" + sdf.format(ende.getTime()));

				final long dauer = ende.getTimeInMillis() - start.getTimeInMillis();
				final long millis = dauer % 1000;
				final long sec = (dauer / 1000) % 60;
				final long min = (dauer / (1000 * 60)) % 60;
				final long hour = dauer / (1000 * 60 * 60);
				out.println("Benötigte Rechenzeit;" + String.format("%d:%02d:%02d:%03d", hour, min, sec, millis)
						+ " Std:Min:Sek:Msec");
				out.println("Pfad von:;" + eingabe.getPfadVon());
				out.println("Pfad bis:;" + eingabe.getPfadBis());
				out.println("Rechenkern:;Java");
				out.println();
				for (int sznrId : sznrAlleIdSet) {
					for (String headerLine : sznrHeader.get(sznrId)) {
						out.println(headerLine);
					}
				}
			}

			// Resultat der Berechnung:
			final BerechnungResultat resultat = new BerechnungResultat(letzteBerechnung, kennzahlenPfadweise,
					kennzahlenPfadweiseLoB, mittelwerteList);
			fortschritt.berechnungBeendet(resultat);

		} catch (Throwable e) {
			fortschritt.berechnungGechrashed(e);
			return;
		}
	}

	/**
	 * Signaliesiere das Ende einer Ausgabe.
	 * 
	 * @param thread
	 *            der beendete Thread
	 */
	public synchronized void ausgabeReady(final AusgabeThread thread) {
		while (true) {
			try {
				ausgaben.put(thread);
				return;
			} catch (InterruptedException e) {
			}
		}
	}
}
