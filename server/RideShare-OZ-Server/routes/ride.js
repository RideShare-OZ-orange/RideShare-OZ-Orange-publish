var express = require('express');
var router = express.Router();
var Ride = require('../models/Ride.js');

router.post('/create', function(req, res, next) {
  Ride.createRide(req, function(doc){
    res.json(doc);
  });
});

router.get('/getall', function(req, res, next) {
  Ride.getAllRides(function(rides){
    res.json(rides);
  });
});

router.get('/search',function(req, res){
  req.body = req.query;
  Ride.searchRide(req, function(locations){
    res.json(locations);
  });
});

router.post('/search',function(req, res){
  Ride.searchRide(req, function(locations){
    res.json(locations);
  });
});

router.post('/request',function(req,res){
  
  Ride.findById(req.body.ride_id, function(err, ride){
    if(ride){
    ride.addRequest(req.userinfo._id,req, function(updated_ride){
      res.json(updated_ride);

    });
  }else{ res.json("no ride");}
  });

});

router.post('/cancel',function(req,res){
  Ride.cancelRide(req, function(flag){
    res.json(flag);
  });
});


router.post('/reject',function(req,res){
  Ride.rejectRequest(req,function(ride){
      res.json(ride);
  });
  
});

router.post('/accept',function(req,res){
  Ride.acceptRequest(req,function(ride){
      res.json(ride);
  });
  
});


router.post('/kick',function(req,res){
  Ride.kickPassenger(req,function(ride){
      res.json(ride);
  });
  
});

router.post('/leave',function(req,res){
  Ride.passengerLeave(req,function(ride){
      res.json(ride);
  });
  
});


module.exports = router;
