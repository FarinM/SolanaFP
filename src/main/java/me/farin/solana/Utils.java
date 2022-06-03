package me.farin.solana;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

public class Utils {


    public static String parse(String responsebody){
        String[] strings = responsebody.split(",");
        for(String s : strings){
            if(s.contains("floorPrice")){
                String[] split = s.split(":");
                return split[1];
            }
        }

        return responsebody;
    }


    public static String parseId(String responsebody){
        String[] strings = responsebody.split(" ");
        for(String s : strings){
            if(s.contains("ALE:CHANNEL_CREATE(ID:")){
                String[] split = s.split(":");
                return split[2];
            }
        }

        return responsebody;
    }



    public static boolean checkIfValidCollection(String name){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api-mainnet.magiceden.dev/v2/collections/" + name.toLowerCase() + "/stats")).build();
        String response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Utils::parse)
                .thenApply(Utils::parse)
                .join();
        try {
            double a = Double.parseDouble(Utils.parse(response)) / 1000000000;
            return true;
        } catch (NumberFormatException e) {
            System.out.println("invalid collection: " +name);
            return false;
        }

    }
}
