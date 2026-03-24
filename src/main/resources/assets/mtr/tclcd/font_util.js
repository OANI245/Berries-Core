importPackage(java.awt);
importPackage(java.awt.font);
importPackage(java.text);

include("js_util.js");

const tu =TextUtil;
function gcp(text){
    return tu.getCjkParts(text);
}
function gncp(text){
    return tu.getNonCjkParts(text);
}

function drawScrollingText(graphics, text,x,y, containerWidth, totalDuration, currentTime) {
    let metrics = graphics.getFontMetrics();
    let textWidth = metrics.stringWidth(text);
    /*if(textWidth<=containerWidth)
    {
        graphics.drawString(text,x+containerWidth/2-textWidth/2,y);
        return;
    }*/

    const progress = (currentTime % totalDuration) / totalDuration;
    const translateX = ((textWidth*2+80) * progress);
    
    // 坐标系变换
    let lt=graphics.getTransform();
    graphics.translate(x+containerWidth-translateX, 0);
    graphics.drawString(text, 0, y); // 假设基线Y坐标为20
    graphics.setTransform(lt);
    return (textWidth+40)/(textWidth*2+80)*totalDuration;
    
  }
  function drawScrollingArrow(graphics, containerWidth,textWidth,x,y, totalDuration, currentTime) {

    //let textWidth = 47;
    const progress = (currentTime % totalDuration) / totalDuration;
    const translateX = (containerWidth+textWidth)*progress;
    
    // 坐标系变换
    let lt=graphics.getTransform();
    graphics.translate(x+translateX, 0);
   let g = graphics;
   g.setColor(Color.WHITE);
   g.setStroke(new BasicStroke(5.5));
   g.drawLine(0,y,-12,y-9);
   g.drawLine(0,y,-12,y+9); // 假设基线Y坐标为20
    graphics.setTransform(lt);
    
   
  }
  function drawScrollingDoor(graphics, containerWidth,picWidth,picHeight,x,y, totalDuration, currentTime,pic,isOpen) {

    //let textWidth = 47;
    const progress = (currentTime % totalDuration) / totalDuration;
    const translateX = (containerWidth+picWidth/2)*progress;
    
    // 坐标系变换
    //let lt=graphics.getTransform();
    //graphics.translate(x+translateX, 0);
   let g = graphics;
   if(isOpen)
   {
    g.drawImage(pic,x+translateX,y,picWidth,picHeight,null);
    g.drawImage(pic,x-translateX-picWidth,y,picWidth,picHeight,null);
    
   }
   else
   {
    g.drawImage(pic,x,y,picWidth,picHeight,null);
    g.drawImage(pic,x-picWidth,y,picWidth,picHeight,null);
    g.setColor(Color.RED);
    g.setStroke(new BasicStroke(10));
    g.drawOval(x-picWidth/2,y+picHeight/2-picWidth/2,picWidth,picWidth);
    g.drawLine(x-picWidth/3,y+picHeight/2+picWidth/3,x+picWidth/3,y+picHeight/2-picWidth/3);
   }
  
   // 假设基线Y坐标为20
    //graphics.setTransform(lt);
    
   
  }
    /**
     * 在目标点的X轴居中绘制文本，若超限则自动拉伸X轴
     * @param g2d         Graphics2D上下文
     * @param text        待绘制的文本
     * @param targetX     目标点的X坐标（居中基准）
     * @param targetY     文本基线的Y坐标（保持不变）
     * @param limitWidth  X轴最大允许宽度
     */
    function drawCenteredAndScaledText(g2d,  text,targetX,targetY,limitWidth) {
        // 保存原始状态以便恢复
        let originalTransform = g2d.getTransform();
        let originalHints = g2d.getRenderingHints();

        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // 测量文本原始宽度
        let metrics = g2d.getFontMetrics();
        let textWidth = metrics.stringWidth(text);

        // 计算X轴缩放比例
        let scaleX = 1.0;
        if (limitWidth != null && textWidth > limitWidth) {
            scaleX = 1.0*limitWidth / textWidth;
        }

        // 应用X轴缩放（Y轴保持1.0）
        g2d.scale(scaleX, 1.0);

        // 计算缩放后的绘制坐标
        // 缩放后实际宽度为 textWidth * scaleX，需重新居中
        let scaledX = (targetX / scaleX - textWidth / 2);

        // 绘制文本
        g2d.drawString(text, scaledX, targetY);

        // 恢复原始状态
        g2d.setTransform(originalTransform);
        g2d.setRenderingHints(originalHints);
    }

/**
 * 绘制长文本，支持自动换行。不会在单词中换行导致截断单词。
 * @param x 渲染 {@link TextLayout} 的 X 坐标。
 * @param y 渲染 {@link TextLayout} 的 Y 坐标。
 * @param breakWidth 一行字符的最大宽度。
 * @return 最后一行 descent line 与 y 之差。
 */
