/**
 * 在用“||”分割的字符串中，返回“||”之前的所有字符。
 * 如果源字符串不使用“||”分割，则返回该字符串。
 * @param {String} src 源字符串。
 * @returns {String}
 */
function getNonExtraParts(src) {
    return src.includes("||") ? TextUtil.getNonExtraParts(src) : src;
}

    /**
     * 绘制一个水平渐变透明矩形（左不透明 → 右全透明）
     * @param g2d        Graphics2D上下文
     * @param x          矩形左上角x坐标
     * @param y          矩形左上角y坐标
     * @param width      矩形宽度
     * @param height     矩形高度
     * @param baseColor  基础颜色（渐变起始颜色，不透明度由渐变控制）
     */
     // 在Rhino中获取绘图上下文（假设通过Java传入）

// 绘制左→右渐变矩形
function drawGradientRect(g2d,x, y, width, height, direction) {
    var startX = (direction === "l") ? x : x + width;
    var endX = (direction === "l") ? x + width : x;
    
    var startPoint = new Point2D.Float(startX, y);
    var endPoint = new Point2D.Float(endX, y);
    var Colorc=g2d.getColor();
    var gradient = new LinearGradientPaint(
        startPoint, endPoint,
        [0.0, 1.0],
        [
            Colorc, // 蓝色原色
            new Color(Colorc.getRed()/255,Colorc.getGreen() /255, Colorc.getBlue()/255, 0)     // 蓝色透明
        ]
    );
    
    g2d.setPaint(gradient);
    g2d.fillRect(x, y, width, height);
}
function getContrastColor(hexColor) {
    // 解析RGB分量
    const r = hexColor.getRed();
    const g = hexColor.getGreen();
    const b = hexColor.getBlue() ;

    // 计算相对亮度（WCAG 2.0标准）
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;

    // 阈值判断（192为常用阈值，可根据需求调整）
    return brightness >= 192 ? '#000000' : '#FFFFFF';
}
/**
 * 在用“|”分割的字符串中，获取其中的 CJK / 非 CJK 部分。
 * 如果源字符串不使用“|”分割，但使用“||”分割，则返回“||”之前的所有字符。
 * 如果源字符串既不使用“|”分割，也不使用“||”分割，则返回该字符串。
 * 如果源字符串多次使用“|”分割，则判断每个部分是否为 CJK 字符，并返回符合条件的所有部分。每个部分间用空格分割。
 * @param {String} src 源字符串。
 * @param {Boolean} isCjk 指定获取字符串中的 CJK 还是非 CJK 部分。
 * @returns {String}
 */
function getMatching(src, isCjk) {
    if (!src.includes("|")) {
        return getNonExtraParts(src);
    }
    return isCjk ? TextUtil.getCjkParts(src) : TextUtil.getNonCjkParts(src);
}

/**
 * 提取源字符串“||”之前的所有字符，然后将所有“|”替换成空格。
 * @param {String} name 源字符串。
 * @returns {String}
 */
function formatName(name) {
    return getNonExtraParts(name).replace('|', ' ');
}

function getMapValueByKey(map, key) {
    for (let entry of map.entrySet()) {
        if (entry.getKey() == key) {
            return entry.getValue();
        }
    }
}

function getMapValueByIndex(map, index) {
    let iterator = map.entrySet().iterator();

    for (let i = 0; i < index && iterator.hasNext(); i++) {
        iterator.next();
    }

    if (iterator.hasNext()) {
        return iterator.next().getValue();
    }

    throw new Error("Map does not contain " + index + " elements");
}

/**
 * 检查某个属性是否存在于对象中，并且属性的 JSON 字符串表示形式等于给定对象的 JSON 字符串表示形式。
 * @param {*} obj 要检查的属性所在的对象。
 * @param {*} propName 要检查的属性名称字符串。
 * @param {*} propValue 要检查的属性值。
 * @returns 
 */
function checkJsonProperty(obj, propName, propValue) {
    return (obj != null && propName in obj) ? JSON.stringify(obj[propName]) == JSON.stringify(propValue) : false;
}

/**
 * 检查某个属性是否存在于对象中，并且属性值等于给定的值。
 * @param {*} obj 要检查的属性所在的对象。
 * @param {*} propName 要检查的属性名称字符串。
 * @param {*} propValue 要检查的属性值。
 * @returns 
 */
function checkProperty(obj, propName, propValue) {
    return (obj != null && propName in obj) ? obj[propName] == propValue : false;
}

function clamp(number, min, max) {
    return Math.min(Math.max(number, min), max);
}

function warn(message) {
    MinecraftClient.displayMessage("§e§l" + message, false);
}