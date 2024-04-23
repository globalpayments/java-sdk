package com.global.api.terminals.upa.subgroups;

import lombok.Getter;
import lombok.Setter;

public class PrintData {
    /**
     * The bitmap must be converted in a base64 encoded data
     */
    @Getter
    @Setter
    private String content;

    /**
     * Message to be displayed in Line 1. If blank, Line 1 will be displayed as a blank line.
     */
    @Getter
    @Setter
    private String line1;

    /**
     * Message to be displayed in Line 2. If not present or blank, Line 2 will be displayed as a
     * blank line.
     */
    @Getter
    @Setter
    private String line2;

    /**
     * Display change after exiting the screen currently displayed (e.g. timeout or cancel). If
     * this parameter is not passed, the settings in the ReturnDefaultScreen command will be
     * followed.
     * Possible Values:
     * 0 = No Screen Change,
     * 1 = Return to Idle Screen
     * NOTES:
     * ● This is applicable to Fully Integrated mode only.
     * ● This will override the settings in the ReturnDefaultScreen command.
     * ● In Semi-Integrated mode, the application will go back to idle after a command is
     * processed.
     */
    @Getter
    @Setter
    private String displayOption;
}
