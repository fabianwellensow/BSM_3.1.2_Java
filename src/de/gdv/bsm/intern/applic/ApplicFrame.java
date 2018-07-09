package de.gdv.bsm.intern.applic;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.gdv.bsm.intern.params.Eingabe;
import de.gdv.bsm.intern.params.VuParameter;

/**
 * Zentrales Fenster der Applikation zur Steuerung.
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
@SuppressWarnings("serial")
public class ApplicFrame extends JFrame {
	private final VuParameter vuParameter;
	private final Eingabe eingabe;

	private final JTextField pfadVon = new JTextField(10);
	private final JTextField pfadBis = new JTextField(10);
	private final JTextField pfad = new JTextField();

	ApplicFrame(final VuParameter vuParameter, final Eingabe eingabe) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Branchen-Simulations-Modell");

		this.vuParameter = vuParameter;
		this.eingabe = eingabe;

		final JPanel center = new LabelPanel() {
			{
				int row = 0;

				final JCheckBox alleSzenarien = new JCheckBox("", eingabe.isAlleSzenarien());
				alleSzenarien.addActionListener(e -> eingabe.setAlleSzenarien(alleSzenarien.isSelected()));
				addLine(row++, "Alle Szenarien rechenen", alleSzenarien);

				int index = 0;
				for (Pair<Integer, String> p : eingabe.getSzenarienList()) {
					if (p.a == eingabe.getSzenario())
						break;
					++index;
				}
				@SuppressWarnings("unchecked")
				final JComboBox<Pair<Integer, String>> szenario = new JComboBox<>(
						eingabe.getSzenarienList().toArray(new Pair[0]));
				if (index < eingabe.getSzenarienList().size()) {
					szenario.setSelectedIndex(index);
				}
				szenario.addActionListener(e -> {
					@SuppressWarnings("unchecked")
					Pair<Integer, String> pair = (Pair<Integer, String>) szenario.getSelectedItem();
					eingabe.setSzenario(pair.a);
				});
				addLine(row++, "Szenario", szenario);

				pfadVon.setText(String.valueOf(eingabe.getPfadVon()));
				addLine(row++, "Pfad von", pfadVon);

				pfadBis.setText(String.valueOf(eingabe.getPfadBis()));
				addLine(row++, "Pfad bis", pfadBis);

				final JCheckBox flvRechnen = new JCheckBox("", eingabe.isFlvRechnen());
				flvRechnen.addActionListener(e -> eingabe.setFlvRechnen(flvRechnen.isSelected()));
				addLine(row++, "FLV rechnen", flvRechnen);

				final JCheckBox negAusfall = new JCheckBox("", eingabe.isNegAusfallwk());
				negAusfall.addActionListener(e -> eingabe.setNegAusfallwk(negAusfall.isSelected()));
				addLine(row++, "neg. Ausfall", negAusfall);

				final JPanel szenarienPfad = new LabelPanel() {
					{
						final JButton button = new JButton("...");
						pfad.setText(eingabe.getPfadSzenariensatz());
						pfad.setEnabled(false);

						button.addActionListener(e -> {
							final JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							fc.setCurrentDirectory(new File(pfad.getText()));
							fc.setMultiSelectionEnabled(false);
							switch (fc.showOpenDialog(button)) {
							case JFileChooser.APPROVE_OPTION:
								pfad.setText(fc.getSelectedFile().getAbsolutePath());
								break;
							}
						});

						addLabelComponent(0, button);
						addFirstComponent(0, pfad);
					}
				};
				addLine(row++, "Szenarien", szenarienPfad);
			}
		};
		add(center, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel() {
			{
				final JButton rechnen = new JButton("Rechnen");
				rechnen.addActionListener(e -> rechnen());
				add(rechnen);

				final JButton ende = new JButton("Ende");
				ende.addActionListener(e -> System.exit(0));
				add(ende);
			}
		};
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	private void rechnen() {
		try {
			try {
				final int pfadVonN = Integer.parseInt(pfadVon.getText());
				int pfadBisN = Integer.parseInt(pfadBis.getText());

				if (pfadVonN < 0) {
					JOptionPane.showMessageDialog(this, "Pfad von muss gr��er gleich 0 sein.", "Fehler",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				eingabe.setPfadVon(pfadVonN);
				eingabe.setPfadBis(pfadBisN);
				eingabe.setPfadSzenariensatz(pfad.getText());
			} catch (NumberFormatException n) {
				JOptionPane.showMessageDialog(this, "Pfad von und Pfad bis m�ssen numerisch sein.", "Fehler",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			final RechenFortschritt rf = new RechenFortschritt(vuParameter, eingabe);
			switch (rf.getExitCode()) {
			case ABBRUCH:
				break;
			case FEHLER:
				throw rf.getCrashReason();
			case OK:
				new ResultFrame(rf.getLetzteBerechnung());
				break;
			default:
				throw new IllegalArgumentException("Unbekannter Exit-Code: " + rf.getExitCode());
			}
		} catch (Throwable e) {
			new ErrorMessageBox(e);
		}
	}
}
