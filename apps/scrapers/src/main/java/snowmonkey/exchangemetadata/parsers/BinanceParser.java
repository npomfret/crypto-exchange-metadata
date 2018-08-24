package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class BinanceParser implements Parser{
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
            if(!label.equals("Level"))
                continue;

            for (Element row : table.getFirstElement("tbody").getAllElements("tr")) {
                System.out.println(row.getAllElements("td"));
            }

        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
