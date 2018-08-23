package snowmonkey.exchangemetadata.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.util.Optional;

public class Fee {
    public final Optional<Rate> rate;
    public final Optional<Fixed> fixed;

    public Fee(Optional<Rate> rate, Optional<Fixed> fixed) {
        this.rate = rate;
        this.fixed = fixed;
    }

    public JsonElement toJson() {
        if (rate.isPresent() && !fixed.isPresent())
            return rate.get().toJson();
        if (!rate.isPresent() && fixed.isPresent())
            return fixed.get().toJson();

        JsonObject root = new JsonObject();
        root.add("rate", rate.get().toJson());
        root.add("fixed", fixed.get().toJson());

        return root;
    }

    public static Fee parse(String feeText) {
        Optional<Rate> rate = Optional.empty();
        Optional<Fixed> fixed = Optional.empty();

        String[] parts = feeText.split("\\s+|\\+");

        for (String part : parts) {
            part = part.trim();

            if (part.matches("^\\d+(\\.\\d+)?%$")) {
                if (rate.isPresent())
                    throw new IllegalStateException("cannot handle a fee with multiple rates");

                rate = Optional.of(new Rate(part));
            } else if (part.matches("^\\d+(\\.\\d+)?$")) {
                if (fixed.isPresent())
                    throw new IllegalStateException("cannot handle a fee with multiple fixed amounts");

                fixed = Optional.of(new Fixed(new BigDecimal(part)));
            }
        }

        if (!rate.isPresent() && !fixed.isPresent())
            throw new IllegalStateException("Cannot parse fee '" + feeText + "'");

        return new Fee(rate, fixed);
    }

    public static class Rate {
        public final String value;//eg "4.1%"

        public Rate(String value) {
            if (!value.endsWith("%"))
                throw new IllegalStateException("Invalid rate '" + value + "', rate should be a numerical percentage string, eg: 4.3%");
            this.value = value;
        }

        public JsonElement toJson() {
            return new JsonPrimitive(value);
        }
    }

    public static class Fixed {
        public final BigDecimal value;

        public Fixed(BigDecimal value) {
            this.value = new BigDecimal(value.toPlainString());
        }

        public JsonElement toJson() {
            return new JsonPrimitive(value);
        }

    }

}
