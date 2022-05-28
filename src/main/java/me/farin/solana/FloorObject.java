package me.farin.solana;

import lombok.Getter;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


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

        try {
            channel.getManager().setName(s.replace(name, "")).queue();
        } catch (MissingAccessException exception){
            channel.getGuild().retrieveAuditLogs().queueAfter(1, TimeUnit.SECONDS, (logs) -> {
                for(AuditLogEntry log : logs){
                    if(log.getType().equals(ActionType.CHANNEL_CREATE)){
                        String b = Utils.parseId(log.toString());
                        if(Long.parseLong(b) - 1 == channel.getIdLong()){
                            log.getUser().openPrivateChannel().flatMap(dm -> dm.sendMessage("Invalid Permissions - Read the readme file https://github.com/FarinM/SolanaFP")).queue();
                        }

                    }
                }
            });
        }

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
