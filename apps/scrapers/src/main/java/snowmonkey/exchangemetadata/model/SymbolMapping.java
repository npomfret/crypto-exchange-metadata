package snowmonkey.exchangemetadata.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import snowmonkey.exchangemetadata.BitsAndBobs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SymbolMapping {
    private final JsonArray mappings;
    private final Map<String, String> currencyNameToId;

    public SymbolMapping(JsonArray mappings, Map<String, String> currencyNameToId) {
        this.mappings = mappings;
        this.currencyNameToId = currencyNameToId;
    }

    public static SymbolMapping create(String exchangeId) {
        try {
            Path mappingFile = Paths.get("../mapping-generation/data/" + exchangeId + ".json");
            JsonArray mappings = BitsAndBobs.readJsonArray(mappingFile);

            Map<String, String> currencyNameToId = new HashMap<>();
            for (JsonElement el : mappings) {
                JsonObject mapping = el.getAsJsonObject();
                System.out.println(mapping);
                String baseCurrency = mapping.getAsJsonPrimitive("baseCurrency").getAsString();
                String baseId = mapping.getAsJsonPrimitive("baseId").getAsString();
                currencyNameToId.put(baseCurrency.toUpperCase(), baseId.toUpperCase());

                String quoteCurrency = mapping.getAsJsonPrimitive("quoteCurrency").getAsString();
                String quoteId = mapping.getAsJsonPrimitive("quoteId").getAsString();
                currencyNameToId.put(quoteCurrency.toUpperCase(), quoteId.toUpperCase());
            }

            return new SymbolMapping(mappings, currencyNameToId);
        } catch (IOException e) {
            throw new IllegalStateException("cannot load symbols for " + exchangeId, e);
        }
    }

    public String currencyNameToNativeSymbol(String currencyName) {
        String id = currencyNameToId.get(currencyName.toUpperCase());
        if(id == null) {
            System.out.println("no mapping for " + currencyName);
            return currencyName;
        }
        return id;
    }
}
