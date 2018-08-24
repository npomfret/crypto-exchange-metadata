package snowmonkey.exchangemetadata.parsers;

import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.SymbolMapping;

public interface Parser {
    /**
     * @return the exchange id as defined in the ccxt project
     */
    String exchangeId();

    ExchangeMetadata generateExchangeMetadata(SymbolMapping symbolMapping) throws Exception;
}
