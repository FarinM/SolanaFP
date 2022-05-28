package me.farin.solana;

import lombok.Getter;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;


@Getter
public class FloorObject {
    private final long id, guildId;
    private final int slot;
    private final String name;
    private final Timer timer;

    public FloorObject(long id, String name, int slot) {
        timer = new Timer();
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.guildId = 0L;
        System.out.println(name + " member: " + BotStartup.shard.getVoiceChannelById(id).getGuild().getMemberCount());
        startChecking();
    }

    public double getNewFloorPrice() {
        String fixedString = name.replaceAll(" ", "_").toLowerCase();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api-mainnet.magiceden.dev/v2/collections/" + fixedString + "/stats")).build();
        String response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .join();


        try {
            return new JSONObject(response).getDouble("floorPrice") / 1000000000;
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

    public void updateFloorPrice() {
        VoiceChannel channel = BotStartup.shard.getVoiceChannelById(id);
        String channelName = channel.getName();
        String[] split = channelName.split(" ");
        split[slot] = String.valueOf(getNewFloorPrice());
        String s = String.join(" ", split);

        channel.getManager().setName(s.replace(name, "")).queue();
    }

    public void startChecking() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (BotStartup.shard.getVoiceChannelById(id) != null && BotStartup.shard.getVoiceChannelById(id).getName().split(" ").length > slot) {
                    updateFloorPrice();
                } else
                    timer.cancel();
            }
        }, 2000, 60 * 1000 * 5);
    }
}
