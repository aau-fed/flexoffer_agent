BJD.controller('ContractController', function ($rootScope, $location, $localStorage, $scope, $http, $routeParams) {
    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    if ($localStorage.userName != $routeParams.userName && $rootScope.role != 'ROLE_ADMIN') {
        $location.path('/dashboard');
        return;
    }


    $rootScope.pageTitle = "Profile Page";
    $rootScope.hide = false;

    $scope.contract = {};

    $scope.getcontract = function () {

        var headers = {
            // authorization: "Basic " + $localStorage.auth,
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: $localStorage.userName == $routeParams.userName ? API_GET_CONTRACT : API_GET_USER_CONTRACT_BY_ORGANIZATION + '/' + $routeParams.userName,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {

                if (response.data.data) {
                    $scope.contract = response.data.data;
                }

            }, function errorCallback(response) {
                console.log(response);
            });
    };

    $scope.getcontract();

});