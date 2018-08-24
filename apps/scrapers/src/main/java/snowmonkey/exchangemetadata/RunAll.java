package snowmonkey.exchangemetadata;

import com.google.gson.JsonObject;
import snowmonkey.exchangemetadata.parsers.CoinFalconParser;
import snowmonkey.exchangemetadata.parsers.CoinexParser;
import snowmonkey.exchangemetadata.parsers.CryptopiaParser;
import snowmonkey.exchangemetadata.parsers.EthfinexParser;
import snowmonkey.exchangemetadata.parsers.ExmoParser;
import snowmonkey.exchangemetadata.parsers.GatecoinParser;
import snowmonkey.exchangemetadata.parsers.HitbtcParser;
import snowmonkey.exchangemetadata.parsers.OkexParser;
import snowmonkey.exchangemetadata.parsers.YobitParser;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunAll {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("../../exchange-metadata.json");
        if(!Files.exists(path))
            throw new IllegalStateException();

        JsonObject exchanges = BitsAndBobs.readJson(path);

        exchanges.add("exmo", ExmoParser.run().toJson());
        exchanges.add("coinex", CoinexParser.run().toJson());
        exchanges.add("coinfalcon", CoinFalconParser.run().toJson());
        exchanges.add("cryptopia", CryptopiaParser.run().toJson());
        exchanges.add("ethfinex", EthfinexParser.run().toJson());
        exchanges.add("gatecoin", GatecoinParser.run().toJson());
        exchanges.add("hitbtc", HitbtcParser.run().toJson());
        exchanges.add("yobit", YobitParser.run().toJson());
        exchanges.add("okex", OkexParser.run().toJson());

        JsonObject output = new JsonObject();
        exchanges.keySet().stream().sorted().forEach(exchangeName -> output.add(exchangeName, exchanges.get(exchangeName)));

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.append(BitsAndBobs.prettyPrint(output));
        }
    }
}
