package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;

public class ExmoParser {

    public static ExchangeMetadata run() throws Exception {
        TradingFees tradingFees = new TradingFees();
        String tradingFee = WebPageParser.parseTradingFee();
        tradingFees.addDefaultFee(parseFee(tradingFee).get());

        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        JsonObject jsonObject = BitsAndBobs.readJson("https://exmo.com/ctrl/feesAndLimits");
        JsonArray asJsonArray = jsonObject.get("data").getAsJsonObject().get("fees").getAsJsonArray();
        for (JsonElement jsonElement : asJsonArray) {
            JsonObject obj = jsonElement.getAsJsonObject();
            String group = obj.get("group").getAsJsonPrimitive().getAsString();

            if (group.equals("crypto")) {
                for (JsonElement item : obj.get("items").getAsJsonArray()) {
                    String ccy = item.getAsJsonObject().get("prov").getAsJsonPrimitive().getAsString();
                    String depositFee = item.getAsJsonObject().get("dep").getAsJsonPrimitive().getAsString();
                    String withdrawalFee = item.getAsJsonObject().get("wd").getAsJsonPrimitive().getAsString();

                    parseFee(depositFee).ifPresent(fee -> depositFees.addFee(ccy, fee));
                    parseFee(withdrawalFee).ifPresent(fee -> withdrawalFees.addFee(ccy, fee));
                }
            } else {
                String ccy = obj.get("title").getAsJsonPrimitive().getAsString();

                for (JsonElement item : obj.get("items").getAsJsonArray()) {
                    String mechanism = item.getAsJsonObject().get("prov").getAsJsonPrimitive().getAsString();
                    String depositFee = item.getAsJsonObject().get("dep").getAsJsonPrimitive().getAsString();
                    String withdrawalFee = item.getAsJsonObject().get("wd").getAsJsonPrimitive().getAsString();

                    parseFee(depositFee).ifPresent(fee -> depositFees.addFee(ccy, mechanism, fee));
                    parseFee(withdrawalFee).ifPresent(fee -> withdrawalFees.addFee(ccy, mechanism, fee));
                }
            }

        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }

    private static Optional<Fee> parseFee(String feeText) {
        if (feeText.equals("-"))
            return Optional.empty();

        if (feeText.equals("0%"))
            feeText = "0";

        return Optional.of(Fee.parse(feeText));
    }

    private static class WebPageParser {

        enum Mode {
            TRADING_FEE
        }

        public static String parseTradingFee() throws InterruptedException, IOException, URISyntaxException {
            Source source = BitsAndBobs.getPage("https://exmo.com/en/docs/fees");

            Mode mode = null;

            for (Element row : source.getAllElements("tr")) {
                if (row.toString().contains("Payment Method")) {
                    mode = Mode.TRADING_FEE;
                    continue;
                }

                if (mode == Mode.TRADING_FEE) {
                    return row.getAllElements("td").get(1).getContent().toString();
                }
            }

            throw new IllegalStateException("Should not get here");
        }
    }

}
