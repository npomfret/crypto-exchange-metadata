package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonObject;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.SymbolMapping;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

import static snowmonkey.exchangemetadata.BitsAndBobs.getPage;

public class YobitParser implements Parser {
    public YobitParser() {
    }

    public static Parser create() {
        return new YobitParser();
    }

    @Override
    public String exchangeId() {
        return "yobit";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata(SymbolMapping symbolMapping) throws Exception {
        Source source = getPage("https://www.yobit.net/en/fees/");

        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        {
            JsonObject response = this.readJson("https://yobit.net/api/3/info").getAsJsonObject();
            JsonObject pairs = response.getAsJsonObject("pairs");
            for (String market : pairs.keySet()) {
                JsonObject item = pairs.getAsJsonObject(market);
                Fee fee = Fee.parse(item.getAsJsonPrimitive("fee") + "%");
                String marketSymbol = market.toUpperCase().replace('_', '/');//todo: map these properly
                tradingFees.addFee(marketSymbol, fee, fee);
            }
        }

        Element table = source.getElementById("fees_table");
        for (Element row : table.getAllElements("tr")) {
            List<Element> cells = row.getAllElements("td");
            if (cells.size() < 4)
                continue;

            String mechanism = cells.get(0).getTextExtractor().toString();
            String depositFeeText = cells.get(2).getTextExtractor().toString().trim();
            String withdrawalFeeText = cells.get(3).getTextExtractor().toString().trim();

            if (mechanism.equals("Coins")) {
                depositFees.addDefaultFee(parseFee(depositFeeText));
                withdrawalFees.addDefaultFee(parseFee(withdrawalFeeText));
                continue;
            }

            {
                if (depositFeeText.length() > 0) {
                    String[] pairs = depositFeeText.split("\\+");
                    for (String pair : pairs) {
                        String[] parts = pair.trim().split("\\s+");
                        for (int i = 0; i < parts.length; i += 2) {
                            String value = parts[i].trim();
                            String ccy = parts[i + 1].trim();
                            depositFees.addFee(ccy, mechanism, parseFee(value));
                        }
                    }
                }
            }

            {
                if (withdrawalFeeText.length() > 0) {
                    String[] pairs = withdrawalFeeText.split("\\+");
                    for (String pair : pairs) {
                        String[] parts = pair.trim().split("\\s+");
                        for (int i = 0; i < parts.length; i += 2) {
                            String value = parts[i].trim();
                            String ccy = parts[i + 1].trim();
                            withdrawalFees.addFee(ccy, mechanism, parseFee(value));
                        }
                    }
                }
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }

    private static Fee parseFee(String text) {
        if (text.equals("Free"))
            return Fee.ZERO_FIXED;

        return Fee.parse(text);
    }
}
