package com.tnt.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

public class OnProgress {
  static class BarThread extends Thread {
    private static int DELAY = 500;
    JProgressBar progressBar;
   

    public BarThread(JProgressBar bar) {
      progressBar = bar;
    }

    public void run() {
      int minimum = progressBar.getMinimum();
      int maximum = progressBar.getMaximum();
      Runnable runner = new Runnable() {
        public void run() {
          int value = progressBar.getValue();
          progressBar.setValue(value+1);
        }
      };
      for (int i=minimum; i<maximum; i++) {
        try {
          SwingUtilities.invokeAndWait(runner);
          // Our task for each step is to just sleep
          Thread.sleep(DELAY);
        } catch (InterruptedException ignoredException) {
        } catch (InvocationTargetException ignoredException) {
        }
      }
    }
  }
  public static void main(String args[]) {
    // Initialize
    final JProgressBar aJProgressBar = new JProgressBar(0, 100);

    final JButton aJButton = new JButton("Start");
    final JButton b = new JButton("Test");
    aJProgressBar.setStringPainted(true); // 显示百分比字符
    aJProgressBar.setIndeterminate(true); // 不确定的进度条
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        aJButton.setEnabled(false);
        Thread stepper = new BarThread(aJProgressBar);
        stepper.start();
      }
    };

    aJButton.addActionListener(actionListener);

    JFrame theFrame = new JFrame("Progress Bars");
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container contentPane = theFrame.getContentPane();
    contentPane.add(aJProgressBar, BorderLayout.NORTH);
    contentPane.add(aJButton, BorderLayout.SOUTH);
    contentPane.add(b,BorderLayout.CENTER);
    theFrame.setSize(300, 100);
    theFrame.show();
  }
} 