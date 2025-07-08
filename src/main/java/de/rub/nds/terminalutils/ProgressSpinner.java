/*
 * Terminal Utils
 *
 * Copyright 2022-2025 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.terminalutils;

import java.io.Console;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for displaying a console-based progress spinner during task execution. Primarily
 * intended for interactive terminal environments. Can be switched on by setting the environment
 * variable "TTY" to "true".
 */
public final class ProgressSpinner {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<String> spinnerTasks = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean spinnerRunning = new AtomicBoolean(false);
    private static final AtomicBoolean isInteractive = new AtomicBoolean(false);
    private static final String[] SPINNER_FRAMES_UTF_8 = {
        "⢎⡰", "⢎⡡", "⢎⡑", "⢎⠱", "⠎⡱", "⢊⡱", "⢌⡱", "⢆⡱"
    };
    private static final String[] SPINNER_FRAMES_DOS = {"|", "/", "-", "\\"};
    private static String[] SPINNER_FRAMES;

    private static volatile boolean firstTaskIsMeta = true;
    private static Thread spinnerThread;

    static {
        initializeInteractiveMode();
    }

    private ProgressSpinner() {
        // Utility class

    }

    /**
     * Starts a spinner for a given task label. Only active in interactive mode.
     *
     * @param taskLabel a descriptive label of the task to be shown in the spinner
     */
    public static void startSpinnerTask(String taskLabel) {
        if (!isInteractive()) {
            return;
        }

        spinnerTasks.add(taskLabel);

        if (spinnerRunning.get()) {
            return;
        }

        spinnerRunning.set(true);
        spinnerThread =
                Thread.ofVirtual()
                        .start(
                                () -> {
                                    int idx = 0;
                                    while (spinnerRunning.get()) {
                                        printSpinner(SPINNER_FRAMES[idx++ % SPINNER_FRAMES.length]);
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            System.out.print("\u001B[?25h");
                                            Thread.currentThread().interrupt();
                                            break;
                                        }
                                    }
                                    clearLine();
                                });
    }

    /**
     * Stops the spinner associated with a specific task. If the task label is empty, the entire
     * spinner is stopped. If no task is left, the entire spinner is stopped.
     *
     * @param taskLabel the label of the task to stop
     */
    public static void stopSpinnerTask(String taskLabel) {
        spinnerTasks.remove(taskLabel);
        if (taskLabel.isEmpty() || spinnerTasks.isEmpty()) {
            stopSpinner();
        }
    }

    /** Stops the spinner completely and clears the task list. */
    public static void stopSpinner() {
        spinnerRunning.set(false);
        spinnerTasks.clear();
        if (System.out.charset().name().equalsIgnoreCase("UTF-8")) {
            // Show the cursor again
            System.out.print("\u001B[?25h");
        }
        if (spinnerThread != null && spinnerThread.isAlive()) {
            try {
                spinnerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Spinner thread was interrupted while stopping", e);
            }
        }
    }

    /**
     * Checks whether the environment is interactive (i.e., suitable for spinner display).
     *
     * @return true if interactive, false otherwise
     */
    public static boolean isInteractive() {
        return isInteractive.get();
    }

    /**
     * Returns whether the first task is considered a meta task.
     *
     * @return true if first task is meta, false otherwise
     */
    public static boolean isFirstTaskIsMeta() {
        return firstTaskIsMeta;
    }

    /**
     * Sets whether the first task is considered a meta task. This will omit the "," after it.
     *
     * @param firstTaskIsMeta true if the first task is meta
     */
    public static void setFirstTaskIsMeta(boolean firstTaskIsMeta) {
        ProgressSpinner.firstTaskIsMeta = firstTaskIsMeta;
    }

    private static void initializeInteractiveMode() {
        Console console = System.console();
        String tty = System.getenv("TTY");
        boolean interactive = console != null || (tty != null && tty.equalsIgnoreCase("true"));
        isInteractive.set(interactive);

        if (System.out.charset().name().equalsIgnoreCase("UTF-8")) {
            SPINNER_FRAMES = SPINNER_FRAMES_UTF_8;
            // hide cursor
            System.out.print("\u001B[?25l");
        } else {
            SPINNER_FRAMES = SPINNER_FRAMES_DOS;
        }

        // Register shutdown hook to ensure cursor is restored
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    if (System.out.charset().name().equalsIgnoreCase("UTF-8")) {
                                        System.out.print("\u001B[?25h"); // Show cursor
                                    }
                                }));
    }

    private static void printSpinner(String frame) {
        String displayText;
        if (spinnerTasks.isEmpty()) {
            displayText = "";
        } else if (firstTaskIsMeta && spinnerTasks.size() > 1) {
            displayText =
                    spinnerTasks.getFirst()
                            + " "
                            + String.join(", ", spinnerTasks.subList(1, spinnerTasks.size()));
        } else {
            displayText = String.join(", ", spinnerTasks);
        }
        if (displayText.length() > 74) {
            displayText = displayText.substring(0, 74) + "…";
        }
        System.out.printf("  %-78s\r", frame + " " + displayText);
    }

    private static void clearLine() {
        System.out.print("\r" + " ".repeat(82) + "\r");
    }
}
