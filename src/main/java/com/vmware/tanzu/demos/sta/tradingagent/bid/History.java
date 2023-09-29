package com.vmware.tanzu.demos.sta.tradingagent.bid;

import java.util.List;

public record History (
        Long id,

        List<Stock> stockList){

}
