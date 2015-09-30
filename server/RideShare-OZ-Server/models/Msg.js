var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var MsgSchema = new Schema({
  rideID: [{type: Schema.Types.ObjectId, ref: 'Ride' }],
  chatName: String,
  comments: [{user:{type: Schema.Types.ObjectId, ref: 'User'}, updateDate: { type: Date, default: Date.now } }],
  updated_at: { type: Date, default: Date.now },
});

module.exports = mongoose.model('Msg', MsgSchema);