var env = {};

// Import variables if present (from env.js)
if(window){  
  Object.assign(env, window.__env);
}

var BJD = angular.module('BJD', [
    'ngRoute',
    'ngStorage',
    'ngCookies',
    'ngMessages',
    'ngAutocomplete',
    'angularUtils.directives.dirPagination',
    'pascalprecht.translate',
    'ui-notification'
]).constant('__env', env);

BJD.config(function (NotificationProvider) {
    NotificationProvider.setOptions({
        delay: 3000,
        startTop: 20,
        startRight: 10,
        verticalSpacing: 20,
        horizontalSpacing: 20,
        positionX: 'right',
        positionY: 'bottom'
    });
});

BJD.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
}]);

var API_BASE_URL = __env.foaApiUrl;
var API_FOG_BASE_URL = __env.fogApiUrl;

var API_KPI_URL = API_BASE_URL + "kpi/";
var API_USER_URL = API_BASE_URL + "prosumer/";
var API_DEVICE_URL = API_BASE_URL + "devices/";
var API_FO_URL = API_BASE_URL + "fo/";

var API_FOG_URL = API_FOG_BASE_URL + "fog/";

//Login and register
var API_USER_LOGIN = API_USER_URL + "login";
var API_USER_REGISTER = API_USER_URL + "register";
var API_USER_FORGOT_PASSWORD = API_USER_URL + "forgotPassword";
var API_USER_USERNAME_EXISTS = API_USER_URL + "userNameExists";
var API_USER_TPLINK_ACCOUNT_EXISTS = API_USER_URL + "tpLinkAccountExists";
var API_USER_REFRESH_TOKEN = API_USER_URL + "refreshToken";

//profile
var API_UPDATE_USER = API_USER_URL + "updateUser";
var API_GET_USER = API_USER_URL + "getUser";
var API_UPDATE_USER_PIC = API_USER_URL + "pic";

// contract and rewards
var API_GET_REWARD = API_USER_URL + "bill";
var API_GET_CONTRACT = API_USER_URL + "contract";

// messages
var API_GET_ALL_MESSAGES = API_USER_URL + "getAllMessages";
var API_GET_UNREAD_MESSAGES = API_USER_URL + "getUnreadMessages";
var API_DELETE_ALL_MESSAGES = API_USER_URL + "clearAllMessages";
var API_MARK_ALL_MESSAGES_AS_READ = API_USER_URL + "markAllMessagesAsRead";

//Devices
var API_GET_DEVICE = API_DEVICE_URL + "getDevice/";
var API_GET_DEVICE_SUMMARY_LIST = API_DEVICE_URL + "deviceSummaryList";
var API_GET_DEVICES = API_DEVICE_URL + "devices";
var API_UPDATE_DEVICE = API_DEVICE_URL + "updateDevice";
var API_TOGGLE_DEVICE = API_DEVICE_URL + "toggleDevice/";
var API_GET_DEVICE_LOCATION = API_DEVICE_URL + "getDevicesLocation";
// var API_GET_DEVICE_LOCATION = "http://localhost/dashboard/api/index.php?action=getdevicelocation";
var API_UPDATE_DEVICE_LOCATION = API_DEVICE_URL + "updateDeviceLocation";
var API_TOGGLE_DEVICE_FLEXIBILITY = API_DEVICE_URL + "toggleDeviceFlexibility/";
var API_RESET_DEVICE_FLEXIBILITY_PARAMS = API_DEVICE_URL + "resetDeviceFlexibilityParams/";
var API_GET_LATEST_CONSUMPTION = API_DEVICE_URL + "getLatestConsumption"; //this gives data for 24 hours, for all devices for the 
var API_GET_CONSUMPTION_FOR_DATE = API_DEVICE_URL + "getConsumptionForDate";
var API_GET_POWER_CONSUMPTION = API_DEVICE_URL + "getPowerConsumption";
var API_GET_DEVICE_SCHEDULES = API_FO_URL + "getSchedules";
var API_GET_DEVICE_CONTROL_HISTORY = API_DEVICE_URL + "getControlHistory";
var API_GET_DEVICE_STATE_HISTORY = API_DEVICE_URL + "getStateHistory";

