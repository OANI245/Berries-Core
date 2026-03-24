/**
 * @author OANI245
 * TRM01型列车主要内容脚本
 */

include(Resources.id("mtrsteamloco:scripts/display_helper.js")); //import显示屏组件
importPackage(java.awt);
importPackage(java.awt.font);
importPackage(java.awt.geom);
importPackage(java.lang);
importPackage(java.util.concurrent);

var rawModels = ModelManager.loadPartedRawModel(Resources.manager(), Resources.id("mtr:train/trm01/text_screens.obj"), null);
var models = uploadPartedModels(rawModels, true);
var speedScreenModel = uploadModel(ModelManager.loadRawModel(Resources.manager(), Resources.id("mtr:train/trm01/speed_screen.obj"), null), true);

const COLOR_RED = new Color(0xEC0000);
const COLOR_ORANGE = new Color(0xFFBD5B); //16进制颜色
const FONT_SIMSUN = Resources.readFont(Resources.id("mtr:font/simsun.ttf"));
const FONT_SANSSERIF = Resources.readFont(Resources.id("tcm:font/unispace.ttf"));
const FONT_SANSSERIF2 = Resources.getSystemFont("Microsoft Yahei UI");

function create(ctx, state, train) {
    state.textTexture = new GraphicsTexture(320, 80);
    state.speedScreenTexture = new GraphicsTexture(800, 200);
    state.ledInnled = models["innled"].copyForMaterialChanges();
    state.ledInnledB = models["innled2"].copyForMaterialChanges();
    state.lcdSpeedScreen = speedScreenModel.copyForMaterialChanges();
    state.ledInnled.replaceAllTexture(state.textTexture.identifier);
    state.ledInnledB.replaceAllTexture(state.textTexture.identifier);
    state.lcdSpeedScreen.replaceAllTexture(state.speedScreenTexture.identifier);
    state.pisRateLimit = new RateLimit(0.025);

    state.screenValue = 0;
    state.startTime = Date.now();

    state.textMessage = "";

    state.pool2 = Executors.newScheduledThreadPool(1);
    state.pool2.scheduleAtFixedRate(new Runnable({
        run: () => {
            let message = getPlainMessage(train);
            if (message !== state.textMessage) {
                state.startTime = Date.now();
            }
            state.textMessage = message;
            ctx.setDebugInfo("text", message)
            //ctx.setDebugInfo("tex", state.textTexture)

            /*if (state.pisRateLimit.shouldUpdate()) {*/
                let trainSpeed = Math.floor((train.speed() * 20) * 3.6); //速度（km/h）
                let graphics = state.textTexture.graphics;
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                graphics.setColor(Color.BLACK);
                graphics.fillRect(0, 0, 320, 80);
                graphics.setFont(FONT_SIMSUN.deriveFont(Font.PLAIN, 14));
                graphics.setColor(COLOR_RED);
                let length = Math.ceil(graphics.getFontMetrics().stringWidth(message)) + 170;
                ctx.setDebugInfo("textLength", length)
                state.screenValue = getTextPosition(0, length, state.screenValue, state.startTime);
                if (state.screenValue < 0) {
                    state.startTime = Date.now();
                }
                graphics.drawString(state.textMessage, 160 - state.screenValue, 15);
                graphics.setColor(COLOR_ORANGE);
                graphics.drawString(state.textMessage, 160 - state.screenValue, 44);

                let g2 = state.speedScreenTexture.graphics;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, 800, 200);
                g2.setColor(!train.getThisRoutePlatforms().isEmpty() ? new Color(train.getThisRoutePlatforms().get(0).route.color) : Color.WHITE);
                g2.fillRect(0, 185, 800, 15);
                g2.setColor(Color.WHITE);
                if (train.isOnRoute()) {
                    g2.setFont(FONT_SANSSERIF.deriveFont(Font.PLAIN, 128));
                    let offset = g2.getFontMetrics().stringWidth(trainSpeed) / 2;
                    g2.drawString(trainSpeed, 200 - offset, 128);
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setFont(FONT_SANSSERIF2.deriveFont(Font.PLAIN, 32));
                    let offset2 = g2.getFontMetrics().stringWidth("km/h") / 2;
                    g2.drawString("km/h", 200 - offset2, 170);
                } else {
                    g2.setFont(FONT_SANSSERIF2.deriveFont(Font.BOLD, 64));
                    let offset = g2.getFontMetrics().stringWidth("列车已回库") / 2;
                    g2.drawString("列车已回库", 200 - offset, 106);
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setFont(FONT_SANSSERIF2.deriveFont(Font.PLAIN, 24));
                    let offset2 = g2.getFontMetrics().stringWidth("The train is in the depot") / 2;
                    g2.drawString("The train is in the depot", 200 - offset2, 155);
                }
                state.textTexture.upload();
                state.speedScreenTexture.upload();
            /*}*/
        }
    }), 0, 1000 / 30, TimeUnit.MILLISECONDS);
}

