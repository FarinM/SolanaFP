package me.farin.solana;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Events extends ListenerAdapter {

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent e) {
        if(e.getChannelType().equals(ChannelType.VOICE)){
            if(e.getChannel().getName().toUpperCase().contains("SOLFLOOR")){
                String[] name = e.getChannel().getName().split(" ");
                int i = 0;
                for(String s : name){
                    if(s.equalsIgnoreCase("solfloor")){
                        if(!Utils.checkIfValidCollection(name[i+1])){
                            e.getGuild().retrieveAuditLogs().queueAfter(1, TimeUnit.SECONDS, (logs) -> {
                                for(AuditLogEntry log : logs){
                                    if(log.getType().equals(ActionType.CHANNEL_CREATE)){
                                        String b = Utils.parseId(log.toString());
                                        if(Long.parseLong(b) - 1 == e.getChannel().getIdLong()){
                                            log.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Invalid Collection! To get the correct Colletion name, follow the image "  )).queue();
                                        }

                                    }
                                }
                            });
                            return;
                        }
                        FloorObject floorObject = new FloorObject(e.getChannel().getIdLong(), name[i+1], i);
                        BotStartup.floorObjects.put(e.getChannel().getIdLong(), floorObject);
                        BotStartup.saveData();

                        int finalI = i;
                        e.getGuild().retrieveAuditLogs().queueAfter(1, TimeUnit.SECONDS, (logs) -> {
                            for(AuditLogEntry log : logs){
                                if(log.getType().equals(ActionType.CHANNEL_CREATE)){
                                    String b = Utils.parseId(log.toString());
                                    if(Long.parseLong(b) - 1 == e.getChannel().getIdLong()){
                                        log.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Channel Created! - Listening to: https://magiceden.io/marketplace/"+name[finalI +1] )).queue();
                                    }

                                }
                            }
                        });

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
                BotStartup.floorObjects.get(entry.getKey()).getTimer().cancel();
                BotStartup.floorObjects.remove(entry.getKey());
                break;
             }
        }
    }


}
