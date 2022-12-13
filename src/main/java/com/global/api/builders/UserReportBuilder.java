package com.global.api.builders;

import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class UserReportBuilder<TResult> extends ReportBuilder<TResult> {
    @Getter @Setter private int page = 1;       // 1: DEFAULT PARAM VALUE
    @Getter @Setter private int pageSize = 5;   // 5: DEFAULT PARAM VALUE

    public SortDirection order;

    public TransactionType transactionType;

    public TransactionModifier transactionModifier = TransactionModifier.None;

    public UserReportBuilder(ReportType type, Class<TResult> clazz) {
        super(type, clazz);
    }

    @Override
    public void setupValidations() {

    }

    public UserReportBuilder<TResult> withModifier(TransactionModifier transactionModifier) {
        this.transactionModifier = transactionModifier;
        return this;
    }

    public UserReportBuilder<TResult> withPaging(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

}