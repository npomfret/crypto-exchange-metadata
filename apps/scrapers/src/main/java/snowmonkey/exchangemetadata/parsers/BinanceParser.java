package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class BinanceParser implements Parser {
    @Override
    public String exchangeId() {
        return "binance";
    }

    public static Parser create() {
        return new BinanceParser();
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        Source source = readWebpage("https://www.binance.com/en/fee/schedule");
        for (Element table : source.getAllElements("table")) {
            Element firstCell = table.getFirstElement("thead").getFirstElement("tr").getFirstElement("th");
            String label = firstCell.getTextExtractor().toString();

            if (label.equals("Level")) {
                for (Element row : table.getFirstElement("tbody").getAllElements("tr")) {
                    List<Element> cells = row.getAllElements("td");

                    String level = cells.get(0).getTextExtractor().toString();
                    String volume = cells.get(1).getTextExtractor().toString();
                    String andOr = cells.get(2).getTextExtractor().toString();
                    String bnbRequirement = cells.get(3).getTextExtractor().toString();

                    String maker1 = cells.get(4).getTextExtractor().toString();
                    String taker1 = cells.get(5).getTextExtractor().toString();

                    String maker2 = cells.get(6).getTextExtractor().toString();
                    String taker2 = cells.get(7).getTextExtractor().toString();

                    tradingFees.addDefaultFeeScheduleItem(String.join(" ", level, volume), Fee.parse(taker1), Fee.parse(maker1));
                    tradingFees.addDefaultFeeScheduleItem(String.join(" ", level, volume, andOr, bnbRequirement), Fee.parse(taker2), Fee.parse(maker2));
                }
            } else if (label.equals("Coin / Token")) {
                for (Element row : table.getFirstElement("tbody").getAllElements("tr")) {
                    List<Element> cells = row.getAllElements("td");

                    String desc = cells.get(3).getTextExtractor().toString();
                    String[] parts = desc.split("\\s+");
                    Fee fee = Fee.parse(parts[0]);
                    String ccy = parts[1];
                    withdrawalFees.addFee(ccy, fee);
                }
            }

        }

        depositFees.addDefaultFee(Fee.ZERO_FIXED);

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
