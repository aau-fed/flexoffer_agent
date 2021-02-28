BJD.controller('ForgotPasswordController', function ($rootScope, $scope, $http, $location, $localStorage, $timeout, $translate) {

    $scope.lang = $translate.use();
    if ($localStorage.selectedLanguage) {
        $scope.lang = $localStorage.selectedLanguage
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
    $rootScope.pageTitle = "Forgot Password Page";

    $scope.email = "";

    $scope.submit = function () {
        var email = $scope.email;

        var headers = {
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_USER_FORGOT_PASSWORD + "?email=" + email,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {

                if (response.data.status == "OK") {

                    $scope.messageType = "alert alert-success";
                    $scope.messageText = response.data.message;

                    $timeout(function () {
                        $location.path("/login");
                    }, 2000);

                } else {
                    $scope.messageType = "alert alert-danger";
                    $scope.messageText = $translate.instant('FORGOT_PASSWORD_ERROR');
                }

            }, function errorCallback(response) {
                $scope.messageType = "alert alert-danger";
                $scope.messageText = $translate.instant('FORGOT_PASSWORD_ERROR');
                console.log(response);
            });
    };
});