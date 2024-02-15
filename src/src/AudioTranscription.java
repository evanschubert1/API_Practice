import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AudioTranscription {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        HttpResponse<String> postResponse;
        HttpResponse<String> getResponse;

        //creates a transcript object that holds the audio url, api ID, and the text transcription
        Transcript transcript = new Transcript();

        transcript.setAudio_url(""); //fill url in here
        Gson gson = new Gson();
        String gsonRequest = gson.toJson((transcript));
        System.out.println(gsonRequest);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", Constants.API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(gsonRequest)).build();
        HttpClient httpClient = HttpClient.newHttpClient();

        postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println(postResponse.body());
        transcript = gson.fromJson(postResponse.body(), Transcript.class);

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transcript.getId()))
                .header("Authorization", Constants.API_KEY)
                .build();

        //while loop that repeatedly checks API status until it means the getResponse can stop
        while(true) {
            getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            transcript = gson.fromJson(getResponse.body(), Transcript.class);

            System.out.println(transcript.getStatus());

            if (transcript.getStatus().equals("completed") || transcript.getStatus().equals("error")) {
                break;
            }
            Thread.sleep(1000);
        }
        System.out.println("Transcription completed:");
        printTranscript(transcript.getText());
    }


    private static void printTranscript(String transcript) {
        int totalLength = transcript.length();
        for(int i = 0; i < totalLength; i+= 100){
            int endIndex = Math.min(i + 100, totalLength);
            System.out.println(transcript.substring(i, endIndex));
        }
    }

}
