BJD.controller('GroupingController', function ($rootScope, $scope, $http, $location, $localStorage, $window, $translate, Notification) {

	//Check if there is session id
	if (!$localStorage.isLoggedIn) {
		$location.path('/login');
		return;
	}

	$scope.groups = {};
	$rootScope.pageTitle = "Device Group";
	$rootScope.hide = false;
	$scope.selectedGroup = null;
	$scope.buttonLabel = null;

	//Get group
	$scope.getgroup = function () {

		$scope.buttonLabel = "GROUPING_BUTTON_ADD";

		/*var headers = {
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

					for (var i in responseData) {
						var groupid = responseData[i].hierarchyId;
						$scope.groups[groupid] = responseData[i];
					}
				}

			}, function errorCallback(response) {
				Notification.error($translate.instant("OPERATION_FAILURE"));
				console.log(response);
			});
	};


	// add group
	$scope.submit = function () {
		var groupname = $scope.groupname;

		console.log(groupname);
		var data = {
			hierarchyName: groupname
		};

		/*var headers = {
			authorization: "Basic " + $localStorage.auth,
			'Content-Type': 'application/JSON'
		};*/

		var headers = { 
            Authorization: $localStorage.token, 
            'Content-Type': 'application/JSON' 
        };

		if ($scope.selectedGroup == null) {
			$http({
					url: API_ADD_GROUP,
					method: 'POST',
					data: data,
					headers: headers
				})
				.then(function (response) {
					Notification.success($translate.instant("OPERATION_SUCCESS"));
					$scope.groupname = "";
					$scope.getgroup();
				}, function errorCallback(response) {
					console.log(response);
					if (response.status == 422) {
						Notification.error({message: $translate.instant("GROUPING_GROUP_EXISTS"), delay: 10000});
					} else {
						Notification.error({message: $translate.instant("OPERATION_FAILURE"), delay: 10000});
					}
				});
		} else {
			console.log($scope.selectedGroup);
			$scope.update($scope.selectedGroup);
		}
	};


	// delete group
	$scope.delete = function (group) {
		if (!$window.confirm($translate.instant("GENERAL_CONFIRMATION"))) {
           return;
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
				url: API_DELETE_GROUP + "/" + group.hierarchyId,
				method: 'DELETE',
				headers: headers
			})
			.then(function (response) {
				Notification.success($translate.instant("OPERATION_SUCCESS"));
				delete $scope.groups[group.hierarchyId];
				$scope.getgroup();

			}, function errorCallback(response) {
				console.log(response);
				Notification.error($translate.instant("OPERATION_FAILURE"));
			});

	};



	$scope.edit = function (group) {

		$scope.groupname = group.hierarchyName;
		$scope.selectedGroup = group;
		$scope.buttonLabel = "GROUPING_BUTTON_UPDATE";
	};


	$scope.update = function (group) {

		console.log($scope.selectedGroup);

		var data = {
			hierarchyId: group.hierarchyId,
			hierarchyName: $scope.groupname,
			userId: group.userId
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
				url: API_UPDATE_GROUP + "/" + group.hierarchyId,
				method: 'PUT',
				data: data,
				headers: headers
			})
			.then(function (response) {
				Notification.success($translate.instant("OPERATION_SUCCESS"));
				delete $scope.groups[group.hierarchyId];
				$scope.groupname = "";
				$scope.getgroup();

			}, function errorCallback(response) {
				console.log(response);
				Notification.error($translate.instant("OPERATION_FAILURE"));
			});

	};




	$scope.getgroup();
});