// Configuration 
var API_START_SIMULATED_DEVICE = API_DEVICE_URL + "loadSimulatedDevice";
var API_CONFIG_GET_ORG_CONTROL_STATUS = API_USER_URL + "getOrganization";
var API_CONFIG_SET_ORG_CONTROL = API_USER_URL + "setOrgControl";
var API_CONFIG_SET_ORG_POOL_CONTROL = API_USER_URL + "setOrgPoolControl";
var API_CONFIG_SET_ORG_POOL_DEVICE_COOLING_PERIOD = API_USER_URL + "setCoolingPeriod";
var API_CONFIG_GET_FOG_STATUS = API_FOG_URL + "getFOGStatus";
var API_CONFIG_START_FOG = API_FOG_URL + "startFoGeneration";
var API_CONFIG_STOP_FOG = API_FOG_URL + "stopFoGeneration";
var API_CONFIG_CLEAR_ORG_FOS = API_FO_URL + "clearOrgFosForDate";

// Device Grouping
var API_GET_GROUPS = API_DEVICE_URL + "getGroups";
var API_ADD_GROUP = API_DEVICE_URL + "addGroup";
var API_DELETE_GROUP = API_DEVICE_URL + "removeGroup";
var API_UPDATE_GROUP = API_DEVICE_URL + "updateGroup";

// User List
var API_REGISTER_USER_BY_ORGANIZATION = API_USER_URL + 'registerUserByOrganization';
var API_GET_USERS_BY_ORGANIZATION = API_USER_URL + 'getUsersByOrganization';
var API_GET_USER_BY_ORGANIZATION = API_USER_URL + 'getUserByOrganization';
var API_UPDATE_USER_BY_ORGANIZATION = API_USER_URL + 'updateUserByOrganization';
var API_REMOVE_USER_BY_ORGANIZATION = API_USER_URL + 'removeUserByOrganization';
var API_GET_USER_CONTRACT_BY_ORGANIZATION = API_USER_URL + 'getUserContractByOrganization';
var API_GET_USER_BILL_BY_ORGANIZATION = API_USER_URL + 'getUserBillByOrganization';
var API_UPDATE_USER_PIC_BY_ORGANIZATION = API_USER_URL + "updateUserPicByOrganization";

// KPI
var API_FOA_KPI = API_KPI_URL + "foaKpi";

var DEFAULT_USER_IMG = "img/nick.png";

var DEFAULT_EMAIL = "@please-set.email";

//Device Icons
var DEVICE_ICONS = {
    HeatPump: 'icon-modal-water-heater',
    Boiler: 'icon-modal-boiler',
    DishWasher: 'icon-modal-washing-dish',
    WasherDryer: 'icon-modal-washing-machine'
};

var COUNTRY_CODES = {
    "10007": "+357",
    "10006": "+49",
    "10004": "+41",
    "10001": "+45"
};

var DEVICE_CATEGORY_MAP = {
    'WetDevice': ['DishWasher', 'WasherDryer'],
    'TCLDevice': ['HeatPump', 'Boiler', 'AirConditioner', 'RoomHeater', 'Refrigerator', 'Freezer'],
    'ChargingDevice': ['ElectricBike', 'ElectricLawnMower', 'ElectricCar']
};

var ALL_ADMINS = ['admin', 'aauAdmin', 'cyprusAdmin', 'swissAdmin', 'swwAdmin', 'testAdmin'];
var SYS_ADMINS = ['aauSysAdmin', 'cyprusSysAdmin', 'swissSysAdmin', 'swwSysAdmin', 'testSysAdmin'];
var SWISS_USERS = ['swissUser','swissAdmin', 'swissSysAdmin'];

