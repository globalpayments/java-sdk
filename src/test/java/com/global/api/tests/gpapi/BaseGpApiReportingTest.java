package com.global.api.tests.gpapi;

import org.joda.time.LocalDate;

import java.util.Date;

public class BaseGpApiReportingTest extends BaseGpApiTest {

    protected static final Date REPORTING_START_DATE = LocalDate.now().minusMonths(6).toDate();
    protected static final Date REPORTING_END_DATE = LocalDate.now().toDate();
    protected static final Date REPORTING_LAST_MONTH_DATE = LocalDate.now().minusMonths(1).toDate();

    protected static final int FIRST_PAGE = 1;
    protected static final int PAGE_SIZE = 10;

}