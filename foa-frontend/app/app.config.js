angular.
module('BJD').
config(['$locationProvider', '$routeProvider', '$translateProvider',
    function config($locationProvider, $routeProvider, $translateProvider) {
        $locationProvider.hashPrefix('!');

        // Language Translate
        // console.log($localStorage.isLoggedIn);
        $translateProvider.translations('en', translationsEN);
        $translateProvider.translations('de', translationsDE);
        $translateProvider.fallbackLanguage('en');
        $translateProvider.preferredLanguage('en');
        
        $routeProvider.
        when('/dashboard', {
            templateUrl: 'pages/dashboard/view.html',
            controller: 'DashboardController'
        })
        .when('/devicelist', {
            templateUrl: 'pages/devicelist/view.html',
            controller: 'DeviceListController'
        }).
        when('/device/:deviceId', {
            templateUrl: 'pages/devicedetail/view.html',
            controller: 'DeviceDetailController'
        }).
        when('/login', {
            templateUrl: 'pages/login/view.html',
            controller: 'LoginController'
        }).
        when('/register', {
            templateUrl: 'pages/register/view.html',
            controller: 'RegisterController'
        }).
        when('/profile/:userName', {
            templateUrl: 'pages/profile/view.html',
            controller: 'ProfileController'
        }).
        when('/contract/:userName', {
            templateUrl: 'pages/contract/view.html',
            controller: 'ContractController'
        }).
        when('/bill/:userName', {
            templateUrl: 'pages/bill/view.html',
            controller: 'BillController'
        }).
        when('/config', {
            templateUrl: 'pages/configuration/view.html',
            controller: 'ConfigController'
        }).
        when('/grouping', {
            templateUrl: 'pages/grouping/view.html',
            controller: 'GroupingController'
        }).
        when('/messages', {
            templateUrl: 'pages/messages/view.html',
            controller: 'MessagesController'
        }).
        when('/language-test', {
            templateUrl: 'pages/language-test/view.html',
            controller: 'LTController'
        }).
        when('/forgotPassword', {
            templateUrl: 'pages/forgotpassword/view.html',
            controller: 'ForgotPasswordController'
        }).

        when('/user', {
            templateUrl: 'pages/user/list.html',
            controller: 'UserListController'
        }).
        when('/user/add', {
            templateUrl: 'pages/user/add.html',
            controller: 'UserAddController'
        }).
        when('/user/edit/:userName', {
            templateUrl: 'pages/user/edit.html',
            controller: 'UserEditController'
        }).

        otherwise('/login');
    }
]);