function drawLongText(g, str, x, y, breakWidth) {
    let paragraph = new AttributedString(str, java.util.Map.of(TextAttribute.FONT, g.getFont())).getIterator();
    let paragraphStart = paragraph.getBeginIndex();
    let paragraphEnd = paragraph.getEndIndex();
    let lineMeasurer = new LineBreakMeasurer(paragraph, g.getFontRenderContext());
    lineMeasurer.setPosition(paragraphStart); // Set position to the index of the first character in the paragraph.

    let beginning = true;
    let descentOffset = 0;
    // Get lines until the entire paragraph has been displayed.
    while (lineMeasurer.getPosition() < paragraphEnd) {
        let layout = lineMeasurer.nextLayout(breakWidth);
        y += beginning ? 0 : layout.getAscent(); // Move y-coordinate by the ascent of the layout.
        layout.draw(g, x, y); // Draw the TextLayout at (drawPosX, drawPosY).
        // y += layout.getDescent(); // Move y-coordinate in preparation for next layout.
        descentOffset += beginning ? layout.getDescent() : layout.getAscent();
        beginning = false;
    }
    return descentOffset;
}

/**
 * 计算符合条件的最大字号。
 * @param maxWidth 以最大字号绘制 text 的最大宽度。传入 0 则不限制。
 * @param maxHeight 以最大字号绘制 text 的最大高度。传入 0 则不限制。
 * @param minFontSize 计算结果不得小于此字号。传入 0 则不限制。
 * @param maxFontSize 计算结果不得大于此字号。传入 0 则不限制。
 * @throws text 为空、maxWidth 小于 0、maxHeight 小于 0、minFontSize 小于 0、maxFontSize 小于 0，或 maxWidth、maxHeight、maxFontSize 都等于 0，抛出此异常。
 * @param vertical 是否竖直绘制。
 * @return 符合条件的最大字号。
 */
function calculateMaxFontSize(g, font, text, maxWidth, maxHeight, minFontSize, maxFontSize, vertical) {
    if (text == null || text.isEmpty() || maxWidth < 0 || maxHeight < 0 || minFontSize < 0 || maxFontSize < 0 || (maxWidth == 0 && maxHeight == 0 && maxFontSize == 0)) {
        throw new IllegalArgumentException("calculateMaxFontSize: Invalid input parameters. text: " + text + ", maxWidth: " + maxWidth + ", maxHeight: " + maxHeight + ", minFontSize: " + minFontSize + ", maxFontSize: " + maxFontSize);
    }
    let fontSize = minFontSize;
    let fontMetrics;
    do {
        fontSize += 0.5;
        fontMetrics = g.getFontMetrics(font.deriveFont(fontSize));
    } while (vertical ? ((maxWidth == 0 || fontMetrics.charWidth('龘') < maxWidth) && (maxHeight == 0 || (fontMetrics.getAscent() + fontMetrics.getDescent()) * text.length() < maxHeight))
            : ((maxWidth == 0 || fontMetrics.stringWidth(text) < maxWidth) && (maxHeight == 0 || fontMetrics.getHeight() < maxHeight)));
    return maxFontSize == 0 ? fontSize : Math.min(fontSize, maxFontSize);
}

function getRGBAValue(r, g, b, a) {
    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
}
function retMax(x,y)
{
    if(x > y) return x;
    else return y;
}
function retMin(x,y)
{
    if(x < y) return x;
    else return y;
}
/**
 * 通过 R、G、B 值创建 Java 的 Color 对象。
 * 不直接 new 的原因是 JavaScript 中的数字在 Rhino 中总是映射为 Java 的 double 类型，这样匹配的 Color 对象的构造方法仅接受 0~1 范围内的数字。
 * @param {Number} r 
 * @param {Number} g 
 * @param {Number} b 
 * @return {Color} Java 的 Color 对象。
 */
function rgbToColor(r, g, b) {
    r = (r - 0.5) / 255;
    g = (g - 0.5) / 255;
    b = (b - 0.5) / 255;
    return new java.awt.Color(clamp(r, 0, 1), clamp(g, 0, 1), clamp(b, 0, 1));
}

function isLightColor(color) {
    let darkness = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
    return darkness < 0.5;
}

function getStringWidth(str,g)
{
    let sum = 0;
    fm=g.getFontMetrics();
    sum = fm.stringWidth(str);
    return sum;
}

function darkenColor(color, factor) {
    let red = color.getRed() * (1 - factor);
    let green = color.getGreen() * (1 - factor);
    let blue = color.getBlue() * (1 - factor);

    red = Math.min(255, Math.max(0, red));
    green = Math.min(255, Math.max(0, green));
    blue = Math.min(255, Math.max(0, blue));

    return rgbToColor(red, green, blue);
}