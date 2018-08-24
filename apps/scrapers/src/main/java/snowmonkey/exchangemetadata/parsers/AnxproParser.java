package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonObject;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.Fee;
import snowmonkey.exchangemetadata.model.TradingFees;
import snowmonkey.exchangemetadata.model.TransferFees;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

public class AnxproParser implements Parser {
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private JsonObject currencyStatic;
    private JsonObject allFees;

    private AnxproParser() {
    }

    public static Parser create() {
        AnxproParser anxproParser = new AnxproParser();
        anxproParser.currencyStatic = anxproParser.readJson("https://anxpro.com/moon/v1/market/currencyStatic").getAsJsonObject();
        anxproParser.allFees = anxproParser.readJson("https://anxpro.com/moon/v1/allFees").getAsJsonObject();
        return anxproParser;
    }

    @Override
    public String exchangeId() {
        return "anxpro";
    }

    @Override
    public ExchangeMetadata generateExchangeMetadata() throws Exception {
        TransferFees depositFees = new TransferFees();
        TransferFees withdrawalFees = new TransferFees();
        TradingFees tradingFees = new TradingFees();

        JsonObject data = allFees.getAsJsonArray("data").get(0).getAsJsonObject();

        {
            JsonObject depositData = data.getAsJsonObject("fiatDeposit");
            for (String ccy : depositData.keySet()) {
                if (depositsEnabled(ccy)) {
                    JsonObject options = depositData.getAsJsonObject(ccy);
                    for (String optionName : options.keySet()) {
                        try {
                            JsonObject option = options.getAsJsonObject(optionName);
                            depositFees.addFee(ccy, optionName, toTransferFee(option));
                        } catch (Exception e) {
                            throw new IllegalStateException("Failed to parse deposit data for: " + optionName, e);
                        }
                    }
                } else {
                    depositFees.addFee(ccy, Fee.NOT_AVAILABLE);
                }
            }

            JsonObject withdrawalData = data.getAsJsonObject("fiatWithdrawal");
            for (String ccy : withdrawalData.keySet()) {
                if (withdrawalsEnabled(ccy)) {
                    JsonObject options = withdrawalData.getAsJsonObject(ccy);
                    for (String optionName : options.keySet()) {
                        try {
                            JsonObject option = options.getAsJsonObject(optionName);
                            withdrawalFees.addFee(ccy, optionName, toTransferFee(option));
                        } catch (Exception e) {
                            throw new IllegalStateException("Failed to parse deposit data for: " + optionName, e);
                        }
                    }
                } else {
                    withdrawalFees.addFee(ccy, Fee.NOT_AVAILABLE);
                }
            }
        }

        {
            JsonObject depositData = data.getAsJsonObject("coinDeposit");
            for (String ccy : depositData.keySet()) {
                if (depositsEnabled(ccy)) {
                    try {
                        JsonObject option = depositData.getAsJsonObject(ccy);
                        Fee fee = toTransferFee(option);
                        depositFees.addFee(ccy, fee);
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to parse deposit data for: " + ccy, e);
                    }
                } else {
                    depositFees.addFee(ccy, Fee.NOT_AVAILABLE);
                }
            }

            JsonObject withdrawalData = data.getAsJsonObject("coinWithdrawal");
            for (String ccy : withdrawalData.keySet()) {
                if (withdrawalsEnabled(ccy)) {
                    JsonObject option = withdrawalData.getAsJsonObject(ccy).getAsJsonObject("CRYPTOCOIN");//ignore the weird "email" fee, it's not metioned on their website
                    try {
                        withdrawalFees.addFee(ccy, toTransferFee(option));
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to parse deposit data for: " + ccy, e);
                    }
                } else {
                    withdrawalFees.addFee(ccy, Fee.NOT_AVAILABLE);
                }
            }

            JsonObject tradingFeeData = data.getAsJsonObject("tradingFee");
            // looks like the API supports bid v's ask fees, but it's all the same so let's ignore this for now
            BigDecimal takerFeePct = tradingFeeData.getAsJsonPrimitive("incomingOrderFeePer").getAsBigDecimal().stripTrailingZeros();
            BigDecimal makerFeePct = tradingFeeData.getAsJsonPrimitive("restingOrderFeePer").getAsBigDecimal().stripTrailingZeros();
            tradingFees.addDefaultFee(Fee.parse(takerFeePct + "%"), Fee.parse(makerFeePct + "%"));
        }

        return new ExchangeMetadata(tradingFees, depositFees, withdrawalFees, new HashMap());
    }

    private Fee toTransferFee(JsonObject option) {
        BigDecimal feeAbs = option.get("feeAbs").getAsBigDecimal().stripTrailingZeros();
        Fee.Fixed fixed = new Fee.Fixed(feeAbs);

        if (option.has("networkFee") && !option.get("networkFee").isJsonNull()) {
            BigDecimal networkFee = option.getAsJsonPrimitive("networkFee").getAsBigDecimal().stripTrailingZeros();
            fixed = new Fee.Fixed(fixed.value.add(networkFee));
        }

        String feePer = option.get("feePer").getAsBigDecimal().multiply(ONE_HUNDRED).stripTrailingZeros().toPlainString() + "%";
        Optional<Fee.Rate> rate = feePer.equals("0%") ? Optional.empty() : Optional.of(new Fee.Rate(feePer));
        return new Fee(rate, Optional.of(fixed));
    }

    private boolean depositsEnabled(String ccy) {
        JsonObject settings = engineSettings(ccy);
        return settings.getAsJsonPrimitive("depositsEnabled").getAsBoolean();
    }

    private boolean withdrawalsEnabled(String ccy) {
        JsonObject settings = engineSettings(ccy);
        return settings.getAsJsonPrimitive("withdrawalsEnabled").getAsBoolean();
    }

    private JsonObject engineSettings(String ccy) {
        return currencyStatic.getAsJsonObject("currencyStatic").getAsJsonObject("currencies").getAsJsonObject(ccy).getAsJsonObject("engineSettings");
    }
}
