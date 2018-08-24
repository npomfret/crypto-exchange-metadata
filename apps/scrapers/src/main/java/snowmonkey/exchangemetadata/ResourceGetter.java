package snowmonkey.exchangemetadata;

import com.google.gson.JsonElement;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static snowmonkey.exchangemetadata.BitsAndBobs.doGet;

public class ResourceGetter {
    private final String exchangeId;

    public ResourceGetter(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public Source getWebPage(String uri) {
        try {
            String body = doGet(new URI(uri));

            saveFile(makePath(uri, ".json"), body);

            Source source = new Source(body);
            source.fullSequentialParse();
            return source;
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new IllegalStateException("Failed to get " + uri, e);
        }
    }

    public JsonElement readJson(String uri) {
        try {
            JsonElement response = BitsAndBobs.readJson(new URL(uri));
            Path file = makePath(uri, ".json");
            saveFile(file, BitsAndBobs.prettyPrint(response));

            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Bad url : " + uri, e);
        }
    }

    private Path makePath(String uri, String suffiix) throws IOException {
        String fileSafe = java.net.URLEncoder.encode(uri, "UTF-8") + suffiix;

        Path dir = Paths.get("src/main/resources/" + exchangeId);
        if (!Files.exists(dir))
            Files.createDirectories(dir);

        return dir.resolve(fileSafe);
    }

    private void saveFile(Path file, String text) throws IOException {
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
    }
}
