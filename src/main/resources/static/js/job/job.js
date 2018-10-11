ProjectApp.controller('JobController', function($scope, $http) {
    $scope.model = {
        //便于测试
        jobClassName: 'com.example.quartz.job.HelloJob',
        jobGroupName: 'test',
        cronExpression: '0/5 * * * * ?'
    };

    $scope.submit = function() {
        $http.post('job/addJob', $scope.model).then(function(response) {
            console.log(response.data);
        })
    };

    $scope.getJobs = function() {
        $http.get('job/queryJob').then(function(response) {
            $scope.jobs = response.data;
        })
    };

    $scope.getJobs();
});