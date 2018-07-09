package de.gdv.bsm.intern.applic;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import de.gdv.bsm.intern.params.SzenarioMappingZeile;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.Berechnung;
import de.gdv.bsm.vu.berechnung.FiAusfallZeile;
import de.gdv.bsm.vu.berechnung.FlvZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;

/**
 * Frame zur Anzeige der errechneten Blätter.
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
@SuppressWarnings("serial")
public class ResultFrame extends JFrame {

	/**
	 * Erstellung und Anzeige des Rahmens.
	 * 
	 * @param berechnungResultat
	 *            die anzuzeigenden Ergebnisse
	 */
	public ResultFrame(final BerechnungResultat berechnungResultat) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("BSM Ergebnis");

		final Berechnung berechnung = berechnungResultat.getLetzteBerechnung();
		final JPanel north = new LabelPanel() {
			{
				final SzenarioMappingZeile szm = berechnung.getVuParameter().getSzenarioMapping()
						.getSzenarionMapping(berechnung.getSzenarioId());

				int row = 0;
				addLine(row++, "Szenario-ID", ineditableTextField(berechnung.getSzenarioId()));
				addLine(row++, "Szenario", ineditableTextField(szm.getName()));
				addLine(row++, "Proj. VT Klassik", ineditableTextField(szm.getProjektionVtKlassik()));
				addLine(row++, "Pfad", ineditableTextField(berechnung.getAktuellerPfad()));
			}

			private JComponent ineditableTextField(final int i) {
				return ineditableTextField(String.valueOf(i));
			}

			private JComponent ineditableTextField(final String text) {
				final JTextField tf = new JTextField(text);
				tf.setEditable(false);
				return tf;
			}
		};
		add(north, BorderLayout.NORTH);

		final JTabbedPane center = new JTabbedPane();

		final TableFieldModel<FiAusfallZeile> fiAusfall = new TableFieldModel<>(berechnung.getFiAusfallZeilen(),
				FiAusfallZeile.class);
		final JTable fiAusfallTable = new FieldTable(fiAusfall);
		center.add("FI Ausfall", new JScrollPane(fiAusfallTable));

		if (berechnung.flvRechnen) {
			final TableFieldModel<FlvZeile> flvZeile = new TableFieldModel<>(berechnung.getFlvZeilen(), FlvZeile.class);
			final JTable flvZeileTable = new FieldTable(flvZeile);
			center.add("flv", new JScrollPane(flvZeileTable));
		}

		final TableFieldModel<RzgZeile> rzg = new TableFieldModel<>(berechnung.getRzgZeilen(), RzgZeile.class);
		final JTable rzgTable = new FieldTable(rzg);
		center.add("rzg", new JScrollPane(rzgTable));

		final TableFieldModel<AggZeile> agg = new TableFieldModel<>(berechnung.getAggZeilen(), AggZeile.class);
		final JTable aggTable = new FieldTable(agg);
		center.add("agg", new JScrollPane(aggTable));

		final List<KennzahlenPfadweise> kennzahlenPfadweise = new ArrayList<>();
		for (int i : berechnungResultat.getKennzahlenPfadweise().keySet()) {
			final Map<Integer, KennzahlenPfadweise> kp = berechnungResultat.getKennzahlenPfadweise().get(i);
			for (int j : kp.keySet()) {
				kennzahlenPfadweise.add(kp.get(j));
			}
		}

		final TableFieldModel<KennzahlenPfadweise> kennzPfad = new TableFieldModel<KennzahlenPfadweise>(
				kennzahlenPfadweise, KennzahlenPfadweise.class);
		final JTable kennzPfadTable = new FieldTable(kennzPfad);
		center.add("Kennzahlen Pfadweise", new JScrollPane(kennzPfadTable));

		final List<KennzahlenPfadweiseLoB> kennzahlenPfadweiseLoB = new ArrayList<>();
		for (int i : berechnungResultat.getKennzahlenPfadweiseLoB().keySet()) {
			final Map<Integer, List<KennzahlenPfadweiseLoB>> kp = berechnungResultat.getKennzahlenPfadweiseLoB().get(i);
			for (int j : kp.keySet()) {
				kennzahlenPfadweiseLoB.addAll(kp.get(j));
			}
		}

		final TableFieldModel<KennzahlenPfadweiseLoB> kennzPfadLoB = new TableFieldModel<KennzahlenPfadweiseLoB>(
				kennzahlenPfadweiseLoB, KennzahlenPfadweiseLoB.class);
		final JTable kennzPfadLobTable = new FieldTable(kennzPfadLoB);
		center.add("Kennzahlen Pfadweise LoB", new JScrollPane(kennzPfadLobTable));

		final SizedTableModel mittelwerteModel = new MittelwerteTableModel(berechnungResultat.getMittelwerte());
		final JTable mittelwerteTable = new FieldTable(mittelwerteModel);
		center.add("Mittelwerte zeitschrittig", new JScrollPane(mittelwerteTable));

		add(center, BorderLayout.CENTER);

		setSize(1200, 800);
		setVisible(true);
	}
}
