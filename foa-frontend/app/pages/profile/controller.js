BJD.controller('ProfileController', function ($rootScope, $scope, $http, $location, $localStorage, $timeout, $routeParams, $translate, Notification) {

    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    if ($localStorage.userName != $routeParams.userName && $rootScope.role != 'ROLE_ADMIN') {
        $location.path('/dashboard');
        return;
    }

    function validateEmail(email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }

    $rootScope.pageTitle = "Profile Page";
    $rootScope.hide = false;

    $scope.username = "";
    $scope.password = "";
    $scope.email = "";
    $scope.tpLinkUserName = "";
    $scope.tpLinkPassword = "";
    $scope.phone = "";
    $scope.phone2 = "";
    $scope.latitude = null;
    $scope.longitude = null;
    $scope.enabled = undefined;
    $scope.userRole = "";

    $scope.vm = {
        address: {}
    };

    $scope.maxPicSize = 5000000; // 5MB

    $scope.admins = ALL_ADMINS;
    $scope.sysAdmins = SYS_ADMINS;

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
                url: $localStorage.userName == $routeParams.userName ? API_GET_USER : API_GET_USER_BY_ORGANIZATION + '/' + $routeParams.userName,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                var responseData = response.data.data;
                //console.log(responseData);

                $scope.username = responseData.userName;
                $scope.email = responseData.email;
                $scope.tpLinkUserName = responseData.tpLinkUserName;
                if (responseData.userAddress !== null) {
                    var userAddress = responseData.userAddress;
                    $scope.phone = userAddress.phone;
                    $scope.phone2 = userAddress.phone2;
                    $scope.latitude = userAddress.latitude;
                    $scope.longitude = userAddress.longitude;
                }
                $scope.pic = responseData.pic !== null ? "data:image/png;base64," + responseData.pic : DEFAULT_USER_IMG;

                // if the logged in user is changing his/her own profile
                if ($localStorage.userName == $routeParams.userName) {
                    $localStorage.pic = responseData.pic;
                }

                if ($localStorage.userName != $routeParams.userName) {
                    // todo: also update in header
                    $scope.organizationId = responseData.organizationId;
                }

                $scope.enabled = responseData.enabled;

                $scope.userRole = responseData.role;

                $timeout(function () {
                    $scope.loadUserLocationMap();
                });
            }, function errorCallback(response) {
                console.log(response);
            });
    };



    //Update user
    $scope.updateUser = function () {

        var username = $scope.username;
        var password = $scope.password;

        var tpLinkUserName = $scope.tpLinkUserName;
        if (tpLinkUserName && !validateEmail(tpLinkUserName)) {
            Notification.error({
                message: "'" + tpLinkUserName + "'" + $translate.instant('USER_INVALID_EMAIL'),
                delay: 10000
            });
            return;
        }
        var tpLinkPassword = $scope.tpLinkPassword;

        var email;
        if ($scope.userRole == 'ROLE_ADMIN') {
            if ($scope.email == null || $scope.email == "") {
                Notification.error({
                    message: $translate.instant('USER_EMAIL_MISSING'),
                    delay: $scope.notificationDelay
                });
                return;
            }
            if (!validateEmail($scope.email)) {
                Notification.error({
                    message: "'" + $scope.email + "'" + $translate.instant('USER_INVALID_EMAIL'),
                    delay: 10000
                });
                return;
            }
            email = $scope.email;
        } else {
            email = $scope.tpLinkUserName ? $scope.tpLinkUserName : username + DEFAULT_EMAIL;
        }

        var enabled = $scope.enabled;
        var role = $scope.userRole;

        var data = {
            userName: username,
            password: password,
            email: email,
            tpLinkUserName: tpLinkUserName,
            tpLinkPassword: tpLinkPassword,
            enabled: enabled,
            role: role,
            userAddress: {
                phone: $scope.phone,
                phone2: $scope.phone2,
                latitude: $scope.latitude,
                longitude: $scope.longitude
            }
        };

        // if admin user is updating another user from his organization then add
        // organizationId to data object 
        if ($localStorage.userName != $routeParams.userName) {
            data.organizationId = $scope.organizationId;
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
                url: $localStorage.userName == $routeParams.userName ? API_UPDATE_USER : API_UPDATE_USER_BY_ORGANIZATION,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                //console.log(response);
                Notification.success($translate.instant('OPERATION_SUCCESS'));

                // clear device search box typed query
                var searchBox = document.getElementById('user-address');
                searchBox.value = "";

                $timeout(function () {
                    $scope.getUser();
                });
            }, function errorCallback(response) {
                console.log(response);
                Notification.error(response.data.message);
            });
    };

    $scope.uploadPic = function () {

        var file = $scope.uploadedFile;
        if (file === undefined) {
            Notification.error($translate.instant('PROFILE_PIC_MISSING'));
            return;
        }
        if (file.type !== "image/jpeg" && file.type !== "image/png") {
            Notification.error("'" + file.name + "'" + $translate.instant('PROFILE_PIC_INVALID'));
            return;
        }
        if (file.size > $scope.maxPicSize) {
            Notification.error($translate.instant('PROFILE_PIC_LIMIT') + $scope.maxPicSize / 1000 + "KB!");
            return;
        }

        var data = new FormData();
        data.append('uploadFile', file);

        // with setting 'Content-Type': undefined, the browser will automatically change the Content-Type to multipart/form-data
        /*var headers = {
            authorization: "Basic " + $localStorage.auth,
            'Content-Type': undefined
        };*/

        var headers = {
            Authorization: $localStorage.token,
            // 'Content-Type': 'application/JSON'
            'Content-Type': undefined
        };

        $http({
                url: $localStorage.userName == $routeParams.userName ? API_UPDATE_USER_PIC : API_UPDATE_USER_PIC_BY_ORGANIZATION + '/' + $routeParams.userName,
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                Notification.success($translate.instant('OPERATION_SUCCESS'));

                $timeout(function () {
                    $scope.getUser();
                });

            }, function errorCallback(response) {
                console.log(response);
                Notification.error(response.data.message);
            });
    };

    /*Map*/
    $scope.loadUserLocationMap = function () {
        var center = new google.maps.LatLng($scope.latitude, $scope.longitude);

        var map = new google.maps.Map(document.getElementById('user-map'), {
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

        $scope.loadUserLocationMap();
    };

    $scope.getUser();
});