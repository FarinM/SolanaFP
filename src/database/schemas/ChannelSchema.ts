import mongoose from 'mongoose';

// Schema
const ChannelSchema = new mongoose.Schema({
    channelId: {type: String, unique: true, required: true},
    collectionIndex: Number,
    collectionName: String,
});

// Model
const Channel = mongoose.model('Channel', ChannelSchema, 'channels');

export default Channel;