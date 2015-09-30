var express = require('express');
var router = express.Router();
var User = require('../models/User.js');
var Group = require('../models/Group.js');


/* GET users listing. */
router.get('/', function(req, res, next) {

  Group.find({}, function(err, groups){
    res.json(groups);
  });

});

router.get('/create', function(req, res) {

  Group.createGroup(req,function(groups){
  res.json(groups);

 })
});

router.get('/request',function(req,res){
  Group.findById(req.query.group_id, function(err, group){
    group.addRequest(req.query.user_id,function(updated_group){
      res.json(updated_group);
    });
  });
  
});

router.get('/accept', function(req, res) {

  Group.acceptRequest(req,function(groups){
  res.json(groups);

 })
});

router.get('/reject', function(req, res) {

  Group.rejectRequest(req,function(groups){
  res.json(groups);

 })
});

router.get('/leave', function(req, res) {

  Group.leaveGroup(req,function(groups){
  res.json(groups);

 })
});



module.exports = router;
