BJD.controller('UserListController', function ($rootScope, $scope, $http, $location, $localStorage, $routeParams, $window, $translate, $timeout, Notification) {

    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    if ($localStorage.role != "ROLE_ADMIN") {
        $location.path('/dashboard');
    }

    $rootScope.pageTitle = "User List";
    $rootScope.hide = false;


    /*Lists*/
    $scope.prosumers = [];
    $scope.admins = [];
    $scope.pageSize = 10;
    $scope.sortKey = 'userName';

    // sort message list
    $scope.sort = function (keyName) {
        $scope.sortKey = keyName; //set the sortKey to the param passed
        $scope.reverse = !$scope.reverse; //if true make it false and vice versa
    };


    //Get user
    $scope.getUsers = function () {
        $("body").LoadingOverlay("show");

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

    $scope.removeUser = function (user) {

        if (!$window.confirm($translate.instant("USER_DELETE_CONFIRM"))) {
            return;
        }
        $("body").LoadingOverlay("show");
        // console.log(API_REMOVE_USER_BY_ORGANIZATION+"/"+user.userName);
        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_REMOVE_USER_BY_ORGANIZATION + "/" + user.userName,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                Notification.success($translate.instant('USER_DELETE_SUCCESS'));
                $timeout(function () {
                    $scope.getUsers();
                });
                // console.log(response);
            }, function errorCallback(response) {
                console.log(response);
                Notification.error($translate.instant('USER_DELETE_ERROR'));
                $("body").LoadingOverlay("hide");
            }).finally(function () {
                $("body").LoadingOverlay("hide");
            });

    };

    $scope.getUsers();

});


BJD.controller('UserAddController', function ($rootScope, $scope, $http, $location, $localStorage, $timeout, $translate, Notification) {

    function validateEmail(email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }

    $scope.countryCodes = COUNTRY_CODES;
    $scope.organizationId = $localStorage.organizationId;

    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    if ($localStorage.role != "ROLE_ADMIN") {
        $location.path('/dashboard');
    }

    $rootScope.pageTitle = "Add User";
    $rootScope.hide = false;

    /*Add / Edit*/
    $scope.username = "";
    $scope.password = "";
    $scope.email = "";
    $scope.tpLinkUserName = "";
    $scope.tpLinkPassword = "";
    $scope.phone = "";
    $scope.latitude = null;
    $scope.longitude = null;
    $scope.newUserRole = 'ROLE_PROSUMER';

    $scope.vm = {
        address: {}
    };

    $scope.submit = function () {

        $scope.isProcessing = true;

        if (!$localStorage.organizationId) {
            Notification.error($translate.instant('USER_ADD_ORG_ERROR'));
            $scope.isProcessing = false;
            return;
        }

        var username = $scope.username;
        var password = $scope.password;

        var tpLinkUserName = $scope.tpLinkUserName;
        if (tpLinkUserName && !validateEmail(tpLinkUserName)) {
            Notification.error({
                message: "'" + tpLinkUserName + "'" + $translate.instant('USER_INVALID_EMAIL'),
                delay: 10000
            });
            $scope.isProcessing = false;
            return;
        }
        var tpLinkPassword = $scope.tpLinkPassword;

        var email;
        if ($scope.newUserRole == 'ROLE_ADMIN') {
            if ($scope.email == null || $scope.email == "") {
                Notification.error({
                    message: $translate.instant('USER_EMAIL_MISSING'),
                    delay: $scope.notificationDelay
                });
                $scope.isProcessing = false;
                return;
            }
            if (!validateEmail($scope.email)) {
                Notification.error({
                    message: "'" + $scope.email + "'" + $translate.instant('USER_INVALID_EMAIL'),
                    delay: 10000
                });
                $scope.isProcessing = false;
                return;
            }
            email = $scope.email;
        } else {
            email = $scope.tpLinkUserName ? $scope.tpLinkUserName : username + DEFAULT_EMAIL;
        }

        var organizationId = $localStorage.organizationId;

        var data = {
            userName: username,
            password: password,
            email: email,
            tpLinkUserName: tpLinkUserName,
            tpLinkPassword: tpLinkPassword,
            organizationId: organizationId,
            role: $scope.newUserRole,
            userAddress: {
                phone: $scope.phone ? $scope.countryCodes[organizationId] + $scope.phone : "",
                latitude: $scope.latitude,
                longitude: $scope.longitude
            }
        };

        // console.log(data);
        // $scope.isProcessing = true;

        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_REGISTER_USER_BY_ORGANIZATION,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                Notification.success($translate.instant('OPERATION_SUCCESS'));
                $timeout(function () {
                    $location.path('/user');
                });
            }, function errorCallback(response) {
                Notification.error({
                    message: response.data.message,
                    delay: 20000
                });
                $scope.isProcessing = false;
            });
    };

    /*Map*/
    $scope.loadNewUserMap = function () {
        var center = new google.maps.LatLng($scope.latitude, $scope.longitude);

        var map = new google.maps.Map(document.getElementById('new-user-map'), {
            zoom: 13,
            center: center,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            zoomControl: false,
            mapTypeControl: false,
            scaleControl: true,
            streetViewControl: false,
            rotateControl: false,
            fullscreenControl: true
        });

        var marker = new google.maps.Marker({
            position: center,
            map: map,
            draggable: true
        });

        google.maps.event.addListener(marker, 'drag', function () {
            $scope.updateMarkerPosition(marker.getPosition());
        });

    };

    $scope.updateMarkerPosition = function (latLng) {
        //console.log(latLng.lat());
        //console.log(latLng.lng());
        $scope.latitude = latLng.lat();
        $scope.longitude = latLng.lng();
    };

    $scope.addressChanged = function () {
        // parse auto complete address
        //console.log($scope.vm.address);
        var vmAddress = $scope.vm.address;
        if (vmAddress.lat !== null) {
            $scope.latitude = vmAddress.lat;
        }
        if (vmAddress.lng !== null) {
            $scope.longitude = vmAddress.lng;
        }

        $scope.loadNewUserMap();
    };

    $scope.loadNewUserMap();
});

