package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonElement;
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

public class GatecoinParser implements Parser {
    public GatecoinParser() {
    }

    public static Parser create() {
        return new GatecoinParser();
    }

    @Override
    public String exchangeId() {
        return "gatecoin";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata(SymbolMapping symbolMapping) throws Exception {
        Source source = getPage("https://gatecoin.com/feeschedule/");

        TradingFees tradingFees = new TradingFees();

        Element feesTable = source.getAllElements("table").get(0);
        for (Element row : feesTable.getFirstElement("tbody").getAllElements("tr")) {
            List<Element> cells = row.getAllElements("td");
            String label = cells.get(0).getTextExtractor().toString() + " rolling 31 day volume";
            String makerFeeText = cells.get(1).getTextExtractor().toString() + "%";
            String takerFeeText = cells.get(2).getTextExtractor().toString() + "%";
            tradingFees.addDefaultFeeScheduleItem(label, Fee.parse(takerFeeText), Fee.parse(makerFeeText));
        }

        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        JsonObject response = this.readJson("https://api.gatecoin.com/Reference/Currencies").getAsJsonObject();

        for (JsonElement currency : response.getAsJsonArray("currencies")) {
            String ccy = currency.getAsJsonObject().getAsJsonPrimitive("code").getAsString();
            boolean withdrawalsEnabled = currency.getAsJsonObject().getAsJsonPrimitive("withdrawalsEnabled").getAsBoolean();
            boolean depositsEnabled = currency.getAsJsonObject().getAsJsonPrimitive("depositsEnabled").getAsBoolean();
            String withdrawalFeeText = currency.getAsJsonObject().getAsJsonPrimitive("withdrawalFee").getAsString();

            {
                Fee fee;
                if (!withdrawalsEnabled) {
                    fee = Fee.NOT_AVAILABLE;
                } else {
                    fee = Fee.parse(withdrawalFeeText);
                }
                withdrawalFees.addFee(ccy, fee);
            }

            {
                Fee fee;
                if (!depositsEnabled) {
                    fee = Fee.NOT_AVAILABLE;
                } else {
                    fee = Fee.ZERO_FIXED;//assume no deposit fees
                }
                depositFees.addFee(ccy, fee);
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
