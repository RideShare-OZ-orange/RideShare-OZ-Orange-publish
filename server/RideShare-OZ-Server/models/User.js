var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var UserSchema = new Schema({
  username: String,
  note: String,
  address: String,
  phone: String,
  DoB: Date,
  driver_license: String,
  groups: [{type: Schema.Types.ObjectId, ref: 'Group' }],
  rides: [{type: Schema.Types.ObjectId, ref: 'Ride' }],
  events: [{type: Schema.Types.ObjectId, ref: 'Event' }],
  updated_at: { type: Date, default: Date.now },
  driver_rate:{ type: Number, default:0},
  passenger_rate:{ type: Number,default:0},
});

UserSchema.statics.createUser = function(username, callback){
  var User = mongoose.model('User');
  var new_user = new User({'username': username});
  new_user.save(function(err){
    /* console.log("========"); */
    /* console.log(new_user); */
    callback(new_user);
  });
};

UserSchema.statics.getAllUsers = function(callback){
  this.find({}, function(err, users){
    callback(users);
  });
};

UserSchema.methods.getRides = function(callback){
  var conditions = {$or:[{'driver':this}, {'passengers':this}]};
  /* var conditions = {}; */
  this.model('Ride').find(conditions).populate('driver passengers.user requests.user',
      'username phone driver_license').exec({}, function(err, rides){
    callback(rides);
  });
};

module.exports = mongoose.model('User', UserSchema);