function render(ctx, state, train) {

    /*ctx.setDebugInfo("texl",state.textTexture.identifier)
    ctx.setDebugInfo("tex",state.textTexture)*/

    let poseStack = new Matrices();

    if (train.trainCars() === 1) {
        poseStack.pushPose();
        poseStack.rotateY(Math.PI);
        ctx.drawCarModel(state.ledInnled, 0, poseStack);
        poseStack.popPushPose();
        ctx.drawCarModel(state.ledInnled, 0, poseStack);
        poseStack.popPushPose();
    } else {
        poseStack.pushPose();
        poseStack.pushPose();
        let cars = train.trainCars();

        for (i = 0; i < cars; i++) {
            if (i === 0) {
                poseStack.rotateY(Math.PI);
                ctx.drawCarModel(state.ledInnled, i, poseStack);
                poseStack.popPushPose();
                poseStack.rotateY(Math.PI);
                ctx.drawCarModel(state.lcdSpeedScreen, i, poseStack);
                poseStack.popPushPose();
            } else if (i === cars - 1) {
                ctx.drawCarModel(state.ledInnled, i, poseStack);
                poseStack.popPushPose();
                ctx.drawCarModel(state.lcdSpeedScreen, i, poseStack);
                poseStack.popPushPose();
            }

            if (((i + 2) % 2 === 0 || i === cars - 2 || i === 1) && (i !== 0)) {
                poseStack.rotateY(Math.PI);
                poseStack.translate(0, 0, -0.685);
                ctx.drawCarModel(state.ledInnledB, i, poseStack);
                poseStack.popPushPose();
            }
            if (((i + 1) % 2 === 0 || i === cars - 2 || i === 1) && (i !== cars - 1)) {
                poseStack.translate(0, 0, -0.685);
                ctx.drawCarModel(state.ledInnledB, i, poseStack);
                poseStack.popPushPose();
            }
            if (((i + 1) % 2 === 0) && (i !== 0 && i !== 1)) {
                poseStack.rotateY(Math.PI);
                poseStack.translate(0, 0, -0.185);
                ctx.drawCarModel(state.ledInnledB, i, poseStack);
                poseStack.popPushPose();
            }
            if (((i + 2) % 2 === 0) && (i !== cars - 1 && i !== cars - 2)) {
                poseStack.translate(0, 0, -0.185);
                ctx.drawCarModel(state.ledInnledB, i, poseStack);
                poseStack.popPushPose();
            }
        }
    }
}

function dispose(ctx, state, train) {
    state.pool2.shutdown();
    state.textTexture.close();
    state.speedScreenTexture.close();
}

function getDefaultMessage() {
    return "欢迎乘坐天城轨道交通       Welcome to taking Tiancheng Rail Transit";
}

