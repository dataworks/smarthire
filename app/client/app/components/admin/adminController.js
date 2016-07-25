applicantControllers.controller('AdminCtrl', ['$scope', 'Admin',
  function($scope, Admin) {

  	$scope.settings = Admin.query(function(settings) {
    	var location = settings.rows[0].jobLocation.reston.values[0];
    	console.log(location)
	});
  	

  	var vm = this;
  	// The model object that we reference
    // on the  element in index.html
    vm.settings = {};
    
    // An array of our form fields with configuration
    // and options set. We make reference to this in
    // the 'fields' attribute on the  element
    vm.settingsFields = [
        {
            key: 'job_location',
            type: 'input',
            templateOptions: {
                type: 'text',
                label: 'Location',
                placeholder: 'Reston, VA',
                required: false
            }
        },
        {
            key: 'resume_length',
            type: 'input',
            templateOptions: {
                type: 'text',
                label: 'Resume length',
                placeholder: '2000',
                required: false
            }
        },
        {
            key: 'experience',
            type: 'input',
            templateOptions: {
                type: 'text',
                label: 'Positions',
                placeholder: 'engineer, developer, software architect',
                required: false
            }
        },
        {
            key: 'experience',
            type: 'input',
            templateOptions: {
                type: 'text',
                label: 'Degrees',
                placeholder: 'tech, computer science, programming',
                required: false
            }
        },
        {
            key: 'contact',
            type: 'input',
            templateOptions: {
                type: 'text',
                label: 'Contact Information',
                placeholder: 'Github, email, LinkedIn, phone',
                required: false
            }
        },
        {
            key: 'key_words',
            type: 'input',
            templateOptions: {
                type: 'text',
                label: 'Tags',
                placeholder: 'ETL, Big Data, Languages',
                required: false
            }
        },
        {
            key: 'relevance',
            type: 'input',
            templateOptions: {
                type: 'text',
                label: 'Relevance',
                placeholder: 'true, false',
                required: false
            }
        },
    ];
    

  }
]);
