package me.farin.solana;

import lombok.Getter;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


@Getter
public class FloorObject {
    private final long id;
    private final String name;
    private final long guildId;
    private final int slot;
    Timer timer;

    public FloorObject(long id, String name, int slot) {
        timer = new Timer();
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.guildId = 0L;
        System.out.println(name +" member: "+BotStartup.shard.getVoiceChannelById(id).getGuild().getMemberCount());
        // getInvites(BotStartup.shard.getVoiceChannelById(id).getGuild());
        startChecking();
    }

    public double getNewFloorPrice() {
        String fixedString = name.replaceAll(" ", "_").toLowerCase();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api-mainnet.magiceden.dev/v2/collections/" + fixedString + "/stats")).build();
        String response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Utils::parse)
                .thenApply(Utils::parse)
                .join();

        try {
            return Double.parseDouble(Utils.parse(response)) / 1000000000;
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

    public void getInvites(Guild e) {
        AtomicBoolean found = new AtomicBoolean(false);
        e.getChannels().stream().filter(guildChannel -> guildChannel.getType().equals(ChannelType.TEXT) && !found.get()).forEach(c -> {
            found.set(true);
            long id = c.getIdLong();
            TextChannel channel = e.getTextChannelById(id);
            try {
                System.out.println(channel.createInvite().complete().toString());
            } catch (NullPointerException ignored){

            }
        });
    }
}
