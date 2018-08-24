package snowmonkey.exchangemetadata.parsers;

import com.google.gson.JsonElement;
import net.htmlparser.jericho.Source;
import snowmonkey.exchangemetadata.ResourceGetter;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.SymbolMapping;

public interface Parser {
    /**
     * @return the exchange id as defined in the ccxt project
     */
    String exchangeId();

    ExchangeMetadata generateExchangeMetadata(SymbolMapping symbolMapping) throws Exception;

    default JsonElement readJson(String uri) {
        return new ResourceGetter(exchangeId()).readJson(uri);
    }

    default Source readWebpage(String uri) {
        return new ResourceGetter(exchangeId()).getWebPage(uri);
    }
}
