package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class EthfinexParser implements Parser {
    public EthfinexParser() {
    }

    public static Parser create() {
        return new EthfinexParser();
    }

    @Override
    public String exchangeId() {
        return "ethfinex";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        Source source = readWebpage("https://www.ethfinex.com/fees");
        List<Element> tables = source.getAllElements("table");

        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        {
            Element tradingFeesTable = tables.get(0);
            for (Element row : tradingFeesTable.getFirstElement("tbody").getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");
                String label = cells.get(0).getTextExtractor().toString();
                String makerFeeDescrption = cells.get(1).getTextExtractor().toString();
                String takerFeeDescrption = cells.get(2).getTextExtractor().toString();
                Fee makerFee = Fee.parse(makerFeeDescrption.replace("minus loyalty fee rebate (variable)", ""));
                Fee takerFee = Fee.parse(takerFeeDescrption);
                tradingFees.addDefaultFeeScheduleItem(label, takerFee, makerFee);
            }
        }

        {
            Element depositFeesTable = tables.get(1);
            for (Element row : depositFeesTable.getFirstElement("tbody").getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");

                String currencyName = cells.get(0).getTextExtractor().toString();
                String feeText = cells.get(1).getTextExtractor().toString();
                Fee fee;
                if (feeText.equals("FREE"))
                    fee = Fee.ZERO_FIXED;
                else
                    fee = Fee.parse(feeText);

                String smallFeeText = cells.get(2).getTextExtractor().toString();
                String ccy;
                if (smallFeeText.equals("FREE"))
                    ccy = currencyName;//todo: map this to a symbol
                else
                    ccy = smallFeeText.split(" ")[1];

                depositFees.addFee(ccy, fee);
            }
        }

        {
            Element withdrawalFeesTable = tables.get(2);
            for (Element row : withdrawalFeesTable.getFirstElement("tbody").getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");

                String feeText = cells.get(1).getTextExtractor().toString();
                Fee fee;
                if (feeText.equals("FREE"))
                    fee = Fee.ZERO_FIXED;
                else
                    fee = Fee.parse(feeText);

                String ccy = feeText.split(" ")[1];

                withdrawalFees.addFee(ccy, fee);
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
