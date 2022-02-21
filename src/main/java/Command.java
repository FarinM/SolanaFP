import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Command extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        String[] message = e.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase("!test")){
            e.getMessage().getChannel().sendMessage("testmesasge").queue();
        }
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent e) {
        if(e.getChannelType().equals(ChannelType.VOICE)){
            if(e.getChannel().getName().contains("SOLFLOOR")){
                String[] name = e.getChannel().getName().split(" ");
                int i = 0;
                String floor;
                for(String s : name){
                    if(s.equalsIgnoreCase("solfloor")){
                        FloorObject floorObject = new FloorObject(e.getChannel().getIdLong(), name[i+1], i+1);
                        BotStartup.floorObjects.put(e.getChannel().getIdLong(), floorObject);
                        floor = String.valueOf(floorObject.getFloorPrice());
                        e.getJDA().getVoiceChannelById(e.getChannel().getIdLong()).getManager().setName(e.getChannel().getName().replace("SOLFLOOR "+name[i+1], floor)).queue();
                        break;
                    }
                    i++;
                }
            }
        }
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent e){
        for (Map.Entry<Long,FloorObject> entry : BotStartup.floorObjects.entrySet()){
            if(entry.getKey().equals(e.getChannel().getIdLong())){
                System.out.println(BotStartup.floorObjects.get(entry.getKey()).getName() + " has been removed");
                BotStartup.floorObjects.remove(entry.getKey());
                break;
             }
        }
    }


}
