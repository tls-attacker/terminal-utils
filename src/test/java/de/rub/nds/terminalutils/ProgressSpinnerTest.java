/*
 * Terminal Utils
 *
 * Copyright 2022-2025 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.terminalutils;

import org.junit.jupiter.api.*;

class ProgressSpinnerTest {

    @Test
    void spinnerNotInteractive() {
        boolean isInteractive =
                System.getenv("TTY") != null && System.getenv("TTY").equalsIgnoreCase("true");
        Assertions.assertEquals(isInteractive, ProgressSpinner.isInteractive());
    }
}
