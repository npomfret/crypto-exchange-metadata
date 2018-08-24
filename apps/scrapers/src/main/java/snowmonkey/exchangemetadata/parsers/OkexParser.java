package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class OkexParser implements Parser {
    public OkexParser() {
    }

    public static Parser create() {
        return new OkexParser();
    }

    @Override
    public String exchangeId() {
        return "okex";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        TradingFees tradingFees = new TradingFees();

        // https://support.okex.com/hc/en-us/articles/360000141391-Service-Fees
        Source source = readWebpage("https://www.okex.com/pages/products/fees.html");

        List<Element> tables = source.getElementById("feeMainContent").getAllElements("table");
        Element feeTable = tables.get(0);
        for (Element row : feeTable.getFirstElement("tbody").getAllElements("tr")) {
            List<Element> cells = row.getAllElements("td");
            String tierText = cells.get(0).getTextExtractor().toString().trim();
            String volumeText = cells.get(1).getTextExtractor().toString().trim();
            String makerFeeText = cells.get(2).getTextExtractor().toString().trim();
            String takerFeeText = cells.get(3).getTextExtractor().toString().trim();
            String label = tierText + ": 30day Trading Volume (BTC) " + volumeText;
            tradingFees.addDefaultFeeScheduleItem(label, Fee.parse(takerFeeText), Fee.parse(makerFeeText));

            //todo: Market maker program rebate
        }

        //todo
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }

}
