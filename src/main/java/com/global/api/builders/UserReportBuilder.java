package com.global.api.builders;

import com.global.api.entities.enums.*;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.SearchCriteriaBuilder;
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
    public MerchantAccountsSortProperty accountOrderBy;

    public TransactionType transactionType;

    public SearchCriteriaBuilder<TResult> _searchBuilder;

    public TransactionModifier transactionModifier = TransactionModifier.None;

    public UserReportBuilder(ReportType type, Class<TResult> clazz) {
        super(type, clazz);
    }

    public SearchCriteriaBuilder<TResult> getSearchBuilder() {
        if (_searchBuilder == null) {
            _searchBuilder = new SearchCriteriaBuilder<TResult>(this);
        }
        return _searchBuilder;
    }

    @Override
    public void setupValidations() {

    }

    public UserReportBuilder<TResult> orderBy(MerchantAccountsSortProperty orderBy, SortDirection direction) {
        accountOrderBy = orderBy;
        order = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public UserReportBuilder<TResult> withModifier(TransactionModifier transactionModifier) {
        this.transactionModifier = transactionModifier;
        return this;
    }

    public UserReportBuilder<TResult> withAccountId(String accountId) {
        this.getSearchBuilder().setAccountId(accountId);
        return this;
    }

    public <T> SearchCriteriaBuilder<TResult> where(SearchCriteria criteria, T value) {
        return getSearchBuilder().and(criteria, value);
    }

    public UserReportBuilder<TResult> withPaging(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

}