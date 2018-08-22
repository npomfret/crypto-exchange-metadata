package snowmonkey.exchangemetadata.model;

import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransferFees {
    private final Map<String, Map<String, Fee>> depositFees = new HashMap<>();
    private final Map<String, Map<String, Fee>> withdrawalFees = new HashMap<>();

    public void addDepositFee(String ccy, Fee fee) {
        depositFees.put(ccy, Collections.singletonMap("default", fee));
    }

    public void addDepositFee(String ccy, String label, Fee fee) {
        if(!depositFees.containsKey(ccy)) {
            depositFees.put(ccy, new HashMap<>());
        }

        depositFees.get(ccy).put(label, fee);
    }

    public void addWithdrawalFee(String ccy, Fee fee) {
        withdrawalFees.put(ccy, Collections.singletonMap("default", fee));
    }

    public void addWithdrawalFee(String ccy, String label, Fee fee) {
        if(!withdrawalFees.containsKey(ccy)) {
            withdrawalFees.put(ccy, new HashMap<>());
        }

        withdrawalFees.get(ccy).put(label, fee);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.add("deposit-fees", fees2Json(depositFees));
        root.add("withdrawal-fees", fees2Json(withdrawalFees));
        return root;
    }

    private static JsonObject fees2Json(Map<String, Map<String, Fee>> allFees) {
        JsonObject root = new JsonObject();

        allFees.keySet().stream().sorted().forEach(ccy -> {
            Map<String, Fee> feesForCcy = allFees.get(ccy);

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
