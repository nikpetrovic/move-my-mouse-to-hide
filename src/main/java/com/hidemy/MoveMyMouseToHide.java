package com.hidemy;

import java.awt.*;
import java.util.Random;

public class MoveMyMouseToHide {
  private final int moveAmount;
  private final int moveDurationMs;
  private final int pauseDurationMs;
  private volatile boolean running = false;

  public MoveMyMouseToHide(int moveAmount, int moveDurationSeconds, int pauseDurationSeconds) {
    this.moveAmount = moveAmount;
    this.moveDurationMs = moveDurationSeconds * 1000;
    this.pauseDurationMs = pauseDurationSeconds * 1000;
  }

  public void start() {
    running = true;
    Thread worker = new Thread(() -> {
      try {
        Robot robot = new Robot();
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point initialLocation = pointerInfo.getLocation();
        Random random = new Random();

        while (running) {
          long moveEndTime = System.currentTimeMillis() + moveDurationMs;

          while (System.currentTimeMillis() < moveEndTime && running) {
            int xMove = random.nextInt(moveAmount * 2) - moveAmount;
            int yMove = random.nextInt(moveAmount * 2) - moveAmount;

            int currentX = (int) initialLocation.getX();
            int currentY = (int) initialLocation.getY();

            robot.mouseMove(currentX + xMove, currentY + yMove);
            Thread.sleep(100);
          }

          if (running) {
            Thread.sleep(pauseDurationMs);
          }
        }
      } catch (AWTException | InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    worker.setDaemon(true);
    worker.start();
  }

  public void stop() {
    running = false;
  }
}
