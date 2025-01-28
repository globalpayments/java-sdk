package com.global.api.terminals.upa.subgroups;

import com.global.api.terminals.upa.Entities.Enums.UpaSafReportDataType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpaSafReportParams {
    private UpaSafReportDataType dataType = UpaSafReportDataType.PRINT;
    private boolean isBackgroundTask = false;
}
