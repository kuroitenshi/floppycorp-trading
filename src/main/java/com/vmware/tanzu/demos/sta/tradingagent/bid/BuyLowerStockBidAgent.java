package com.vmware.tanzu.demos.sta.tradingagent.bid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@ConditionalOnProperty(name = "app.agent.strategy", havingValue = "buy-lower-stock")
class BuyLowerStockBidAgent implements BidAgent {
    private final Logger logger = LoggerFactory.getLogger(BuyLowerStockBidAgent.class);


    /**
     * 1st and 2nd data will store = we do nothing
     *
     *
     *
     */
    private List<History> histories = new ArrayList<>();

    private List<History> movingAverageList = new ArrayList<>();

    List<Map<String, BigDecimal>> listOfCalculatedMovingAverages = new ArrayList<>();

    private Long time;

    private int lastPeriod = 4;

    private int firstPeriod = 0;


    @Override
    public List<BidAgentRequest> execute(Context ctx) {
        // Sort input stocks against price.
        final List<Stock> sortedStocks = new ArrayList<>(ctx.stocks());
        sortedStocks.sort(Comparator.comparing(Stock::price));

        var history = new History(time++, sortedStocks);
        histories.add(history);

        if(histories.size() >=3 ){
            var lastPeriodStockList = histories.get(lastPeriod);

            //adding
            for(var h : histories){
                Map<String, BigDecimal> movingAverage = new HashMap<>();
                var stockList = h.stockList();
                for(var stock: stockList){
                    if(movingAverage.get(stock.symbol()) != null){
                        var totPrice = movingAverage.get(stock.symbol());
                        totPrice.add(stock.price());
                        totPrice = totPrice.divide(new BigDecimal(3));
                        movingAverage.put(stock.symbol(), totPrice);
                        listOfCalculatedMovingAverages.add(movingAverage);

                        //then remove the last data
                        histories.remove(0);
                    }

                }

            }
        }

        if(listOfCalculatedMovingAverages.size() == 2){
            var firstMA = listOfCalculatedMovingAverages.get(0);
            var secondMA = listOfCalculatedMovingAverages.get(1);

            List<BidAgentRequest> toBuy = new ArrayList<>();
            for(var secondMaStock : secondMA.entrySet()){
                var firstMaStock =  firstMA.get(secondMaStock.getKey());

                var diff = secondMaStock.getValue().subtract(firstMaStock);
                if(diff.compareTo(BigDecimal.ZERO) > 0){
                    toBuy.add(new BidAgentRequest(secondMaStock.getKey(), 1000));
                }
            }
            return toBuy;
        }


        //removing


        final Stock lowerStock = sortedStocks.get(0);
        logger.info("Found a stock with the lower value: {}", lowerStock.symbol());
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "BUY_LOWER_STOCK";
    }

    private BigDecimal calculateMovingAverage(List<Stock> stocklist){
        BigDecimal total = new BigDecimal(0);
        for(var stock: stocklist) {
            var price =  stock.price();
            total.add(price);
        }

        total.divide(new BigDecimal(3));
        return total;
    }
}
