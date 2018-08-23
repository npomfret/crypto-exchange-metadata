package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import snowmonkey.exchangemetadata.BitsAndBobs;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;

import static snowmonkey.exchangemetadata.BitsAndBobs.getJson;

public class CryptopiaParser {
    public static void main(String[] args) throws Exception {
        ExchangeMetadata exchangeMetadata = run();

        System.out.println(BitsAndBobs.prettyPrint(exchangeMetadata.toJson()));
    }

    public static ExchangeMetadata run() throws Exception {

        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        depositFees.addDefaultFee(Fee.parse("0"));//can't find a deposit fee, assume its zero

        TransferFees withdrawalFees = new TransferFees();

        {
            JsonObject response = getJson(URI.create("https://www.cryptopia.co.nz/api/GetCurrencies"));
            for (JsonElement item : response.getAsJsonArray("Data")) {
                JsonObject obj = item.getAsJsonObject();
                String ccy = obj.getAsJsonPrimitive("Symbol").getAsString();
                BigDecimal withdrawFee = obj.getAsJsonPrimitive("WithdrawFee").getAsBigDecimal();
                withdrawalFees.addFee(ccy, Fee.parse(withdrawFee.stripTrailingZeros().toPlainString()));
            }
        }

        {
            JsonObject response = getJson(URI.create("https://www.cryptopia.co.nz/api/GetTradePairs"));
            for (JsonElement item : response.getAsJsonArray("Data")) {
                JsonObject obj = item.getAsJsonObject();
                String feeText = obj.getAsJsonPrimitive("TradeFee").getAsBigDecimal().stripTrailingZeros().toPlainString() + "%";
                String market = obj.getAsJsonPrimitive("Label").getAsString();
                Fee fee = Fee.parse(feeText);
                tradingFees.addFee(market, fee, fee);
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
