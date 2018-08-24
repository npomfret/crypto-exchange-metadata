package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class BitZParser implements Parser {
    @Override
    public String exchangeId() {
        return "bitz";
    }

    public static Parser create() {
        return new BitZParser();
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        depositFees.addDefaultFee(Fee.ZERO_FIXED);

        Source source = readWebpage("https://www.bit-z.com/fee?welcome=");
        List<Element> tables = source.getAllElements("table");

        for (Element row : tables.get(0).getFirstElement("tbody").getAllElements("tr")) {
            List<Element> cells = row.getAllElements("td");
            String ccy = cells.get(0).getTextExtractor().toString();
            String fee = cells.get(1).getTextExtractor().toString().split("\\s+")[0];
            withdrawalFees.addFee(ccy, Fee.parse(fee));
        }

        for (Element row : tables.get(1).getFirstElement("tbody").getAllElements("tr")) {
            List<Element> cells = row.getAllElements("td");
            String market = cells.get(0).getTextExtractor().toString();
            String buyFee = cells.get(1).getTextExtractor().toString().split("\\s+")[0];
            String sellFee = cells.get(2).getTextExtractor().toString().split("\\s+")[0];

            //todo: support buy fee v's sell fee
            // doesn't matter right now because they're all the same
            if(!buyFee.equals(sellFee))
                throw new IllegalStateException(row.toString());

            Fee fee = Fee.parse(buyFee);
            tradingFees.addFee(market, fee);
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
