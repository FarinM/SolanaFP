import mongoose from "mongoose";
import ChannelModel from './schemas/ChannelSchema'


export const connectDB = async () => {
    mongoose.connect(process.env.MONGOOSE_URI!);

    mongoose.connection.on('connected', () => {
        console.log('Mongoose is connected!');
    });
}


export const saveChannel = async (channelId: string, collectionIndex: number, collectionName: string) => {
    const model = new ChannelModel({
        channelId,
        collectionIndex,
        collectionName
    })


    await model.save()
}

export const removeChannel = async (channelId: string) => {
    await ChannelModel.findOneAndDelete({ channelId })
    console.log("deleted channel", channelId)
}

export const findChannel = async (channelId: string) => {
    const channel = await ChannelModel.findOne({channelId})

    if(channel) return channel

    return null
}

export const getAllChannels = async() => {
    return await ChannelModel.find({})
}
