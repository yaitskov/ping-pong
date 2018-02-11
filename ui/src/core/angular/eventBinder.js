export default function eventBinder($scope, listenerMap) {
    const cleaners = Object.entries(listenerMap).
          map(([topic, listener]) => $scope.$on(topic, listener));
    $scope.$on('$destroy', () => cleaners.forEach(cleaner => cleaner()));
    return cleaners;
}
