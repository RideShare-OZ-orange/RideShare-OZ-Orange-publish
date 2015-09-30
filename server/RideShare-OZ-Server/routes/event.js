var express = require('express');
var router = express.Router();
var Event = require('../models/Event.js');

router.get('/', function(req, res, next) {

  Group.find({}, function(err, groups){
    res.json(groups);
  });

});

router.get('/create', function(req, res, next) {

Event.createEvent(req, function(event){
		 res.json(event);
	});

});

module.exports = router;