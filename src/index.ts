import { Client, Intents, VoiceChannel } from 'discord.js';
import { ActivityTypes } from 'discord.js/typings/enums';
import dotenv from 'dotenv'
import { connectDB, findChannel, getAllChannels, removeChannel, saveChannel } from './database/database';
import FloorClass from './utils/FloorClass';
dotenv.config()

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export const client = new Client({ intents: [Intents.FLAGS.GUILDS] });
export const activeChannels: FloorClass[] = []

client.on('ready', () => {
    console.log(`Logged in as ${client?.user?.tag}!`);
    client?.user?.setActivity('magiceden', { type: ActivityTypes.PLAYING });
});

connectDB()

client.on("channelCreate", async (channel) => {
    if (channel instanceof VoiceChannel) {
        const name = channel.name.toLocaleLowerCase()
        const split = name.split(" ")
        const collectionIndex = split.indexOf("solfloor")
        console.log(collectionIndex)
        const collectionName = split[collectionIndex + 1]

        if (name.includes("solfloor") && collectionIndex != null) {
            split[collectionIndex] = ""
            split[collectionIndex + 1] = "..."
            const newName = split.join(" ")
            channel.setName(newName)
            const floor = new FloorClass(collectionIndex, collectionName, channel.id)

            if (await floor.fetchPrice()) {
                floor.startInterval()
                activeChannels.push(floor)
                await saveChannel(channel.id, collectionIndex, collectionName)
            }
        }
    }
})

client.on('channelDelete', async (channel) => {
    if (await findChannel(channel.id)) {
        const active = activeChannels.find((x) => x.channelId === channel.id)
        if (active) {
            clearInterval(active.interval)
        }

        await removeChannel(channel.id)
    }
})

const loadData = async () => {
    const channels = await getAllChannels()
    console.log(channels.length)

    for (const channel of channels) {
        const discordChannel = await client.channels.fetch(channel.channelId).catch(err => { console.log("channel does not exist") })
        if (!discordChannel) {
            await removeChannel(channel.channelId)
        } else {
            const floor = new FloorClass(channel.collectionIndex!, channel.collectionName!, channel.channelId)
            if (await floor.fetchPrice()) {
                floor.startInterval()
                activeChannels.push(floor)
            }
        }
        await sleep(3000)
    }
}
loadData()


client.login(process.env.DISCORD_TOKEN);