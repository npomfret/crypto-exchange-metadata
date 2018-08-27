package snowmonkey.exchangemetadata;

import com.google.gson.JsonObject;
import snowmonkey.exchangemetadata.model.SymbolMapping;
import snowmonkey.exchangemetadata.parsers.AnxproParser;
import snowmonkey.exchangemetadata.parsers.BinanceParser;
import snowmonkey.exchangemetadata.parsers.BitZParser;
import snowmonkey.exchangemetadata.parsers.BitbankParser;
import snowmonkey.exchangemetadata.parsers.BitbayParser;
import snowmonkey.exchangemetadata.parsers.BitfinexParser;
import snowmonkey.exchangemetadata.parsers.BitflyerParser;
import snowmonkey.exchangemetadata.parsers.CoinFalconParser;
import snowmonkey.exchangemetadata.parsers.CoinexParser;
import snowmonkey.exchangemetadata.parsers.CryptopiaParser;
import snowmonkey.exchangemetadata.parsers.EthfinexParser;
import snowmonkey.exchangemetadata.parsers.ExmoParser;
import snowmonkey.exchangemetadata.parsers.GatecoinParser;
import snowmonkey.exchangemetadata.parsers.HitbtcParser;
import snowmonkey.exchangemetadata.parsers.OkexParser;
import snowmonkey.exchangemetadata.parsers.Parser;
import snowmonkey.exchangemetadata.parsers.YobitParser;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RunAll {
    private static class Parsers {
        private final Map<String, Parser> map = new HashMap<>();

        public static Parsers create() {

            Parsers parsers = new Parsers();
//            parsers.add(AnxproParser.create());
//            parsers.add(BinanceParser.create());
//            parsers.add(BitZParser.create());
//            parsers.add(BitbankParser.create());
//            parsers.add(BitbayParser.create());
//            parsers.add(BitfinexParser.create());
            parsers.add(BitflyerParser.create());
//            parsers.add(CoinexParser.create());
//            parsers.add(CoinFalconParser.create());
//            parsers.add(CryptopiaParser.create());
//            parsers.add(EthfinexParser.create());
//            parsers.add(ExmoParser.create());
//            parsers.add(GatecoinParser.create());
//            parsers.add(HitbtcParser.create());
//            parsers.add(YobitParser.create());
//            parsers.add(OkexParser.create());
            return parsers;
        }

        private void add(Parser parser) {
            map.put(parser.exchangeId(), parser);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Paths.get(".").toAbsolutePath());

        Path outputFile = Paths.get("../../exchange-metadata.json");
        if (!Files.exists(outputFile))
            throw new IllegalStateException();

        JsonObject exchanges = BitsAndBobs.readJson(outputFile);

        Parsers parsers = Parsers.create();
        for (Parser parser : parsers.map.values()) {
            String exchangeId = parser.exchangeId();

            SymbolMapping symbolMapping = SymbolMapping.create(exchangeId);

            exchanges.add(parser.exchangeId(), parser.generateExchangeMetadata().toJson());
        }

        JsonObject output = new JsonObject();
        exchanges.keySet().stream().sorted().forEach(exchangeName -> output.add(exchangeName, exchanges.get(exchangeName)));

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.append(BitsAndBobs.prettyPrint(output));
        }
    }

}
