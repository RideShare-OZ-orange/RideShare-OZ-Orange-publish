var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var EventSchema = new Schema({
  eventName: String,
  eventlocation: String,
  eventInfo: [Number],
  startTime:Date,
  group: {type: Schema.Types.ObjectId, ref: 'Group'},
  organizer: {type: Schema.Types.ObjectId, ref: 'User' },
  Members: [{type: Schema.Types.ObjectId, ref: 'User' }],
});


  EventSchema.statics.createEvent= function(req,callback) {
  var Event = mongoose.model('Event');
  var events = new Event();
  events.eventName = req.query.event_name;
  var lon=req.query.e_lon;
  var lat=req.query.e_lat;
  events.eventlocation=[lon,lat];
  events.eventInfo=req.query.eventInfo;
  events.group=req.query.group_id;
  events.startTime=req.query.date;
  User.findById(req.query.organizer, function(err, user){
    events.organizer=user;
    events.save(function(err, doc){
      if (err) {
        console.log(err);
      }
      callback(doc);
      });
    });
  };

module.exports = mongoose.model('Event', EventSchema);