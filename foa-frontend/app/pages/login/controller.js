//SS.controller('LoginController', function ($scope, $http, $localStorage) {
BJD.controller('LoginController', function ($scope, $http, $rootScope, $localStorage, $location, $cookieStore, $translate, $timeout) {

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

    $rootScope.pageTitle = "Login Page";
    $rootScope.hide = true;

    $scope.submit = function () {

        var username = $scope.username;
        var password = $scope.password;

        $scope.isProcessing = true;

        var data = {
            userName: username,
            password: password
        };

        var headers = {
            authorization: "Basic " + btoa(username + ":" + password),
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_USER_LOGIN,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                $scope.isProcessing = false;
                var responseData = response.data.data;
                if (response.data.message == "success") {
                    // console.log(responseData);
                    $localStorage.token = responseData.token;
                    $localStorage.timed = new Date();
                    $localStorage.isLoggedIn = 1;
                    $localStorage.userName = $scope.username;
                    $localStorage.auth = btoa($scope.username + ":" + $scope.password);
                    $localStorage.role = responseData.role;
                    $localStorage.organizationId = responseData.organizationId;

                    //Initial setup of role
                    $rootScope.role = responseData.role;

                    $timeout(function () {
                        $scope.getUser();
                    });

                    $location.path('/dashboard');
                } else {
                    $scope.messageType = "alert alert-danger";
                    $scope.messageText = $translate.instant('LOGIN_ERROR');
                }

            }, function errorCallback(response) {
                $scope.messageType = "alert alert-danger";
                $scope.messageText = $translate.instant('LOGIN_ERROR');
                $scope.isProcessing = false;
            });
    };


    $scope.getUser = function () {

        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_GET_USER,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                var responseData = response.data.data;
                $localStorage.pic = responseData.pic;
            }, function errorCallback(response) {
                console.log(response);
            });
    };


});