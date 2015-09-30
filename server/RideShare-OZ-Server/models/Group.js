var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var User = require('../models/User.js');

var GroupsSchema = new Schema({
  groupname: String,
  introduction: String,
  location: [Number],
  adminID: [{type: Schema.Types.ObjectId, ref: 'User' }],
  members: [{type: Schema.Types.ObjectId, ref: 'User' }],
  requests: [{
    user:{type: Schema.Types.ObjectId, ref: 'User' },
    requestDate:{ type: Date, default: Date.now }
  }],
});


GroupsSchema.statics.createGroup= function(req,callback) {
  var Group = mongoose.model('Group');
  var groups = new Group();
  groups.groupname = req.query.name;
  var lon=req.query.g_lon;
  var lat=req.query.g_lat;
  groups.location=[lon,lat];
  groups.introduction=req.query.introduction;
  User.findById(req.query.admin_id, function(err, user){
    groups.adminID=user;
    groups.save(function(err, doc){
      if (err) {
        console.log(err);
      }
      callback(doc);
      });
    });
  };


  GroupsSchema.methods.addRequest= function(user_id,callback){

  this.requests.push({'user':user_id});
  this.save(function(err, doc){
    callback(doc);
  });
};

  GroupsSchema.statics.acceptRequest= function(req,callback){

 //add use to passenger
  this.findById(req.query.group_id,function(err,doc){
    
    doc.members.push(req.query.user_id);
    doc.save();
  });
  User.findById(req.query.user_id,function(err,user){
  	user.groups.push(req.query.group_id);
  	user.save();
  });
  //delete the user in requests
  
   this.findByIdAndUpdate(req.query.group_id,{$pull:{'requests':{'user':req.query.user_id}}},function(err,doc){
    callback(doc);
  });

};


GroupsSchema.statics.rejectRequest= function(req,callback){

this.findByIdAndUpdate(req.query.group_id,{$pull:{'requests':{'user':req.query.user_id}}},function(err,doc){
  console.log(doc);
    callback(doc);
  });
};


GroupsSchema.statics.leaveGroup= function(req,callback){

	User.findByIdAndUpdate(req.query.user_id, {$pull:{'groups':req.query.group_id}},function(err,groups){
		console.log(groups);
	});
	this.findByIdAndUpdate(req.query.group_id,{$pull:{'members':req.query.user_id}},function(err,groups){
    console.log(groups);
    callback(groups);
  });

};


module.exports = mongoose.model('Group', GroupsSchema);
