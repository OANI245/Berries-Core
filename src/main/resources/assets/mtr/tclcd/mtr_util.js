include("js_util.js");


/**
 * 表示列车无有效路线。
 */
var STATUS_NO_ROUTE = "no_route";
/**
 * 表示列车正在车厂中等待发车。
 */
var STATUS_WAITING_FOR_DEPARTURE = "waiting_for_departure";
/**
 * 表示列车正在离开车厂。
 */
var STATUS_LEAVING_DEPOT = "leaving_depot";
/**
 * 表示列车正在路线区间正常行驶。
 */
var STATUS_ON_ROUTE = "on_route";
/**
 * 表示列车正停靠站台。
 */
var STATUS_ARRIVED = "arrived";
/**
 * 表示列车正由当前线路终点站驶向下一线路起点站。
 */
var STATUS_CHANGING_ROUTE = "changing_route";
/**
 * 表示列车正在返回车厂。
 */
var STATUS_RETURNING_TO_DEPOT = "returning_to_depot";


/**
 * 停站开门时获取列车的哪些车门无法正常打开。如果一侧的车门均无法打开，则这一侧的开门信息均不会被返回。如果车门不处于正在打开的状态，返回 null。
 * @param {Train} train 要获取开门信息的列车。
 * @returns
 */
function getDoorOpenInfo(train) {
    
}

/**
 * 停站开门时获取列车的哪些车门正常打开。如果车门不处于正在打开的状态，返回 null。
 * @param {Train} train 要获取开门信息的列车。
 * @returns doorOpenMode 3:两侧开门 2：右侧 1：左侧 0：都不开
 */
function getDoorOpeningSide(train)
{
    if (train.isDoorOpening()) {
        let doorOpenMode;
        let templ=0,tempr=0;
        for (let carIndex = 0; carIndex < train.trainCars(); carIndex++) {
        templ=templ||train.doorLeftOpen[carIndex];
        tempr=tempr||train.doorRightOpen[carIndex];
        }
if (templ&&tempr) {
            doorOpenMode = 3;
            
        }
        else if (tempr) {
            doorOpenMode = 2;
            
        }
        else if (templ) {
            doorOpenMode = 1;
            
        }
        else doorOpenMode = 0;
        return doorOpenMode;
    } else {
        return null;
    }
}

/**
 * 获取列车状态。
 * @param {Train} train 要获取列车状态的列车。
 * @return {String} 列车状态。
 * @see {@link STATUS_NO_ROUTE}, {@link STATUS_WAITING_FOR_DEPARTURE}, {@link STATUS_LEAVING_DEPOT}, {@link STATUS_ON_ROUTE}, {@link STATUS_ARRIVED}, {@link STATUS_CHANGING_ROUTE}, {@link STATUS_RETURNING_TO_DEPOT}.
 */
function getTrainStatus(train) {
    let trainStatus = null;
    if (train.getAllPlatforms().size() == 0) {
        trainStatus = STATUS_NO_ROUTE;
    } else if (!train.isOnRoute()) {
        trainStatus = STATUS_WAITING_FOR_DEPARTURE;
    } else if (train.getAllPlatformsNextIndex() == train.getAllPlatforms().size()) {
        trainStatus = STATUS_RETURNING_TO_DEPOT;
    } else if (train.railProgress() == train.getAllPlatforms().get(train.getAllPlatformsNextIndex()).distance) {
        trainStatus = STATUS_ARRIVED;
    } else if (train.getAllPlatformsNextIndex() == 0) {
        trainStatus = STATUS_LEAVING_DEPOT;
    } else if (train.getThisRoutePlatformsNextIndex() == train.getThisRoutePlatforms().size()) {
        trainStatus = STATUS_CHANGING_ROUTE;
    } else {
        trainStatus = STATUS_ON_ROUTE;
    }
    return trainStatus;
}

/**
 * 获取列车的下一个路线。
 * @param {Train} train 要获取下一个路线的列车。
 * @return {Route} 下一个路线。如果不存在或下一个路线为隐藏路线，返回 null。
 */
function getNextRoute(train) {
    if (train.getAllPlatforms().get(train.getAllPlatforms().size() - 1) != train.getThisRoutePlatforms().get(train.getThisRoutePlatforms().size() - 1)) { // 列车路径的终点不为本线路终点
        let nextRoute = train.getAllPlatforms().get(train.getAllPlatforms().indexOf(train.getThisRoutePlatforms().size() - 1) + 1).route; // 返回本线路中点下一个车站所属的路线
        return nextRoute.isHidden ? null : nextRoute;
    }
    return null;
}

