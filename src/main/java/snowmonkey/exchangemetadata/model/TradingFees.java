package snowmonkey.exchangemetadata.model;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TradingFees {
    private static class FeePair {
        public final Fee taker;
        public final Fee maker;

        private FeePair(Fee taker, Fee maker) {
            this.taker = taker;
            this.maker = maker;
        }

        public JsonObject toJson() {
            JsonObject root = new JsonObject();
            root.add("taker", taker.toJson());
            root.add("maker", maker.toJson());
            return root;
        }
    }

    private final Map<String, Collection<FeePair>> fees = new HashMap<>();

    public void addDefaultFee(Fee fee) {
        addDefaultFee(fee, fee);
    }

    public void addDefaultFee(Fee taker, Fee maker) {
        fees.put("default", Collections.singleton(new FeePair(taker, maker)));
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (String ccy : fees.keySet()) {
            Collection<FeePair> fees = this.fees.get(ccy);

            if(fees.size() == 1) {
                FeePair feePair = fees.iterator().next();
                root.add(ccy, feePair.toJson());
            } else {
                throw new IllegalStateException("todo");
            }
        }
        return root;
    }
}
