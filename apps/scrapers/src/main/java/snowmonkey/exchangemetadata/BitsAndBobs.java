package snowmonkey.exchangemetadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import net.htmlparser.jericho.Source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class BitsAndBobs {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static Source getPage(String uri) throws URISyntaxException, IOException, InterruptedException {
        String body = doGet(new URI(uri));
        Source source = new Source(body);
        source.fullSequentialParse();

        return source;
    }

    public static String doGet(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.of(10, SECONDS))
                .header("key1", "value1")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandler.asString());

        return response.body();
    }

    public static JsonObject readJson(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    public static JsonArray readJsonArray(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return new JsonParser().parse(reader).getAsJsonArray();
        }
    }

    public static JsonElement readJson(URL url) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return new JsonParser().parse(in);
        }
    }

    public static String prettyPrint(JsonElement e) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        return gson.toJson(e);
    }

    public static BigDecimal percentToBigDecimal(String s) {
        try {
            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            Number parse = percentFormat.parse(s);
            return new BigDecimal(parse.toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    public static String decimalToPercent(BigDecimal value, int scale) {
        return value.divide(ONE_HUNDRED, scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "%";
    }
}
