package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.BitsAndBobs;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class CoinFalconParser {
    public static void main(String[] args) throws Exception {
        ExchangeMetadata exchangeMetadata = run();

        System.out.println(BitsAndBobs.prettyPrint(exchangeMetadata.toJson()));
    }

    public static ExchangeMetadata run() throws URISyntaxException, IOException, InterruptedException {
        Source source = BitsAndBobs.getPage("https://coinfalcon.com/fees");
        List<Element> tables = source.getAllElements("table");

        TradingFees tradingFees = new TradingFees();

        {
            String labelTemplate = "User 30 day volume in BTC";

            Fee makerFee = Fee.parse("0");//assume all maker fees are zero

            Element tradingFeesTable = tables.get(0);
            for (Element row : tradingFeesTable.getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");
                String requiredVolume = cells.get(0).getContent().toString()
                        .replace("&lt;", "<")
                        .replace("&gt;", ">");
                String takerFee = cells.get(1).getContent().toString();
                if (cells.size() >= 3) {
                    String makerFeeText = cells.get(2).getContent().toString();
                    if (!makerFeeText.equals("Free"))
                        throw new IllegalStateException("maker fee is assumed to be zero, not: " + makerFeeText);
                }

                String label = labelTemplate + " " + requiredVolume;
                tradingFees.addDefaultFeeScheduleItem(label, Fee.parse(takerFee), makerFee);
            }
        }

        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        {
            Fee depositFee = null;

            for (Element row : tables.get(1).getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");
                String mechanism = cells.get(0).getTextExtractor().toString();

                String withdrawalFeeText = cells.get(1).getTextExtractor().toString();
                if(cells.size() >= 3) {
                    String depositFeeText = cells.get(2).getTextExtractor().toString();
                    if(depositFeeText.equals("Free"))
                        depositFee = Fee.parse("0");
                    else
                        depositFee = Fee.parse(depositFeeText);
                }

                if(mechanism.equals("Ethereum Tokens"))
                    continue;//todo: model the "Suggested network fee x 2.3" somehow

                String ccy = withdrawalFeeText.split(" ")[1];

                if(ccy.equals("€"))
                    ccy = "EUR";

                depositFees.addFee(ccy, depositFee);
                withdrawalFees.addFee(ccy, Fee.parse(withdrawalFeeText));
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
