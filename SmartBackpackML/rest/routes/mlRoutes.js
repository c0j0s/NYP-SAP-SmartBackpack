'use strict';
module.exports = function(app) {
  var ml = require('../controllers/mlController');

  // todoList Routes
  app.route('/test')
    .get(ml.test);


//   app.route('/tasks/:taskId')
//     .get(ml.read_a_task)
//     .put(ml.update_a_task)
//     .delete(ml.delete_a_task);
};