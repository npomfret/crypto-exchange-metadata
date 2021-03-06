package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.SymbolMapping;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class BitfinexParser implements Parser {
    public BitfinexParser() {
    }

    public static Parser create() {
        return new BitfinexParser();
    }

    @Override
    public String exchangeId() {
        return "bitfinex";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        Source source = readWebpage("https://www.bitfinex.com/fees");
        List<Element> tables = source.getAllElements("table");

        SymbolMapping symbolMapping = this.symbolMapping();

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


                if (currencyName.equals("Bank wire")) {
                    Fee fiatFee = Fee.parse(feeText.split(" ")[0]);//todo: check this is correct
                    depositFees.addFee("USD", fiatFee);
                    depositFees.addFee("GBP", fiatFee);
                    depositFees.addFee("JPY", fiatFee);
                    depositFees.addFee("EUR", fiatFee);
                } else {
                    depositFees.addFee(ccy, fee);
                }
            }
        }

        {
            Element withdrawalFeesTable = tables.get(2);
            for (Element row : withdrawalFeesTable.getFirstElement("tbody").getAllElements("tr")) {
                List<Element> cells = row.getAllElements("td");

                String currencyName = cells.get(0).getTextExtractor().toString().trim();
                String feeText = cells.get(1).getTextExtractor().toString();

                Fee fee;
                String ccy;
                if (feeText.equals("FREE")) {
                    fee = Fee.ZERO_FIXED;

                    ccy = currencyName;//todo: fix this (use the api)
                } else {
                    fee = Fee.parse(feeText);
                    ccy = feeText.split(" ")[1];
                }

                //todo: replace all this and get fees from private api: https://api.bitfinex.com/v:version/account_fees

                if (currencyName.equals("TetherUSD (Omni)"))
                    ccy = "TUSD";
                if (currencyName.equals("TetherUSE (Ethereum ERC20)"))
                    ccy = "TUSE";
                if (currencyName.equals("TetherEUE (Ethereum ERC20)"))
                    ccy = "TEUE";

                if (currencyName.equals("Bank wire") || currencyName.startsWith("Express bank wire")) {
                    Fee fiatFee = Fee.parse(feeText.split(" ")[0]);//todo: check this is correct

                    withdrawalFees.addFee("USD", fiatFee);
                    withdrawalFees.addFee("GBP", fiatFee);
                    withdrawalFees.addFee("JPY", fiatFee);
                    withdrawalFees.addFee("EUR", fiatFee);
                } else {
                    withdrawalFees.addFee(ccy, fee);
                }
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }
}
