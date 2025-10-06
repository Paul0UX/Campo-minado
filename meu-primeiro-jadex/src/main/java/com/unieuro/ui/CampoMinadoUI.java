package com.unieuro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class CampoMinadoUI extends JFrame {

    private final JButton[][] botoes;
    private final JLabel tempoLabel;
    private final JLabel minasLabel;

    public CampoMinadoUI() {
        setTitle("Campo Minado - Visualização");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Cabeçalho
        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        tempoLabel = new JLabel("Tempo: 0s");
        minasLabel = new JLabel("Minas encontradas: 0");
        infoPanel.add(tempoLabel);
        infoPanel.add(minasLabel);
        add(infoPanel, BorderLayout.NORTH);

        // Tabuleiro
        JPanel boardPanel = new JPanel(new GridLayout(9, 9));
        botoes = new JButton[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                botoes[i][j] = new JButton("");
                botoes[i][j].setEnabled(false);
                botoes[i][j].setBackground(Color.LIGHT_GRAY);
                boardPanel.add(botoes[i][j]);
            }
        }
        add(boardPanel, BorderLayout.CENTER);

        setVisible(true);

        // Thread que atualiza a interface a cada 2 segundos
        new Thread(this::atualizarLoop).start();
    }

    private void atualizarLoop() {
        Path campoPath = Paths.get("campo.csv");
        Path minasPath = Paths.get("minas_encontradas.txt");
        Path tempoPath = Paths.get("tempo.txt");

        while (true) {
            try {
                if (Files.exists(campoPath)) {
                    atualizarTabuleiro(campoPath);
                }

                if (Files.exists(minasPath)) {
                    String minas = Files.readString(minasPath).trim();
                    minasLabel.setText("Minas encontradas: " + minas);
                }

                if (Files.exists(tempoPath)) {
                    String tempo = Files.readString(tempoPath).trim();
                    tempoLabel.setText("Tempo: " + tempo + "s");
                }

                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void atualizarTabuleiro(Path arquivo) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo);
        for (int i = 0; i < linhas.size(); i++) {
            String[] celulas = linhas.get(i).split(",");
            for (int j = 0; j < celulas.length; j++) {
                String valor = celulas[j].trim();

                // Define cores e texto
                if (valor.equals("*")) {
                    botoes[i][j].setText("💣");
                    botoes[i][j].setBackground(Color.RED);
                } else if (valor.equals("X")) {
                    botoes[i][j].setText("⛔");
                    botoes[i][j].setBackground(Color.GRAY);
                } else if (valor.matches("\\d")) {
                    botoes[i][j].setText(valor);
                    botoes[i][j].setBackground(Color.WHITE);
                } else {
                    botoes[i][j].setText("");
                    botoes[i][j].setBackground(Color.LIGHT_GRAY);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CampoMinadoUI::new);
    }
}
