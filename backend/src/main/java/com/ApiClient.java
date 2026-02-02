import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    public static void sendOffer(
            String item,
            String vendor,
            int price,
            String category,
            int etaMin,
            int sweet,
            int simple
    ) throws Exception {

        URL url = new URL("http://localhost:8080/api/offers");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = """
        {
          "item": "%s",
          "vendor": "%s",
          "price": %d,
          "category": "%s",
          "etaMin": %d,
          "sweet": %d,
          "simple": %d
        }
        """.formatted(item, vendor, price, category, etaMin, sweet, simple);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to send offer, HTTP " + responseCode);
        }
    }
}