/**
 * 获取列车侧线所在的车厂。
 * @param {Train} train 要获取车厂的列车。
 * @return {Depot} 列车侧线所在的车厂。
 */
function getDepot(train) {
    return getMapValueByKey(MTRClientData.DATA_CACHE.sidingIdToDepot, train.siding().id);
}

/**
 * 获取列车当前路线信息。
 * @param {Train} train 要获取路线信息的列车。
 * @param {PlatformInfo} platformInfo 本线路某一站台，用于获取路线信息。
 */
function getRouteInfo(train, trainStatus, platformInfo) {
    if (platformInfo == null) {
        return null;
    }

    let routeName = platformInfo.route.name;
    let routeColor = new Color(platformInfo.route.color);
    let destination = platformInfo.destinationStation.name;
    let depotName = getDepot(train).name; // TODO 支持隐藏车厂名
    let routeInfo = { routeName: routeName, routeColor: routeColor, destination: destination, depotName: depotName, stationInfoList: [] };

    if (trainStatus == STATUS_RETURNING_TO_DEPOT) { // 对于回库车，train.getThisRoutePlatforms() 为空，故设置只有终点站和车厂名称的 stationInfoList
        routeInfo.stationInfoList.push({ stationName: platformInfo.station.name, interchangeInfo: getAllInterchangeRoutes(platformInfo.station, platformInfo.route, null) });
        routeInfo.stationInfoList.push({ stationName: depotName });
    } else {
        for (let platform of train.getThisRoutePlatforms()) {
            routeInfo.stationInfoList.push({ platformName: platform.platform.name, platformDwellTime: platform.platform.dwellTime, stationName: platform.station.name, interchangeInfo: getAllInterchangeRoutes(platform.station, platform.route, getNextRoute(train)) });
        }
    }

    return routeInfo;
}

/**
 * 获取某车站的所有换乘信息（包括连接车站）。
 * @param {Station} station 要获取换乘信息的车站。
 * @param {Route} thisRoute 本路线。
 * @param {Route} nextRoute 下一个路线。可以为 null。
 * @return {Array} 换乘信息数组。
 */
function getAllInterchangeRoutes(station, thisRoute, nextRoute) {
    let interchangeRoutes = [];
    getInterchangeRoutes(station, thisRoute, nextRoute, false, interchangeRoutes); // 获取本站的换乘信息
    getMapValueByKey(MTRClientData.DATA_CACHE.stationIdToConnectingStations, station).forEach(connectingStation => { // 获取连接车站的换乘信息
        getInterchangeRoutes(connectingStation, thisRoute, nextRoute, true, interchangeRoutes);
    });
    return interchangeRoutes;
}

/**
 * 获取某车站（不含连接车站）的换乘信息。
 * @param {Station} station 要获取换乘信息的车站。
 * @param {Route} thisRoute 本路线。
 * @param {Route} nextRoute 下一个路线。可以为 null。
 * @param {Boolean} isConnectingStation 在获取连接车站的换乘信息时，此参数应传入 true，否则为 false。
 * @param {Array} interchangeRoutes 换乘信息数组，获取到的换乘信息会追加到该数组末尾。
 */
function getInterchangeRoutes(station, thisRoute, nextRoute, isConnectingStation, interchangeRoutes) {
    let thisRouteNameSplit = getNonExtraParts(thisRoute.name);
    let nextRouteNameSplit = nextRoute == null ? null : getNonExtraParts(nextRoute.name);

    let routesInStation = getMapValueByKey(MTRClientData.DATA_CACHE.stationIdToRoutes, station.id);
    

    if (routesInStation != null) {
        for (let interchangeRoute of routesInStation.values()) {
            let cjk = TextUtil.getCjkParts(interchangeRoute.name);
            let ncjk = TextUtil.getNonCjkParts(interchangeRoute.name);
            let lu = /路$/,db = /大巴$/,brt=/BRT$/;
            if (interchangeRoute.name != thisRouteNameSplit && interchangeRoute.name != nextRouteNameSplit && (lu.test(cjk)== 0&&db.test(cjk)== 0&&brt.test(ncjk)== 0) ){
                interchangeRoutes.push({ name: interchangeRoute.name, color: new Color(interchangeRoute.color), isConnectingStation: isConnectingStation,isRailwayRoute: /^[GDKTZgdktz]\d*/.test(cjk)|| /^[GDKTZgdktz]\d*/.test(ncjk) ,isExpressRoute: /^[CScs]\d*/.test(cjk)|| /^[CScs]\d*/.test(ncjk)});
            }
        }
    }
}