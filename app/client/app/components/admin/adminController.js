applicantControllers.controller('AdminCtrl', ['$scope', 'Admin',
  function($scope, Admin) {
    var vm = this;

    $scope.settings = Admin.query(function(settings) {
      vm.settings = {
        job_location: '' + $scope.settings.rows[0].jobLocation.reston.values[0]
      }
      console.log($scope.settings.rows[0].jobLocation.reston.values[0]);
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
          {name: 'True', value: 'true'},
          {name: 'False', value: 'false'}
        ]
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
      // all lower case
      key: 'contact',
      type: 'select',
      templateOptions: {
        type: 'checkbox',
        label: 'Contact Information',
        required: false,
        options: [
          {name: 'Email', value: 'email'},
          {name: 'Phone', value: 'phone'},
          {name: 'LinkedIn', value: 'linkedin'},
          {name: 'GitHub', value: 'github'},
          {name: 'URLs', value: 'urls'},
          {name: 'Indeed', value: 'indeed'}
        ]
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
          {name: 'True', value: 'true'},
          {name: 'False', value: 'false'}
        ]
      }
    },
    ];
    

  }
  ]);
