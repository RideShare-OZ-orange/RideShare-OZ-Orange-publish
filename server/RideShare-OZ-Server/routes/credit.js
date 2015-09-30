var express = require('express');
var router = express.Router();
var Credit = require('../models/Credit.js');





router.get('/getall', function(req, res, next) {
 Credit.getAllCredit(function(credit){
 	res.json(credit);
 });
});

router.get('/rate', function(req, res) {
 Credit.addRate(req,function(rate){
 	res.json(rate);
 });
});

router.get('/avg', function(req, res) {
	Credit.avgRate(req,function(result){
		res.json(result);
	});

});

module.exports = router;