BJD.controller('UserEditController', function ($rootScope, $scope, $http, $location, $localStorage, $routeParams, $timeout, Notification) {

    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    if ($localStorage.role != "ROLE_ADMIN") {
        $location.path('/dashboard');
    }

    $rootScope.pageTitle = "Edit User";
    $rootScope.hide = false;


    /*Add / Edit*/
    $scope.username = "";
    $scope.password = "";
    $scope.email = "";
    $scope.tpLinkUserName = "";
    $scope.tpLinkPassword = "";
    $scope.address1 = "";
    $scope.address2 = "";
    $scope.city = "";
    $scope.country = "";
    $scope.state = "";
    $scope.postalCode = "";
    $scope.phone = "";
    $scope.latitude = null;
    $scope.longitude = null;
    $scope.organizationId = null;

    $scope.vm = {
        address: {}
    };

    //Get user
    $scope.getuser = function () {
        $("body").LoadingOverlay("show");

        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_GET_USER_BY_ORGANIZATION + '/' + $routeParams.userName,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                var responseData = response.data.data;
                // console.log(responseData);
                if (!responseData) {
                    Notification.error("No user found. Redirecting to list.");
                    $timeout(function () {
                        $location.path('/user');
                    });
                }

                $scope.username = responseData.userName;
                $scope.email = responseData.email;
                $scope.tpLinkUserName = responseData.tpLinkUserName;
                $scope.organizationId = responseData.organizationId;
                if (responseData.userAddress !== null) {
                    var userAddress = responseData.userAddress;
                    $scope.address1 = userAddress.address1;
                    $scope.address2 = userAddress.address2;
                    $scope.city = userAddress.city;
                    $scope.state = userAddress.state;
                    $scope.postalCode = userAddress.postalcode;
                    $scope.country = userAddress.country;
                    $scope.phone = userAddress.phone;
                    $scope.latitude = userAddress.latitude;
                    $scope.longitude = userAddress.longitude;
                }
            }, function errorCallback(response) {
                console.log(response);
                $("body").LoadingOverlay("hide");
            }).finally(function () {
                $("body").LoadingOverlay("hide");
            });
    };

    $scope.addressChanged = function () {
        // parse auto complete address
        var vmAddress = $scope.vm.address;
        if (vmAddress.postal_code !== null) {
            $scope.postalCode = vmAddress.postal_code;
        }
        if (vmAddress.country !== null) {
            $scope.country = vmAddress.country;
        }
        if (vmAddress.locality !== null) {
            $scope.city = vmAddress.locality;
        }
        if (vmAddress.route !== null) {
            $scope.address1 = vmAddress.route;
        }
        if (vmAddress.lat !== null) {
            $scope.latitude = vmAddress.lat;
        }
        if (vmAddress.lng !== null) {
            $scope.longitude = vmAddress.lng;
        }
        /*
        if (vmAddress.street_number !== null) {
            $scope.address2 = "Street Number " + vmAddress.street_number;
        }
        */
    };

    $scope.submit = function () {

        $("body").LoadingOverlay("show");

        var username = $scope.username;
        var password = $scope.password;
        var email = $scope.email;
        var tpLinkUserName = $scope.tpLinkUserName;
        var tpLinkPassword = $scope.tpLinkPassword;
        var organizationId = $scope.organizationId;

        // parse auto complete address
        // console.log($scope.vm.address);


        var data = {
            userName: username,
            password: password,
            email: email,
            tpLinkUserName: tpLinkUserName,
            tpLinkPassword: tpLinkPassword,
            organizationId: organizationId,
            userAddress: {
                address1: $scope.address1,
                address2: $scope.address2,
                city: $scope.city,
                state: $scope.state,
                postalcode: $scope.postalCode,
                country: $scope.country,
                phone: $scope.phone,
                latitude: $scope.latitude,
                longitude: $scope.longitude
            }
        };

        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': 'application/JSON'
        };*/

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_UPDATE_USER_BY_ORGANIZATION,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                Notification.success("Update Successful");
            }, function errorCallback(response) {
                console.log(response);
                $("body").LoadingOverlay("hide");
            }).finally(function () {
                $("body").LoadingOverlay("hide");
            });
    };

    $scope.getUser();
});