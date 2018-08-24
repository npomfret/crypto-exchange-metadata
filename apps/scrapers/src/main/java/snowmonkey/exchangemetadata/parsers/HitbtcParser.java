package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import snowmonkey.exchangemetadata.BitsAndBobs;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.SymbolMapping;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.net.URI;
import java.util.HashMap;

public class HitbtcParser implements Parser {
    public HitbtcParser() {
    }

    public static Parser create() {
        return new HitbtcParser();
    }

    @Override
    public String exchangeId() {
        return "hitbtc2";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata(SymbolMapping symbolMapping) throws Exception {
        TradingFees tradingFees = new TradingFees();
        tradingFees.addDefaultFee(Fee.parse("0.1%"), Fee.parse("-0.01%"));//https://hitbtc.com/fees-and-limits

        TransferFees depositFees = new TransferFees();
        depositFees.addDefaultFee(Fee.ZERO_FIXED);
        depositFees.addFee("BTC", Fee.parse("0.0006"));// hardcoded because can't find in an api call
        depositFees.addFee("ETH", Fee.parse("0.003"));// hardcoded because can't find in an api call

        TransferFees withdrawalFees = new TransferFees();
        for (JsonElement jsonElement : this.readJson("https://api.hitbtc.com/api/2/public/currency").getAsJsonArray()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            String ccy = obj.getAsJsonPrimitive("id").getAsString();
            boolean delisted = obj.getAsJsonPrimitive("delisted").getAsBoolean();
            boolean payinEnabled = obj.getAsJsonPrimitive("payinEnabled").getAsBoolean();
            boolean payoutEnabled = obj.getAsJsonPrimitive("payoutEnabled").getAsBoolean();

            if (delisted || !payinEnabled)
                depositFees.addFee(ccy, Fee.NOT_AVAILABLE);

            if (delisted || !payoutEnabled)
                withdrawalFees.addFee(ccy, Fee.NOT_AVAILABLE);
            else
                withdrawalFees.addFee(ccy, Fee.parse(obj.getAsJsonPrimitive("payoutFee").getAsString()));
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
