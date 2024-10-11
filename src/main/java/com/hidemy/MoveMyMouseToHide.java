package com.hidemy;

import java.awt.*;
import java.util.Random;

public class MoveMyMouseToHide {
  private static final int MOVE_DURATION_MS = 2000; // 2 seconds
  private static final int PAUSE_DURATION_MS = 10000; // 10 seconds
  private int moveAmount; // Dynamic movement amount
  private volatile boolean running = false;

  public MoveMyMouseToHide(int moveAmount) {
    this.moveAmount = moveAmount;
  }

  public void start() {
    running = true;
    new Thread(() -> {
      try {
        Robot robot = new Robot();
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point initialLocation = pointerInfo.getLocation();
        Random random = new Random();

        while (running) {
          long moveEndTime = System.currentTimeMillis() + MOVE_DURATION_MS;

          while (System.currentTimeMillis() < moveEndTime && running) {
            int xMove = random.nextInt(moveAmount * 2) - moveAmount;
            int yMove = random.nextInt(moveAmount * 2) - moveAmount;

            int currentX = (int) initialLocation.getX();
            int currentY = (int) initialLocation.getY();

            robot.mouseMove(currentX + xMove, currentY + yMove);
            Thread.sleep(100);
          }

          Thread.sleep(PAUSE_DURATION_MS);
        }
      } catch (AWTException | InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
  }

  public void stop() {
    running = false;
  }
}