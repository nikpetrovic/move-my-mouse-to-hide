package com.hidemy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class MSTeamsActivityMonitor {

  private static final int MOVE_AMOUNT_MIN   = 10;
  private static final int MOVE_AMOUNT_MAX   = 1000;
  private static final int MOVE_DURATION_MIN = 1;
  private static final int MOVE_DURATION_MAX = 5;
  private static final int PAUSE_DURATION_MIN = 5;
  private static final int PAUSE_DURATION_MAX = 120;

  private static final Color BG_DARK       = new Color(18, 18, 35);
  private static final Color CARD_BG       = new Color(28, 28, 52);
  private static final Color CARD_BORDER   = new Color(50, 50, 90);
  private static final Color ACCENT_TEAL   = new Color(32, 178, 140);
  private static final Color ACCENT_CYAN   = new Color(0, 210, 210);
  private static final Color ACCENT_AMBER  = new Color(220, 130, 20);
  private static final Color TEXT_PRIMARY  = new Color(230, 230, 255);
  private static final Color TEXT_MUTED    = new Color(140, 140, 180);
  private static final Color ERROR_COLOR   = new Color(255, 80, 80);
  private static final Color SUCCESS_COLOR = new Color(50, 220, 140);

  private static MoveMyMouseToHide randomMouseMover;
  private static JButton startButton;
  private static JButton stopButton;
  private static JLabel statusLabel;
  private static JLabel statusDot;
  private static Timer deferredStopTimer;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(MSTeamsActivityMonitor::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}

    JFrame frame = new JFrame("MS Teams Worker \uD83E\uDD78");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);

    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    root.setBackground(BG_DARK);
    root.setBorder(new EmptyBorder(20, 24, 24, 24));

    // ── Header ──────────────────────────────────────────────────────────────
    JLabel title = new JLabel("MS Teams Worker \uD83E\uDD78");
    title.setFont(new Font("Segoe UI", Font.BOLD, 20));
    title.setForeground(ACCENT_CYAN);
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    root.add(title);

    JLabel subtitle = new JLabel("Keep your status green while you grab coffee \u2615");
    subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    subtitle.setForeground(TEXT_MUTED);
    subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
    root.add(subtitle);
    root.add(Box.createVerticalStrut(18));

    // ── Config card ─────────────────────────────────────────────────────────
    JPanel card = createCard();
    card.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 4, 5, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Row 0 – Move amount
    JTextField moveAmountField = styledField("100");
    JLabel moveAmountError = errorLabel();
    addFieldRow(card, gbc, 0,
        "Movement amount",
        "(pixels, " + MOVE_AMOUNT_MIN + " \u2013 " + MOVE_AMOUNT_MAX + ")  How far the cursor drifts from its origin.",
        moveAmountField, moveAmountError);

    // Row 1 – Move duration
    JTextField moveDurationField = styledField("2");
    JLabel moveDurationError = errorLabel();
    addFieldRow(card, gbc, 1,
        "Move duration (sec)",
        "(" + MOVE_DURATION_MIN + " \u2013 " + MOVE_DURATION_MAX + " sec)  How long the cursor moves before pausing.",
        moveDurationField, moveDurationError);

    // Row 2 – Pause interval
    JTextField pauseDurationField = styledField("30");
    JLabel pauseDurationError = errorLabel();
    addFieldRow(card, gbc, 2,
        "Pause interval (sec)",
        "(" + PAUSE_DURATION_MIN + " \u2013 " + PAUSE_DURATION_MAX + " sec)  How long to wait between movement bursts.",
        pauseDurationField, pauseDurationError);

    // Row 3 – Deferred stop checkbox
    JCheckBox deferredStopCheck = new JCheckBox("Deferred stop");
    deferredStopCheck.setFont(new Font("Segoe UI", Font.BOLD, 13));
    deferredStopCheck.setForeground(TEXT_PRIMARY);
    deferredStopCheck.setOpaque(false);
    deferredStopCheck.setFocusPainted(false);
    gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.weightx = 1;
    card.add(deferredStopCheck, gbc);

    // Row 4 – "Stop the move after" label + input + sec + mins label
    JLabel stopAfterLabel = new JLabel("Stop the move after");
    stopAfterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
    stopAfterLabel.setForeground(TEXT_PRIMARY);
    stopAfterLabel.setEnabled(false);
    gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1; gbc.weightx = 0;
    card.add(stopAfterLabel, gbc);

    JPanel stopAfterInline = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    stopAfterInline.setOpaque(false);
    JTextField stopAfterField = styledFieldNarrow("120");
    stopAfterField.setEnabled(false);
    JLabel stopAfterSuffix = new JLabel("sec");
    stopAfterSuffix.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    stopAfterSuffix.setForeground(TEXT_MUTED);
    stopAfterSuffix.setEnabled(false);
    JLabel minsLabel = new JLabel("2.00 mins");
    minsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    minsLabel.setForeground(ACCENT_CYAN);
    minsLabel.setEnabled(false);
    stopAfterInline.add(stopAfterField);
    stopAfterInline.add(stopAfterSuffix);
    stopAfterInline.add(minsLabel);
    gbc.gridx = 1; gbc.gridy = 10; gbc.weightx = 1;
    card.add(stopAfterInline, gbc);

    gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2; gbc.weightx = 1;
    JLabel stopAfterHint = new JLabel("(1 \u2013 28800 sec)  After this many seconds the mover will automatically stop.");
    stopAfterHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    stopAfterHint.setForeground(TEXT_MUTED);
    card.add(stopAfterHint, gbc);

    JLabel stopAfterError = errorLabel();
    gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2;
    card.add(stopAfterError, gbc);

    card.setAlignmentX(Component.LEFT_ALIGNMENT);
    root.add(card);
    root.add(Box.createVerticalStrut(16));

    // Wire deferred-stop checkbox
    deferredStopCheck.addActionListener(e -> {
      boolean checked = deferredStopCheck.isSelected();
      stopAfterLabel.setEnabled(checked);
      stopAfterField.setEnabled(checked);
      stopAfterSuffix.setEnabled(checked);
      minsLabel.setEnabled(checked);
    });

    // Live minutes converter
    stopAfterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      private void update() {
        String text = stopAfterField.getText().trim();
        try {
          int secs = Integer.parseInt(text);
          if (secs > 0) {
            minsLabel.setText(String.format("%.2f mins", secs / 60.0));
          } else {
            minsLabel.setText("—");
          }
        } catch (NumberFormatException ex) {
          minsLabel.setText("—");
        }
      }
      public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
      public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
      public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    });

    // ── Buttons ─────────────────────────────────────────────────────────────
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    btnPanel.setOpaque(false);
    btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    startButton = accentButton("\u25B6  Start", ACCENT_TEAL);
    stopButton  = accentButton("\u25A0  Stop",  ACCENT_AMBER);
    stopButton.setEnabled(false);

    btnPanel.add(startButton);
    btnPanel.add(stopButton);
    root.add(btnPanel);
    root.add(Box.createVerticalStrut(14));

    // ── Status bar ──────────────────────────────────────────────────────────
    JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    statusRow.setOpaque(false);
    statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    statusDot = new JLabel("\u25CF");
    statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    statusDot.setForeground(ERROR_COLOR);

    statusLabel = new JLabel("Inactive \u2013 cursor is not moving");
    statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
    statusLabel.setForeground(TEXT_PRIMARY);

    statusRow.add(statusDot);
    statusRow.add(statusLabel);
    root.add(statusRow);

    // ── Wire actions ────────────────────────────────────────────────────────
    startButton.addActionListener(e -> {
      boolean valid = true;

      int moveAmount = 0;
      try {
        moveAmount = Integer.parseInt(moveAmountField.getText().trim());
        if (moveAmount < MOVE_AMOUNT_MIN || moveAmount > MOVE_AMOUNT_MAX) {
          moveAmountError.setText("Must be between " + MOVE_AMOUNT_MIN + " and " + MOVE_AMOUNT_MAX);
          valid = false;
        } else {
          moveAmountError.setText(" ");
        }
      } catch (NumberFormatException ex) {
        moveAmountError.setText("Enter a whole number (" + MOVE_AMOUNT_MIN + " \u2013 " + MOVE_AMOUNT_MAX + ")");
        valid = false;
      }

      int moveDuration = 0;
      try {
        moveDuration = Integer.parseInt(moveDurationField.getText().trim());
        if (moveDuration < MOVE_DURATION_MIN || moveDuration > MOVE_DURATION_MAX) {
          moveDurationError.setText("Must be between " + MOVE_DURATION_MIN + " and " + MOVE_DURATION_MAX);
          valid = false;
        } else {
          moveDurationError.setText(" ");
        }
      } catch (NumberFormatException ex) {
        moveDurationError.setText("Enter a whole number (" + MOVE_DURATION_MIN + " \u2013 " + MOVE_DURATION_MAX + ")");
        valid = false;
      }

      int pauseDuration = 0;
      try {
        pauseDuration = Integer.parseInt(pauseDurationField.getText().trim());
        if (pauseDuration < PAUSE_DURATION_MIN || pauseDuration > PAUSE_DURATION_MAX) {
          pauseDurationError.setText("Must be between " + PAUSE_DURATION_MIN + " and " + PAUSE_DURATION_MAX);
          valid = false;
        } else {
          pauseDurationError.setText(" ");
        }
      } catch (NumberFormatException ex) {
        pauseDurationError.setText("Enter a whole number (" + PAUSE_DURATION_MIN + " \u2013 " + PAUSE_DURATION_MAX + ")");
        valid = false;
      }

      int stopAfterSecs = 0;
      if (deferredStopCheck.isSelected()) {
        try {
          stopAfterSecs = Integer.parseInt(stopAfterField.getText().trim());
          if (stopAfterSecs < 1 || stopAfterSecs > 28800) {
            stopAfterError.setText("Must be between 1 and 28800");
            valid = false;
          } else {
            stopAfterError.setText(" ");
          }
        } catch (NumberFormatException ex) {
          stopAfterError.setText("Enter a whole number (1 \u2013 28800)");
          valid = false;
        }
      }

      frame.pack();

      if (!valid) return;

      randomMouseMover = new MoveMyMouseToHide(moveAmount, moveDuration, pauseDuration);
      randomMouseMover.start();
      setActive(true);

      if (deferredStopCheck.isSelected()) {
        final int delayMs = stopAfterSecs * 1000;
        deferredStopTimer = new Timer(delayMs, ev -> {
          if (randomMouseMover != null) randomMouseMover.stop();
          setActive(false);
          deferredStopTimer = null;
        });
        deferredStopTimer.setRepeats(false);
        deferredStopTimer.start();
      }
    });

    stopButton.addActionListener(e -> {
      if (deferredStopTimer != null) {
        deferredStopTimer.stop();
        deferredStopTimer = null;
      }
      if (randomMouseMover != null) {
        randomMouseMover.stop();
      }
      setActive(false);
    });

    frame.setContentPane(root);
    frame.pack();
    frame.setMinimumSize(frame.getSize());
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private static JPanel createCard() {
    JPanel card = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CARD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.setColor(CARD_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
      }
    };
    card.setOpaque(false);
    card.setBorder(new EmptyBorder(14, 16, 14, 16));
    return card;
  }

  private static void addFieldRow(JPanel card, GridBagConstraints gbc, int row,
      String labelText, String hintText,
      JTextField field, JLabel errorLabel) {

    int baseRow = row * 3;

    gbc.gridx = 0; gbc.gridy = baseRow; gbc.gridwidth = 1; gbc.weightx = 0;
    JLabel lbl = new JLabel(labelText);
    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lbl.setForeground(TEXT_PRIMARY);
    card.add(lbl, gbc);

    gbc.gridx = 1; gbc.gridy = baseRow; gbc.weightx = 1;
    card.add(field, gbc);

    gbc.gridx = 0; gbc.gridy = baseRow + 1; gbc.gridwidth = 2; gbc.weightx = 1;
    JLabel hint = new JLabel(hintText);
    hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    hint.setForeground(TEXT_MUTED);
    card.add(hint, gbc);

    gbc.gridx = 0; gbc.gridy = baseRow + 2; gbc.gridwidth = 2;
    card.add(errorLabel, gbc);
  }

  private static JTextField styledField(String defaultValue) {
    JTextField f = new JTextField(defaultValue, 6) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(40, 40, 70));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        super.paintComponent(g);
        g2.dispose();
      }
    };
    f.setOpaque(false);
    f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    f.setForeground(ACCENT_CYAN);
    f.setCaretColor(ACCENT_CYAN);
    f.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(CARD_BORDER, 1, true),
        new EmptyBorder(4, 8, 4, 8)));
    f.setHorizontalAlignment(SwingConstants.CENTER);
    f.setMaximumSize(new Dimension(80, 30));

    f.addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_CYAN, 2, true),
            new EmptyBorder(3, 7, 3, 7)));
      }
      @Override public void focusLost(FocusEvent e) {
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
      }
    });
    return f;
  }

  private static JTextField styledFieldNarrow(String defaultValue) {
    JTextField f = styledField(defaultValue);
    f.setColumns(4);
    f.setMaximumSize(new Dimension(55, 30));
    return f;
  }

  private static JLabel errorLabel() {
    JLabel lbl = new JLabel(" ");
    lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
    lbl.setForeground(ERROR_COLOR);
    return lbl;
  }

  private static JButton accentButton(String text, Color color) {
    JButton btn = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base = isEnabled() ? color : color.darker().darker();
        GradientPaint gp = new GradientPaint(0, 0, base.brighter(), 0, getHeight(), base.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btn.setForeground(Color.WHITE);
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setBorder(new EmptyBorder(8, 22, 8, 22));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }

  private static void setActive(boolean active) {
    startButton.setEnabled(!active);
    stopButton.setEnabled(active);
    if (active) {
      statusDot.setForeground(SUCCESS_COLOR);
      statusLabel.setText("Active \u2013 cursor is moving \uD83D\uDFE2");
      statusLabel.setForeground(SUCCESS_COLOR);
    } else {
      statusDot.setForeground(ERROR_COLOR);
      statusLabel.setText("Inactive \u2013 cursor is not moving");
      statusLabel.setForeground(TEXT_PRIMARY);
    }
  }
}
