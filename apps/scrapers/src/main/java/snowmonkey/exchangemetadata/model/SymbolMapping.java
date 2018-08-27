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

    public SymbolMapping(JsonArray mappings) {
        this.mappings = mappings;
    }

    public static SymbolMapping create(String exchangeId) {
        try {
            Path mappingFile = Paths.get("../mapping-generation/data/" + exchangeId + ".json");
            JsonArray mappings = BitsAndBobs.readJsonArray(mappingFile);

            return new SymbolMapping(mappings);
        } catch (IOException e) {
            throw new IllegalStateException("cannot load symbols for " + exchangeId, e);
        }
    }

}