var SIM_DEVICE_MODELS = {
    "PV": {
        "device_name": "PV",
        "model_name": "OnOff",
        "params": {
            "p_on": 2500,
        }
    },
    "Wind": {
        "device_name": "Wind",
        "model_name": "OnOff",
        "params": {
            "p_on": 3000,
        }
    },
    "Refrigerator": {
        "device_name": "Refrigerator",
        "model_name": "ExponentialDecay",
        "params": {
            "lambda": 0.27,
            "p_active": 126.19,
            "p_peak": 650.5
        }
    },
    "AirConditioner": {
        "device_name": "AirConditioner",
        "model_name": "LogarithmicGrowth",
        "params": {
            "lambda": 13.78,
            "p_base": 2120.46 + 126.19
        }
    },
    "HeatPump": {
        "device_name": "HeatPump",
        "model_name": "SISOLinearSystem",
        "params": {
            "A": -0.01,
            "B": 0.002,
            "C": 1,
            "D": 0
        }
    }
};

var showValidationMessage = function (obj) {
    var len = obj.data.length;
    for (var i = 0; i < len; i++) {
        //Remove old error messages
        var parentnode = document.getElementById(obj.data[i].field);
        var errornode = document.getElementById("error-msg-" + obj.data[i].field);
        if (errornode) {
            errornode.remove();
        }

        var spannode = document.createElement("SPAN");
        var textnode = document.createTextNode(obj.data[i].message);
        spannode.className = "error-msg alert alert-danger";
        spannode.id = "error-msg-" + obj.data[i].field;
        spannode.appendChild(textnode);
        parentnode.appendChild(spannode);
    }
};


BJD.controller('Ctrl', function ($localStorage, $location, $scope, $rootScope, $window, $timeout, $translate, $http) {
    $scope.refreshedAt = $localStorage.timed;

    $scope.signout = function (confirm = true) {
        if (confirm) {
            if (!$window.confirm($translate.instant("USER_LOGOUT_CONFIRM"))) {
                return;
            }
        }
        var lng = $localStorage.selectedLanguage;
        $localStorage.$reset({
            selectedLanguage: lng
        });
        $rootScope.role = "";
        $timeout(function () {
            $location.path('/login');
        });
    };

    //Load custom scripts
    $scope.$on('$viewContentLoaded', function () {
        $timeout(function () {
            customScript();
        });
        //Check expiration time of token
        if ($scope.checkIsLoggedIn()) {
            var decToken = JSON.parse(atob($localStorage.token.split('.')[1]));
            if (decToken.exp < new Date().getTime() / 1000) {
                $scope.signout(false);
            }
        }

        $scope.setRefreshToken();

        //Broadcast event to reload message count.
        $scope.$broadcast('refreshHeader');
    });

    if (!$rootScope.role) {
        $rootScope.role = $localStorage.role;
    }

    // $rootScope.role = $localStorage.role;
    //$scope.userRole = $localStorage.role;

    //Check if there is session
    //    if (!$localStorage.isLoggedIn) {
    //        $timeout(function () {
    //            $location.path('/login');
    //        });
    //    }


    $scope.checkIsLoggedIn = function () {
        if ($localStorage.isLoggedIn) {
            return true;
        } else {
            return false;
        }
    };

    /*Keep refresh token in queue*/
    $scope.setRefreshToken = function () {
        $timeout(function () {
            var rd = new Date($scope.refreshedAt);
            var cd = new Date();
            var diff = parseInt((cd - rd) / 1000 / 60);
            // console.log("Difference: "+diff);
            if ($scope.checkIsLoggedIn() && (diff > 56)) {
                // console.log("refreshToken called on "+diff+" Minutes");
                $scope.refreshedAt = new Date();
                $localStorage.timed = $scope.refreshedAt;
                $scope.refreshToken();
            } else if ($scope.checkIsLoggedIn()) {
                $scope.setRefreshToken();
            }
        }, 120000);

    };

    $scope.refreshToken = function () {
        /*$localStorage.timed = new Date();
        console.log("refreshToken called on "+Date());
        $scope.setRefreshToken();*/
        if ($scope.checkIsLoggedIn()) {
            var headers = {
                Authorization: $localStorage.token,
                'Content-Type': 'application/JSON'
            };

            $http({
                url: API_USER_REFRESH_TOKEN,
                method: 'GET',
                headers: headers
            }).then(function (response) {
                if (response.data.status == "OK") {
                    $localStorage.token = response.data.data.token;
                    $localStorage.timed = new Date();
                }
                $timeout(function () {
                    $scope.setRefreshToken();
                });

            }, function errorCallback(response) {
                console.log(response);
                return;
            });
        }
    };

});

