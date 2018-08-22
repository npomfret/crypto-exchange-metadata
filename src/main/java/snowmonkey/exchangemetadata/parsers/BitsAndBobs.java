package snowmonkey.exchangemetadata.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class BitsAndBobs {
    public static Source getPage(String rui) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(rui))
                .timeout(Duration.of(10, SECONDS))
                .header("key1", "value1")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandler.asString());

        Source source = new Source(response.body());
        source.fullSequentialParse();

        return source;
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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(e);
    }
}
