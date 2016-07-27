applicantControllers.controller('AdminCtrl', ['$scope', 'Admin',
  function($scope, Admin) {
    var vm = this;

    $scope.settings = Admin.query(function(settings) {
      vm.settings = {
        job_location: $scope.settings.rows[0].jobLocation.reston.values[0].location,
        resume_length: $scope.settings.rows[0].resumeLength.standardLength.enabled,
        relevance: $scope.settings.rows[0].relevance.relevance.enabled,
        experience: $scope.settings.rows[0].experience.techExperience.values[0].positions,

      }
      console.log($scope.settings.rows[0].experience.techExperience.values[0].positions);
    });

    // console.log($scope.jobLocation);



  	// The model object that we reference
    // on the  element in index.html
    // vm.settings = {};
    
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
        // placeholder: 'Reston, VA',
        required: false
      }
    },
    {
      key: 'resume_length',
      type: 'radio',
      templateOptions: {
        label: 'Resume length',
        required: false,
        options: [
          {name: 'True', value: true},
          {name: 'False', value: false}
        ]
      }
    },
    {
      key: 'experience',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Positions',
        required: false
      }
    },
    {
      key: 'experience_deg',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Degrees',
        placeholder: 'tech, computer science, programming',
        required: false
      }
    },
    {
      template:'<b> Contact Information </b>'
    },
    {
      key: 'email',
      type: 'checkbox',
      templateOptions: {
        label: 'Email',
        required: false,
        value: 'email'
      }
    },
    {
      key: 'phone',
      type: 'checkbox',
      templateOptions: {
        label: 'Phone',
        required: false,
        value: 'phone'
      }
    },
    {
      key: 'urls',
      type: 'checkbox',
      templateOptions: {
        label: 'URLs',
        required: false,
        value: 'urls'
      }
    },
    {
      key: 'linkedin',
      type: 'checkbox',
      templateOptions: {
        label: 'LinkedIn',
        required: false,
        value: 'linkedin'
      }
    },
    {
      key: 'github',
      type: 'checkbox',
      templateOptions: {
        label: 'GitHub',
        required: false,
        value: 'github'
      }
    },
    {
      key: 'indeed',
      type: 'checkbox',
      templateOptions: {
        label: 'Indeed',
        required: false,
        value: 'indeed'
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
      type: 'radio',
      templateOptions: {
        label: 'Relevance',
        required: false,
        options: [
          {name: 'True', value: true},
          {name: 'False', value: false}
        ]
      }
    },
    ];
    

  }
  ]);
