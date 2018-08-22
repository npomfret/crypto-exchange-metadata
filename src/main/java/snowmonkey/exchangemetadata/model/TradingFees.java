package snowmonkey.exchangemetadata.model;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TradingFees {
    private final Map<String, Collection<Fee>> fees = new HashMap<>();

    public void addDefaultFee(Fee fee) {
        fees.put("default", Collections.singleton(fee));
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (String ccy : fees.keySet()) {
            Collection<Fee> fees = this.fees.get(ccy);

            if(fees.size() == 1) {
                root.add(ccy, fees.iterator().next().toJson());
            } else {
                throw new IllegalStateException("todo");
            }
        }
        return root;
    }
}
