var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var Ride = require('../models/Ride.js');
var User = require('../models/User.js');

var CreditSchema = new Schema({
  type : String,
  receiver: String,//{type: Schema.Types.ObjectId, ref: 'User' },
  rater : {type: Schema.Types.ObjectId, ref: 'User' },
  rate : Number,
  ride_id: {type: Schema.Types.ObjectId, ref: 'Ride' },
  updateDate:{ type: Date, default: Date.now },
  comment: String,
});


CreditSchema.statics.getAllCredit = function(callback){
  this.find({}, function(err, credit){
    callback(credit);
  });
};


CreditSchema.statics.addRate = function(req,callback){
	var Credit = mongoose.model('Credit');
  	var credit = new Credit();
  		if(req.query.type==="driver"||req.query.type==="passenger"){
  			
			credit.ride_id=req.query.ride_id;
			credit.rater=req.query.rater_id;
			credit.receiver=req.query.receiver_id;
			credit.comment=req.query.comment;
			credit.rate=req.query.rate;
			credit.type=req.query.type;
			credit.save();
			if(req.query.type==="passenger"){
				this.aggregate([
					{	
						$match:{$and:[{type:"passenger"},{receiver:req.query.receiver_id}]}
					},
					{
						$group:{
							_id:"$receiver",
							avgRate:{$sum:"$rate"},
							count:{$sum:1}
						}
					}
				],function(err,result){
					User.findById(req.query.receiver_id,function(err,user){
						user.passenger_rate=(Number(result[0].avgRate)+Number(req.query.rate))/(Number(result[0].count)+1);
						user.save();
					})
				});
				
			}else if(req.query.type==="driver"){
				this.aggregate([
					{	
						$match:{$and:[{type:"driver"},{receiver:req.query.receiver_id}]}
					},
					{
						$group:{
							_id:"$receiver",
							avgRate:{$sum:"$rate"},
							count:{$sum:1}
						}
					}
				],function(err,result){
					User.findById(req.query.receiver_id,function(err,user){
						user.driver_rate=(Number(result[0].avgRate)+Number(req.query.rate))/(Number(result[0].count)+1);
						user.save();
					})
				});	
			} 

			callback("save success!");
	}else{
		callback("type is wrong!")
	}


};


CreditSchema.statics.avgRate = function(req,callback){
  
		this.aggregate([
			{	$match:{$and:[{type:"driver"},{receiver:req.query.receiver_id}]}

			},
			{
				$group:{
					_id:"$receiver",
					avgRate:{$avg:"$rate"},
					count:{$sum:1}
				}
			}
		],function(err,result){
			console.log(result[0].avgRate);
			callback(result);
		})
};


module.exports = mongoose.model('Credit', CreditSchema);