package snowmonkey.exchangemetadata.parsers;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.util.HashMap;
import java.util.List;

public class BitflyerParser implements Parser {
    @Override
    public String exchangeId() {
        return "bitflyer";
    }

    private enum Mode {
        GENERAL_FEES, TRADING_DISCOUNTS, JPY_WITHDRAWALS, JPY_TRANSFERS
    }

    public static Parser create() {
        return new BitflyerParser();
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        TradingFees tradingFees = new TradingFees();
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();

        Source source = readWebpage("https://bitflyer.com/en-jp/commission");
        //todo: https://bitflyer.com/en-us/commission

        Mode mode = null;

        for (Element element : source.getElementById("Fees").getParentElement().getChildElements()) {
            if (element.getName().equals("h3")) {
                String heading = element.getTextExtractor().toString().trim();

                if (heading.equals("Fees"))
                    mode = Mode.GENERAL_FEES;
                else if (heading.startsWith("Bitcoin Trading Fee"))
                    mode = Mode.TRADING_DISCOUNTS;
                else if (heading.startsWith("Japanese Yen Withdrawal Fee"))
                    mode = Mode.JPY_WITHDRAWALS;
                else if (heading.startsWith("Real-time Transfer Fee"))
                    mode = Mode.JPY_TRANSFERS;
                else
                    mode = null;
            } else if (element.getName().equals("table")) {
                if (Mode.GENERAL_FEES.equals(mode)) {
                    handleTable1(tradingFees, withdrawalFees, element);
                } else if (Mode.TRADING_DISCOUNTS.equals(mode)) {
                    handleTable2(tradingFees, element);
                } else if (Mode.JPY_WITHDRAWALS.equals(mode)) {
                    handleTable3(withdrawalFees, element);
                } else if (Mode.JPY_TRANSFERS.equals(mode)) {
                    String labelCol2 = null;
                    String labelCol3 = null;

                    for (Element row : element.getAllElements("tr")) {
                        List<Element> cells = row.getChildElements();
                        System.out.println(cells);

                        String cell1 = cells.get(0).getTextExtractor().toString().trim();
                        String cell2 = cells.get(1).getTextExtractor().toString().trim();
                        if (cell1.equals("")) {
                            // header row
                            labelCol2 = cell2;
                            labelCol3 = cells.get(2).getTextExtractor().toString().trim();
                        } else {
                            {
                                String[] parts = cell2.split("\\s+");
                                if(cell1.equals("Withdrawal"))
                                    withdrawalFees.addFee(parts[1], "Real-time Transfer " + labelCol2, Fee.parse(parts[0]));
                                else
                                    depositFees.addFee(parts[1], "Real-time Transfer " + labelCol2, Fee.parse(parts[0]));
                            }

                            if(cells.size() > 2) {
                                String cell3 = cells.get(2).getTextExtractor().toString().trim();

                                {
                                    String[] parts = cell3.split("\\s+");
                                    if(cell1.equals("Withdrawal"))
                                        withdrawalFees.addFee(parts[1], "Real-time Transfer " + labelCol3, Fee.parse(parts[0]));
                                    else
                                        depositFees.addFee(parts[1], "Real-time Transfer " + labelCol3, Fee.parse(parts[0]));
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }

    private void handleTable3(TransferFees withdrawalFees, Element element) {
        String labelCol2 = null;
        String labelCol3 = null;

        for (Element row : element.getAllElements("tr")) {
            List<Element> cells = row.getChildElements();
            String cell1 = cells.get(0).getTextExtractor().toString().trim();
            String cell2 = cells.get(1).getTextExtractor().toString().trim();
            String cell3 = cells.get(2).getTextExtractor().toString().trim();

            if (cell1.equals("")) {
                // header row
                labelCol2 = cell2;
                labelCol3 = cell3;
            } else {
                {
                    String[] parts = cell2.split("\\s+");
                    withdrawalFees.addFee(parts[1], cell1 + " " + labelCol2, Fee.parse(parts[0]));
                }
                {
                    String[] parts = cell3.split("\\s+");
                    withdrawalFees.addFee(parts[1], cell1 + " " + labelCol3, Fee.parse(parts[0]));
                }
            }
        }
    }

    private void handleTable2(TradingFees tradingFees, Element element) {
        for (Element row : element.getAllElements("tr")) {
            List<Element> cells = row.getChildElements();

            if (cells.get(0).getName().equals("th")) {
                // ignore for now
            } else {
                String label = cells.get(0).getTextExtractor().toString().trim();
                String feeText = cells.get(1).getTextExtractor().toString().trim();
                Fee fee = Fee.parse(feeText);
                tradingFees.addDefaultFeeScheduleItem(label, fee, fee);
            }
        }
    }

    private void handleTable1(TradingFees tradingFees, TransferFees withdrawalFees, Element element) {
        String cell3 = null;
        String cell1 = null;

        for (Element row : element.getAllElements("tr")) {
            List<Element> header = row.getAllElements("th");
            List<Element> cells = row.getAllElements("td");

            if (header.size() > 0)
                cell1 = header.get(0).getTextExtractor().toString().trim();

            String cell2 = cells.get(0).getTextExtractor().toString().trim();
            if (cells.size() > 1)
                cell3 = cells.get(1).getTextExtractor().toString().trim();

            if (cell1.equals("Altcoin Trading Fee") && cell2.equals("Lightning Spot (ETH/BTC)")) {
                tradingFees.addFee("ETH/BTC", Fee.parse(cell3));
            } else if (cell1.equals("Altcoin Trading Fee") && cell2.equals("Lightning Spot (BCH/BTC)")) {
                tradingFees.addFee("BCH/BTC", Fee.parse(cell3));
            } else if (cell1.equals("Altcoin Trading Fee") && cell2.equals("Altcoin Market")) {
                if (cell3.contains("FREE")) {
                    String nonBtcMarkets = "^((?!BTC).)*$";
                    tradingFees.addFee(nonBtcMarkets, Fee.ZERO_FIXED, Fee.ZERO_FIXED);
                } else
                    throw new IllegalStateException("page has changed");
            } else if (cell1.equals("Bitcoin Transaction Fee")) {
                String[] parts = cell2.split(" ");
                Fee fee = Fee.parse(parts[0]);
                withdrawalFees.addFee(parts[1], fee);
            } else if (cell1.equals("Altcoin Transaction Fee")) {
                String ccy;
                Fee fee;
                if (cell3.equals("FREE")) {
                    fee = Fee.ZERO_FIXED;
                    ccy = cell2;//todo: translate this
                } else {
                    String[] parts = cell3.split(" ");
                    ccy = parts[1];
                    fee = Fee.parse(parts[0]);
                }

                withdrawalFees.addFee(ccy, fee);
            }

            //todo: figure out what can be transacted with bitwire
        }
    }
}
