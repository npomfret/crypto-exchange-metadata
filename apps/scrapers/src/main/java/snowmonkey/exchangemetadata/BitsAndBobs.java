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
import java.io.Reader;
import java.math.BigDecimal;
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
    public static Source getPage(String uri) throws URISyntaxException, IOException, InterruptedException {
        String body = doGet(new URI(uri));
        Source source = new Source(body);
        source.fullSequentialParse();

        return source;
    }

    public static JsonObject getJson(String uri) throws IOException, InterruptedException {
        return getJson(URI.create(uri));
    }

    public static JsonObject getJson(URI uri) throws IOException, InterruptedException {
        return getJsonElement(uri).getAsJsonObject();
    }

    public static JsonElement getJsonElement(URI uri) throws IOException, InterruptedException {
        return new JsonParser().parse(doGet(uri));
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

    public static void main(String[] args) throws Exception {
        String uri ="https://www.okcoin.com/api/v1/withdraw_info.do";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublisher.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandler.asString());

        String body = response.body();
        System.out.println(body);

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

    public static JsonObject readJson(String url) throws IOException {
        return readJson(new URL(url));
    }

    public static JsonObject readJson(URL url) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return new JsonParser().parse(in).getAsJsonObject();
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
}
