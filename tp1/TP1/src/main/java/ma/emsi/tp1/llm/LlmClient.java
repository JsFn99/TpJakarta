package ma.emsi.tp1.llm;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class LlmClient {

    private String apiKey;
    private Client clientRest;
    private final WebTarget target;

    public LlmClient() {
        // on lis la clé API depuis les variables d'environnement
        this.apiKey = System.getenv("GEMINI_KEY");
        this.clientRest = ClientBuilder.newClient();
        this.target = clientRest.target("https://api.gemini.google.com");
    }

    public Response envoyerRequete(Entity requestEntity) {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        // Envoie la requête POST au LLM
        return request.post(requestEntity);
    }

    public void closeClient() {
        this.clientRest.close();
    }
}

