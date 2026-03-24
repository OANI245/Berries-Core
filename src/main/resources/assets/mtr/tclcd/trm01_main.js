importPackage(java.awt);
importPackage(java.awt.geom);
importPackage(java.lang);
importPackage(java.util.concurrent);

include(Resources.id("mtrsteamloco:scripts/display_helper.js"));
include("draw.js");


const leftPos3tc = [[-1.11006875,2.27455,0.575],
    [-1.12036875,2.04255,0.575],
    [-1.12036875,2.04255,-0.575],
    [-1.11006875,2.27455,-0.575]];
const rightPos3tc = [[1.11006875,2.27455,-0.575],
    [1.12036875,2.04255,-0.575],
    [1.12036875,2.04255,0.575],
    [1.11006875,2.27455,0.575]];
let slotCfg3tc = {
    "version": 1,
    "texSize": [WIDTH, HEIGHT*4],
    "slots": [
        {
            "name": "lcd_door_left",
            "texArea": [0, 0, WIDTH, HEIGHT],
            "pos": [
                leftPos3tc//[[0,0,0],[0,0,0],[0,0,0],[0,0,0]]
            ],
            "offsets": [[0, 0, 10], [0, 0, 5], [0, 0, 0], [0, 0, -5], [0, 0, -10]]
        },
        {
            "name": "lcd_door_right",
            "texArea": [0, HEIGHT*2, WIDTH, HEIGHT],
            "pos": [
                rightPos3tc//[[0,0,0],[0,0,0],[0,0,0],[0,0,0]]
            ],
            "offsets": [[0, 0, 10], [0, 0, 5], [0, 0, 0], [0, 0, -5], [0, 0, -10]]
        }
    ]
};

var dhlcdtcBase = new DisplayHelper(slotCfg3tc);

var meetNTEVersionRequirement = Resources.getNTEVersionInt() >= 500;
var isMTR4 = Resources.getMTRVersion().includes("4.0");

function create(ctx, state, train) {
    // state.pisRateLimit = new RateLimit(0.1);
    state.RateLimitTc = new RateLimit(0.01);
    state.RateLimitLight = new RateLimit(0.5);
    state.doorSec = new StateTracker();
    state.doorSec.setState("normal");
    state.dhlcdtc = dhlcdtcBase.create();
    state.Ratex = 0;
    state.tcCycleTrackerText=new CycleTracker(["cjk",5,"ncjk",5])
    state.tcRoll = new CycleTracker(["rt",4]);

    state.pool = Executors.newScheduledThreadPool(1);
    state.pool.scheduleAtFixedRate(new Runnable({
        run: () => {
            if (!meetNTEVersionRequirement || isMTR4) {
                let g = state.dhlcdtc.graphicsFor("lcd_door_left");
                drawBlueScreen(g, VERSION_ERROR);

                g = state.dhlcdtc.graphicsFor("lcd_door_right");
                drawBlueScreen(g, VERSION_ERROR);

                state.dhlcdtc.upload();

                return;
            }
            if(state.RateLimitLight.shouldUpdate()){
                state.Ratex++;
                if(state.Ratex > 1) state.Ratex = 0;
            }
            let platformInfo = null;
            state.tcCycleTrackerText.tick();
            state.trainStatus = getTrainStatus(train);
            switch (state.trainStatus) {
                case STATUS_NO_ROUTE:
                    platformInfo = null;
                    break;
                case STATUS_RETURNING_TO_DEPOT:
                    platformInfo = train.getAllPlatforms().get(train.getAllPlatforms().size() - 1);
                    break;
                case STATUS_WAITING_FOR_DEPARTURE:
                    platformInfo = train.getAllPlatforms().get(0);
                    break;
                case STATUS_CHANGING_ROUTE:
                    platformInfo = train.getAllPlatforms().get(train.getAllPlatformsNextIndex());
                    break;
                default:
                    platformInfo = train.getThisRoutePlatforms().get(train.getThisRoutePlatformsNextIndex());
                    break;
            }
            const routeInfo = getRouteInfo(train, state.trainStatus, platformInfo);
            if (!checkJsonProperty(state, "routeInfo", routeInfo)) { // 如果 state 中不存在 routeInfo 或 routeInfo 改变
                print("列车 " + train.id() + " 的当前路线信息：" + JSON.stringify(routeInfo));
                state.routeInfo = routeInfo;
            }
            state.OpenSide = (getDoorOpeningSide(train) != null ? getDoorOpeningSide(train) : (state.trainStatus === STATUS_ARRIVED ? state.OpenSide : 0));
            try {
                let g = state.dhlcdtc.graphicsFor("lcd_door_left");
                drawScreen(g, state.routeInfo, state.trainStatus === STATUS_RETURNING_TO_DEPOT ? 1 : train.getThisRoutePlatformsNextIndex(), state.trainStatus, state.Ratex, train, 0, state, ctx);
                g = state.dhlcdtc.graphicsFor("lcd_door_right");
                drawScreen(g, state.routeInfo, state.trainStatus === STATUS_RETURNING_TO_DEPOT ? 1 : train.getThisRoutePlatformsNextIndex(), state.trainStatus, state.Ratex, train, 1, state, ctx);
                state.dhlcdtc.upload();
            } catch (e) {
            }
        }
    }), 0, 1000 / 20, TimeUnit.MILLISECONDS);
}


