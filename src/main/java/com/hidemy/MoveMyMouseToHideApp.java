package com.hidemy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MoveMyMouseToHideApp {
    private static MoveMyMouseToHide randomMouseMover;
    private static JTextField moveAmountField;
    private static JLabel statusLabel;
    private static JButton startButton;
    private static JButton stopButton;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Move My Mouse To Hide");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setResizable(false); // Make the window unresizable
        frame.setLayout(null);

        JLabel label = new JLabel("Amount of movement:");
        label.setBounds(10, 20, 150, 25);
        frame.add(label);

        moveAmountField = new JTextField("100"); // Default value
        moveAmountField.setBounds(160, 20, 100, 25);
        frame.add(moveAmountField);

        startButton = new JButton("Start");
        startButton.setBounds(10, 60, 100, 30);
        frame.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.setBounds(120, 60, 100, 30);
        stopButton.setEnabled(false); // Disable stop button initially
        frame.add(stopButton);

        statusLabel = new JLabel("The movement is not active");
        statusLabel.setBounds(10, 100, 250, 25);
        statusLabel.setForeground(Color.RED); // Default to red
        frame.add(statusLabel);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String moveAmountText = moveAmountField.getText();
                if (moveAmountText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Amount of movement is mandatory.");
                    return;
                }
                try {
                    int moveAmount = Integer.parseInt(moveAmountText);
                    randomMouseMover = new MoveMyMouseToHide(moveAmount);
                    randomMouseMover.start();
                    updateStatus(true);
                    setButtonStates(false); // Disable start button and enable stop button
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid integer.");
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (randomMouseMover != null) {
                    randomMouseMover.stop();
                    updateStatus(false);
                    setButtonStates(true); // Enable start button and disable stop button
                }
            }
        });

        frame.setVisible(true);
    }

    private static void updateStatus(boolean isActive) {
        if (isActive) {
            statusLabel.setText("The movement is active");
            statusLabel.setForeground(new Color(0, 153, 0)); // A different shade of green
        } else {
            statusLabel.setText("The movement is not active");
            statusLabel.setForeground(Color.RED); // Change label color to red
        }
    }

    private static void setButtonStates(boolean isStartEnabled) {
        startButton.setEnabled(isStartEnabled);
        stopButton.setEnabled(!isStartEnabled);
    }
}