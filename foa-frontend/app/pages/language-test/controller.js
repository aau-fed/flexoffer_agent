BJD.controller('LTController', function ($rootScope, $scope, $http, $location, $localStorage, $window) {

	//Check if there is session id
	if (!$localStorage.isLoggedIn) {
		$location.path('/login');
		return;
	}

	$rootScope.hide = false;

	
});