package snowmonkey.exchangemetadata.model;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TransferFees {
    private final Map<String, Map<String, Fee>> fees = new HashMap<>();

    public void addFee(String ccy, Fee fee) {
        fees.put(ccy, Collections.singletonMap("default", fee));
    }

    public void addDefaultFee(Fee fee) {
        fees.put("default", Collections.singletonMap("default", fee));
    }

    public void addFee(String ccy, String label, Fee fee) {
        if (!fees.containsKey(ccy)) {
            fees.put(ccy, new HashMap<>());
        }

        fees.get(ccy).put(label, fee);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();

        fees.keySet().stream().sorted().forEach(ccy -> {
            Map<String, Fee> feesForCcy = fees.get(ccy);

            if (feesForCcy.size() == 1) {
                root.add(ccy, feesForCcy.values().iterator().next().toJson());
            } else {
                JsonObject obj = new JsonObject();
                feesForCcy.keySet().stream().sorted().forEach(label -> {
                    obj.add(label, feesForCcy.get(label).toJson());
                });
                root.add(ccy, obj);
            }
        });

        return root;
    }
}
