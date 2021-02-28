BJD.controller('MessagesController', function($rootScope, $scope, $http, $location, $localStorage, $route, $window, $translate, Notification) {
    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    $rootScope.pageTitle = "Messages";
    $rootScope.hide = false;  /*Header and menu*/
    
    $scope.messages = [];
    $scope.pageSize = 15;
    $scope.sortKey = 'messageDate';
    $scope.reverse = true;
 
    // sort message list
    $scope.sort = function(keyName) {
        $scope.sortKey = keyName;   //set the sortKey to the param passed
        $scope.reverse = !$scope.reverse; //if true make it false and vice versa
    };


    $scope.getMessages = function() {

        /*Loading*/
        $("body").LoadingOverlay("show");

        /*var headers = { authorization: "Basic " + $localStorage.auth, 'Content-Type': 'application/JSON' };*/
        var headers = { 
            Authorization: $localStorage.token, 
            'Content-Type': 'application/JSON' 
        };
        $http({
                url: API_GET_ALL_MESSAGES,
                method: 'GET',
                headers: headers
            })
            .then(function(response) {
                var responseData = response.data.data;
                // console.log(response);
                if (responseData.length) {
                    $scope.messages = responseData;
                }else{
                    // console.log("No data.");
                    $(".message-container").html('<div class="accordion-content-title">No Messages</div>');
                }

            }, function errorCallback(response) {
                console.log(response);
            })
            .finally(function(){
                 $("body").LoadingOverlay("hide");
            });
    };

    $scope.clearAllMessages = function() {

        if (!$window.confirm($translate.instant('GENERAL_CONFIRMATION'))) {
            return;
        }

        /*var headers = { authorization: "Basic " + $localStorage.auth, 'Content-Type': 'application/JSON' };*/
        var headers = { 
            Authorization: $localStorage.token, 
            'Content-Type': 'application/JSON' 
        };
        $http({
                url: API_DELETE_ALL_MESSAGES,
                method: 'DELETE',
                headers: headers
            })
            .then(function(response) {
                Notification.success($translate.instant('OPERATION_SUCCESS'));
                $route.reload();
            }, function errorCallback(response) {
                Notification.error($translate.instant('OPERATION_FAILURE'));
            });
    };
    
    $scope.getMessages();
});