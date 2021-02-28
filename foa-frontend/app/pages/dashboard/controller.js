BJD.controller('DashboardController', function ($rootScope, $scope, $http, $location, $localStorage, $translate) {
    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    $rootScope.pageTitle = "Dashboard";
    $rootScope.hide = false; /*Header and menu*/

    /*$scope.activeProsumers = 0;
    $scope.activeDevices = 0;
    $scope.flexOfferCount = 0;
    $scope.flexibilityRatio = 0;*/

    $scope.voltageMin;
    $scope.voltageMax;
    $scope.mapType = "state";

    $scope.latitude = 55.696139;
    $scope.longitude = 12.515207;
    $scope.zoom = 3;

    $scope.groups = {};
    $scope.deviceHierarchy = null;

    $scope.clusterInfoWindows = [];

    $scope.filterBy = "users";

    $scope.devices = [];
    $scope.admins = [];
    $scope.prosumers = [];

    $scope.selectedUser = "";
    $scope.selectedGroup = "";

    $scope.userName = $localStorage.userName;
    $scope.swissUsers = SWISS_USERS;

    //Get user
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
                $scope.username = responseData.userName;
                $scope.tpLinkUserName = responseData.tpLinkUserName;
                if (responseData.userAddress !== null) {
                    var userAddress = responseData.userAddress;
                    if (userAddress.latitude !== 0 && userAddress.longitude !== 0) {
                        $scope.latitude = userAddress.latitude;
                        $scope.longitude = userAddress.longitude;
                        $scope.zoom = 15;
                    }
                }

            }, function errorCallback(response) {
                console.log(response);
            });
    };

    $scope.getKpis = function () {
        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_FOA_KPI,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {

                function computerActiveProsumers() {

                    var prosumerGroups = {};
                    for (var id in $scope.groups)  {
                        key = $scope.groups[id]['userId'];
                        if (key in prosumerGroups) {
                            prosumerGroups[key] += 1;
                        } else {
                            prosumerGroups[key] = 1;
                        }
                    } 

                    var totalProsumers = response.data.data.activeProsumers[0];
                    for (var i in prosumerGroups) {
                        totalProsumers += prosumerGroups[i] -1;
                    }

                    return totalProsumers;
                }

                if (response.data.status == "OK") {

                    //console.log(response);
                    var circleActiveProsumers = new ProgressBar.Circle('#s-block-prosumer', {
                        color: '#f88e3d',
                        trailColor: '#f7b889',
                        strokeWidth: 10,
                        text: {
                            value: computerActiveProsumers()
                        }
                    });
                    circleActiveProsumers.animate(1.0);
                    $scope.activeProsumersDelta = response.data.data.activeProsumers[1];


                    var circleActiveDevices = new ProgressBar.Circle('#s-block-active', {
                        color: '#00d554',
                        trailColor: '#bee8b8',
                        strokeWidth: 10,
                        text: {
                            value: response.data.data.activeDevices[0]
                        }
                    });
                    circleActiveDevices.animate(1.0);
                    $scope.activeDevicesDelta = response.data.data.activeDevices[1];


                    var circleFlexOffers = new ProgressBar.Circle('#s-block-flex', {
                        color: '#ec83fc',
                        trailColor: '#e4a2ef',
                        strokeWidth: 10,
                        text: {
                            value: response.data.data.foCount[0]
                        }
                    });
                    circleFlexOffers.animate(1.0);
                    $scope.flexOffersDelta = response.data.data.foCount[1];


                    var circleFlexRatio = new ProgressBar.Circle('#s-block-ratio', {
                        color: '#0d90d2',
                        trailColor: '#a1d0e8',
                        strokeWidth: 10,
                        text: {
                            value: response.data.data.flexRatio[0].toFixed(3)
                        }
                    });
                    //circleFlexRatio.animate(response.data.data / 100);
                    circleFlexRatio.animate(1.0);
                    $scope.flexRatioDelta = response.data.data.flexRatio[1].toFixed(0);


                    var circleRewards = new ProgressBar.Circle('#s-block-rewards', {
                        color: '#f88e3d',
                        trailColor: '#f7b889',
                        strokeWidth: 10,
                        text: {
                            value: response.data.data.rewards[0].toFixed(1)
                        }
                    });
                    circleRewards.animate(1.0);
                    $scope.rewardsDelta = response.data.data.rewards[1].toFixed(0);

                }
            }, function errorCallback(response) {
                console.log(response);
            });
    };


    $scope.getDevices = function () {

        /*Loading*/
        $("body").LoadingOverlay("show");

        var url = API_GET_DEVICE_LOCATION;

        if ($scope.deviceHierarchy) {
            url = API_GET_DEVICE_LOCATION + "/" + $scope.deviceHierarchy;
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
                url: url,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                    var responseData = response.data.data;
                    if (responseData.length) {

                        $scope.devices = responseData;

                        $scope.renderMap();

                    } else {
                        $("#map").html('<p>' + $translate.instant("DASHBOARD_MAP_EMPTY") + '</p>');
                    }


                },
                function errorCallback(response) {
                    console.log(response);
                })
            .finally(function () {
                $("body").LoadingOverlay("hide");
            });
    };

    $scope.dox = function () {
        // $scope.mapType = "voltage";
        $scope.getDevices();
    };

    //Get groups
    $scope.getGroups = function () {

        /* var headers = {
             authorization: "Basic " + $localStorage.auth,
             'Content-Type': 'application/JSON'
         };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_GET_GROUPS,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {

                var responseData = response.data.data;

                if (responseData.length) {
                    // $scope.groups = {
                    //     "0":{
                    //         hierarchyId: 0,
                    //         hierarchyName: "None"
                    //     }
                    // };
                    for (var i in responseData) {
                        var groupId = responseData[i].hierarchyId;
                        $scope.groups[groupId] = responseData[i];
                    }
                }

                $scope.getKpis();

            }, function errorCallback(response) {
                console.log(response);
            });
    };

    $scope.getUsers = function () {

        $scope.prosumers = [];
        $scope.admins = [];

        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_GET_USERS_BY_ORGANIZATION,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                var responseData = response.data.data;

                if (responseData.length) {
                    for (var i in responseData) {
                        if (responseData[i].role == 'ROLE_ADMIN') {
                            $scope.admins.push(responseData[i]);
                        } else {
                            $scope.prosumers.push(responseData[i]);
                        }
                    }
                }
            }, function errorCallback(response) {
                console.log(response);
                $("body").LoadingOverlay("hide");
            })
            .finally(function () {
                $("body").LoadingOverlay("hide");
            });
    };


    $scope.$on('$routeChangeSuccess', function () {
        $scope.getUser();
        if ($localStorage.role == "ROLE_ADMIN") {
            $scope.getGroups();
        } else {
            $scope.getKpis();
        }
        $scope.getUsers();
        $scope.getDevices();

    });



    $scope.renderMap = function () {

        if (!$scope.devices.length) {
            $("#map").html('<p>' + $translate.instant("DASHBOARD_MAP_EMPTY") + '</p>');
            return;
        }

        var center;
        if ($scope.selectedUser != null &&
            $scope.selectedUser.userAddress != null &&
            $scope.selectedUser.userAddress.latitude != 0 &&
            $scope.selectedUser.userAddress.longitude != 0) {
            var lat = $scope.selectedUser.userAddress.latitude;
            var lon = $scope.selectedUser.userAddress.longitude;
            center = new google.maps.LatLng(lat, lon);
        } else {
            center = new google.maps.LatLng($scope.latitude, $scope.longitude);
        }

        var mapOptions = {
            zoom: $scope.zoom,
            center: center,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(document.getElementById('map'), mapOptions);
        var infoWindow = new google.maps.InfoWindow();
        var markers = [];

        var filteredDevices = [];
        var idx;
        if (($scope.selectedUser == null || $scope.selectedUser == "") &&
            ($scope.selectedGroup == null || $scope.selectedGroup == "")) {
            filteredDevices = $scope.devices;
        } else if (($scope.selectedUser != null && $scope.selectedUser != "") &&
            ($scope.selectedGroup == null || $scope.selectedGroup == "")) {
            for (idx in $scope.devices) {
                var device = $scope.devices[idx];
                var userName = device.deviceId.split("@")[0];
                if (userName == $scope.selectedUser.userName) {
                    filteredDevices.push(device);
                }
            }
            //$scope.selectedUser = "";
        } else if (($scope.selectedUser == null || $scope.selectedUser == "") &&
            ($scope.selectedGroup != null && $scope.selectedGroup != "")) {
            for (idx in $scope.devices) {
                var device = $scope.devices[idx];
                var deviceHierarchy = device.deviceHierarchy;
                if (deviceHierarchy != null && deviceHierarchy.hierarchyId == $scope.selectedGroup) {
                    filteredDevices.push(device);
                }
            }
           //$scope.selectedGroup = "";
        } else if (($scope.selectedUser != null && $scope.selectedUser != "") &&
            ($scope.selectedGroup != null && $scope.selectedGroup != "")) {
            console.log("this block should never be executed!");
        }

        if (!filteredDevices.length) {
            $("#map").html('<p>' + $translate.instant("DASHBOARD_MAP_EMPTY") + '</p>');
            return;
        }

        $.each(filteredDevices, function (key, device) {
            if (device.latitude != 0 && device.longitude != 0) {
                var latLng;
                var deviceVoltage;
                var markerIcon;
                var deviceState;
                if (device.latitude != 0) {
                    latLng = new google.maps.LatLng(device.latitude, device.longitude);
                    if ($scope.mapType == "voltage") {

                        deviceVoltage = device.consumptionTs.latestVoltage;
                        // console.log($scope.voltageMin, $scope.voltageMax, deviceVoltage);
                        if (deviceVoltage <= $scope.voltageMin || deviceVoltage >= $scope.voltageMax) {
                            markerIcon = "img/marker-red.png";
                        } else {
                            markerIcon = "img/marker-green.png";
                        }

                    } else {
                        deviceState = device.deviceState;
                        markerIcon = "img/marker-grey.png";

                        if (deviceState == "Idle") {
                            markerIcon = "img/marker-yellow.png";
                        }
                        if (deviceState == "Disconnected") {
                            markerIcon = "img/marker-red.png";
                        }
                        if (deviceState == "Operating") {
                            markerIcon = "img/marker-green.png";
                        }

                    }
                    var marker = new google.maps.Marker({
                        position: latLng,
                        map: map,
                        icon: markerIcon,
                        device: device
                    });

                    markers.push(marker);
                }
            }
        });

        /*Set info window for each marker*/
        markers.forEach(function (mrk) {

            google.maps.event.addListener(mrk, 'click', function () {
                infoWindow.setContent(
                    '<div class="infoWindow-wrap">' +
                    '<div class="infoWindow-body">' +
                    '<table class="table table-striped table-condensed">' +
                    '<tbody>' +
                    '<tr>' +
                    '<td>' +
                    '<strong>' +
                    $translate.instant('DASHBOARD_INFO_WINDOW_POWER') +
                    '</strong>' +
                    '</td>' +
                    '<td>' +
                    mrk.device.consumptionTs.latestPower +
                    '</td>' +
                    '</tr>' +
                    '<tr>' +
                    '<td>' +
                    '<strong>' +
                    $translate.instant('DASHBOARD_INFO_WINDOW_VOLTAGE') +
                    '</strong>' +
                    '</td>' +
                    '<td>' +
                    mrk.device.consumptionTs.latestVoltage +
                    '</td>' +
                    '</tr>' +
                    '<tr>' +
                    '<td>' +
                    '<strong>' +
                    $translate.instant('DASHBOARD_INFO_WINDOW_STATE') +
                    '</strong>' +
                    '</td>' +
                    '<td>' +
                    mrk.device.deviceState +
                    '</td>' +
                    '</tr>' +
                    '<tr>' +
                    '<td></td>' +
                    '<td>' +
                    '<a target="_blank" href="#!/device/' + mrk.device.deviceId + '">' +
                    $translate.instant('DASHBOARD_INFO_WINDOW_VIEW_DETAIL') +
                    '</a>' +
                    '</td>' +
                    '</tr>' +
                    '</tbody>' +
                    '</table>' +
                    '</div>' +
                    '</div>'
                );

                infoWindow.open(map, mrk);
            });
        });

        /*Marker Cluster*/
        var opt_options = {
            maxZoom: 17, // turn off clustering at max zoom
            imagePath: "img/markercluster/m"
        };
        var markerCluster = new MarkerClusterer(map, markers, opt_options);

        var infoWindowC = new google.maps.InfoWindow({
            pixelOffset: new google.maps.Size(0, -8)
        });
        google.maps.event.addListener(markerCluster, 'mouseover', function (cluster) {
            var sum = 0;
            mrks = cluster.getMarkers();
            $.each(mrks, function (i, mr) {
                sum += mr.device.consumptionTs.latestVoltage;
            });
            var avg = sum / mrks.length;

            infoWindowC.setContent($translate.instant("DASHBOARD_INFO_WINDOW_CLUSTER") + ": " + avg.toFixed(1));
            infoWindowC.setPosition(cluster.getCenter());
            infoWindowC.open(map);
        });
        google.maps.event.addListener(markerCluster, 'mouseout', function (cluster) {
            //infoWindowC.close(map); // causes flickering behavior
        });
        google.maps.event.addListener(markerCluster, 'click', function (cluster) {
            infoWindowC.close(map);
        });

        // auto fit map to markers
        var bounds = new google.maps.LatLngBounds();
        for (var i in markers) {
            if (markers[i].device.latitude != 0 && markers[i].device.longitude != 0) {
                bounds.extend(markers[i].position); // your marker position, must be a LatLng instance
            }
        }

        map.fitBounds(bounds); // map should be your map class
    };


    $scope.resetGroupFilter = function () {
        $scope.selectedGroup = "";
    };

    $scope.resetUserFilter = function () {
        $scope.selectedUser = "";
    };

    $scope.resetFilters = function () {
        $scope.selectedUser = "";
        $scope.selectedGroup = "";
        $scope.renderMap();
    };

});