package snowmonkey.exchangemetadata.model;

import com.google.gson.JsonArray;
import snowmonkey.exchangemetadata.BitsAndBobs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SymbolMapping {
    public SymbolMapping(JsonArray mappings) {
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
