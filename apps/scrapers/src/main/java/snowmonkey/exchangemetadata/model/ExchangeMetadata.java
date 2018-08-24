package snowmonkey.exchangemetadata.model;

import com.google.gson.JsonObject;

import java.util.Map;

public class ExchangeMetadata {
    public final TradingFees tradingFees;
    public final TransferFees depositFees;
    public final TransferFees withdrawalFees;
    public final Map markets;

    public ExchangeMetadata(TradingFees tradingFees, TransferFees depositFees, TransferFees withdrawalFees, Map markets) {
        this.tradingFees = tradingFees;
        this.depositFees = depositFees;
        this.withdrawalFees = withdrawalFees;
        this.markets = markets;
    }

    public JsonObject toJson() {
        JsonObject all = new JsonObject();
        all.add("trading-fees", tradingFees.toJson());
        all.add("deposit-fees", depositFees.toJson());
        all.add("withdrawal-fees", withdrawalFees.toJson());
        all.add("markets", new JsonObject());
        return all;
    }

}
