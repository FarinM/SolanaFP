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
    private final int slot;
    private double floorPrice;
    Timer timer;

    public FloorObject(long id, String name, int slot) {
        timer = new Timer();
        this.id = id;
        this.name = name;
        this.slot = slot;
        getNewFloorPrice();
        startChecking();
    }

    public void getNewFloorPrice(){
        String fixedString = name.replaceAll(" ", "_").toLowerCase();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api-mainnet.magiceden.dev/v2/collections/"+fixedString+"/stats")).build();
        String response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Utils::parse)
                .thenApply(Utils::parse)
                .join();
        floorPrice = Double.parseDouble(Utils.parse(response));
    }

    public void updateFloorPrice(){
        VoiceChannel channel = BotStartup.shard.getVoiceChannelById(id);
        String name = channel.getName();
        String[] split = name.split(" ");

        split[slot -1] = String.valueOf(floorPrice);
        String fixedString = String.join(" ", split);

        channel.getManager().setName(fixedString.replace("SOLFLOOR", "")).queue();
    }

    public void startChecking(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getFloorPrice();
                updateFloorPrice();
                System.out.println("automatically saved!");
            }
        }, 0, 60*1000*5);
    }




}
