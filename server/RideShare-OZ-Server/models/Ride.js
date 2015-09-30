var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var User = require('../models/User.js');
var Group = require('../models/Group.js');

var RideSchema = new Schema({
  arrival_time: Date,
  start_time: Date,
  seats: Number,
  start_point: {type:[Number],index:'2d'}, // Lon, Lat
  start_add: String,
  end_point: {type:[Number],index:'2d'},
  destination: String,
  driver: {type: Schema.Types.ObjectId, ref: 'User' },
  group: {type: Schema.Types.ObjectId, ref: 'Group'},
  events: {type: Schema.Types.ObjectId, ref: 'Event'},
  passengers: [{
    user:{ type: Schema.Types.ObjectId, ref: 'User' },
    pickup_point:{ type: [Number] },
    pickup_time:Date,
    pickup_add:String
  }],
  requests: [{
    user:{type: Schema.Types.ObjectId, ref: 'User' },
    pickup_point:{ type: [Number] },
    state: String,
    note: String,
    pickup_time: Date,
    pickup_add: String
  }],
  updated_at: { type: Date, default: Date.now },
  note: String,
});


RideSchema.statics.getAllRides = function(callback){
  this.find().populate('driver passengers.user requests.user',
      'username phone driver_license').exec({}, function(err, rides){
    callback(rides);
  });
};

RideSchema.statics.createRide = function(req,callback){
  var Ride = mongoose.model('Ride');
  var ride = new Ride();
  ride.arrival_time = req.body.arrival_time;
  ride.start_time=req.body.start_time;
  ride.seats = req.body.seat;
  var start_lon=req.body.s_lon;
  var start_lat=req.body.s_lat;
  ride.start_add=req.body.start_add;
  ride.destination=req.body.destination;
  ride.start_point=[Number(start_lon),Number(start_lat)];
  var end_lon=req.body.e_lon;
  var end_lat=req.body.e_lat;
  ride.end_point=[Number(end_lon),Number(end_lat)];

  User.findById(req.userinfo._id, function(err, user){
    ride.driver = user;
    Group.findById(req.body.group_id,function(err,group){
      ride.group = group;
      ride.save(function(err, doc){
        if (err) {
          console.log(err);
        }
        callback(doc);
      });
    });
  });
};


// lat lon  desitination  date 
RideSchema.statics.searchRide = function(req,callback){

  var start = [];
  start[0] = Number(req.body.s_lon);
  start[1] = Number(req.body.s_lat); 
  
  var e_lon = req.body.e_lon;
  var e_lat = req.body.e_lat; 
  //need have arrival time
  var maxDistance = 0.01;
  var limit = 10;
  var groupID = req.body.group_id;
  var arrival_time = req.body.arrival_time;
  var s_time = arrival_time.substring(0, arrival_time.length - 14) + 'T00:00:00.000Z';
  var e_time = arrival_time.substring(0, arrival_time.length - 14) + 'T23:59:59.000Z';
  console.log(s_time);
  console.log(e_time);
  console.log(start);
  
  // this.aggregate([
  //   {
  //     $match:{
  //       'start_point': start
  //     }
  //   }
    
  // ], function(err, locations){
  //   callback(locations);
  // });,
  var rides=[];

  this.find({
    'group':groupID,
    'arrival_time':{"$gte":new Date(s_time),"$lt":new Date(e_time)},
    'start_point': {
      $nearSphere: start,
      $maxDistance: maxDistance

    }
  }).populate('driver passengers.user requests.user',
      'username phone driver_license').exec({},function(err,ride){
    if (ride) {
      // console.log("============");
      // console.log(ride);
    ride.forEach(function(ride){
        // console.log(ride.end_point[0]);
        // console.log(e_lon);
        //e_lon=Number(e_lon)+1;
        // console.log(Number(e_lon)+1);
        // console.log(Number(e_lon)-1);
        //e_lat=Number(e_lat)+1;
   if((Number(e_lon)-0.01)<ride.end_point[0]&&ride.end_point[0]<(Number(e_lon)+0.01)&&(Number(e_lat)-0.01)<ride.end_point[1]&&ride.end_point[1]<(Number(e_lat)+0.01)){
          rides.push(ride);
       }
   //      //console.log(time);
      });

    }
    callback(rides);
    
  });



  // .limit(limit).exec(function(err, locations) {
  //       qry.where('end_point').near({
  //         center: end,
  //         maxDistance :maxDistance
    
  //       }).exec(function(err,ride){callback(ride);});
  //     }
  //    );




};

