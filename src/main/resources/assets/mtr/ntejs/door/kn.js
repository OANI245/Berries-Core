/**
 * @author OANI245
 * 仿康尼机电内藏电磁门门控脚本
 */

var doorLeftOut = uploadModel(
    ModelManager.loadRawModel(Resources.manager(), Resources.id(`mtr:train/${TRAIN_NAME}/door_left_out.obj`), null),
    false
);
var doorLeftIn = uploadModel(
    ModelManager.loadRawModel(Resources.manager(), Resources.id(`mtr:train/${TRAIN_NAME}/door_left_in.obj`), null),
    true
);
var doorRightOut = uploadModel(
    ModelManager.loadRawModel(Resources.manager(), Resources.id(`mtr:train/${TRAIN_NAME}/door_right_out.obj`), null),
    false
);
var doorRightIn = uploadModel(
    ModelManager.loadRawModel(Resources.manager(), Resources.id(`mtr:train/${TRAIN_NAME}/door_right_in.obj`), null),
    true
);

function create(ctx, state, train) {
    state.doorLeftOut = doorLeftOut.copyForMaterialChanges();
    state.doorLeftIn = doorLeftIn.copyForMaterialChanges();
    state.doorRightOut = doorRightOut.copyForMaterialChanges();
    state.doorRightIn = doorRightIn.copyForMaterialChanges();
    if (typeof TEXTURE !== "undefined") {
        state.doorLeftOut.replaceAllTexture(Resources.id(TEXTURE));
        state.doorRightOut.replaceAllTexture(Resources.id(TEXTURE));
        state.doorLeftIn.replaceAllTexture(Resources.id(TEXTURE));
        state.doorRightIn.replaceAllTexture(Resources.id(TEXTURE));
    }
}

function render(ctx, state, train) {
    let poseStack = new Matrices();
    for (i = 0; i < train.trainCars(); i++) {
        poseStack.pushPose();
        let doorX = getDoorValue(0.796, train.doorValue(), train.isDoorOpening());
        let doorXL = train.doorLeftOpen[i] ? doorX : 0.0;
        let doorXR = train.doorRightOpen[i] ? doorX : 0.0;
        poseStack.popPushPose();
        poseStack.rotateY(Math.PI);
        poseStack.translate(0, 0, doorXL);
        ctx.drawCarModel(state.doorLeftOut, i, poseStack);
        poseStack.popPushPose();
        poseStack.rotateY(Math.PI);
        poseStack.translate(0, 0, doorXL);
        ctx.drawCarModel(state.doorLeftIn, i, poseStack);
        poseStack.popPushPose();
        poseStack.rotateY(Math.PI);
        poseStack.translate(0, 0, -doorXL);
        ctx.drawCarModel(state.doorRightOut, i, poseStack);
        poseStack.popPushPose();
        poseStack.rotateY(Math.PI);
        poseStack.translate(0, 0, -doorXL);
        ctx.drawCarModel(state.doorRightIn, i, poseStack);
        poseStack.popPushPose();
        poseStack.translate(0, 0, doorXR);
        ctx.drawCarModel(state.doorLeftOut, i, poseStack);
        poseStack.popPushPose();
        poseStack.translate(0, 0, doorXR);
        ctx.drawCarModel(state.doorLeftIn, i, poseStack);
        poseStack.popPushPose();
        poseStack.translate(0, 0, -doorXR);
        ctx.drawCarModel(state.doorRightOut, i, poseStack);
        poseStack.popPushPose();
        poseStack.translate(0, 0, -doorXR);
        ctx.drawCarModel(state.doorRightIn, i, poseStack);
        poseStack.popPushPose();
    }
}

function getDoorValue(doorMax, value, opening) {
    if (value <= 0.0) {
        return 0.0;
    } else if (opening) {
        if (value < 0.34) {
            return 0;
        } else if (value >= 1) {
            return doorMax;
        } else {
            return (value - 0.34) * 1.5 * doorMax;
        }
    } else {
        if (value < 0.1) {
            return smoothEnds(-0.08, 0.08, -0.25, 0.25, value);
        } else if (value < 0.3) {
            return value * 1.15 * 0.37;
        } else if (value < 0.42) {
            return smoothEnds(0.12, 1.72, 0.22, 1.52, value);
        } else if (value >= 1) {
            return doorMax;
        } else {
            return (value - 0.2) * 1.25 * doorMax;
        }
    }
}

function smoothEnds(startValue, endValue, startTime, endTime, time) {
    if (time < startTime) return startValue;
    if (time > endTime) return endValue;
    let timeChange = endTime - startTime;
    let valueChange = endValue - startValue;
    return valueChange * (1 - Math.cos(Math.PI * (time - startTime) / timeChange)) / 2 + startValue;
}

// 把 loadRawModels 得到的 Map 里的各个内容分别上传
function uploadModel(rawModel, interior) {
    let result = {};
    rawModel.setAllRenderType(interior ? "interior" : "exterior")
    rawModel.applyUVMirror(false, true);
    return ModelManager.uploadVertArrays(rawModel);
}