BJD.controller('HeaderCtrl', function ($rootScope, $scope, $localStorage, $http, $translate, $route, $location, $timeout, Notification) {

    $scope.messageCount = 0;
    $scope.unreadMessages = [];
    $scope.lng = $translate.use();

    if ($localStorage.selectedLanguage) {
        $scope.lng = $localStorage.selectedLanguage;
        $translate.use($localStorage.selectedLanguage);
    } else {
        $localStorage.selectedLanguage = $scope.lng;
    }


    $scope.userName = function () {
        return $localStorage.userName;
    };


    $scope.userImg = function () {
        return ($localStorage.pic !== undefined && $localStorage.pic !== null) ? "data:image/png;base64," + $localStorage.pic : DEFAULT_USER_IMG;
    };


    $scope.getHeaderMessages = function () {
        var headers = {
            // authorization: "Basic " + $localStorage.auth,
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };

        $http({
            url: API_GET_UNREAD_MESSAGES,
            method: 'GET',
            headers: headers
        }).then(function (response) {
            $scope.unreadMessages = [];
            if (response.data.status == "OK" && response.data.data[0] != null) {
                $timeout(function () {
                    $scope.messageCount = response.data.data.length;
                });
                $scope.unreadMessages = response.data.data;
            } else {
                $scope.unreadMessages = [{
                    message: 'No new messages'
                }];
            }
        }, function errorCallback(response) {
            console.log(response);
        });
    };


    $scope.changeLanguage = function () {
        // $scope.lng = langKey;
        $translate.use($scope.lng);
        $localStorage.selectedLanguage = $scope.lng;
    };


    $(".msg-notify-link").click(function () {
        $("ul.messages").toggle('fast');
    });


    // console.log($localStorage.auth);
    $scope.$on('refreshHeader', function () {
        if ($localStorage.isLoggedIn) {
            $scope.getHeaderMessages();
        }
    });

    $scope.getUnreadMessages = function () {
        $timeout(function () {
            return $scope.unreadMessages;
        });
    };

    $scope.markAllMessagesAsRead = function () {
        if ($scope.messageCount == 0) {
            return;
        }

        var headers = {
            // authorization: "Basic " + $localStorage.auth,
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $http({
            url: API_MARK_ALL_MESSAGES_AS_READ,
            method: 'POST',
            headers: headers
        }).then(function (response) {
            $scope.messageCount = 0;
        }, function errorCallback(response) {
            console.log(response);
        });
    };
});


BJD.controller('NavBarController', function ($rootScope, $scope, $localStorage, $location) {
    $scope.userName = function () {
        return $localStorage.userName;
    };
});

// DIRECTIVE - FILE MODEL
BJD.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function () {
                scope.$apply(function () {
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

BJD.filter('propsFilter', function () {
    return function (items, props) {
        var out = [];

        if (angular.isArray(items)) {
            var keys = Object.keys(props);

            items.forEach(function (item) {
                var itemMatches = false;

                for (var i = 0; i < keys.length; i++) {
                    var prop = keys[i];
                    var text = props[prop].toLowerCase();
                    if (item[prop].toString().toLowerCase().indexOf(text) !== -1) {
                        itemMatches = true;
                        break;
                    }
                }

                if (itemMatches) {
                    out.push(item);
                }
            });
        } else {
            // Let the output be the input untouched
            out = items;
        }

        return out;
    };
});