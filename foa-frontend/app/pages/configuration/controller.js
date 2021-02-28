BJD.controller('ConfigController', function ($rootScope, $scope, $http, $location, $localStorage, $translate, Notification, $window) {
    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    $rootScope.pageTitle = "Configuration";
    $rootScope.hide = false;

    $scope.simDeviceModels = SIM_DEVICE_MODELS;
    //$scope.simDevices = ["CONFIG_SIMULATOR_FREEZER", "CONFIG_SIMULATOR_AIR_CONDITIONER"];
    $scope.simDevices = ["Freezer", "AirConditioner"];
    $scope.controlStatus = ["CONFIG_CONTROL_ENABLED", "CONFIG_CONTROL_DISABLED"];
    $scope.orgStatus = "";
    $scope.orgPoolControl = "";
    $scope.poolDeviceCoolingPeriod = 180;
    $scope.fogStatus = "Offline";

    $scope.admins = ALL_ADMINS;
    $scope.sysAdmins = SYS_ADMINS;
    $scope.allAdmins = ALL_ADMINS + SYS_ADMINS;
    $scope.userName = $localStorage.userName;
    $scope.deviceType = null;

    $scope.getOrg = function () {
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_GET_ORG_CONTROL_STATUS,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                var org = response.data.data;
                $scope.orgStatus = org.directControlMode == "Active" ? $scope.controlStatus[0] : $scope.controlStatus[1];
                $scope.orgPoolControl = org.poolBasedControl == true ? $scope.controlStatus[0] : $scope.controlStatus[1];
                $scope.poolDeviceCoolingPeriod = org.poolDeviceCoolingPeriod;
            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };

    $scope.setOrgControl = function (status) {
        if (!$window.confirm($translate.instant('GENERAL_CONFIRMATION'))) {
            return;
        }
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_SET_ORG_CONTROL + "/" + status,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {
                if (response.data.status == "OK") {
                    Notification.success($translate.instant("OPERATION_SUCCESS"));
                    $scope.getOrg();
                }

            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };

    $scope.setOrgPoolControl = function (status) {
        if (!$window.confirm($translate.instant('GENERAL_CONFIRMATION'))) {
            return;
        }
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_SET_ORG_POOL_CONTROL + "/" + status,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {
                if (response.data.status == "OK") {
                    Notification.success($translate.instant("OPERATION_SUCCESS"));
                    $scope.getOrg();
                }

            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };


    $scope.setPoolDeviceCoolingPeriod = function (newPeriod) {
        if (!$window.confirm($translate.instant('GENERAL_CONFIRMATION'))) {
            return;
        }
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_SET_ORG_POOL_DEVICE_COOLING_PERIOD + "/" + newPeriod,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {
                if (response.data.status == "OK") {
                    Notification.success($translate.instant("OPERATION_SUCCESS"));
                    $scope.getOrg();
                }

            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };


    $scope.getFOGStatus = function () {

        if ($scope.sysAdmins.indexOf($scope.userName) == -1) {
            return;
        }

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_GET_FOG_STATUS,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                $scope.fogStatus = response.data.data.start == 1 ? $scope.controlStatus[0] : $scope.controlStatus[1];
            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };


    $scope.startFOG = function () {

        if (!$window.confirm($translate.instant('GENERAL_CONFIRMATION'))) {
            return;
        }

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_START_FOG,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {
                if (response.data.status == "OK") {
                    Notification.success($translate.instant("OPERATION_SUCCESS"));
                } else {
                    Notification.info($translate.instant("OPERATION_SUCCESS"));
                }
                $scope.getFOGStatus();
            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };

    $scope.stopFOG = function () {

        if (!$window.confirm($translate.instant('GENERAL_CONFIRMATION'))) {
            return;
        }

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_STOP_FOG,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {
                if (response.data.status == "OK") {
                    Notification.success($translate.instant("OPERATION_SUCCESS"));
                } else {
                    Notification.info($translate.instant("OPERATION_SUCCESS"));
                }
                $scope.getFOGStatus();
            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };

    $scope.clearFOs = function () {

        if (!$window.confirm($translate.instant('GENERAL_CONFIRMATION'))) {
            return;
        }

        var foDate = moment().format('YYYY-MM-DD');
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_CONFIG_CLEAR_ORG_FOS + "/" + foDate,
                method: 'DELETE',
                headers: headers
            })
            .then(function (response) {
                console.log(response);
                if (response.data.status == "OK") {
                    Notification.success(response.data.message);
                }
            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
            });
    };

    $scope.addSimulatedLoad = function () {
        var data = $scope.simDeviceModels[$scope.deviceType];
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_START_SIMULATED_DEVICE,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                if (response.status == 200) {
                    Notification.success($translate.instant("OPERATION_SUCCESS"));
                } else {
                    Notification.error($translate.instant("OPERATION_FAILURE"));
                }
            }, function errorCallback(response) {
                Notification.error($translate.instant("OPERATION_FAILURE"));
                console.log(response);
            });
    };

    $("[type='number']").keypress(function (evt) {
        evt.preventDefault();
    });

    $scope.getOrg();
    $scope.getFOGStatus();
});