package com.global.api.terminals.enums;

import com.global.api.entities.enums.IFlag;

import java.util.EnumSet;
import java.util.Set;

public enum TerminalReportType implements IFlag {
    LocalDetailReport,
    GetBatchReport;

    @Override
    public long getLongValue() {
        return 1L << this.ordinal();
    }

    public static Set<TerminalReportType> getSet(long value) {
        EnumSet<TerminalReportType> flags = EnumSet.noneOf(TerminalReportType.class);
        for(TerminalReportType flag : TerminalReportType.values()) {
            long flagValue = flag.getLongValue();
            if((flagValue & value) == flagValue)
                flags.add(flag);
        }
        return flags;
    }

}
