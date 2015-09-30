var express = require('express');
var router = express.Router();
var User = require('../models/User.js');

/* Login. */
router.post('/login', function(req, res, next) {
  User.findOne({'username': req.userinfo.email}, function(err, user){
    if (!user) {
      User.createUser(req.userinfo.email, function(new_user){
        new_user.getRides(function(rides){
          res.json(rides);
        });
      });
    } else {
      user.getRides(function(rides){
        res.json(rides);
      });
    }
  });
  /* res.status(500).send('Something broke!'); */
});

router.post('/info', function(req, res, next) {
  User.findOne({'username': req.userinfo.email}, function(err, user){
    res.json(user);
  });
});

router.post('/getRides', function(req, res, next) {
  User.findOne({'username': req.userinfo.email}, function(err, user){
    /* res.json(user); */
    user.getRides(function(rides){
      res.json(rides);
    });
  });
});

router.post('/update', function(req, res, next){
  User.findOne({'username': req.userinfo.email}, function(err, user){
    user.address = (req.body.address)? req.body.address : user.address;
    user.note = (req.body.note)? req.body.note : user.note;
    user.phone = (req.body.phone)? req.body.phone : user.phone;
    user.DoB = (req.body.DoB)? req.body.DoB : user.DoB;
    user.driver_license = (req.body.driver_license)? req.body.driver_license : user.driver_license;
    user.save(function(err){
      res.json(user);
    });
  });
});

router.post('/getall', function(req, res, next) {
  User.getAllUsers(function(users){
    res.json(users);
  });
});

router.get('/getall', function(req, res, next) {
  User.getAllUsers(function(users){
    res.json(users);
  });
});

module.exports = router;