function dispose(ctx, state, train) {
    state.pool.shutdown();
    state.dhlcdtc.close();
}

function render(ctx, state, train) {
    for (let i = 0; i < train.trainCars(); i++) {
        if (train.shouldRender()) {
            ctx.drawCarModel(state.dhlcdtc.model, i, null);
        }
    }
}

function shouldRepaintLCD(state, train) {
    let trainStatus = getTrainStatus(train);
    if (checkProperty(state, "trainStatus", trainStatus)) { // 如果 state 中存在 trainStatus 且符合当前状态
        return true;
    } else {
        print("列车状态更新为：" + trainStatus);
        state.trainStatus = trainStatus;
        return true;
    }
}

function getCubeVertices(p1, p2, center, rx, ry, rz) {
    const rxRad = rx * Math.PI / 180;
    const ryRad = ry * Math.PI / 180;
    const rzRad = rz * Math.PI / 180;

    let c = new Vector3f(center[0], center[1], center[2]);

    if (p1[1] == p2[1]) {
        let v1 = new Vector3f(p1[0], p1[1], p1[2]);
        let v2 = new Vector3f(p2[0], p1[1], p1[2]);
        let v3 = new Vector3f(p2[0], p2[1], p2[2]);
        let v4 = new Vector3f(p1[0], p2[1], p2[2]);

        v1.sub(c);
        v1.rotX(rxRad);
        v1.rotY(ryRad);
        v1.rotZ(rzRad);
        v1.add(c);

        v2.sub(c);
        v2.rotX(rxRad);
        v2.rotY(ryRad);
        v2.rotZ(rzRad);
        v2.add(c);

        v3.sub(c);
        v3.rotX(rxRad);
        v3.rotY(ryRad);
        v3.rotZ(rzRad);
        v3.add(c);

        v4.sub(c);
        v4.rotX(rxRad);
        v4.rotY(ryRad);
        v4.rotZ(rzRad);
        v4.add(c);

        return [
            [v1.x(), v1.y(), v1.z()],
            [v2.x(), v2.y(), v2.z()],
            [v3.x(), v3.y(), v3.z()],
            [v4.x(), v4.y(), v4.z()]
        ];
    }
    else if (p1[0] == p2[0]) {
        let v1 = new Vector3f(p1[0], p1[1], p1[2]);
        let v2 = new Vector3f(p1[0], p2[1], p1[2]);
        let v3 = new Vector3f(p2[0], p2[1], p2[2]);
        let v4 = new Vector3f(p2[0], p1[1], p2[2]);

        v1.sub(c);
        v1.rotX(rxRad);
        v1.rotY(ryRad);
        v1.rotZ(rzRad);
        v1.add(c);

        v2.sub(c);
        v2.rotX(rxRad);
        v2.rotY(ryRad);
        v2.rotZ(rzRad);
        v2.add(c);

        v3.sub(c);
        v3.rotX(rxRad);
        v3.rotY(ryRad);
        v3.rotZ(rzRad);
        v3.add(c);

        v4.sub(c);
        v4.rotX(rxRad);
        v4.rotY(ryRad);
        v4.rotZ(rzRad);
        v4.add(c);

        return [
            [v1.x(), v1.y(), v1.z()],
            [v2.x(), v2.y(), v2.z()],
            [v3.x(), v3.y(), v3.z()],
            [v4.x(), v4.y(), v4.z()]
        ];
    }
    else {
        throw "指定对角顶点不与地面垂直或平行：顶点一 [" + p1 + "]，顶点二 [" + p2 + "]。";
    }
}