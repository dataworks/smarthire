var esservice = require("./elasticsearch.js");
var config = require("./config.js");

/*
 *lists applicants based on type
 */
exports.listApplicants = function(req, res, type) {
  var query = '*';
  if (type !== 'new') {
    query = 'type: ' + type;
  }

  console.log(req.query);
  if (req.query.query) {// != null || req.query.query.length < 1) {
    console.log("Querying");
    esservice.query(config.applicants, req, res, req.query.query, null);
  }
  else {
    esservice.query(config.labels, {query: {size: 5000}}, res, query, function(res, hits) {
      var labelQuery = buildQuery(res, hits, type);
      esservice.query(config.applicants, req, res, labelQuery, null);
    },function (error, response) {
      console.log(error);
    });  
  }

}

/*
 * Builds the query string for ES
 */
function buildQuery(res, hits, type) {
  if (hits && hits.length > 0) {
    var ids = hits.map(function(hit) {
      return hit.id;
    });
    //same query logic * or NOT id ()
    if (ids && ids.length > 0) {
      if (type === 'new') {
        return "NOT id:(" + ids.join(" ") + ")";
      } else if (type === 'favorite' || type === 'archive' || type === 'review') {
        return "id:(" + ids.join(" ") + ")";
      }
    }
  }
  return type === 'new' ? '*' : ' ';
}

// $(function() {

//   // We can attach the `fileselect` event to all file inputs on the page
//   $(document).on('change', ':file', function() {
//     var input = $(this),
//         numFiles = input.get(0).files ? input.get(0).files.length : 1,
//         label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
//     input.trigger('fileselect', [numFiles, label]);
//   });

//   // We can watch for our custom `fileselect` event like this
//   $(document).ready( function() {
//       $(':file').on('fileselect', function(event, numFiles, label) {

//           var input = $(this).parents('.input-group').find(':text'),
//               log = numFiles > 1 ? numFiles + ' files selected' : label;

//           if( input.length ) {
//               input.val(log);
//           } else {
//               if( log ) alert(log);
//           }

//       });
//   });