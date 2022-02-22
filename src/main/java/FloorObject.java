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
        getNewFloorPrice();
        updateFloorPrice();
        startChecking();
        if(first) {
            this.slot--;
            this.first = false;
        }
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
        String channelName = channel.getName();
        String[] split = channelName.split(" ");

        split[slot] = String.valueOf(floorPrice);
        String fixedString = String.join(" ", split);

        channel.getManager().setName(fixedString.replace("SOLFLOOR", "").replace(name, "")).queue();
    }

    public void startChecking(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getNewFloorPrice();
                updateFloorPrice();
                System.out.println("automatically saved!");
            }
        }, 60, 60*1000*5);
    }


    public boolean getFirst() {
        return first;
    }
}
