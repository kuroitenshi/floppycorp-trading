package com.vmware.tanzu.demos.sta.tradingagent.bid;

import java.math.BigDecimal;

public record MovingAverage(
        String symbol,

        BigDecimal total

) {
}
