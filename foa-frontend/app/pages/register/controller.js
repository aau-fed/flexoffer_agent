BJD.controller('RegisterController', function ($rootScope, $scope, $http, $location, $localStorage, $timeout, $translate, Notification) {

    function validateEmail(email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }

    $scope.lang = $translate.use();
    if ($localStorage.selectedLanguage) {
        $scope.lang = $localStorage.selectedLanguage;
        $translate.use($localStorage.selectedLanguage);
    } else {
        $localStorage.selectedLanguage = $scope.lang;
    }
    $scope.changeLanguage = function () {
        $translate.use($scope.lang);
        $localStorage.selectedLanguage = $scope.lang;
    };


    //Check if there is session id
    if ($localStorage.isLoggedIn) {
        $location.path('/dashboard');
    }

    $rootScope.hide = true;
    $rootScope.pageTitle = "Register Page";


    $scope.formData = {};

    $scope.countryCodes = COUNTRY_CODES;

    $scope.notificationDelay = 10000;

    $scope.submit = function () {
        var username = $scope.formData.username;
        var password = $scope.formData.password;
        var rePassword = $scope.formData.rePassword;
        var tpLinkUserName = $scope.formData.tpLinkUserName;
        var tpLinkPassword = $scope.formData.tpLinkPassword;
        var email = tpLinkUserName ? tpLinkUserName : username + DEFAULT_EMAIL;

        if (validateEmail(username) || username.includes("@")) {
            Notification.error({
                message: $translate.instant('SIGNUP_USERNAME_INVALID'),
                delay: 20000
            });
            return;
        }

        if (!password || password.length == 0) {
            Notification.error({
                message: $translate.instant('SIGNUP_PASSWORD_MISSING'),
                delay: 20000
            });
            return;
        }

        if (!rePassword || rePassword.length === 0 || rePassword !== password) {
            Notification.error({
                message: $translate.instant('SIGNUP_PASSWORD_MISMATCH'),
                delay: 20000
            });
            return;
        }

        var organizationId = $scope.formData.organizationId;
        if (!organizationId || organizationId == null || organizationId == 0 || organizationId == "") {
            Notification.error({
                message: $translate.instant('SIGNUP_ORG_MISSING'),
                delay: 20000
            });
            return;
        }

        var data = {
            userName: username,
            password: password,
            tpLinkUserName: tpLinkUserName,
            tpLinkPassword: tpLinkPassword,
            organizationId: organizationId,
            email: email,
        };


        userAddress = {};
        if ($scope.formData.phone) {
            var phone = $scope.countryCodes[organizationId] + $scope.formData.phone;
            userAddress = {
                phone: phone
            };
        }
        data.userAddress = userAddress;

        var headers = {
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_USER_REGISTER,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {

                if (response.data.status == "OK") {

                    Notification.success($translate.instant('SIGNUP_SUCCESS'));

                    $timeout(function () {
                        $location.path("/login");
                    }, 2000);

                } else {
                    Notification.error({
                        message: $translate.instant('SIGNUP_ERROR'),
                        delay: $scope.notificationDelay
                    });
                }

            }, function errorCallback(response) {
                Notification.error({
                    message: response.data.message,
                    delay: $scope.notificationDelay
                });
                console.log(response);
            });
    };



    $scope.steps = [
        'Login Credentials',
        'TP-Link Kasa Account Info',
        'User Details'
    ];
    $scope.selection = $scope.steps[0];

    $scope.getCurrentStepIndex = function () {
        // Get the index of the current step given selection
        return _.indexOf($scope.steps, $scope.selection);
    };

    // Go to a defined step index
    $scope.goToStep = function (index) {
        if (!_.isUndefined($scope.steps[index])) {
            $scope.selection = $scope.steps[index];
        }
    };

    $scope.hasNextStep = function () {
        var stepIndex = $scope.getCurrentStepIndex();
        var nextStep = stepIndex + 1;
        // Return true if there is a next step, false if not
        return !_.isUndefined($scope.steps[nextStep]);
    };

    $scope.hasPreviousStep = function () {
        var stepIndex = $scope.getCurrentStepIndex();
        var previousStep = stepIndex - 1;
        // Return true if there is a next step, false if not
        return !_.isUndefined($scope.steps[previousStep]);
    };

    $scope.incrementStep = function () {
        if ($scope.hasNextStep()) {
            var stepIndex = $scope.getCurrentStepIndex();
            var nextStep = stepIndex + 1;
            $scope.selection = $scope.steps[nextStep];
        }
    };

    $scope.decrementStep = function () {
        if ($scope.hasPreviousStep()) {
            var stepIndex = $scope.getCurrentStepIndex();
            var previousStep = stepIndex - 1;
            $scope.selection = $scope.steps[previousStep];
        }
    };

    $scope.verifyCurrentStep = function () {
        if ($scope.getCurrentStepIndex() == 0) {
            // Increment step if username available
            $scope.incrementStepIfUserNameAvailable();
        } else if ($scope.getCurrentStepIndex() == 1) {
            // Increment step if tplink account available
            $scope.incrementStepIfTpLinkAccountAvailable();
        } else if ($scope.getCurrentStepIndex() == 2) {
            // Call register function to complete registration
            $scope.submit();
        }
    };

    $scope.incrementStepIfUserNameAvailable = function () {

        var username = $scope.formData.username;
        var password = $scope.formData.password;
        var rePassword = $scope.formData.rePassword;

        if (username == null || username == "") {
            Notification.error({
                message: $translate.instant('SIGNUP_USERNAME_MISSING'),
                delay: $scope.notificationDelay
            });
            //Notification.error({message: "Must provide a valid email.", delay:$scope.notificationDelay});
            return;
        }

        if (validateEmail(username) || username.includes("@")) {
            Notification.error({
                message: $translate.instant('SIGNUP_USERNAME_INVALID'),
                delay: 20000
            });
            return;
        }

        if (!password || password.length == 0) {
            Notification.error({
                message: $translate.instant('SIGNUP_PASSWORD_MISSING'),
                delay: 20000
            });
            return;
        }

        if (!rePassword || rePassword.length === 0 || rePassword !== password) {
            Notification.error({
                message: $translate.instant('SIGNUP_PASSWORD_MISMATCH'),
                delay: $scope.notificationDelay
            });
            return;
        }

        var data = {
            userName: username,
            password: password
        };

        var headers = {
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_USER_USERNAME_EXISTS,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {

                if (response.data.status == "OK") {
                    $scope.incrementStep();
                } else {
                    Notification.error({
                        message: $translate.instant('SIGNUP_USERNAME_TAKEN'),
                        delay: $scope.notificationDelay
                    });
                }

            }, function errorCallback(response) {
                if (response.status == 409) {
                    Notification.error({
                        message: $translate.instant('SIGNUP_USERNAME_TAKEN'),
                        delay: $scope.notificationDelay
                    });
                } else {
                    Notification.error({
                        message: $translate.instant('INTERNAL_SERVER_ERROR'),
                        delay: $scope.notificationDelay
                    });
                }
            });

    };

    $scope.incrementStepIfTpLinkAccountAvailable = function () {
        var tpLinkUserName = $scope.formData.tpLinkUserName;
        var password = $scope.formData.tpLinkPassword;
        var rePassword = $scope.formData.reTpLinkPassword;

        if (tpLinkUserName == null || tpLinkUserName == "") {
            Notification.error({
                message: $translate.instant('SIGNUP_TPLINK_USERNAME_MISSING'),
                delay: $scope.notificationDelay
            });
            return;
        }

        if (!validateEmail(tpLinkUserName)) {
            Notification.error({
                message: "'" + tpLinkUserName + "'" +  $translate.instant('USER_INVALID_EMAIL'),
                delay: 10000
            });
            return;
        }

        if (!password || password.length == 0) {
            Notification.error({
                message: $translate.instant('SIGNUP_PASSWORD_MISSING'),
                delay: 20000
            });
            return;
        }

        if (!rePassword || rePassword.length === 0 || rePassword !== password) {
            Notification.error({
                message: $translate.instant('SIGNUP_PASSWORD_MISMATCH'),
                delay: $scope.notificationDelay
            });
            return;
        }


        var data = {
            tpLinkUserName: tpLinkUserName
        };

        var headers = {
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_USER_TPLINK_ACCOUNT_EXISTS,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {

                if (response.data.status == "OK") {
                    $scope.incrementStep();
                } else {
                    Notification.error({
                        message: $translate.instant('SIGNUP_TPLINK_USERNAME_TAKEN'),
                        delay: $scope.notificationDelay
                    });
                }

            }, function errorCallback(response) {
                if (response.status == 409) {
                    Notification.error({
                        message: $translate.instant('SIGNUP_TPLINK_USERNAME_TAKEN'),
                        delay: $scope.notificationDelay
                    });
                } else {
                    Notification.error({
                        message: $translate.instant('INTERNAL_SERVER_ERROR'),
                        delay: $scope.notificationDelay
                    });
                }
            });
    };


    $scope.skipTpLinkAccount = function () {
        $scope.formData.tpLinkUserName = "";
        $scope.formData.tpLinkPassword = "";
        $scope.formData.reTpLinkPassword = "";
        $scope.incrementStep();
    };
});