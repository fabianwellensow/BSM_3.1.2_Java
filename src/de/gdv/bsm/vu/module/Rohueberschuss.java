package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.inProzent;
import static de.gdv.bsm.vu.module.Functions.nanZero;

import de.gdv.bsm.intern.params.VUHistorie;

/**
 * Funktionen des Excel-Moduls <code>Kundenverhalten</code>.
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
public class Rohueberschuss {

	/**
	 * YG 03.04.2014 Berechnet die Zinszusatzreserve f�r die Rechnungszinsgeneration rz zum Zeitpunkt t
	 * 
	 * @param altNeu
	 *            alt_neu - Kennzeichen Alt- / Neubestand
	 * @param rz
	 *            Rechnungszinsgeneration in Basispunkten
	 * @param ref
	 *            Referenzzinssatz
	 * @param ref2M
	 *            Referenzzinssatz, 2M
	 * @param aufwand
	 *            Aufwand f�r die Zinszusatzreserve in Basispunkten bezogen auf die HGB-Deckungsr�ckstellung
	 * @param dr
	 *            HGB-Deckungsr�ckstellung
	 * @param zzrMethode
	 *            ZZR Methode f�r Altbestand
	 * @param korrekturRechnungsgrundl
	 *            Korrekturterm f�r den Referenzzins aufgrund reduzierter Margen in Rechnungsgrundlagen Biometrie und
	 *            Kosten
	 * @param refStartR
	 *            Startwert Referenzzins
	 * @return der Wert
	 */
	public static double zzrJ(final String altNeu, final double rz, final double ref, final double ref2M,
			final double aufwand, final double dr, final String zzrMethode, final double korrekturRechnungsgrundl,
			final double refStartR) {
		double ref_zins;
		if (altNeu.equals("a") && zzrMethode.equals("2M")) {
			ref_zins = ref2M;
		} else {
			ref_zins = ref;
		}

		return Math.max(
				Math.max(rz - 10000.0 * Math.min(Math.max(refStartR, ref_zins), ref_zins + korrekturRechnungsgrundl),
						0.0) * aufwand * dr,
				0.0);
	}

	/**
	 * Addiert alle Leistungen auf. <br/>
	 * Funktionsname in Excel: L_garantiert.
	 * 
	 * @param lGarantiertOSonstErl
	 *            Garantierte Leistungen, ohne sonstige Erlebnsfallleistungen
	 * @param lGarantiertSonstErl
	 *            sonstige Erlebensfallleistungen
	 * @param vgLaKapWahlXsAggr
	 *            relative Leistungsanpassung der sonstigen Erlebensfallleistungen durch dynamische Kapitalwahl
	 * @return Leistungen
	 */
	public static double lGarantiert(final double lGarantiertOSonstErl, final double lGarantiertSonstErl,
			final double vgLaKapWahlXsAggr) {
		return lGarantiertOSonstErl + (1.0 + nanZero(vgLaKapWahlXsAggr)) * lGarantiertSonstErl;
	}

	/**
	 * Berechnet die determinischen garantierten Leistungen. <br/>
	 * Funktionsname in Excel: L_garantiert_det.
	 * 
	 * @param lGarantiertOSonstErl
	 *            Garantierte Leistungen, ohne sonstige Erlebnsfallleistungen
	 * @param sonstigeErlebensfallLeistungen
	 *            sonstige Erlebensfallleistungen
	 * @return Leistungen
	 */
	public static double lGarantiertDet(final double lGarantiertOSonstErl,
			final double sonstigeErlebensfallLeistungen) {
		return lGarantiertOSonstErl + sonstigeErlebensfallLeistungen;
	}

	/**
	 * Berechnet die Aufwendungen f�r den tariflichen Rechnungszins. <br/>
	 * Funktionsname in Excel: rmZ_Tarif.
	 * 
	 * @param rmzTarif
	 *            Rechnnungsm��iger Zinsaufwand aus der deterministischen Projektion
	 * @param leAggrRz
	 *            Garantierte Leistungserh�hung aus dem Jahr t-1 f�r die RZG rz
	 * @param lbwT
	 *            Leistungsbarwert aus der deterministischer Projektion, Jahr t
	 * @param lbwT1
	 *            Leistungsbarwert aus der deterministischer Projektion, Jahr t-1
	 * @param lt
	 *            Gesamtleistung, determinisch, Jahr t
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param rz
	 *            Rechnungszins
	 * @param sueAfV
	 *            S�AF Entnahme
	 * @param barAus
	 *            Barauszahlung
	 * @param rkwXs
	 *            R�ckkaufswerte Excess Betrag
	 * @param lErlT
	 *            Sonstige Erlebens-fallleistungen
	 * @param kaXs
	 *            Kapitalabfindung Excess Betrag
	 * @param vgLaKapWahlXsAggr
	 *            Leistungsanpassung Kapitalwahl
	 * @param vgLbwSonstErl
	 *            Leistungsbarwert SonstErll, deterministisch
	 * @param vuZeitpunkt
	 *            Monat der Auszahlung
	 * @param vgLambda
	 *            Gesamt Storno
	 * @param beitraegeStoch
	 *            Beitr�ge, stochastisch
	 * @param beitraegeDet
	 *            Pr�mien
	 * @param kategorie
	 *            Kategorie
	 * @return Aufwendungen
	 */
	public static double rmZTarif(final double rmzTarif, final double leAggrRz, final double lbwT, final double lbwT1,
			final double lt, final int t, final double rz, final double sueAfV, final double barAus, final double rkwXs,
			final double lErlT, final double kaXs, final double vgLaKapWahlXsAggr, final double vgLbwSonstErl,
			final double vuZeitpunkt, final double vgLambda, final double beitraegeStoch, final double beitraegeDet,
			final String kategorie) {

		double rmZTarif;
		if (t > 0 && lbwT1 > 0.001) {
			final double rzProzent = inProzent(rz);
			final double alpha = Math.pow(1.0 + rzProzent, 1.0 - vuZeitpunkt / 12.0) - 1.0;
			final double beta = Math.pow(1.0 + rzProzent, vuZeitpunkt / 12.0) - 1.0;

			rmZTarif = vgLambda * rmzTarif + nanZero(leAggrRz) * (lbwT1 * rzProzent - vgLambda * alpha * lt)
					+ rzProzent * vgLambda * nanZero(vgLaKapWahlXsAggr) * vgLbwSonstErl
					- alpha * (rkwXs + (1 + nanZero(leAggrRz)) * kaXs + vgLambda * nanZero(vgLaKapWahlXsAggr) * lErlT)
					+ (nanZero(sueAfV) + nanZero(barAus)) * beta;

			if (kategorie.equals("FLV")) {
				rmZTarif = rmZTarif + (beitraegeStoch - vgLambda * beitraegeDet) * alpha;
			}
		} else {
			rmZTarif = 0.0;
		}
		return rmZTarif;
	}

	/**
	 * Berechnet ZZR gesamt. <br/>
	 * Funktionsname in Excel: ZZR_gesamt.
	 * 
	 * @param zzrUebAlt
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Altbestandes
	 * @param zzrUebNeu
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Neubestandes
	 * @param zzrNueb
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen eine Gesch�ftzweiges
	 * @return der Wert
	 */
	public static double zzrGesamt(final double zzrUebAlt, final double zzrUebNeu, final double zzrNueb) {
		return zzrUebAlt + zzrUebNeu + zzrNueb;
	}

	/**
	 * Berechnet die Ver�nderung der Zinszusatzreserve aus der Zinszusatzreserve aus dem aktuellen und aus der
	 * Zinszusatzreserve des Vorjahres. <br/>
	 * Funktionsname in Excel: deltaZZR.
	 * 
	 * @param zzr
	 *            Zinszusatzreserve aus dem aktuellen Jahr
	 * @param zzrVorjahr
	 *            Zinszusatzreserve des Vorjahres
	 * @return Ver�nderung der Zinszusatzreserve
	 */
	public static double deltaZzr(final double zzr, final double zzrVorjahr) {
		return zzr - zzrVorjahr;
	}

	/**
	 * Berechnet den S�AF, gesamt. <br/>
	 * Funktionsname in Excel: SUEAF.
	 * 
	 * @param sueAfAlt
	 *            Summierter Schluss�berschussfondsanteil der einzelnen Bestandsgruppen des Altbestandes
	 * @param sueAfNeu
	 *            Summierter Schluss�berschussfondsanteil der einzelnen Bestandsgruppen des Neubestandes
	 * @return der Wert
	 */
	public static double sueAf(final double sueAfAlt, final double sueAfNeu) {
		return sueAfAlt + sueAfNeu;
	}

	/**
	 * Berechnet die rechnungsm��igen Zinsen rmZ_gesamt. <br/>
	 * Funktionsname in Excel: rmZ_Gesamt.
	 * 
	 * @param rmzTarif
	 *            tariflicher Rechnungszins
	 * @param deltaZzr
	 *            Ver�nderung der Zinszusatzreserve
	 * @return rechnungsm��ige Zinsen
	 */
	public static double rmZGesamt(final double rmzTarif, final double deltaZzr) {
		return rmzTarif + deltaZzr;
	}

	/**
	 * Berechnet die anrechenbaren Kapitalertr�ge, indem die Nettoverzinsung auf die mittleren zinstrangenden Passiva
	 * von Jahresanfang und Jahresende angewendet wird zzgl. nicht festgelegte RfB. <br/>
	 * Funktionsname in Excel: Kapitalertrag_anrechenbar.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param nvz
	 *            Nettoverzinsung
	 * @param drVorDeklUeb
	 *            Deckungsr�ckstellung f�r das �berschussberechtigte Gesch�ft des aktuellen Gesch�ftsjahres vor
	 *            Deklaration
	 * @param drLockInUebV
	 *            Deckungsr�ckstellung f�r das �berschussberechtigte Gesch�ft zum Zeitpunkt t - 1
	 * @param zzrUeb
	 *            Zinszusatzreserve des �berschussberechtigen Gesch�ftes in t
	 * @param zzrUebV
	 *            Zinszusatzreserve des �berschussberechtigen Gesch�ftes in t-1
	 * @param nfRfBv
	 *            Nicht festgelegt R�ckstellungen f�r Beitragsr�ckerstattung zum Zeitpunkt t - 1
	 * @return Kapitalertr�ge
	 */
	public static double kapitalertragAnrechenbar(final int t, final double nvz, final double drVorDeklUeb,
			final double drLockInUebV, final double zzrUeb, final double zzrUebV, final double nfRfBv) {
		if (t == 0) {
			return 0.0;
		} else {
			return nvz * (0.5 * (drVorDeklUeb + nanZero(drLockInUebV) + zzrUeb + nanZero(zzrUebV)) + nanZero(nfRfBv));
		}
	}

	/**
	 * Bestimmt das �brige Ergebnisse f�r das �berschussberechtigte, klassische Gesch�ft unter Ber�cksichtigung der
	 * Managementregeln. <br/>
	 * Funktionsname in Excel: Kostenueberschuss_Bestand.
	 * 
	 * @param uebrErg
	 *            �brigens Ergebnis f�r das klassische, �berschussberechtigte Gesch�ft
	 * @param qNG
	 *            Managementregel, die festlegt, welcher Teil des �brigen Ergebnisses zur Finanzierung des k�nftigen
	 *            Gesch�fts herangezogen wird
	 * @return Ergebnisse
	 */
	public static double kostenueberschussBestand(final double uebrErg, final double qNG) {
		return Math.min((1.0 - qNG) * uebrErg, uebrErg);
	}

	/**
	 * Berechnet den Mindestkapitalertrag unter Ber�cksichtigung des LVRG und der Trennung in Alt-/Neubestand. <br/>
	 * Funktionsname in Excel: Mindestkapitalertrag_LVRG_alt_neu.
	 * 
	 * @param iLVRG
	 *            Schalter Verrechnungsm�glichkeit nach LVRG (1 = komplett verwenden, 0 = nicht verwenden)
	 * @param kePlan
	 *            Kapitalertrag vor der Realisierung von Realwerten
	 * @param rmzGesamtUebAlt
	 *            Rechnungsm��ige Zinsen des �berschussberechtigten Gesch�fts, Altbestand
	 * @param rmzGesamtUebNeu
	 *            Rechnungsm��ige Zinsen des �berschussberechtigten Gesch�fts, Neubestand
	 * @param rmzGesamtNueb
	 *            Rechnungsm��ige Zinsen des nicht �berschussberechtigten Gesch�fts
	 * @param reUebAlt
	 *            Risiko�berschuss des �berschussberechtigten Gesch�fts, Altbestand
	 * @param reUebNeu
	 *            Risiko�berschuss des �berschussberechtigten Gesch�fts, Neubestand
	 * @param reNueb
	 *            Risiko�berschuss des nicht �berschussberechtigten Gesch�fts
	 * @param ueeBestandUebAlt
	 *            Kosten�berschuss (Bestand) des �berschussberechtigten Gesch�fts, Altbestand
	 * @param ueeBestandUebNeu
	 *            Kosten�berschuss (Bestand) des �berschussberechtigten Gesch�fts, Neubestand
	 * @param ueeNueb
	 *            Kosten�berschuss des nicht �berschussberechtigten Gesch�fts
	 * @param jueZiel
	 *            Zieljahres�berschuss
	 * @param ziRaZu
	 *            Zinsratenzuschlag
	 * @param zinsenGrk
	 *            Zinsen f�r Nachrangsdarlehen und Genussrechtkapital
	 * @return der Wert
	 */
	public static double mindestkapitalertragLvrgAltNeu(final double iLVRG, final double kePlan,
			final double rmzGesamtUebAlt, final double rmzGesamtUebNeu, final double rmzGesamtNueb,
			final double reUebAlt, final double reUebNeu, final double reNueb, final double ueeBestandUebAlt,
			final double ueeBestandUebNeu, final double ueeNueb, final double jueZiel, final double ziRaZu,
			final double zinsenGrk) {
		final double Mindestkapitalertrag_LVRG_alt_neu;

		if (kePlan < (rmzGesamtUebAlt + rmzGesamtUebNeu + rmzGesamtNueb) && iLVRG > 0.0) {

			final double JUE_re_uee = reUebNeu - 0.9 * Math.max(reUebNeu, 0.0) + ueeBestandUebNeu
					- 0.5 * Math.max(ueeBestandUebNeu, 0.0) + reUebAlt - 0.9 * Math.max(reUebAlt, 0) + ueeBestandUebAlt
					- 0.5 * Math.max(ueeBestandUebAlt, 0) + reNueb + ueeNueb;

			Mindestkapitalertrag_LVRG_alt_neu = rmzGesamtUebAlt + rmzGesamtUebNeu
					- iLVRG * (0.9 * Math.max(reUebAlt, 0.0) + 0.5 * Math.max(ueeBestandUebAlt, 0.0))
					+ -iLVRG * (0.9 * Math.max(reUebNeu, 0.0) + 0.5 * Math.max(ueeBestandUebNeu, 0.0)) + rmzGesamtNueb
					- Math.max(JUE_re_uee - jueZiel, 0.0);
		} else {
			final double erfuelleMindZV = rmzGesamtUebAlt + rmzGesamtUebNeu + 0.9 * Math.max(reUebAlt, 0.0) - reUebAlt
					+ 0.9 * Math.max(reUebNeu, 0.0) - reUebNeu + 0.5 * Math.max(ueeBestandUebAlt, 0.0)
					- ueeBestandUebAlt + 0.5 * Math.max(ueeBestandUebNeu, 0.0) - ueeBestandUebNeu + rmzGesamtNueb
					- reNueb - ueeNueb;

			final double rmZErh = (rmzGesamtUebAlt + rmzGesamtUebNeu) / 0.9 + rmzGesamtNueb;

			Mindestkapitalertrag_LVRG_alt_neu = Math.min(rmZErh, erfuelleMindZV + jueZiel);
		}

		return Mindestkapitalertrag_LVRG_alt_neu - ziRaZu + zinsenGrk;
	}

	/**
	 * Berechnet den Roh�berschuss. <br/>
	 * Funktionsname in Excel: RohUeb.
	 * 
	 * @param ke
	 *            Kapitalertrag
	 * @param rmzGesamtUeb
	 *            Rechnungsm��iger Zinsaufwand f�r das �berschussberechtigte Gesch�ft
	 * @param rmzGesamtNueb
	 *            Rechnungsm��iger Zinsaufwand f�r das nicht-�berschussberechtigte Gesch�ft
	 * @param risikoUeberschussUeb
	 *            Risiko�berschuss aus dem �berschussberechtigten Gesch�ft
	 * @param risikoUeberschussNueb
	 *            Risiko�berschuss aus dem nicht-�berschussberechtigten Gesch�ft
	 * @param kostenUeberschussBestandUeb
	 *            Kosten�berschuss aus dem �berschussberechtigten Gesch�ft
	 * @param kostenUeberschussNueb
	 *            Kosten�berschuss aus dem nicht-�berschussberechtigten Gesch�ft
	 * @param kaAufwendungen
	 *            Aufwendugen f�r Kapitalanlagen
	 * @param zinsenGrk
	 *            Zinsen f�r Nachrangsdarlehen und Genussrechtkapital
	 * @return Roh�berschuss
	 */
	public static double rohueb(final double ke, final double rmzGesamtUeb, final double rmzGesamtNueb,
			final double risikoUeberschussUeb, final double risikoUeberschussNueb,
			final double kostenUeberschussBestandUeb, final double kostenUeberschussNueb, final double kaAufwendungen,
			final double zinsenGrk) {
		return ke - kaAufwendungen - rmzGesamtUeb + risikoUeberschussUeb + kostenUeberschussBestandUeb - rmzGesamtNueb
				+ risikoUeberschussNueb + kostenUeberschussNueb - zinsenGrk;
	}

	/**
	 * Berechnet die Mindestbeteiligung entsprechend der Mindestzuf�hrungsverordnung<br/>
	 * Funktionsname in Excel: MindZf.
	 * 
	 * @param aKE
	 *            Anrechenbare Kapitalertr�ge
	 * @param rmZAlt
	 *            rmZ UEB, alt + Delta ZZR UEB, alt
	 * @param reAlt
	 *            Risikoergebnis UEB, alt
	 * @param ueeAlt
	 *            �briges Ergebnis Bestand UEB , alt
	 * @param drAltV
	 *            Deckungsr�ckstellung, Lockin, �EB, alt
	 * @param zzrAltV
	 *            ZZR UEB, alt
	 * @param sueafAltV
	 *            S�AF, UEB, alt
	 * @param rmZNeu
	 *            rmZ UEB, alt + Delta ZZR UEB, neu
	 * @param reNeu
	 *            Risikoergebnis UEB, neu
	 * @param ueeNeu
	 *            �briges Ergebnis Bestand UEB , neu
	 * @param drNeuV
	 *            Deckungsr�ckstellung, Lockin, �EB, neu
	 * @param zzrNeuV
	 *            ZZR UEB, neu
	 * @param sueafNeuV
	 *            S�AF, UEB, neu
	 * @return der Wert
	 */
	public static double mindZf(final double aKE, final double rmZAlt, final double reAlt, final double ueeAlt,
			final double drAltV, final double zzrAltV, final double sueafAltV, final double rmZNeu, final double reNeu,
			final double ueeNeu, final double drNeuV, final double zzrNeuV, final double sueafNeuV) {
		double aKETb = 0.0;
		double zinstraegerGes = drAltV + zzrAltV + sueafAltV + drNeuV + zzrNeuV + sueafNeuV;
		if (zinstraegerGes > 0.001) {
			aKETb = aKE * (drAltV + zzrAltV + sueafAltV) / zinstraegerGes;
		}
		double mindZf_KE = Math.max(0.9 * aKETb - rmZAlt, 0) + Math.min(aKETb - rmZAlt, 0.0);
		double MindZf = Math.max(mindZf_KE + 0.9 * Math.max(reAlt, 0) + 0.5 * Math.max(ueeAlt, 0.0), 0.0);
		aKETb = 0.0;
		if (zinstraegerGes > 0.001) {
			aKETb = aKE * (drNeuV + zzrNeuV + sueafNeuV) / zinstraegerGes;
		}
		mindZf_KE = Math.max(0.9 * aKETb - rmZNeu, 0) + Math.min(aKETb - rmZNeu, 0.0);
		MindZf = MindZf + Math.max(mindZf_KE + 0.9 * Math.max(reNeu, 0.0) + 0.5 * Math.max(ueeNeu, 0.0), 0.0);
		return MindZf;

	}

	/**
	 * Berechnet Mindestbeteiligung K�rzungskonto. <br/>
	 * Funktionsname in Excel: MindZf_KK.
	 * 
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param mindZfKkV
	 *            Mindestzuf�hrung K�rzungskonto Vorjahr
	 * @param mindZf
	 *            Mindestzuf�hrung
	 * @param rfBZuf
	 *            RfB-Zuf�hrung
	 * @return Mindestbeteiligung
	 */
	public static double mindZfKk(final int t, final double mindZfKkV, final double mindZf, final double rfBZuf) {
		if (t > 0) {
			return Math.max(nanZero(mindZfKkV) + mindZf - rfBZuf, 0.0);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die Mindestbeteiligung der Versicherungsnehmner mit Hilfe eines Zuf�hrungsplans. <br/>
	 * Funktionsname in Excel: MindZf_ges.
	 * 
	 * @param mindZf
	 *            Mindestbeteiligung gem�� Mindestzuf�hrungsverordnung
	 * @param vgMindZfKk
	 *            Mindestbeteiligung K�rzungskonto zum Zeitpunkt t - 1
	 * @return Mindestbeteiligung
	 */
	public static double mindZfGes(final double mindZf, final double vgMindZfKk) {
		return mindZf + nanZero(vgMindZfKk);
	}

	/**
	 * Berechnet den Zieljahres�berschuss. <br/>
	 * Funktionsname in Excel: JUE_Ziel.
	 * 
	 * @param strategie
	 *            Strategie zur Ermittlung der Beteiligung der Versicherungsnehmer und Jahres�berschuss
	 *            (1=Zielverzinsung, 2=Zielbeteiligung)
	 * @param t
	 *            Zeitpunkt
	 * @param ekVj
	 *            Eigenkapital vom Vorjahr
	 * @param rEk
	 *            Managementregel r_EK zum Zeitpunkt T
	 * @param steuersatz
	 *            Steuersatz
	 * @param ijUEZ
	 *            Schalter, Jahreszielerh�hung
	 * @param jueErhoehungV
	 *            Jahres�berschusszielerh�hung, Vorjahr
	 * @return Zieljahres�berschuss
	 */
	public static double jueZiel(final int strategie, final int t, final double ekVj, final double rEk,
			final double steuersatz, final boolean ijUEZ, final double jueErhoehungV) {
		double jueZiel = 0.0;
		switch (strategie) {
		case 1: // Zielverzinsung
			jueZiel = rEk / (1 - (steuersatz)) * ekVj + (ijUEZ ? Functions.nanZero(jueErhoehungV) : 0.0);
			break;
		case 2:
		case 3: // 'Zielbeteiligung, dynamisch Zielbeteiligung
			jueZiel = 0.0;
			break;
		}
		return jueZiel;
	}

	/**
	 * Berechnet die Erh�hung eines Zieljahres�berschusses<br/>
	 * Funktionsname in Excel: JUE_Zielerhoehung.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param iJuez
	 *            Schalter, Berechung der Erh�hung des Zieljahres�berschuss
	 * @param jueZiel
	 *            - Zieljahres�berschuss
	 * @param jUe
	 *            Jahres�berschuss
	 * @param strategie
	 *            ??
	 * @return der Wert
	 */
	public static double jUeZielerhoehung(final int t, final boolean iJuez, final double jueZiel, final double jUe,
			final int strategie) {

		switch (strategie) {
		case 1:
			if (t == 0) {
				return 0.0;
			}
			if (iJuez) {
				return Math.max(jueZiel - jUe, 0.0);
			} else {
				return 0.0;
			}
		default:
			return 0.0;
		}

	}

	/**
	 * Berechnet den realisierten Jahres�berschuss und die R�ckstellungen f�r Beitragsr�ckerstattungszuf�hrung und gibt
	 * das gew�nschte Ergebnis zur�ck. <br/>
	 * Name in Excel: JUE_RfBZuf.
	 * 
	 * @param ergebnis
	 *            Marker, der angibt, welches Ergebnis zur�ckgegeben werden soll (1 - realisierter Jahres�berschuss, 2 -
	 *            RfB Zuf, 3 - RfB 56b-Entnahme)
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param rohueberschuss
	 *            Roh�berschuss
	 * @param mindZfGes
	 *            Mindestzuf�hrung gesamt (inkl. K�rzungskonto)
	 * @param mindZf
	 *            Mindestzuf�hrung
	 * @param jueZiel
	 *            Ziel-Jahres�berschuss
	 * @param ueeBestand
	 *            Kosten�berschuss Bestand
	 * @param freieRfbVj
	 *            freie RfB zum Zeitpunkt t - 1
	 * @param rfbZufVj
	 *            Zuf�hrung zur R�ckstellung f�r Beitragsr�ckerstattung aus den Vorjahren
	 * @param vuHistorie
	 *            historische Werte f�r die RfB Zuf�hrung und den Roh�berschuss (in Excel zwei Parameter!)
	 * @param pRohueb
	 *            Managementregel (Prozentwert der angibt, wie hoch die Beteiligung der VN am Roh�berschuss sein soll)
	 * @param i56bfRfB
	 *            Managementregel (Soll im Fall einer negativen Roh�berschussbeteiligung eine Entnahme aus der freien
	 *            RfB vorgenommen werden
	 * @param i56bSueaf
	 *            Managementregel (Soll im Fall einer negativen Roh�berschussbeteiligung eine Entnahme aus dem // S�AF
	 *            vorgenommen werden
	 * @param sueafVj
	 *            S�AF aus dem Vorjahr
	 * @param strategie
	 *            Strategie zur Ermittlung der Beteiligung der Versicherungsnehmer und Jahres�berschuss (1 =
	 *            Zielverzinsung, 2 = Zielbeteiligung)
	 * @param rfb56bVj
	 *            H�he der Entnahme aus der nicht festgelegten RfB gem�� �56b VAG zum Zeitpunkt t-1
	 * @param drVorDeklUeb
	 *            Deckungsr�ckstellung vor Deklaration zum Ende des Jahres t
	 * @param drLockInUebV
	 *            Deckungsr�ckstellung f�r garantierte Leistungen zum Ende des Jahres t-1
	 * @param ke
	 *            Kapitalertrag
	 * @param aKe
	 *            Anrechenbare Kapitalertr�ge
	 * @return der Wert
	 */
	public static double jUERfBZuf(final int ergebnis, final int t, final double[] rohueberschuss,
			final double mindZfGes, final double mindZf, final double jueZiel, final double ueeBestand,
			final double freieRfbVj, final double[] rfbZufVj, final VUHistorie vuHistorie, final double pRohueb,
			final double i56bfRfB, final double i56bSueaf, final double sueafVj, final int strategie,
			final double[] rfb56bVj, final double drVorDeklUeb, final double drLockInUebV, final double ke,
			final double aKe) {
		double JUe = 0.0, rfbZuf = 0.0, RfB56b = 0.0;

		double KE_EK;
		double JUE_Ziel_3;

		// kein Unterschied bei Strategie
		// Ermittlung der Hilfekennzahlen
		final double sumRfb10 = mittlRfbZuf10J(t, vuHistorie, // RfB_Historie,
				rfbZufVj, rfb56bVj);
		final double sumRohueb10 = mittlRohueb10J(t, vuHistorie, // Rohueb_Historie,
				rohueberschuss);

		final double mBeteiligungVN;
		if (sumRohueb10 > 1.0) {
			mBeteiligungVN = Math.max(Math.min(sumRfb10 / sumRohueb10, 0.9), 0.0);
		} else {
			mBeteiligungVN = 0.9;
		}

		final double rohUeb = nanZero(rohueberschuss[t]);

		// a
		if (rohUeb > mindZfGes) {
			switch (strategie) {
			case 1: // Steuerung �ber die VN-Zielbeteiligung
				JUe = Math.min(jueZiel, rohUeb - mindZfGes);
				rfbZuf = rohUeb - JUe;
				RfB56b = 0.0;
				break;
			case 2: // Steuerung �ber eine Zielverzinsung des Eigenkapitals
				rfbZuf = Math.max(pRohueb * rohUeb, mindZfGes);
				JUe = rohUeb - rfbZuf;
				RfB56b = 0.0;
				break;
			case 3: // KE_EK = NVZ * (EK + BW_GRK + DR_NUEB) + ZiRaZu
				KE_EK = ke - aKe;
				JUE_Ziel_3 = Math.max((1.0 - pRohueb) * (rohUeb - KE_EK), 0) + KE_EK;
				rfbZuf = Math.max(rohUeb - JUE_Ziel_3, mindZfGes);
				JUe = rohUeb - rfbZuf;
				break;
			}
			// b
		} else if (Math.min(0, ueeBestand) <= rohUeb && rohUeb <= mindZfGes) {
			// b1
			if (mindZf <= rohUeb && rohUeb <= mindZfGes) {
				// kein Unterschied bei Strategie
				JUe = 0.0;
				rfbZuf = rohUeb;
				RfB56b = 0.0;
				// b2
			} else {
				// kein Unterschied bei Strategie
				rfbZuf = Math.min(rohUeb - Math.min(ueeBestand, 0), mindZf);
				JUe = rohUeb - rfbZuf;
				RfB56b = 0.0;
			}
			// c
		} else {
			rfbZuf = 0.0;
			RfB56b = Math.min(-1.0 * mBeteiligungVN * rohUeb,
					i56bfRfB * Math.max(freieRfbVj, 0.0) + i56bSueaf * Math.max(sueafVj, 0.0));
			JUe = rohUeb + RfB56b;
		}

		if (drLockInUebV == 0.0 && drVorDeklUeb == 0.0) {
			JUe = JUe + rfbZuf;
			rfbZuf = 0.0;
		}

		switch (ergebnis) {
		case 1:
			return JUe;
		case 2:
			return rfbZuf;
		case 3:
			return RfB56b;
		default:
			// dieser Fall darf eigentlich nicht eintreten
			return Double.NaN;
		}
	}

	/**
	 * Berechnet die Deckungsr�ckstellung des aktuellen Gesch�ftsjahres vor Deklaration. <br/>
	 * Funktionsname in Excel: DR_vor_Dekl.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param dr
	 *            Deckungsr�ckstellung des letzten Jahres
	 * @param leAggrV
	 *            Leistungserh�hung, aggregiert, aus dem Vorjahr
	 * @param laKwXsAggr
	 *            Leistungsanpassung Kapitalwahl
	 * @param lbwGarSonErl
	 *            Leistungsbarwert SonstErll, deterministisch
	 * @param lbw
	 *            Leistungsbarwert
	 * @param lambda
	 *            Gesamt Storno
	 * @return Deckungsr�ckstellung
	 */
	public static double drVorDekl(final int t, final double dr, final double leAggrV, final double laKwXsAggr,
			final double lbwGarSonErl, final double lbw, final double lambda) {
		if (t == 0) {
			return dr;
		} else {
			return (dr + laKwXsAggr * lbwGarSonErl) * lambda + (Double.isNaN(leAggrV) ? 0.0 : leAggrV) * lbw;
		}
	}

	/**
	 * Berechnet den ausgesch�tteten Unternehmensgewinn aus dem erzielten Jahres�berschuss und dem freiwerdenden bzw.
	 * aufzubauenden Eigenkapital. <br/>
	 * Funktionsname in Excel: ZAG.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param jue
	 *            Jahres�berschuss
	 * @param steuer
	 *            Steuer
	 * @param ek
	 *            Eigenkapital
	 * @param ekVorjahr
	 *            Eigenkapital aus dem Vorjahr
	 * @param omega
	 *            Projektionsl�nge
	 * @return Unternehmensgewinn
	 */
	public static double zag(final int t, final double jue, final double steuer, final double ek,
			final double ekVorjahr, final int omega) {
		if (t == 0 || t >= omega) {
			return 0.0;
		} else {
			return jue - steuer + (ekVorjahr - ek);
		}
	}

	/**
	 * Berechnet den zus�tzlichen ZAG im letzten Zeitpunkt. <br/>
	 * Funktionsname in Excel: ZAG_Endzahlung.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Projektionsl�nge
	 * @param cf
	 *            Cashflow, das nachdem aller verbindlichkeiten gedeckt sind, �brig bleibt
	 * @param steuer
	 *            Ertragssteuer
	 * @return ZAG
	 */
	public static double zagEndzahlung(final int t, final double omega, final double cf, final double steuer) {
		if (t == omega) {
			return cf - steuer;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet zum Zeitpunkt T den Referenzzinssatz der letzten 10 Jahre (d.h der Jahre T-(10-1),..., T). <br/>
	 * Funktionsname in Excel: ReferenzZinssatz
	 * 
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param zzrSpotEsgArr
	 *            Renditen von AAA-Anleihen der Jahre -9,...,0
	 * @param zinsArr
	 *            Rendite einer 10 J�hrigen Anleihe der Jahre 0,1,2,..., T
	 * @return Referenzzinssatz
	 */
	public static double referenzZinssatz(final int t, final double[] zzrSpotEsgArr, final double[] zinsArr) {
		double referenzZinssatz = 0.0;
		for (int i = 0; i <= 9; ++i) {
			if (t - i > 0) {
				referenzZinssatz += zzrSpotEsgArr[i];
			} else {
				referenzZinssatz += zinsArr[i];
			}
		}
		return referenzZinssatz /= 10.0;

	}

	/**
	 * Berechnet den Refernzzinssatz 2M. <br/>
	 * Funktionsname in Excel: refZins_2M
	 * 
	 * @param refZinsV
	 *            Referenzzinssatz, Vorjahr
	 * @param refZins
	 *            Referenzzinssatz
	 * @param refZins2Mv
	 *            Referenzzinssatz 2M, Vorjahr
	 * @param basisZins
	 *            Basiszinssatz
	 * @param t
	 *            Zeit
	 * @param anwendungsDifferenzAnteilig
	 *            Parameter f�r Methode 2M
	 * @return der Wert
	 */
	public static double refZins2M(final double refZinsV, final double refZins, final double refZins2Mv,
			final double basisZins, final int t, double anwendungsDifferenzAnteilig) {
		final double refzinsVergleich;
		double refZins2m = 0.0;
		if (t == 1) {
			refzinsVergleich = refZinsV;
		} else {
			refzinsVergleich = nanZero(refZins2Mv);
		}
		if (refZins <= refzinsVergleich) {
			refZins2m = Math.max(
					refzinsVergleich - anwendungsDifferenzAnteilig * Math.max(refzinsVergleich - basisZins, 0.0),
					refZins);
		} else {
			refZins2m = Math.min(
					refzinsVergleich + anwendungsDifferenzAnteilig * Math.max(basisZins - refzinsVergleich, 0),
					refZins);
			if (refZins2m < refzinsVergleich && basisZins > refzinsVergleich) {
				refZins2m = refzinsVergleich;
			}
		}
		return refZins2m;
	}

	/**
	 * Berechnet zum Zeitpunkt T die mittlere RfB Zuf�hrung der letzten 10 Jahre (d.h der Jahre T-10,..., T-1). Name in
	 * Excel: mittlRfBZuf_10J.
	 * 
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param vuHistorie
	 *            historische Werte VU
	 * @param rfbZufHistProj
	 *            Zuf�hrung zur RfB der Jahre -9,-8,...,0
	 * @param rfb56bHistProj
	 *            Zuf�hrung zur RfB der Jahre 0,1,2,3
	 * @return
	 */
	private static double mittlRfbZuf10J(final int t, final VUHistorie vuHistorie, // final double[] RfB_Zuf_hist,
			final double[] rfbZufHistProj, final double[] rfb56bHistProj) {

		double mittlRfbZuf10J = 0.0;
		int zeit = t - 1;
		for (int i = 0; i <= 9; ++i) {
			if (zeit > 0) {
				mittlRfbZuf10J += rfbZufHistProj[zeit] - rfb56bHistProj[zeit];
			} else {
				mittlRfbZuf10J += vuHistorie.get(zeit).getRfBZufuehrung(); // RfB_Zuf_hist[10 + t - i - 1];
			}
			--zeit;
		}
		return mittlRfbZuf10J / 10.0;
	}

	/**
	 * Berechnet zum Zeitpunkt T den mittleren Roh�berschuss der letzten 10 Jahre (d.h der Jahre T-10,..., T-1). Name in
	 * Excel: mittlRohueb_10J
	 * 
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param vuHistorie
	 *            historische Werte VU
	 * @param rohuebhistProj
	 *            Roh�berschuss der Jahre 0,1,2,3
	 * @return
	 */
	private static double mittlRohueb10J(final int t, final VUHistorie vuHistorie, final double[] rohuebhistProj) {
		double mittlRohueb10J = 0.0;
		int zeit = t - 1;
		for (int i = 0; i <= 9; ++i) {
			if (zeit > 0) {
				// 'mittlRohueb_10J = mittlRohueb_10J + WorksheetFunction.Max(Rohueb_hist_proj(t - i, 1), 0)
				mittlRohueb10J += rohuebhistProj[zeit];
			} else {
				// 'mittlRohueb_10J = mittlRohueb_10J + WorksheetFunction.Max(Rohueb_hist(10 + t - i - 1, 1), 0)
				mittlRohueb10J += vuHistorie.get(zeit).getRohueberschuss(); // Rohueb_hist[10 + t - i - 1];
			}
			--zeit;
		}
		return mittlRohueb10J / 10.0;
	}

	/**
	 * Schreibt das Eigenkapital fort. <br/>
	 * Funktionsname in Excel: Eigenkapital_Fortschreibung.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param ekZiel
	 *            Managementregel (Wie hoch soll das Eigenkapital in Prozent der Deckungsr�ckstellung ausfallen?)
	 * @param dr
	 *            Deckungsr�ckstellung
	 * @return Eigenkapital
	 */
	public static double eigenkapitalFortschreibung(final int t, final double ekZiel, final double dr) {
		if (t > 0) {
			return ekZiel * dr;
		} else {
			// Dieser Fall darf nicht eintreten.
			return 0.0;
		}
	}

	/**
	 * Schreibt das Genussrechkapital und Nachrangsdarlehen fort. <br/>
	 * Funktionsname in Excel: GR_NRD.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param vgGrNrd
	 *            Genussrechtkapital und Nachrangdarlehen aus dem Vorjahr
	 * @param rueckZahlung
	 *            R�ckzahlung des Genussrechtkapitals und Nachrangdarlehen im Jahr t
	 * @return Wert
	 */
	public static double grNrd(int t, final double vgGrNrd, final double rueckZahlung) {
		if (t > 0) {
			return nanZero(vgGrNrd) - rueckZahlung;
		} else {
			// Dieser Fall darf nicht eintreten.
			return 0.0;
		}
	}

	/**
	 * Berechnet die abzuf�hrende Ertragssteuer. <br/>
	 * Funktionsname in Excel: Ertragssteuer.
	 * 
	 * @param jue
	 *            Jahres�berschuss
	 * @param steuersatz
	 *            Steuersatz
	 * @param vv
	 *            Verlustvortrag
	 * @return Ertragssteuer
	 */
	public static double ertragssteuer(final double jue, final double steuersatz, final double vv) {
		// 'Teil des Verlustes zum anrechnen mit Gewinnen im Jahr T
		final double VV_x = Math.min(nanZero(vv), 0.4 * Math.min(1000000, nanZero(jue)) + 0.6 * nanZero(jue));
		// 'Steuerberechnung
		return Math.max(jue - VV_x, 0) * steuersatz;
	}

	/**
	 * Berechnet die abzuf�hrende Ertragssteuer nach der LS-Korrektur<br/>
	 * Funktionsname in Excel: Ertragssteuer_LS.
	 * 
	 * @param ertragssteuer
	 *            Ertragssteuer
	 * @param lsHgb
	 *            Latente Steuer HGB im Zeitpunkt t
	 * @return der Wert
	 */
	public static double ertragssteuerLs(final double ertragssteuer, final double lsHgb) {
		return Math.max(ertragssteuer - lsHgb, 0.0);
	}

	/**
	 * Berechnet HGB-Latente Steuer<br/>
	 * Funktionsname in Excel: LS_HGB
	 * 
	 * @param lsHgbV
	 *            HGB Latente Steuer, Vorjahr
	 * @param ertragssteuer
	 *            Steuer, festgelegt
	 * @param ertragssteuerLs
	 *            Steuer, nach der LS-Korrektur
	 * @return der Wert
	 */
	public static double lsHgb(final double lsHgbV, final double ertragssteuer, final double ertragssteuerLs) {
		return lsHgbV - (ertragssteuer - ertragssteuerLs);
	}

	/**
	 * Berechnet den Verlustvortrag (Sammelkonto f�r Verluste aus den Vorjahren). <br/>
	 * Funktionsname in Excel: VV.
	 * 
	 * @param vgVv
	 *            Verlustvortrag aus dem Vorjahr
	 * @param jue
	 *            Jahres�berschuss
	 * @param ertragssteuer
	 *            Ertragssteuer im Jahr t
	 * @param steuersatz
	 *            Steuersatz im Jahr t
	 * @return Verlustvortrag
	 */
	public static double vv(final double vgVv, final double jue, final double ertragssteuer, final double steuersatz) {
		if (steuersatz == 0) {
			return nanZero(vgVv) - jue;
		} else {
			return nanZero(vgVv) + (ertragssteuer / steuersatz - jue);
		}
	}

	/**
	 * Berechnet die nicht festgelegten R�ckstellungen f�r Beitragsr�ckerstattung. <br/>
	 * Funktionsname in Excel: nfRfB.
	 * 
	 * @param sueAfAgg
	 *            Schluss�berschussfonds
	 * @param fRfBFrei
	 *            freie R�ckstellungen f�r Beitragsr�ckerstattung
	 * @return R�ckstellung
	 */
	public static double nfRfB(final double sueAfAgg, final double fRfBFrei) {
		return sueAfAgg + fRfBFrei;
	}

	/**
	 * Berechnet den ZAG zum Auszahlungszeitpunkt.<br/>
	 * Funktionsname in Excel: ZAG_faellig.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param vgZag
	 *            ZAG aus dem t-1
	 * @return ZAG
	 */
	public static double zagFaellig(final int t, final double vgZag) {
		return (t == 1 ? 0.0 : vgZag);
	}

}
