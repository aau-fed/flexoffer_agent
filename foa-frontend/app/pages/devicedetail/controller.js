BJD.controller('DeviceDetailController', function ($rootScope, $localStorage, $location, $scope, $http, $timeout, $routeParams, $window, $translate, Notification) {

    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    $rootScope.pageTitle = "Device Detail";
    $rootScope.hide = false; /*Header and menu*/

    $scope.noChange = true;
    $scope.deviceId = $routeParams.deviceId;
    $scope.deviceDetail = {};
    $scope.deviceTypeIconUrl = '';
    $scope.deviceStateIconUrl = '';


    $scope.deviceType = '';

    $scope.dailyControlStart = 0;
    $scope.dailyControlEnd = 0;
    $scope.noOfInterruptionInADay = 0;
    $scope.maxInterruptionLength = 0;
    $scope.minInterruptionInterval = 0;
    $scope.maxInterruptionDelay = 0;
    $scope.latestAcceptanceTime = 0;

    $scope.vm1 = {
        address: {}
    };
    $scope.locChange = false;
    $scope.latitude = 0;
    $scope.longitude = 0;

    $scope.deviceCategory = '';

    $scope.deviceCategoryMap = DEVICE_CATEGORY_MAP;

    $scope.isDeviceFlexible = false;

    $scope.Math = Math;

    $scope.groupNames = [];
    $scope.groupIds = [];
    $scope.hierarchyName = '';

    $scope.dateFormat = 'YYYY-MM-DD';
    $scope.datetimeFormat = 'YYYY-MM-DDTHH:mm:ss';
    $scope.dateFormatWithoutTime = 'YYYY-MM-DDT00:00:00';
    $scope.dateFormatWithoutSeconds = 'YYYY-MM-DDTHH:mm:00';
    $scope.specificReadingsDate = null;
    $scope.specificControlDate = null;

    $scope.lastReadingDate = {};
    $scope.devicePowerTimeSeries = {};
    $scope.deviceEnergyTimeSeries = {};
    $scope.deviceScheduleData = {};
    $scope.powerChart = {};
    $scope.xAxisLabels = [];


    $scope.getDeviceDetail = function (resetParams) {

        $(".content-tab").LoadingOverlay("show");

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: API_GET_DEVICE + $scope.deviceId,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                $(".content-tab").LoadingOverlay("hide");
                if (response.data.data) {
                    $scope.deviceDetail = response.data.data;

                    $scope.deviceType = $scope.deviceDetail.deviceType;

                    var deviceType = $scope.deviceDetail.deviceType;
                    if ($scope.deviceCategoryMap['TCLDevice'].indexOf(deviceType) > -1) {
                        $scope.deviceCategory = 'TCLDevice';
                    } else if ($scope.deviceCategoryMap['WetDevice'].indexOf(deviceType) > -1) {
                        $scope.deviceCategory = 'WetDevice';
                    } else if ($scope.deviceCategoryMap['ChargingDevice'].indexOf(deviceType) > -1) {
                        $scope.deviceCategory = 'ChargingDevice';
                    } else {
                        $scope.deviceCategory = '';
                    }

                    /*Track deviceHierarchy*/
                    if ($scope.deviceDetail.deviceHierarchy != null) {
                        $scope.hierarchyName = $scope.deviceDetail.deviceHierarchy.hierarchyName;
                    } else {
                        $scope.hierarchyName = '';
                    }

                    $timeout(function () {

                        $scope.deviceTypeIconUrl = $scope.getDeviceTypeIconUrl($scope.deviceDetail.deviceType);
                        $scope.deviceStateIconUrl = $scope.getDeviceStateIconUrl($scope.deviceDetail.deviceState);
                        $scope.deviceStatus = ($scope.deviceDetail.deviceState == 'Operating') ? true : false;
                        $scope.isDeviceFlexible = ($scope.deviceDetail.flexible == true) ? true : false;

                        if ($.isEmptyObject($scope.groupNames)) {
                            $scope.getGroups();
                        }

                        if ($scope.deviceDetail.deviceFlexibilityDetail != null) {
                            $scope.dailyControlStart = $scope.deviceDetail.deviceFlexibilityDetail.dailyControlStart;
                            $scope.dailyControlEnd = $scope.deviceDetail.deviceFlexibilityDetail.dailyControlEnd;
                            $scope.noOfInterruptionInADay = $scope.deviceDetail.deviceFlexibilityDetail.noOfInterruptionInADay;
                            $scope.maxInterruptionLength = $scope.deviceDetail.deviceFlexibilityDetail.maxInterruptionLength;
                            $scope.minInterruptionInterval = $scope.deviceDetail.deviceFlexibilityDetail.minInterruptionInterval;
                            $scope.maxInterruptionDelay = $scope.deviceDetail.deviceFlexibilityDetail.maxInterruptionDelay;
                            $scope.latestAcceptanceTime = $scope.deviceDetail.deviceFlexibilityDetail.latestAcceptanceTime;
                        }

                        $scope.latitude = $scope.deviceDetail.latitude;
                        $scope.longitude = $scope.deviceDetail.longitude;

                        //$scope.loadDeviceLocationMap();
                        $scope.activateScripts();
                        if (resetParams) {
                            $scope.resetFlexParams();
                        }
                    });

                    $timeout(function () {
                        $scope.loadDeviceLocationMap();
                    }, 1000);

                } else {
                    $(".content-tab").html("<div class='panel panel-default'><h5 class='text-center'>No Data Found.</h5></div>");
                }

            }, function errorCallback(response) {
                console.log(response);
                Notification.error({
                    message: $translate.instant('DEVICEDETAIL_FETCH_ERROR'),
                    delay: 10000
                });
                $(".content-tab").html("<div class='panel panel-default'></div>");
                $(".content-tab").LoadingOverlay("hide");
            });

    };

    $scope.valueChanged = function () {
        $timeout(function () {
            $scope.noChange = false;
        });
    };

    $scope.locChanged = function () {
        $timeout(function () {
            $scope.locChange = true;
        });
    };

    $scope.activateScripts = function () {
        /*Date picker*/
        var date_input_reading = $('#date-picker');
        datefield = date_input_reading.datepicker({
            format: 'mm/dd/yyyy',
            todayHighlight: true,
            autoclose: true,
            startDate: new Date('2018-01-01'),
            endDate: 'today'
            //endDate: new Date(moment().add(1, 'days')) // tomorrow
        }).on('changeDate', function (ev) {
            $scope.specificReadingsDate = ev.date;
            $scope.getAndPlotDeviceData();
        });

        /*Date picker*/
        var control_date_input_reading = $('#control-date-picker');
        datefield2 = control_date_input_reading.datepicker({
            format: 'mm/dd/yyyy',
            todayHighlight: true,
            autoclose: true,
            startDate: new Date('2018-01-01'),
            endDate: 'today'
            //endDate: new Date(moment().add(1, 'days')) // tomorrow
        }).on('changeDate', function (ev) {
            $scope.specificControlDate = ev.date;
            $scope.getDeviceControlHistory();
        });

        /*Range selector*/

        $timeout(function () {

            function prettify_time(n) {
                var num = n % 12 == 0 ? 12 : n % 12;
                return 12 <= n ? n == 24 ? num + ":00 am" : num + ":00 pm" : num < 10 ? "0" + num + ":00 am" : num + ":00 am";
                //return n > 11 ? n + ":00" : n < 10 ? "0" + n + ":00" : n + ":00";
            }

            function slices_to_time(n) {
                n = n * 15;
                var hr;
                var min;
                if (n < 60) {
                    hr = "00";
                    min = n < 10 ? "0" + n : n;
                } else {
                    hr = Math.floor(n / 60);
                    hr = hr < 10 ? "0" + hr : hr;
                    min = n % 60;
                    min = min == 0 ? "00" : min < 10 ? "0" + min : min;
                }
                return hr + ":" + min + " hrs";
            }

            function getDBI(deviceType) {
                if ($scope.deviceCategoryMap['WetDevice'].indexOf(deviceType) > -1) {
                    return 32;
                } else {
                    return 4;
                }
            }

            var $dcw = $("#daily-control-window");
            var $msd = $("#maximum-start-delay");
            var $lnt = $("#latest-notification-time");
            var $ipd = $("#interruptions-per-day");
            var $doi = $("#duration-of-interruption");
            var $dbi = $("#distance-between-interruptions");
            var $cbi = $("#charged-battery-in");

            //$(".daily-control-window").ionRangeSlider({
            $dcw.ionRangeSlider({
                type: "double",
                grid: true,
                min: 0,
                max: 24,
                from: $scope.dailyControlStart,
                to: $scope.dailyControlEnd,
                prettify: prettify_time,
                drag_interval: true,
                onChange: function (data) {
                    $scope.dailyControlStart = data.from;
                    $scope.dailyControlEnd = data.to;
                    $scope.valueChanged();
                }
            });

            //$(".maximum-start-delay").ionRangeSlider({
            $msd.ionRangeSlider({
                grid: true,
                min: 0,
                from_min: 1,
                max: 1 * 4 * 12,
                step: 1,
                prettify: slices_to_time,
                prettify_enabled: true,
                from_shadow: true,
                onChange: function (data) {
                    $scope.maxInterruptionDelay = data.from;
                    $scope.valueChanged();
                },
                onFinish: function(data) {
                    var lnt_instance = $lnt.data("ionRangeSlider"); 
                    lnt_instance.update({
                        from_max: data.from
                    });
                }
            });

            //$(".latest-notification-time").ionRangeSlider({
            $lnt.ionRangeSlider({
                grid: true,
                min: 0,
                from_min: 0,
                max: 1 * 4 * 12,
                from_max: $msd.data("from"),
                step: 1,
                prettify: slices_to_time,
                prettify_enabled: true,
                from_shadow: true,
                onChange: function (data) {
                    $scope.latestAcceptanceTime = data.from;
                    $scope.valueChanged();
                }
            });

            //$(".interruptions-per-day").ionRangeSlider({
            $ipd.ionRangeSlider({
                grid: true,
                min: 0,
                from_min: 1,
                max: 24,
                step: 1,
                prettify_enabled: false,
                from_shadow: true,
                onChange: function (data) {
                    $scope.noOfInterruptionInADay = data.from;
                    $scope.valueChanged();
                }
            });

            //$(".duration-of-interruption").ionRangeSlider({
            $doi.ionRangeSlider({
                grid: true,
                min: 0,
                from_min: 1,
                max: 1 * 4 * 12,
                from_max: 1 * 4 * 2,
                step: 1,
                prettify: slices_to_time,
                prettify_enabled: true,
                from_shadow: true,
                to_shadow: true,
                onChange: function (data) {
                    $scope.maxInterruptionLength = data.from;
                    $scope.valueChanged();
                }
            });

            //$(".distance-between-interruptions").ionRangeSlider({
            $dbi.ionRangeSlider({
                grid: true,
                min: 0,
                from_min: getDBI($scope.deviceType),
                max: 1 * 4 * 12,
                step: 1,
                prettify: slices_to_time,
                prettify_enabled: true,
                from_shadow: true,
                onChange: function (data) {
                    $scope.minInterruptionInterval = data.from;
                    $scope.valueChanged();
                }
            });

            //$(".charged-battery-in").ionRangeSlider({
            $cbi.ionRangeSlider({
                grid: true,
                min: 0,
                from_min: 1,
                max: 1 * 4 * 12,
                step: 1,
                prettify: slices_to_time,
                prettify_enabled: true,
                from_shadow: true,
                onChange: function (data) {
                    $scope.maxInterruptionDelay = data.from;
                    $scope.valueChanged();
                }
            });
        });
    };


    $scope.getDeviceTypeIconUrl = function (type) {
        var url = '';
        switch (type) {
            case 'HeatPump':
                url = 'img/load-icons/heat-pump.png';
                break;

            case 'Boiler':
                url = 'img/load-icons/boiler.png';
                break;

            case 'DishWasher':
                url = 'img/load-icons/dishwasher.png';
                break;

            case 'WasherDryer':
                url = 'img/load-icons/washing-machine.png';
                break;

            case 'AirConditioner':
                url = 'img/load-icons/air-conditioning.png';
                break;

            case 'ElectricBike':
                url = 'img/load-icons/e-bike.png';
                break;

            case 'RoomHeater':
                url = 'img/load-icons/room-heater.png';
                break;

            case 'ElectricLawnMower':
                url = 'img/load-icons/lawn-mower.png';
                break;

            case 'ElectricCar':
                url = 'img/load-icons/electric-car.png';
                break;

            case 'Refrigerator':
                url = 'img/load-icons/refrigerator.png';
                break;

            case 'Freezer':
                url = 'img/load-icons/freezer.png';
                break;

            default:
                url = 'img/load-icons/unknown-load.png';

        }
        return url;
    };

    $scope.getDeviceStateIconUrl = function (state) {
        var url = '';
        switch (state) {
            case 'Idle':
                url = 'img/yellow.png';
                break;

            case 'Disconnected':
                url = 'img/red.png';
                break;

            case 'Operating':
                url = 'img/green.png';

                break;

            default:
                url = 'img/gray.png';

        }
        return url;
    };

    //Switch On/Off devices
    $scope.toggleDevicePower = function () {

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        var url = API_TOGGLE_DEVICE + $scope.deviceId;
        // var url = "http://localhost/dashboard/api/index.php?action=getdevicelocation";

        if ($scope.deviceStatus) {
            url += "/1";
        } else {
            url += "/0";
        }

        $http({
                url: url,
                method: 'POST',
                headers: headers
            })
            .then(function () {

                $timeout(function () {
                    if ($scope.deviceStatus) {
                        $scope.deviceStateIconUrl = $scope.getDeviceStateIconUrl("Operating");
                    } else {
                        $scope.deviceStateIconUrl = $scope.getDeviceStateIconUrl("Idle");
                    }
                });
            });
    };


    $scope.loadDeviceLocationMap = function () {
        var center = new google.maps.LatLng($scope.latitude, $scope.longitude);

        var map = new google.maps.Map(document.getElementById('device-map'), {
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
        $scope.latitude = latLng.lat();
        $scope.longitude = latLng.lng();
        $scope.locChanged();
        $scope.valueChanged();
    };

    $scope.deviceLocationChanged = function () {
        // parse auto complete address
        //console.log($scope.vm1.address);
        var vmAddress = $scope.vm1.address;
        if (vmAddress.lat !== null && vmAddress.lng !== null) {
            $scope.latitude = vmAddress.lat;
            $scope.longitude = vmAddress.lng;
            $scope.loadDeviceLocationMap();
            $scope.locChanged();
            $scope.valueChanged();
        }
    };

    $scope.updateDeviceLocation = function () {

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        var data = {
            deviceId: $scope.deviceDetail.deviceId,
            latitude: $scope.latitude,
            longitude: $scope.longitude
        };

        $http({
                url: API_UPDATE_DEVICE_LOCATION,
                // url: "http://localhost/dashboard/api/index.php?action=updatedevice",
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                $(".content-tab").LoadingOverlay("hide");

                if (response.data.status == "OK") {
                    //Notification.success($translate.instant('OPERATION_SUCCESS'));
                    //console.log(response.data.message);
                } else {
                    Notification.error({
                        message: $translate.instant('OPERATION_FAILURE'),
                        delay: 10000
                    });
                    console.log(response.data.message);
                }

                $scope.locChange = false;

                $scope._updateDevice();

                // clear device search box typed query
                var searchBox = document.getElementById('device-address');
                searchBox.value = "";

            }, function errorCallback(response) {
                $(".content-tab").LoadingOverlay("hide");
                console.log(response);
                Notification.error({
                    message: $translate.instant('OPERATION_FAILURE'),
                    delay: 10000
                });
            });


    };


    //Get groups
    $scope.getGroups = function () {

        $scope.groupNames.push("");
        $scope.groupIds.push(0);

        // if ($rootScope.role != 'ROLE_ADMIN') {
        //     if ($scope.deviceDetail.deviceHierarchy) {
        //         $scope.groupNames.push($scope.deviceDetail.deviceHierarchy.hierarchyName);
        //         $scope.groupIds.push($scope.deviceDetail.deviceHierarchy.hierarchyId);
        //     }
        //     return;
        // }

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
                    for (var i in responseData) {
                        $scope.groupNames.push(responseData[i].hierarchyName);
                        $scope.groupIds.push(responseData[i].hierarchyId);
                    }
                }

            }, function errorCallback(response) {
                console.log(response);
                Notification.error({
                    message: $translate.instant('DEVICEDETAIL_FETCH_ERROR'),
                    delay: 10000
                });
            });
    };


    $scope.updateDevice = function () {

        if (!$window.confirm($translate.instant("DEVICEDETAIL_CONFIRM_UPDATE"))) {
            return;
        }

        $(".content-tab").LoadingOverlay("show");


        if ($scope.locChange) {
            $scope.updateDeviceLocation();
        } else {
            $scope._updateDevice();
        }


        $scope.noChange = true;
        $(".content-tab").LoadingOverlay("hide");
    };

    $scope._updateDevice = function () {
        var deviceType = $scope.deviceType ? $scope.deviceType : $scope.deviceDetail.deviceType;

        var dailyControlStart = $("#" + "daily-control-window").data('from');
        var dailyControlEnd = $("#" + "daily-control-window").data('to');
        var noOfInterruptionInADay = $("#" + "interruptions-per-day").val();
        var maxInterruptionLength = $("#" + "duration-of-interruption").val();
        var minInterruptionInterval = $("#" + "distance-between-interruptions").val();
        var maxInterruptionDelay = $scope.deviceCategoryMap['ChargingDevice'].indexOf(deviceType) > -1 ?
            $("#" + "charged-battery-in").val() :
            $("#" + "maximum-start-delay").val();
        var latestAcceptanceTime = Math.min($("#" + "latest-notification-time").val(), maxInterruptionDelay);
        var deviceFlexibilityDetail = {
            dailyControlStart: dailyControlStart,
            dailyControlEnd: dailyControlEnd,
            noOfInterruptionInADay: noOfInterruptionInADay,
            maxInterruptionLength: maxInterruptionLength,
            minInterruptionInterval: minInterruptionInterval,
            maxInterruptionDelay: maxInterruptionDelay,
            latestAcceptanceTime: latestAcceptanceTime
        };

        var deviceHierarchyObj = null;
        if ($scope.hierarchyName != '') {
            deviceHierarchyObj = {
                hierarchyId: $scope.groupIds[$scope.groupNames.indexOf($scope.hierarchyName)],
                hierarchyName: $scope.hierarchyName
            };
        } else {
            deviceHierarchyObj = null;
        }

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        var data = {
            deviceId: $scope.deviceDetail.deviceId,
            deviceType: deviceType,
            deviceFlexibilityDetail: deviceFlexibilityDetail,
            deviceHierarchy: deviceHierarchyObj,
            flexible: $scope.isDeviceFlexible
        };

        $http({
                url: API_UPDATE_DEVICE,
                // url: "http://localhost/dashboard/api/index.php?action=updatedevice",
                method: 'POST',
                data: data,
                headers: headers
            })
            .then(function (response) {
                //Messages
                if (response.data.status == "OK") {
                    console.log(response.data.message);
                    Notification.success($translate.instant('OPERATION_SUCCESS'));
                } else {
                    console.log(response.data.message);
                    Notification.error({
                        message: $translate.instant('DEVICEDETAIL_FETCH_ERROR'),
                        delay: 10000
                    });
                }

                $timeout(function () {
                    $scope.getDeviceDetail(true);
                });

            }, function errorCallback(response) {
                console.log(response);
                Notification.error({
                    message: $translate.instant('DEVICEDETAIL_FETCH_ERROR'),
                    delay: 10000
                });
            });
    };


    $scope.setDeviceType = function (dType) {
        if ($scope.deviceType == dType) {
            return;
        }

        // reset only of dType is in different category from $scope.deviceType
        if ($scope.deviceCategoryMap[$scope.deviceCategory].indexOf($scope.deviceType) == -1) {
            $scope.resetFlexParams();
        }

        $scope.deviceType = dType;
        $scope.valueChanged();
    };


    $scope.setDeviceCategory = function (dCat) {
        $scope.deviceCategory = dCat;
    };


    $scope.resetFlexParams = function () {
        if ($scope.deviceDetail.deviceFlexibilityDetail != null) {

            var $dcw = $("#daily-control-window");
            var $msd = $("#maximum-start-delay");
            var $lnt = $("#latest-notification-time");
            var $ipd = $("#interruptions-per-day");
            var $doi = $("#duration-of-interruption");
            var $dbi = $("#distance-between-interruptions");
            var $cbi = $("#charged-battery-in");

            $scope.dailyControlStart = $scope.deviceDetail.deviceFlexibilityDetail.dailyControlStart;
            $scope.dailyControlEnd = $scope.deviceDetail.deviceFlexibilityDetail.dailyControlEnd;
            var dcw_instance = $dcw.data("ionRangeSlider");
            dcw_instance.update({
                from: $scope.dailyControlStart,
                to: $scope.dailyControlEnd
            });

            $scope.maxInterruptionDelay = $scope.deviceDetail.deviceFlexibilityDetail.maxInterruptionDelay;
            var msd_instance = $msd.data("ionRangeSlider");
            msd_instance.update({
                from: $scope.maxInterruptionDelay
            });

            $scope.latestAcceptanceTime = $scope.deviceDetail.deviceFlexibilityDetail.latestAcceptanceTime;
            var lnt_instance = $lnt.data("ionRangeSlider");
            lnt_instance.update({
                from: $scope.latestAcceptanceTime,
                from_max: $scope.maxInterruptionDelay
            });

            var cbi_instance = $cbi.data("ionRangeSlider");
            cbi_instance.update({
                from: $scope.maxInterruptionDelay
            });

            $scope.noOfInterruptionInADay = $scope.deviceDetail.deviceFlexibilityDetail.noOfInterruptionInADay;
            var ipd_instance = $ipd.data("ionRangeSlider");
            ipd_instance.update({
                from: $scope.noOfInterruptionInADay
            });

            $scope.maxInterruptionLength = $scope.deviceDetail.deviceFlexibilityDetail.maxInterruptionLength;
            var doi_instance = $doi.data("ionRangeSlider");
            doi_instance.update({
                from: $scope.maxInterruptionLength
            });

            $scope.minInterruptionInterval = $scope.deviceDetail.deviceFlexibilityDetail.minInterruptionInterval;
            var dbi_instance = $dbi.data("ionRangeSlider");
            dbi_instance.update({
                from: $scope.minInterruptionInterval
            });
        }
    };


    $scope.discardChanges = function ($event) {
        $scope.resetData();
        $scope.resetFlexParams();

        $scope.noChange = true;
        $scope.locChange = false;

        // clear device search box typed query
        var searchBox = document.getElementById('device-address');
        searchBox.value = "";

        // restore device group drop-down selected menu
        var groupSelect = document.getElementById('group-select');
        groupSelect.value = 'string:' + $scope.hierarchyName;

        // reload map to restore marker
        $timeout(function () {
            $scope.loadDeviceLocationMap();
        });
    };


    $scope.resetData = function () {

        $scope.deviceType = $scope.deviceDetail.deviceType;

        var deviceType = $scope.deviceDetail.deviceType;
        if ($scope.deviceCategoryMap['TCLDevice'].indexOf(deviceType) > -1) {
            $scope.deviceCategory = 'TCLDevice';
        } else if ($scope.deviceCategoryMap['WetDevice'].indexOf(deviceType) > -1) {
            $scope.deviceCategory = 'WetDevice';
        } else if ($scope.deviceCategoryMap['ChargingDevice'].indexOf(deviceType) > -1) {
            $scope.deviceCategory = 'ChargingDevice';
        } else {
            $scope.deviceCategory = '';
        }

        if ($scope.deviceDetail.deviceHierarchy != null) {
            $scope.hierarchyName = $scope.deviceDetail.deviceHierarchy.hierarchyName;
        } else {
            $scope.hierarchyName = '';
        }

        $scope.deviceTypeIconUrl = $scope.getDeviceTypeIconUrl($scope.deviceDetail.deviceType);
        $scope.deviceStateIconUrl = $scope.getDeviceStateIconUrl($scope.deviceDetail.deviceState);
        $scope.deviceStatus = ($scope.deviceDetail.deviceState == 'Operating') ? true : false;
        $scope.isDeviceFlexible = ($scope.deviceDetail.flexible == true) ? true : false;

        $scope.latitude = $scope.deviceDetail.latitude;
        $scope.longitude = $scope.deviceDetail.longitude;
    };


    $scope.changeDeviceGroup = function (val) {
        $scope.valueChanged();
        $timeout(function () {
            $scope.hierarchyName = val;
        });
    };


    $scope.factoryResetDeviceFlexibilityParams = function () {

        if (!$window.confirm($translate.instant("DEVICEDETAIL_CONFIRM_FACTORY_RESET"))) {
            return;
        }

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        var url = API_RESET_DEVICE_FLEXIBILITY_PARAMS + $scope.deviceDetail.deviceId;

        $http({
                url: url,
                method: 'POST',
                headers: headers
            })
            .then(function (response) {
                Notification.success($translate.instant('OPERATION_SUCCESS'));
                $timeout(function () {
                    $scope.getDeviceDetail();
                });

                $timeout(function () {
                    $scope.discardChanges();
                }, 500);

            }, function errorCallback(response) {
                console.log(response);
                Notification.error({
                    message: $translate.instant('OPERATION_FAILURE'),
                    delay: 10000
                });
            });
    };


    $scope.getDevicePowerTimeSeries = function () {
        $(".device-chart-page").LoadingOverlay("show");
        var fromDate = '';
        var toDate = '';
        var deviceId = $scope.deviceId;
        var req_url = API_GET_POWER_CONSUMPTION;

        $scope.devicePowerTimeSeries = {};
        $scope.deviceEnergyTimeSeries = {};

        if ($scope.specificReadingsDate) {
            fromDate = moment($scope.specificReadingsDate).format($scope.dateFormat)+"T00:00:00";
            toDate = moment($scope.specificReadingsDate).format($scope.dateFormat)+"T23:59:59";
        } else {
            fromDate = moment().format($scope.dateFormat)+"T00:00:00";
            toDate = moment().format($scope.dateFormat)+"T23:59:59";
        }

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
                url: req_url + "/" + deviceId + "/" + fromDate + "/" + toDate,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                $scope.preparePowerAndEnergyTs(response.data.data);
            }, function errorCallback(response) {
                console.log(response);
                Notification.error($translate.instant('OPERATION_FAILURE'));
                $(".device-chart-page").LoadingOverlay("hide");
            })
            .finally(function () {
                $(".device-chart-page").LoadingOverlay("hide");
            });
    };


    $scope.preparePowerAndEnergyTs = function (data) {

        if (data && Object.keys(data).length) {

            $scope.xAxisLabels = [];

            var dateFormat = $scope.dateFormatWithoutSeconds;
            var date = new Date(moment().format(dateFormat));
            if ($scope.specificReadingsDate) {
                date = new Date(moment($scope.specificReadingsDate).format(dateFormat));
            }
            for (var h = 0; h < 24; h++) {
                for (var m = 0; m < 60; m++) {
                    date.setHours(h);
                    date.setMinutes(m);
                    $scope.devicePowerTimeSeries[moment(date).format(dateFormat)] = null;
                    $scope.deviceEnergyTimeSeries[moment(date).format(dateFormat)] = null;
                    $scope.xAxisLabels.push(moment(date).format(dateFormat));
                }
            }

            $.each(data, function (timestamp, power) {
                var datetime = (timestamp);
                var d1 = new Date(datetime);
                var ts = moment(d1).format(dateFormat);
                $scope.devicePowerTimeSeries[ts] = power;
            });

            var prev = 0;
            $.each($scope.devicePowerTimeSeries, function (timestamp, power) {
                $scope.deviceEnergyTimeSeries[timestamp] = (prev + power) * 60 / 3600; // sampling freq in sec / sec in hour
                prev = prev + power;
            });
        }

        $scope.getDeviceSchedule();
    };


    $scope.getDeviceSchedule = function () {
        $(".device-chart-page").LoadingOverlay("show");

        $scope.deviceScheduleData = {};
        var deviceId = $scope.deviceId;

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        var fromDate = moment().format($scope.dateFormat)+"T00:00:00";
        var toDate = moment().format($scope.dateFormat)+"T23:59:59";

        if ($scope.specificReadingsDate) {
            fromDate = moment($scope.specificReadingsDate).format($scope.dateFormat)+"T00:00:00";
            toDate = moment($scope.specificReadingsDate).format($scope.dateFormat)+"T23:59:59";
        }

        $http({
                url: API_GET_DEVICE_SCHEDULES + "/" + deviceId + "/" + fromDate + "/" + toDate,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                $scope.deviceScheduleData = response.data.data;
                $scope.plotDeviceData();
            }, function errorCallback(response) {
                console.log(response);
            }).finally(function () {
                $(".device-chart-page").LoadingOverlay("hide");
            });
    };



    $scope.plotDeviceData = function () {

        var powerSeriesColor = '#50B432'; // Highcharts.getOptions().colors[5];
        var energySeriesColor = 'steelblue'; //Highcharts.getOptions().colors[0];
        var scheduleSeriesColor = '#ED561B'; //Highcharts.getOptions().colors[2];
        var interruptionColor = 'gold'; //Highcharts.getOptions().colors[2];
        var interruptionBandColor = 'lightgoldenrodyellow';
        var powerSeriesName = 'Power';
        var energySeriesName = 'Energy';
        var scheduleSeriesName = 'Schedule';

        var options = {
            chart: {
                zoomType: 'x'
            },
            credits: {
                enabled: false
            },
            title: {
                text: 'Device Power, Energy, and Schedule',
                align: 'center'
            },
            plotOptions: {
                series: {
                    turboThreshold: 1440,
                    connectNulls: true,
                    label: {
                        enabled: false
                    },
                    marker: {
                        // enabled: false,
                        // radius: 5,
                        enabledThreshold: 2.0
                    },
                    //borderWidth: 1,
                    //borderColor: 'grey'
                },
                column: {
                    pointPadding: 0,
                    pointPlacement: -0.30,
                    pointRange: 25,
                    //maxPointWidth: 50,
                }
            },
            xAxis: [{
                labels: {
                    formatter: function () {
                        return moment($scope.xAxisLabels[this.value]).format("HH:mm");
                    },
                    rotation: -45
                },
                //categories: $scope.xAxisLabels,
                crosshair: true,
                plotLines: [{
                    color: 'black', // Red
                    width: $scope.specificReadingsDate && new Date(moment($scope.specificReadingsDate)).getDate() != new Date().getDate() ? 0 : 2, // don't show vertical line if displaying past data
                    value: new Date().getHours() * 60 + new Date().getMinutes(),
                    zIndex: 5,
                    dashStyle: 'dash',
                    label: {
                        text: $scope.specificReadingsDate && new Date(moment($scope.specificReadingsDate)).getDate() != new Date().getDate() ? '' : 'Current Time', // don't show vertical line label if displaying past data
                        /*
                        style: {
                            fontSize: '10px',
                            fontWeight: 'normal'
                        }
                        */
                    }
                }]
            }],
            yAxis: [{ // Primary yAxis
                min: 0,
                labels: {
                    formatter: function () {
                        return this.value.toFixed(1) + ' W';
                    },
                    style: {
                        color: powerSeriesColor
                    }
                },
                title: {
                    text: powerSeriesName,
                    style: {
                        color: powerSeriesColor
                    }
                }
            }, { // Secondary yAxis
                min: 0,
                gridLineWidth: 0,
                title: {
                    text: energySeriesName,
                    style: {
                        color: energySeriesColor
                    }
                },
                labels: {
                    formatter: function () {
                        return this.value.toFixed(1) + ' Wh';
                    },
                    style: {
                        color: energySeriesColor
                    }
                },
                opposite: true
            }, { // Tertiary yAxis
                //endOnTick: false,
                categories: ['Disabled', 'Enabled', '', '', '', ''],
                min: 0,
                max: 5,
                gridLineWidth: 0,
                title: {
                    text: scheduleSeriesName,
                    style: {
                        color: scheduleSeriesColor
                    },
                    y: 90
                },
                labels: {
                    /*
                    formatter: function () {
                        if (this.value == 0) {
                            return 'Off';
                        } else if (this.value == 1) {
                            return 'On';
                        } else {
                            return '';
                        }
                    },
                    */
                    style: {
                        color: scheduleSeriesColor
                    }
                },
                opposite: true
            }],
            tooltip: {
                formatter: function () {
                    // The first returned item is the header, subsequent items are the points
                    return ['<b>' + moment($scope.xAxisLabels[this.x]).format("YYYY-MM-DD HH:mm") + '</b><br>'].concat(
                        this.points.map(function (point) {
                            var tt = '<span style="color:' + point.color + '">\u25CF</span> ';
                            if (point.isNull) {
                                return tt + point.series.name + ': <b>' + 'Null' + '</b><br>';
                            }
                            if (point.series.name == powerSeriesName) {
                                tt += point.series.name + ': <b>' + point.y.toFixed(1) + ' W';
                            }
                            if (point.series.name == energySeriesName) {
                                tt += point.series.name + ': <b>' + point.y.toFixed(1) + ' Wh';
                            }
                            if (point.series.name == scheduleSeriesName) {
                                tt += 'State' + ': <b>' + (point.y == 0 ? 'Disabled' : 'Enabled');
                            }
                            return tt + '</b><br>';
                        })
                    );
                },
                shared: true
            },

            legend: {
                layout: 'horizontal',
                align: 'center',
                verticalAlign: 'bottom',
                floating: false,
                //x: -80,
                //y: 55,
                //backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || 'rgba(255,255,255,0.25)',
                labelFormatter: function () {
                    return this.name == scheduleSeriesName ? 'Normal Operation' : this.name;
                }
            },
            series: [{
                    name: powerSeriesName,
                    type: 'spline',
                    data: [],
                    color: powerSeriesColor,
                    zIndex: 1,
                },
                {
                    name: energySeriesName,
                    type: 'column',
                    yAxis: 1,
                    data: [],
                    color: energySeriesColor,
                    zIndex: 0,
                }, {
                    name: scheduleSeriesName,
                    type: 'line',
                    step: true,
                    yAxis: 2,
                    data: [],
                    color: scheduleSeriesColor,
                    zIndex: 2,
                    marker: {
                        enabled: false
                    },
                    zoneAxis: 'x',
                    zones: [],
                    //dashStyle: 'shortdot',
                },
                {
                    name: 'Controlled Interruption',
                    type: 'line',
                    marker: {
                        enabled: false
                    },
                    color: interruptionColor,
                    data: []
                }
            ],
            responsive: {
                rules: [{
                    condition: {
                        maxWidth: 500
                    },
                    chartOptions: {
                        legend: {
                            floating: false,
                            layout: 'horizontal',
                            align: 'center',
                            verticalAlign: 'bottom',
                            x: 0,
                            y: 0
                        }
                    }
                }]
            }
        };

        var dateFormat = $scope.dateFormatWithoutSeconds;

        // push power time series
        if ($scope.devicePowerTimeSeries && Object.keys($scope.devicePowerTimeSeries).length) {

            var powerData = Array($scope.xAxisLabels.length).fill(null);
            $.each($scope.devicePowerTimeSeries, function (xa, ya) {
                var datetime = (xa);
                var d1 = new Date(datetime);
                var timestamp = moment(d1).format(dateFormat);

                var idx = $scope.xAxisLabels.indexOf(timestamp);
                powerData.splice(idx, 1, ya);
            });
            options.series[0].data = powerData;
        }

        // push energy time series
        if ($scope.deviceEnergyTimeSeries && Object.keys($scope.deviceEnergyTimeSeries).length) {

            var old_timestamp = "1970-01-01T00:00:00.000+0000";
            var old_value = 0;
            var energyData = Array($scope.xAxisLabels.length).fill(null);
            $.each($scope.deviceEnergyTimeSeries, function (xa, ya) {

                if (!($scope.specificReadingsDate && new Date(moment($scope.specificReadingsDate)).getDate() != new Date().getDate())) {
                    var currentMinutes = new Date().getHours() * 60 + new Date().getMinutes();
                    var xaMinutes = new Date(xa).getHours() * 60 + new Date(xa).getMinutes();

                    if (xaMinutes > currentMinutes) { // stop calculating energy for beyond current time
                        return;
                    }
                }


                if (moment(xa).diff(moment(old_timestamp), "minutes") >= 15) {
                    var timestamp = moment(new Date(xa)).format(dateFormat);

                    var idx = $scope.xAxisLabels.indexOf(timestamp);
                    if (idx > 0) {
                        // don't count power consumption during the first minute the device starts
                        prev_ya = $scope.deviceEnergyTimeSeries[$scope.xAxisLabels[idx - 1]];
                        energyData.splice(idx, 1, Math.max(prev_ya - old_value, 0));
                    } else {
                        energyData.splice(idx, 1, Math.max(ya - old_value, 0));
                    }

                    old_timestamp = xa;
                    old_value = ya;
                }
            });
            options.series[1].data = energyData;
        }

        // push schedules
        //var scheduleData = Array($scope.xAxisLabels.length).fill($scope.deviceDetail.defaultState);
        var scheduleData = Array($scope.xAxisLabels.length).fill(1);
        if ($scope.deviceScheduleData && Object.keys($scope.deviceScheduleData).length) {

            //var scheduleData = Array($scope.xAxisLabels.length).fill(1);
            var zoneData = []; // different colors for subsections of line
            var plotBands = [];
            for (var i = 0; i < $scope.deviceScheduleData.length; i++) {
                item = $scope.deviceScheduleData[i];
                if (!$.isEmptyObject(item)) {
                    var scheduleStartTime = new Date(item.scheduleStartTime + ".000+0000"); // todo send UTC time from server
                    var flexOfferCreationTime = new Date(item.flexOfferCreationTime + ".000+0000"); // todo send UTC time from server
                    var scheduleStartTimeMinutes = scheduleStartTime.getHours() * 60 + scheduleStartTime.getMinutes();
                    var scheduleDuration = 15 * (item.flexOffer.flexOfferSchedule.scheduleSlices.length); // todo: get this from server
                    var scheduleStopTimeMinutes = scheduleStartTimeMinutes + scheduleDuration;
                    var idx;
                    var foType = item.foType;

                    // for wet devices, FOA switches off devices and generates FO. We show this in the plot
                    if (foType == 1 || $scope.deviceCategory == 'WetDevice' || $scope.deviceCategory == 'ChargingDevice') {
                        // FO creation time is always the previous slice (e.g. it's 12:30 if actual creation time
                        // is between 12:30 and 12:45. Thus we add 15 minutes to interruptionStartTime)
                        //var interruptionStartTime = new Date(moment(item.flexOffer.creationTime).add(15, 'minute'));
                        var interruptionStartTime = flexOfferCreationTime;
                        var interruptionStartTimeMinutes = interruptionStartTime.getHours() * 60 + interruptionStartTime.getMinutes();
                        var interruptionStopTimeMinutes = interruptionStartTimeMinutes + (scheduleStartTimeMinutes - interruptionStartTimeMinutes);

                        for (idx = interruptionStartTimeMinutes; idx < interruptionStopTimeMinutes; idx++) {
                            scheduleData.splice(idx, 1, 0);
                        }
                        zoneData.push({
                            value: Math.max(interruptionStartTimeMinutes, 0)
                        }, {
                            value: interruptionStopTimeMinutes,
                            color: interruptionColor
                        });

                        plotBands.push({
                            from: interruptionStartTimeMinutes,
                            to: interruptionStopTimeMinutes,
                            color: interruptionBandColor,
                            zIndex: 0
                        });
                    }

                    for (idx = scheduleStartTimeMinutes; idx < scheduleStopTimeMinutes; idx++) {
                        //scheduleData.splice(idx, 1, item.amount[0] == 0 ? 0 : 1);
                        scheduleData.splice(idx, 1, item.amount[0] > 0.001 ? 1 : 0);
                    }

                    zoneData.push({
                        value: Math.max(scheduleStartTimeMinutes, 0)
                    }, {
                        value: scheduleStopTimeMinutes,
                        color: interruptionColor
                    });

                    plotBands.push({
                        from: scheduleStartTimeMinutes,
                        to: scheduleStopTimeMinutes,
                        color: interruptionBandColor,
                        zIndex: 0
                    });
                }
            }
            options.series[2].zones = zoneData;
            options.xAxis[0].plotBands = plotBands;
        }
        options.series[2].data = scheduleData;

        Highcharts.chart('device_chart', options);
    };

    $scope.getDeviceControlHistory = function () {

        $scope.deviceControlData = {};
        var deviceId = $scope.deviceId;

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        var startDateTime = moment().format($scope.dateFormat) + "T00:00:00";
        var endDateTime = moment().format($scope.dateFormat) + "T23:59:59";

        if ($scope.specificControlDate) {
             startDateTime = moment($scope.specificControlDate).format($scope.dateFormat) + "T00:00:00";
             endDateTime = moment($scope.specificControlDate).format($scope.dateFormat) + "T23:59:59";
        }

        $http({
                url: API_GET_DEVICE_CONTROL_HISTORY + "/" + deviceId + "/" + startDateTime + "/" + endDateTime,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                $scope.deviceControlData = response.data.data;
                $scope.getDeviceStateHistory();
            }, function errorCallback(response) {
                console.log(response);
            }).finally(function () {
            });
    };

    $scope.getDeviceStateHistory = function () {

        $scope.deviceStateHistory = {};
        var deviceId = $scope.deviceId;

        var headers = {
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        var startDateTime = moment().format($scope.dateFormat) + "T00:00:00";
        var endDateTime = moment().format($scope.dateFormat) + "T23:59:59";

        if ($scope.specificControlDate) {
             startDateTime = moment($scope.specificControlDate).format($scope.dateFormat) + "T00:00:00";
             endDateTime = moment($scope.specificControlDate).format($scope.dateFormat) + "T23:59:59";
        }

        $http({
                url: API_GET_DEVICE_STATE_HISTORY + "/" + deviceId + "/" + startDateTime + "/" + endDateTime,
                method: 'GET',
                headers: headers
            })
            .then(function (response) {
                $scope.deviceStateHistory = response.data.data;
            }, function errorCallback(response) {
                console.log(response);
            }).finally(function () {
            });
    };

    $scope.getAndPlotDeviceData = function () {
        $scope.getDevicePowerTimeSeries(); // we call other functions from inside this function
    };

    $scope.getDeviceDetail();
});