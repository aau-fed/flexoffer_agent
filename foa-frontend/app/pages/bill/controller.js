BJD.controller('BillController', function($rootScope, $scope, $http, $location, $localStorage, $routeParams) {
    //Check if there is session id
    if (!$localStorage.isLoggedIn) {
        $location.path('/login');
        return;
    }

    if ($localStorage.userName != $routeParams.userName && $rootScope.role != 'ROLE_ADMIN') {
        $location.path('/dashboard');
        return;
    }

    $rootScope.pageTitle = "Bill";
    $rootScope.hide = false;

    $scope.getReward = function(date = '') {
        if(date==''){
           date = moment().subtract(1, 'months').format('YYYY/MM');
        }
        //console.log(date);
        var headers = {
            // authorization: "Basic " + $localStorage.auth,
            Authorization: $localStorage.token,
            'Content-Type': 'application/JSON'
        };
        $scope.simMessageText = "Fetching bill from FMAN, please wait...";
        $http({
            url: $localStorage.userName == $routeParams.userName ? API_GET_REWARD + "/" + date: API_GET_USER_BILL_BY_ORGANIZATION + '/' + $routeParams.userName + "/" + date,
            method: 'GET',
            headers: headers
        }).then(function(response) {
            var data = response.data.data;

            $scope.numFlexOffers = data.numFlexOffers;
            $scope.numOfExecutedFlexOffers = data.numOfExecutedFlexOffers;

            $scope.rewardFixed = data.rewardFixed;

            $scope.totalTimeFlex = data.totalTimeFlex;
            $scope.rewardTotalTimeFlex = data.rewardTotalTimeFlex;

            $scope.totalEnergyFlex = data.totalEnergyFlex;
            $scope.rewardTotalEnergyFlex = data.rewardTotalEnergyFlex;

            $scope.numCustomScheduleActivations = data.numCustomScheduleActivations;
            $scope.rewardTotalSchedFixed = data.rewardTotalSchedFixed;

            $scope.totalStartTimeDeviations = data.totalStartTimeDeviations;
            $scope.rewardTotalSchedEST = data.rewardTotalSchedEST;

            $scope.totalEnergyDeviations = data.totalEnergyDeviations;
            $scope.rewardTotalSchedEnergy = data.rewardTotalSchedEnergy;

            $scope.rewardTotal = data.rewardTotal;
            $scope.executionImbalance = data.executionImbalance;

        }, function errorCallback(response) {
            console.log(response);
        });
    };

    $scope.activateScripts = function() {
        /*Date picker*/
            var date_input = $('#date-reward');
            var datefield = date_input.datepicker({
              format: 'MM/yyyy',
              startView: "months", 
              minViewMode: "months",
              todayHighlight: true,
              autoclose: true
            }).on('changeDate', function(ev) {
              $scope.getReward(moment(ev.date).format('YYYY/MM'));
            });

            $('#PrintBill').on('click', function(){
                window.print();
            });
    }
    
    $scope.activateScripts();
    $scope.getReward();
});