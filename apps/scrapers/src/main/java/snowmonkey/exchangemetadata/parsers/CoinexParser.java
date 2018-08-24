package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class CoinexParser implements Parser {
    public CoinexParser() {
    }

    public static Parser create() {
        return new CoinexParser();
    }

    @Override
    public String exchangeId() {
        return "coinex";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        Source source = readWebpage("https://www.coinex.com/fees");

        TradingFees tradingFees = new TradingFees();
        List<Element> tables = source.getAllElements("table");
        {
            //todo: market by market fees are available in https://www.coinex.com/res/market/ ... but they're all the same
            Element feeRow = tables.get(0).getAllElements("tr").get(1);
            List<Element> cells = feeRow.getAllElements("td");
            String makerFee = cells.get(1).getContent().toString();
            String takerFee = cells.get(2).getContent().toString();

            tradingFees.addDefaultFee(Fee.parse(takerFee), Fee.parse(makerFee));
        }

        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        {
            Element transferFeesTable = tables.get(1);


            for (Element row : transferFeesTable.getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");
                if (cells.isEmpty())
                    continue;
                //colums: type,	Minimal Deposit,	Minimal Withdrawal,	Deposit Fee,	Withdrawal Fee
                String ccy = cells.get(0).getContent().toString();
                String depositFee = cells.get(3).getContent().toString();
                String withdrawalFee = cells.get(4).getContent().toString();

                depositFees.addFee(ccy, parseFee(depositFee));
                withdrawalFees.addFee(ccy, parseFee(withdrawalFee));
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }

    private static Fee parseFee(String feeText) {
        if (feeText.equals("FREE"))
            return Fee.parse("0");

        return Fee.parse(feeText);
    }

}