RideSchema.methods.addRequest = function(user_id,req,callback){
  /* console.log(this); */ 
  var pickup_point = [];
  pickup_point[0] = req.body.p_lon;
  pickup_point[1] = req.body.p_lat;
  var note = req.body.note;
  var pickup_time = req.body.pickup_time;
  var count=0;
  this.requests.forEach(function(request){
    if (String(request.user)==String(user_id)) {
        count=1;
    }});
  if(count===0){
  this.requests.push({
    'user':user_id,
    'state':"unaccept",
    'pickup_point':pickup_point,
    'note':note,
    'pickup_time':pickup_time,
    'pickup_add':req.body.pickup_add
  });
  this.save(function(err, doc){
    callback(doc);
  });
  }else{callback("already exist");} 
};


RideSchema.statics.cancelRide = function(req,callback){
  var ride_id=req.body.ride_id;
  console.log(req.userinfo._id);
  this.find({_id:ride_id}).remove(function(err){
    callback("deleted");
  });
};

RideSchema.statics.rejectRequest= function(req,callback){
var ride_id=req.body.ride_id;
var user_id=req.userinfo._id;
  this.findByIdAndUpdate(ride_id,{$pull:{'requests':{'user':user_id}}},
      function(err,doc){callback(doc);});
};


RideSchema.statics.acceptRequest= function(req,callback){
var ride_id=req.body.ride_id;
var user_id=req.userinfo._id; 
var pickup_point=[];
var pickup_time; 
var pickup_add;
var user_in_passenger=0;
 //add use to passenger
  this.findById(ride_id,function(err,doc){

    doc.passengers.forEach(function(passenger){
        if (String(passenger.user)==String(user_id))
        {user_in_passenger=1;}

    });
    if(user_in_passenger===0){
      doc.requests.forEach(function(request){
    
      if (String(request.user)==String(user_id)) {
        pickup_point=request.pickup_point;
        pickup_time=request.pickup_time;
        pickup_add=request.pickup_add;
        // doc.seats=Number(doc.seats)-1;
        doc.passengers.push({
          'user':user_id,
          'pickup_point':pickup_point,
          'pickup_time':pickup_time,
          'pickup_add':pickup_add
        });
        doc.save();
      }
    });}
  });
  //delete the user in requests
  this.findByIdAndUpdate(ride_id,{$pull:{'requests':{'user':user_id}}},function(err,doc){
  callback(doc);
  });
};

RideSchema.statics.kickPassenger= function(req,callback){
  var ride_id=req.body.ride_id;
  var user_id=req.userinfo._id; 
  this.findByIdAndUpdate(ride_id,{$pull:{'passengers':{'user':user_id}}},
      function(err,doc){
        // doc.seats=Number(doc.seats)+1;
       // doc.save();
        callback(doc);
      });
};

RideSchema.statics.passengerLeave= function(req,callback){
  var ride_id=req.body.ride_id;
  var user_id=req.userinfo._id; 
  this.findByIdAndUpdate(ride_id,{$pull:{'passengers':{'user':user_id}}},
      function(err,doc){
       
        // doc.seats=Number(doc.seats)+1;
       // doc.save();
        callback(doc);
      });
};

module.exports = mongoose.model('Ride', RideSchema);
