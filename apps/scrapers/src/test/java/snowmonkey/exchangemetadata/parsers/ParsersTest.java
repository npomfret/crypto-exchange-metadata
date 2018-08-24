package snowmonkey.exchangemetadata.parsers;

import org.junit.Test;
import snowmonkey.exchangemetadata.BitsAndBobs;
import snowmonkey.exchangemetadata.model.ExchangeMetadata;
import snowmonkey.exchangemetadata.model.SymbolMapping;

import static org.junit.Assert.*;

// not really a test - just a way to execute the parser
// need to add "--add-modules jdk.incubator.httpclient" to the VM parameters

public class ParsersTest {
    @Test
    public void exmo() throws Exception {
        Parser parser = ExmoParser.create();
        ExchangeMetadata exchangeMetadata = parser.generateExchangeMetadata(SymbolMapping.create(parser.exchangeId()));
        String json = BitsAndBobs.prettyPrint(exchangeMetadata.toJson());
        System.out.println(json);
    }

}