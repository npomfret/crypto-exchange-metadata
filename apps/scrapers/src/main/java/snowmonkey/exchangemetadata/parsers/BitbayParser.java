package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class BitbayParser implements Parser {
    @Override
    public String exchangeId() {
        return "bitbay";
    }

    public static Parser create() {
        return new BitbayParser();
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        Source source = readWebpage("https://bitbay.net/en/fees");
        List<Element> tables = source.getAllElements("table");

        {
            for (Element row : tables.get(0).getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");

                if(cells.isEmpty())
                    continue;

                String market = cells.get(0).getTextExtractor().toString().trim().replace(" - ", "/");
                String maker = cells.get(1).getTextExtractor().toString().trim();
                String taker = cells.get(2).getTextExtractor().toString().trim();

                tradingFees.addFee(market, Fee.parse(taker), Fee.parse(maker));
            }
        }

        {
            for (Element row : tables.get(1).getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");

                if (cells.isEmpty())
                    continue;

                String taker = cells.get(0).getTextExtractor().toString().trim();
                String label = cells.get(1).getTextExtractor().toString().trim();
                String maker = cells.get(2).getTextExtractor().toString().trim();
                tradingFees.addDefaultFeeScheduleItem("30 DAY VOLUME IN ANY CURRENCY " + label, Fee.parse(taker), Fee.parse(maker));
            }
        }

        {
            Element deposits = source.getElementById("deposits");

            //can't parse these yet
            depositFees.addFee("EUR", Fee.ZERO_FIXED);
            depositFees.addFee("USD", Fee.ZERO_FIXED);
            depositFees.addFee("PLN", Fee.ZERO_FIXED);

            for (Element item : deposits.getFirstElement("ul").getAllElements("li")) {
                String feeText = item.getTextExtractor().toString().trim();

                String[] parts = feeText.split("\\s+");
                depositFees.addFee(parts[1], Fee.parse(parts[0]));
            }
        }

        {
            Element withdrawals = source.getElementById("withdrawals");

            //can't parse these yet
            withdrawalFees.addFee("PLN", "Wire bank transfer", Fee.parse("4"));
            withdrawalFees.addFee("PLN", "ATM withdrawal (100 - 1000 PLN)", Fee.parse("10"));
            withdrawalFees.addFee("PLN", "ATM withdrawal (2000 PLN)", Fee.parse("20"));
            withdrawalFees.addFee("EUR", "SEPA bank transfer", Fee.parse("4"));
            withdrawalFees.addFee("USD", "SWIFT bank transfer", Fee.parse("0.25%"));

            for (Element item : withdrawals.getFirstElement("ul").getAllElements("li")) {
                String feeText = item.getTextExtractor().toString().trim();

                String[] parts = feeText.split(": ");
                withdrawalFees.addFee(parts[0], Fee.parse(parts[1]));
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
