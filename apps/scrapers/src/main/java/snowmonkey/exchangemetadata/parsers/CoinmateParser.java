package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.BitsAndBobs;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CoinmateParser {

    public static void main(String[] args) throws Exception {
        ExchangeMetadata metadata = run();
        System.out.println(BitsAndBobs.prettyPrint(metadata.toJson()));
    }

    public static ExchangeMetadata run() throws URISyntaxException, IOException, InterruptedException {
        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        Source page = BitsAndBobs.getPage("https://coinmate.io/fees");

        Path path = Paths.get("src/main/resources/coinmate/tradeFeeValues.json");//downloaded via browser

        JsonArray fees = BitsAndBobs.readJsonArray(path);

        {
            String volume = null;
            for (JsonElement feeZone : fees.get(1).getAsJsonObject().getAsJsonArray("feeZones")) {
                JsonObject obj = feeZone.getAsJsonObject();

                String makerFeeValue = obj.getAsJsonPrimitive("makerFeeValue").getAsString();
                String takerFeeValue = obj.getAsJsonPrimitive("takerFeeValue").getAsString();
                volume = obj.get("tradingVolume").isJsonNull() ? volume.replace('<', '>') : "< " + obj.getAsJsonPrimitive("tradingVolume").getAsString();

                tradingFees.addDefaultFeeScheduleItem("30 DAY TRADING VOLUME " + volume + " EUR", Fee.parse(takerFeeValue + "%"), Fee.parse(makerFeeValue + "%"));
            }
        }

        {
            String volume = null;
            JsonObject feeSection = fees.get(0).getAsJsonObject();

            List<String> markets = new ArrayList<>();
            for (JsonElement currencyPair : feeSection.getAsJsonArray("currencyPairs")) {
                markets.add(currencyPair.getAsJsonObject().getAsJsonPrimitive("name").getAsString());
            }
            String marketsPattern = toRegexp(markets);

            for (JsonElement feeZone : feeSection.getAsJsonArray("feeZones")) {
                JsonObject obj = feeZone.getAsJsonObject();

                String makerFeeValue = obj.getAsJsonPrimitive("makerFeeValue").getAsString();
                String takerFeeValue = obj.getAsJsonPrimitive("takerFeeValue").getAsString();
                volume = obj.get("tradingVolume").isJsonNull() ? volume.replace('<', '>') : "< " + obj.getAsJsonPrimitive("tradingVolume").getAsString();

                tradingFees.addFee(marketsPattern, Fee.parse(takerFeeValue + "%"), Fee.parse(makerFeeValue + "%"), "30 DAY TRADING VOLUME " + volume + " EUR");
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }

    private static String toRegexp(List<String> markets) {
        Optional<String> reduce = markets.stream().map(market -> "(^" + market + "$)").reduce((s, s2) -> s + "|" + s2);

        return reduce.get();
    }

}
