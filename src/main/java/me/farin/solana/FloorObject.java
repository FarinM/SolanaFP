package me.farin.solana;

import lombok.Getter;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;


@Getter
public class FloorObject {
    private final long id;
    private final String name;
    private final long guildId;
    private int slot;
    private double floorPrice;
    private boolean first;
    Timer timer;

    public FloorObject(long id, String name, int slot, boolean first) {
        timer = new Timer();
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.first = first;
        this.guildId = 0L;
        updateFloorPrice();
        if (first) {
            this.slot--;
            this.first = false;
            startChecking();
        }
    }

    public double getNewFloorPrice() {
        String response = null;
            String fixedString = name.replaceAll(" ", "_").toLowerCase();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api-mainnet.magiceden.dev/v2/collections/" + fixedString + "/stats")).build();
            response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Utils::parse)
                    .thenApply(Utils::parse)
                    .join();

        try {
            return Double.parseDouble(Utils.parse(response));
        } catch (NumberFormatException e){
            BotStartup.floorObjects.remove(this.getId());
            return 0D;
        }

    }

    public void updateFloorPrice() {
        VoiceChannel channel = BotStartup.shard.getVoiceChannelById(id);
        String channelName = channel.getName();
        String[] split = channelName.split(" ");
        String fixedString = String.join(" ", split);

        Double d = getNewFloorPrice();
        floorPrice = d;

        channel.getManager().setName(fixedString
                .replaceAll("(?i)solfloor", "")
                .replace(name, String.valueOf(d)
                        .replace(String.valueOf(floorPrice), String.valueOf(d)))).queue();
    }

    public void startChecking() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateFloorPrice();
                if(floorPrice == 0) {
                    timer.cancel();
                    return;
                }
                System.out.println("automatically saved!");
            }
        }, 3000, 60 * 1000 * 5);
    }


    public boolean getFirst() {
        return first;
    }
}
