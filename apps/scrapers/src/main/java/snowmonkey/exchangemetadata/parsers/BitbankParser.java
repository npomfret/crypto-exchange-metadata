package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class BitbankParser implements Parser {
    @Override
    public String exchangeId() {
        return "bitbank";
    }

    public static Parser create() {
        return new BitbankParser();
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        Source source = readWebpage("https://bitbank.cc/docs/fees/");
        List<Element> tables = source.getAllElements("table");

        Fee makerFee = null;
        Fee takerFee = null;

        Element tradingFeesTable = tables.get(0);
        for (Element row : tradingFeesTable.getAllElements("tr")) {
            List<Element> cells = row.getAllElements("td");

            String market = cells.get(0).getTextExtractor().toString().trim();
            if (market.length() == 0)
                continue;

            if (cells.size() > 1) {
                String maker = cells.get(1).getTextExtractor().toString()
                        .replaceAll("（.*", "")
                        .trim();

                if ("無料".equals(maker))
                    makerFee = Fee.ZERO_FIXED;
                else
                    makerFee = Fee.parse(maker);
            }

            if (cells.size() > 2) {
                String taker = cells.get(2).getTextExtractor().toString()
                        .replaceAll("（.*", "")
                        .trim();

                if ("無料".equals(taker))
                    takerFee = Fee.ZERO_FIXED;
                else
                    takerFee = Fee.parse(taker);
            }

            tradingFees.addFee(market, takerFee, makerFee);
        }

        Element transferFeesTable = tables.get(1);
        for (Element row : transferFeesTable.getAllElements("tr")) {
            List<Element> cells = row.getAllElements("td");

            String ccy = cells.get(0).getTextExtractor().toString()
                    .replaceAll("（.*", "")
                    .trim();

            if (ccy.equals(""))//heading row
                continue;

            if (ccy.equals("日本円"))
                ccy = "JPY";

            String depositFeeText = cells.get(1).getTextExtractor().toString().trim()
                    .replace("円", "JPY ")
                    .replace("万", "0000 ")
                    .replaceAll("（.*", "")
                    .replace("以上", "or more ")
                    .replace("無料", "0")
                    .trim();

            depositFees.addFee(ccy, Fee.parse(depositFeeText));

            String withdrawalFeeText = cells.get(2).getTextExtractor().toString().trim()
                    .replace("円", "JPY")
                    .replace("万", "0000")
                    .replaceAll("（.*", "")
                    .replace("以上", "or more")
                    .replace("無料", "0")
                    .trim();

            if (ccy.equals("JPY")) {
                withdrawalFees.addFee(ccy, "less than 30,000 yen", Fee.parse("540"));
                withdrawalFees.addFee(ccy, "over 30,000 yen", Fee.parse("756"));
            } else {
                withdrawalFees.addFee(ccy, Fee.parse(withdrawalFeeText));
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