function getPlainMessage(train) {
    //let platforms = train.getAllPlatforms();
    let oneWayPlatforms = train.getThisRoutePlatforms();
    let paths = train.path();

    if (!train.isOnRoute() || oneWayPlatforms.isEmpty() || paths.isEmpty() || train.getThisRoutePlatformsNextIndex() === oneWayPlatforms.size()) {
        return getDefaultMessage();
    }

    let nextStation = oneWayPlatforms.get(train.getThisRoutePlatformsNextIndex());
    let lastStation = oneWayPlatforms.get(oneWayPlatforms.size() - 1);

    let path = paths.get(train.getRailIndex(train.getRailProgress(0), true));

    /*let splitedLastStationName = lastStation.station.name.split("|");
    let splitedNextStationName = nextStation.station.name.split("|");*/

    let destCJK = TextUtil.getCjkParts(lastStation.station.name);
    let dest = TextUtil.getNonCjkParts(lastStation.station.name);
    let nextStationCJK = TextUtil.getCjkParts(nextStation.station.name);
    let nextStationPlain = TextUtil.getNonCjkParts(nextStation.station.name);

    if (path.dwellTime > 0) {
        if (nextStation === lastStation) {
            return `即将到达终点站${nextStationCJK}。感谢您乘坐天城轨道交通。     We are now arriving at ${nextStationPlain}, The train terminus here, Thank you for taking Tiancheng Rail Transit.`;
        }
        if (!nextStationCJK.toString().endsWith("站")) {
            nextStationCJK = nextStationCJK + "站";
        }
        return `即将到达${nextStationCJK}。     We are now arriving at ${nextStationPlain}.`;
    }

    let transferMessage = getTransferMessage(nextStation);

    if (nextStation === lastStation) {
        return `下一站是终点站${nextStationCJK}，${transferMessage[0]}    The next station is ${nextStationPlain}, The train terminus here. ${transferMessage[1]}`;
    }

    return `本次列车终点站是${destCJK}，下一站${nextStationCJK}。${transferMessage[0]}   This train is bound for ${dest}, The next station is ${nextStationPlain}. ${transferMessage[1]}`;
}

function getTransferMessage(pi) {
    let canTransferRoutes = [];
    getInterchangeRoutes(pi.station, pi.route, null, false, canTransferRoutes);
    if (canTransferRoutes.length === 0) {
        return ["", ""];
    }

    let textCJK = "您可以在此站换乘";
    let text = "Change here for ";

    for (let i = 0; i < canTransferRoutes.length; i++) {
        if (i !== 0) {
            textCJK += "、";
            text += ", ";
        }

        let route = canTransferRoutes[i];

        textCJK += TextUtil.getCjkParts(route.name);
        text += TextUtil.getNonCjkParts(route.name);
    }

    textCJK += "。";
    text += ". ";

    return [textCJK, text];
}

function getTextPosition(startValue, endValue, value, startTime) {
    let currentTime = Date.now();
    if (value >= endValue) {
        return startValue - 1;
    } else {
        return (currentTime - startTime) / 1000 * 60
    }
}

function uploadPartedModels(rawModels, interior) {
    let result = {};
    for (it = rawModels.entrySet().iterator(); it.hasNext(); ) {
        entry = it.next();
        entry.getValue().applyUVMirror(false, true);
        entry.getValue().setAllRenderType(interior ? "interior" : "exterior");
        result[entry.getKey()] = ModelManager.uploadVertArrays(entry.getValue());
    }
    return result;
}

function uploadModel(rawModel, interior) {
    let result = {};
    rawModel.setAllRenderType(interior ? "interior" : "exterior")
    rawModel.applyUVMirror(false, true);
    return ModelManager.uploadVertArrays(rawModel);
}

function getInterchangeRoutes(station, thisRoute, nextRoute, isConnectingStation, interchangeRoutes) {
    let thisRouteNameSplit = getNonExtraParts(thisRoute.name);
    let nextRouteNameSplit = nextRoute == null ? null : getNonExtraParts(nextRoute.name);

    let routesInStation = getMapValueByKey(MTRClientData.DATA_CACHE.stationIdToRoutes, station.id);

    if (routesInStation != null) {
        for (let interchangeRoute of routesInStation.values()) {
            if (interchangeRoute.name != thisRouteNameSplit && interchangeRoute.name != nextRouteNameSplit) {
                interchangeRoutes.push({ name: interchangeRoute.name, color: new Color(interchangeRoute.color), isConnectingStation: isConnectingStation });
            }
        }
    }
}

function getNonExtraParts(src) {
    return src.includes("||") ? TextUtil.getNonExtraParts(src) : src;
}

function getMapValueByKey(map, key) {
    for (let entry of map.entrySet()) {
        if (entry.getKey() == key) {
            return entry.getValue();
        }
    }
}