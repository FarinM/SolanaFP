import { VoiceChannel } from "discord.js";
import fetch from "node-fetch";
import { client } from "..";

export default class FloorClass {
    collectionName: string;
    collectionIndex: number;
    channelId: string;
    interval: NodeJS.Timer | undefined;

    constructor(collectionIndex: number, collectionName: string, channelId: string) {
        this.collectionName = collectionName
        this.collectionIndex = collectionIndex
        this.channelId = channelId
    }

    async fetchPrice() {
        const resp = await (await fetch(`https://api-mainnet.magiceden.dev/v2/collections/${this.collectionName}/stats`)).json()

        return resp.floorPrice / 1000000000
    }

    async startInterval() {
        this.updateFloorPrice()
        this.interval = setInterval(async () => {
            this.updateFloorPrice()
        }, 180000);
    }

    async updateFloorPrice() {
        const channel = await client.channels.fetch(this.channelId)
        if(channel?.type !== "GUILD_VOICE") return;
        const floorPrice = await this.fetchPrice();
        const channelName = channel?.name.split(" ")
        channelName[this.collectionIndex] = floorPrice.toString()
        console.log(channelName)

        channel.setName(channelName.join(" "))
    }
}