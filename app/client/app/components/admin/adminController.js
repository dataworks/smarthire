applicantControllers.controller('AdminCtrl', ['$scope', 'Admin',
  function($scope, Admin) {
    var vm = this;

    $scope.settings = Admin.query(function(settings) {
      vm.settings = {
        job_location_enable: $scope.settings.rows[0].jobLocation.reston.enabled,
        job_location: $scope.settings.rows[0].jobLocation.reston.values[0].location,
        job_location_dist: $scope.settings.rows[0].jobLocation.reston.values[0].maxDistance,
        resume_length: $scope.settings.rows[0].resumeLength.standardLength.enabled,
        relevance: $scope.settings.rows[0].relevance.relevance.enabled,
        experience_enable: $scope.settings.rows[0].experience.techExperience.enabled,
        experience: $scope.settings.rows[0].experience.techExperience.values[0].positions,
        experience_deg: $scope.settings.rows[0].experience.techExperience.values[0].degrees,
        email: $scope.settings.rows[0].contactInfo.allInfo.enabled,
        phone: $scope.settings.rows[0].contactInfo.allInfo.enabled,
        urls: $scope.settings.rows[0].contactInfo.allInfo.enabled,
        indeed: $scope.settings.rows[0].contactInfo.allInfo.enabled,
        linkedin: $scope.settings.rows[0].contactInfo.allInfo.enabled,
        github: $scope.settings.rows[0].contactInfo.allInfo.enabled,
        contact_enable: $scope.settings.rows[0].contactInfo.allInfo.enabled,
        etl_enable: $scope.settings.rows[0].keywords.etl.enabled,
        webApp_enable: $scope.settings.rows[0].keywords.webApp.enabled,
        languages_enable: $scope.settings.rows[0].keywords.languages.enabled,
        dbms_enable: $scope.settings.rows[0].keywords.dbms.enabled,
        mobile_enable: $scope.settings.rows[0].keywords.mobile.enabled,
        bigData_enable: $scope.settings.rows[0].keywords.bigData.enabled,
        etl: $scope.settings.rows[0].keywords.etl.values,
        webApp: $scope.settings.rows[0].keywords.webApp.values,
        languages: $scope.settings.rows[0].keywords.languages.values,
        dbms: $scope.settings.rows[0].keywords.dbms.values,
        mobile: $scope.settings.rows[0].keywords.mobile.values,
        bigData: $scope.settings.rows[0].keywords.bigData.values,

      }
    });

  	// The model object that we reference
    // on the  element in index.html
    // vm.settings = {};
    
    // An array of our form fields with configuration
    // and options set. We make reference to this in
    // the 'fields' attribute on the  element
    vm.settingsFields = [
    {
      key: 'job_location_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Location',
        required: false
      }
    },
    {
      key: 'job_location',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'City, State',
        // placeholder: 'Reston, VA',
        required: false
      },
      hideExpression: '!model.job_location_enable'
    },
    {
      key: 'job_location_dist',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Max Distance',
        // placeholder: 'Reston, VA',
        required: false
      },
      hideExpression: '!model.job_location_enable'
    },
    {
      key: 'resume_length',
      type: 'checkbox',
      templateOptions: {
        label: 'Resume length',
        required: false
      }
    },
    {
      key: 'experience_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Experience',
        required: false
      }
    },
    {
      key: 'experience',
      type: 'textarea',
      templateOptions: {
        label: 'Positions',
        required: false,
      },
      hideExpression: '!model.experience_enable'
    },
    {
      key: 'experience_deg',
      type: 'textarea',
      templateOptions: {
        label: 'Degrees',
        placeholder: 'tech, computer science, programming',
        required: false,
      },
      hideExpression: '!model.experience_enable'
    },
    {
      key: 'contact_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Contact Information',
        required: false
      }
    },
    {
      key: 'email',
      type: 'checkbox',
      templateOptions: {
        label: 'Email',
        required: false,
        value: 'email'
      },
      hideExpression: '!model.contact_enable'
    },
    {
      key: 'phone',
      type: 'checkbox',
      templateOptions: {
        label: 'Phone',
        required: false,
        value: 'phone'
      },
      hideExpression: '!model.contact_enable'
    },
    {
      key: 'urls',
      type: 'checkbox',
      templateOptions: {
        label: 'URLs',
        required: false,
        value: 'urls'
      },
      hideExpression: '!model.contact_enable'
    },
    {
      key: 'linkedin',
      type: 'checkbox',
      templateOptions: {
        label: 'LinkedIn',
        required: false,
        value: 'linkedin'
      },
      hideExpression: '!model.contact_enable'
    },
    {
      key: 'github',
      type: 'checkbox',
      templateOptions: {
        label: 'GitHub',
        required: false,
        value: 'github'
      },
      hideExpression: '!model.contact_enable'
    },
    {
      key: 'indeed',
      type: 'checkbox',
      templateOptions: {
        label: 'Indeed',
        required: false,
        value: 'indeed'
      },
      hideExpression: '!model.contact_enable'
    },
    {
      key: 'etl_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Key Words',
        required: false
      }
    },
    {
      key: 'etl',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'ETL',
        required: false
      },
      hideExpression: '!model.etl_enable'
    },
    {
      key: 'webApp_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Key Words',
        required: false
      }
    },
    {
      key: 'webApp',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Web App',
        required: false
      },
      hideExpression: '!model.webApp_enable'
    },
    {
      key: 'languages_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Key Words',
        required: false
      }
    },
    {
      key: 'languages',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Languages',
        required: false
      },
      hideExpression: '!model.languages_enable'
    },
    {
      key: 'dbms_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Key Words',
        required: false
      }
    },
    {
      key: 'dbms',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Database',
        required: false
      },
      hideExpression: '!model.dbms_enable'
    },
    {
      key: 'mobile_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Key Words',
        required: false
      }
    },
    {
      key: 'mobile',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Mobile',
        required: false
      },
      hideExpression: '!model.mobile_enable'
    },
    {
      key: 'bigData_enable',
      type: 'checkbox',
      templateOptions: {
        label: 'Key Words',
        required: false
      }
    },
    {
      key: 'bigData',
      type: 'input',
      templateOptions: {
        type: 'text',
        label: 'Big Data',
        required: false
      },
      hideExpression: '!model.bigData_enable'
    },
    {
      key: 'relevance',
      type: 'checkbox',
      templateOptions: {
        label: 'Relevance',
        required: false,
      }
    },
    ];
  }
  ]);
