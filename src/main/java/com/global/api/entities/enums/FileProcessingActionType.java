package com.global.api.entities.enums;

import java.util.EnumSet;
import java.util.Set;

public enum FileProcessingActionType implements IFlag {
    CREATE_UPLOAD_URL(1),

    GET_DETAILS(2);

    private int value;

    FileProcessingActionType(int value) {
        this.value = value;
    }

    @Override
    public long getLongValue() {
        return 1 << this.ordinal();
    }

    public static Set<FileProcessingActionType> getSet(long value) {
        EnumSet<FileProcessingActionType> flags = EnumSet.noneOf(FileProcessingActionType.class);
        for (FileProcessingActionType flag : FileProcessingActionType.values()) {
            long flagValue = flag.getLongValue();
            if ((flagValue & value) == flagValue)
                flags.add(flag);
        }
        return flags;
    }

}
