package snowmonkey.exchangemetadata.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingFees {

    private static class FeePair {
        public final String label;
        public final Fee taker;
        public final Fee maker;

        private FeePair(Fee taker, Fee maker) {
            this(null, taker, maker);
        }

        private FeePair(String label, Fee taker, Fee maker) {
            this.label = label;
            this.taker = taker;
            this.maker = maker;
        }

        public JsonObject toJson() {
            JsonObject root = new JsonObject();

            if (label != null)
                root.addProperty("label", label);

            root.add("taker", taker.toJson());
            root.add("maker", maker.toJson());
            return root;
        }
    }

    private final Map<String, List<FeePair>> fees = new HashMap<>();

    public void addFee(String market, Fee takerFee, Fee makerFee) {
        addFee(market, takerFee, makerFee, null);
    }

    public void addDefaultFeeScheduleItem(String label, Fee takerFee, Fee makerFee) {
        addFee("default", takerFee, makerFee, label);
    }

    private void addFee(String marketName, Fee takerFee, Fee makerFee, String label) {
        if (!fees.containsKey(marketName))
            fees.put(marketName, new ArrayList<>());

        List<FeePair> list = fees.get(marketName);
        list.add(new FeePair(label, takerFee, makerFee));
    }

    public void addDefaultFee(Fee fee) {
        addDefaultFee(fee, fee);
    }

    public void addDefaultFee(Fee taker, Fee maker) {
        fees.put("default", Collections.singletonList(new FeePair(taker, maker)));
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (String ccy : fees.keySet()) {
            Collection<FeePair> fees = this.fees.get(ccy);

            if (fees.size() == 1) {
                FeePair feePair = fees.iterator().next();
                root.add(ccy, feePair.toJson());
            } else {
                JsonArray items = new JsonArray();
                for (FeePair fee : fees) {
                    items.add(fee.toJson());
                }
                root.add(ccy, items);
            }
        }
        return root;
    }
}
