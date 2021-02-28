BJD.controller('DeviceListController', function ($rootScope, $localStorage, $location, $scope, $http, $timeout, $translate, Notification) {
    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    $rootScope.pageTitle = "Devices";
    $rootScope.hide = false; /*Header and menu*/
    $scope.devices = {};
    $scope.hidex = true;

    $scope.groups = {};
    $scope.deviceSummaryList = [];
    $scope.pageSize = 10;
    $scope.sortKey = 'deviceId';

    // sort device list
    $scope.sort = function (keyName) {
        $scope.sortKey = keyName; //set the sortKey to the param passed
        $scope.reverse = !$scope.reverse; //if true make it false and vice versa
    };

    //Get device summary list
    $scope.getDeviceSummaryList = function (overLay) {

        /*Loading*/
        if (overLay) {
            $("body").LoadingOverlay("show");
        }

        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
                url: API_GET_DEVICE_SUMMARY_LIST,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {

                var responseData = response.data.data;

                if (responseData.length) {

                    for (var i in responseData) {
                        var deviceId = responseData[i].deviceId;
                        $scope.deviceSummaryList[i] = responseData[i];
                        $scope.deviceSummaryList[i].class = 'C-' + deviceId.split('@')[1];

                        if (responseData[i].deviceType && DEVICE_ICONS[responseData[i].deviceType]) {

                            $scope.deviceSummaryList[i].icon = DEVICE_ICONS[responseData[i].deviceType];

                        } else {
                            $scope.deviceSummaryList[i].icon = "icon ion-ios-circle-filled";
                        }
                        if (responseData[i].deviceState == "Operating") {

                            $scope.deviceSummaryList[i].icon += " on";

                        }
                    }


                    var userName = $localStorage.userName;
                    if (ALL_ADMINS.indexOf(userName) > -1 || SYS_ADMINS.indexOf(userName) > -1) {
                        $scope.sortKey = 'userName';
                    }

                    if (SWISS_USERS.indexOf(userName) > -1) {
                        $scope.sortKey = 'groupName';
                    }

                    /*Once devices are loaded, activate js scripts*/
                    $timeout(function () {
                        $scope.hidex = false; //To synchronize loading overlay and data plotted
                    });
                } else {
                    $scope.hidex = false;
                    $(".dashboard-container").html('<div class="accordion-content-title">No Devices</div>');
                }

                /*
                $timeout(function () {
                    if ($localStorage.isLoggedIn) {
                        $scope.getDeviceSummaryList(false);
                    }
                }, 10000);
                */

            }, function errorCallback(response) {
                console.log(response);
            })
            .finally(function () {
                $("body").LoadingOverlay("hide");
            });
    };

    //Switch On/Off devices
    $scope.toggleDeviceFlexibility = function (device) {

        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/
        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        var url = API_TOGGLE_DEVICE_FLEXIBILITY + device.deviceId;

        if (device.flexible) {
            url += "/1";
        } else {
            url += "/0";
        }

        $http({
                url: url,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {
                console.log(response);
                Notification.success($translate.instant('OPERATION_SUCCESS'));
                /*
                $timeout(function () {
                    $scope.getDeviceSummaryList();
                });
                */
            }, function errorCallback(response) {
                console.log(response);
                Notification.error({
                    message: $translate.instant('OPERATION_FAILURE'),
                    delay: 10000
                });
            });
    };

    $scope.getDeviceSummaryList(true);

});