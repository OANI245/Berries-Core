importPackage(java.awt);

include("font_util.js");
include("js_util.js");
include("mtr_util.js");
include("pis_config.js");
include("lang.js");

const Arrow1 = Resources.readBufferedImage(Resources.idRelative("1.png"));
const Arrow2 = Resources.readBufferedImage(Resources.idRelative("2.png"));
const BG = Resources.readBufferedImage(Resources.idRelative("background.png"));//背景图片，相对路径
const Logo = Resources.readBufferedImage(Resources.idRelative("logo.png"));//更改此处图标位置
const TL = Resources.readBufferedImage(Resources.idRelative("tielu.png"));
const CJ = Resources.readBufferedImage(Resources.idRelative("chengji.png"));

const door_w=135*1.2,door_h=227*1.2;
const CONFIG = JSON.parse(Resources.readString(Resources.idr("config.json")));

const background_color = 1;//背景色系，深色0，浅色1
const DOOR = Resources.readBufferedImage(Resources.idRelative(background_color == 1?"bdoor.png":"wdoor.png"));

const WIDTH = 2816; // 231
const HEIGHT = 512; // 40


const AUTHOR = "Code by wxyt_cr200j,Jeffreyg1228";
const LCD_VERSION = "V2025.3 (Preview)";

const ART_FONT = Resources.readFont(Resources.idr("artfont.ttf"));//艺术字字体，相对路径
const ROBOTO_REGULAR = Resources.readFont(Resources.idr("fonts/roboto/roboto-regular.ttf"));
const ROBOTO_BOLD = Resources.readFont(Resources.idr("fonts/roboto/roboto-bold.ttf"));
const SOURCE_HAN_SANS_CN_REGULAR = Resources.readFont(Resources.idr("fonts/source-han-sans-cn/source-han-sans-cn-regular.otf"));
const SOURCE_HAN_SANS_CN_BOLD = Resources.readFont(Resources.idr("fonts/source-han-sans-cn/source-han-sans-cn-bold.otf"));
const frontcolor = background_color ==0?Color.WHITE:Color.BLACK;
function drawScreen(g, routeInfo, nextStationIndex, trainStatus,ModeI,train,isLeftDoor,state,ctx) {
    // 清屏
    g.setColor(frontcolor);
    g.fillRect(0, 0, WIDTH, HEIGHT);
    g.setColor(Color.LIGHT_GRAY);
    g.fillRect(3, 3, WIDTH - 6, HEIGHT - 6);

    // 绘制 LCD 作者和版本
    const font = ROBOTO_REGULAR.deriveFont(12.0);
    const fm = g.getFontMetrics(font);
    g.setColor(Color.GRAY);
    g.setFont(font);
    g.drawString(AUTHOR, WIDTH - fm.stringWidth(AUTHOR) - 10, HEIGHT - 2 * fm.getHeight());
    g.drawString(LCD_VERSION, WIDTH - fm.stringWidth(LCD_VERSION) - 10, HEIGHT - fm.getHeight());

    if (trainStatus == STATUS_NO_ROUTE || routeInfo == null) {
        drawBlueScreen(g, NO_ROUTE_TIP);
        return;
    }
    let routeColor = Color.BLUE;
    const routeName = routeInfo.routeName;
    const routeNumber = TextUtil.getExtraParts(routeName).replaceAll(".*\\{([^}]*)}.*", "$1"); // 表示 routeName “||” 后的 “{}”中内容；如果不含“{}”，则为 TextUtil.getExtraParts(routeName) 的值
    if(routeInfo.routeColor)routeColor = routeInfo.routeColor;
    const destination = routeInfo.destination;
    const depotName = routeInfo.depotName;
    const showCjk = !TextUtil.getCjkParts(routeName).isEmpty(); // showCjk 用于右屏的不变信息（例如“即将到达”“已到达”）

    drawRouteLinea(g,routeColor,routeInfo,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,ctx);
}

function  drawRouteLinea(g,routeColor,routeInfo,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,ctx)
{
    g.drawImage(BG,0,0,WIDTH,HEIGHT,null);
    g.setColor(routeColor);
    g.fillRoundRect(700,-100,WIDTH-750,220,100,100);
    g.drawImage(Logo,0,3,140,140,null);
    g.setFont(SOURCE_HAN_SANS_CN_BOLD.deriveFont(46.5));
    g.setColor(frontcolor);
    g.drawString("天城轨道交通",152,73);
    g.setFont(ROBOTO_REGULAR.deriveFont(30));
    g.setColor(frontcolor);
    g.drawString("Tiancheng Rail Transit",140,113);
    g.setColor(routeColor);
    g.fillRoundRect(480,10,200,110,60,60);
    g.setColor(Color.decode(getContrastColor(routeColor)));
    let fonta =SOURCE_HAN_SANS_CN_BOLD.deriveFont(35);
    g.setFont(fonta);
    let fma=g.getFontMetrics(fonta);
    let scjk=TextUtil.getCjkParts(routeInfo.routeName);
    let sncjk=TextUtil.getNonCjkParts(routeInfo.routeName);
    let lc = getStringWidth(scjk,g);
    drawCenteredAndScaledText(g,scjk,580,60,160);
    //g.drawString(scjk,580-lc/2,60);
    let fontb =ROBOTO_REGULAR.deriveFont(30);
    let fmb=g.getFontMetrics(fontb);
    let ln = getStringWidth(sncjk,g);
    g.setFont(fontb);
    drawCenteredAndScaledText(g,sncjk,580,95,160);
    //g.drawString(sncjk,580-ln/2,95);
    let stationInfoList = routeInfo.stationInfoList;
    let stationCount = routeInfo.stationInfoList.length;    
    fonta =SOURCE_HAN_SANS_CN_REGULAR.deriveFont(79);
    g.setFont(fonta);
    scjk=TextUtil.getCjkParts(stationInfoList[stationCount-1].stationName);
    sncjk=TextUtil.getNonCjkParts(stationInfoList[stationCount-1].stationName);
    let text=state.tcCycleTrackerText.stateNow()=="cjk"?"终点站："+scjk:"Terminal:"+sncjk;
    let location = (WIDTH - 800),loca = (WIDTH - 800)/3;
    drawCenteredAndScaledText(g,text,750+location/2,88,location);
    /*let tspeed =Math.round(train.speed() * 20 * 3600.0 / 1000).toString();
    text=state.tcCycleTrackerText.stateNow()=="cjk"?"速度："+tspeed+"km/h":"Speed:"+tspeed+"km/h";
    drawCenteredAndScaledText(g,text,WIDTH - 100 - loca/2,88,loca);*/
    if(state.trainStatus==STATUS_ARRIVED)
    {
        drawTCAtStation(g,routeColor,routeInfo,stationInfoList,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,stationCount,ctx);
        return 0;
    }
    state.doorSec.setState("normal");
    drawTCLine(g,routeColor,routeInfo,stationInfoList,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,stationCount,ctx);
}
function  drawTCAtStation(g,routeColor,routeInfo,stationInfoList,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,stationCount,ctx)
{
    state.OpenSide = (getDoorOpeningSide(train) != null?getDoorOpeningSide(train):(trainStatus == STATUS_ARRIVED?state.OpenSide:0));
    let OpenSide = state.OpenSide;
  //  var stationConfig = getStationConfig(stations, nextIndex);
   // var doorDirection;
    
    //g.setColor(Color.BLACK);
    //g.fillRect(-10,20,30,30);
    //g.fillRect(0,50,30,30);
  //  state.stationConfig = stationConfig;
   // state.d// = doorDirection == 2 || doorDirection == (isLeftDoor?1:2);
    state.keepRender = 0;
    ctx.setDebugInfo("openside",OpenSide);
    if(state.doorSec.stateNow() == "normal"&&OpenSide != 0)state.doorSec.setState("open");
    if(isLeftDoor)
    {
        if((OpenSide == 3||OpenSide == 1))
        {
    
            //if(state.doorSec.stateNow() == "normal")state.doorSec.setState("open");
            let artfont =ART_FONT.deriveFont(210),engfont = ROBOTO_REGULAR.deriveFont(58);
            let cjkname=gcp(stationInfoList[nextStationIndex].stationName),ncjkname=gncp(stationInfoList[nextStationIndex].stationName);
            g.setFont(artfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,cjkname,WIDTH/2,320,WIDTH/3);
            g.setFont(engfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,ncjkname,WIDTH/2,430,WIDTH/3);
            drawScrollingDoor(g,0,door_w,door_h,WIDTH/3-door_w*3,180,3,state.doorSec.stateNowDuration(),DOOR,1);
            let doorfontc = SOURCE_HAN_SANS_CN_REGULAR.deriveFont(50),doorfontn = ROBOTO_REGULAR.deriveFont(35);
            g.setColor(frontcolor);
            g.setFont(doorfontc);
            g.drawString("本侧开门",WIDTH/3-door_w*1.5,285);
            g.setFont(doorfontn);
            g.drawString("This side",WIDTH/3-door_w*1.5,325);
        }/*
        if(state.doorWillOpen)
        {
            g.setColor(Color.GREEN);
            g.fillRect(10,10,20,20);
        }
        */
        else if(OpenSide == 0)
        {
            drawTCLine(g,routeColor,routeInfo,stationInfoList,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,stationCount,ctx);
        }
        else
        {
            let artfont =ART_FONT.deriveFont(210),engfont = ROBOTO_REGULAR.deriveFont(58);
            let cjkname=gcp(stationInfoList[nextStationIndex].stationName),ncjkname=gncp(stationInfoList[nextStationIndex].stationName);
            g.setFont(artfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,cjkname,WIDTH/2,320,WIDTH/3);
            g.setFont(engfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,ncjkname,WIDTH/2,430,WIDTH/3);
            drawScrollingDoor(g,0,door_w,door_h,WIDTH/3*2+door_w*3,180,3,state.doorSec.stateNowDuration(),DOOR,0);
            let doorfontc = SOURCE_HAN_SANS_CN_REGULAR.deriveFont(50),doorfontn = ROBOTO_REGULAR.deriveFont(35);
            
            g.setColor(frontcolor);
            g.setFont(doorfontc);
            g.drawString("对侧开门",WIDTH/3*2+door_w*1.5-getStringWidth("对侧开门",g),285);
            g.setFont(doorfontn);
            g.drawString("Other side",WIDTH/3*2+door_w*1.5-getStringWidth("Other side",g),325);
            /*
            let linel=WIDTH - 400;
            let stalen=linel*1.0/(stationCount-1);
            let start = 0,end = stationCount - 1,now = nextStationIndex;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(170,295,30+stalen*(now-start-1 < 0?0:now-start-1),30);
            //if(nextStationIndex>2)drawGradientRect(g,70,295,100,30,"r");
            g.setColor(routeColor);
            g.fillRect(stalen*(now-start-1 < 0?0:now-start-1)+200,295,linel-stalen*(now-start-1 < 0?0:now-start-1)+30,30);
            drawScrollingArrow(g,stalen,0,stalen*(now-start-1 < 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration());
            drawScrollingArrow(g,stalen,0,stalen*(now-start-1< 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration() + 0.3);
          //  drawGradientRect(g,200+linel,295,100,30,"l");
            for(let ii =0;ii<stationCount;ii++){
                let loc=200+stalen*ii;
                let InterWidth = 0;
                let isTL = 0,isCJ = 0;
                        for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                        {
                            let nameCJK = gcp(stationInfoList[ii].interchangeInfo[j].name);
                            let nameNCJK = gncp(stationInfoList[ii].interchangeInfo[j].name);
                            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                            let Nfont = ROBOTO_REGULAR.deriveFont(28);
                            g.setFont(Cfont);
                            let cw = getStringWidth(nameCJK,g);
                            g.setFont(Nfont);
                            let nw = getStringWidth(nameNCJK,g);
                            InterWidth += (40 + retMax(cw,nw));
                            if(j !=stationInfoList[ii].interchangeInfo.length-1 )InterWidth += 20;
                            if(stationInfoList[ii].interchangeInfo[j].isRailwayRoute == 1) isTL = 1;
                            if(stationInfoList[ii].interchangeInfo[j].isExpressRoute == 1) isCJ = 1;
                        }
                        
                if(ii<nextStationIndex)
                    {   let lt = g.getTransform();
                        let scalex = 1;
                        if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                            {
                                scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                                g.scale(scalex,1);
                            }
                            g.translate(loc / scalex-InterWidth / 2,0);
                        let lastx = 0;
                        for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                        {
                            let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                            let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                            let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                            let Nfont = ROBOTO_REGULAR.deriveFont(28);
                            g.setColor(Color.LIGHT_GRAY);
                            g.setFont(Cfont);
                            let ircl = getStringWidth(ircn,g);
                            g.setFont(Nfont);
                            let irnl = getStringWidth(irnn,g);
                            let TagW = retMax(ircl,irnl) + 40;
                            g.fillRoundRect(lastx,140,TagW,100,30,30);
                            g.setStroke(new BasicStroke(15));
                            g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                            g.setColor(Color.WHITE);
                            g.setFont(Cfont);
                            drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                            g.setFont(Nfont);
                            drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                            lastx += TagW;
                            lastx += 20;
                        }
                        g.setTransform(lt);
                        g.setColor(Color.LIGHT_GRAY);
                        g.fillOval(loc-40,270,80,80);
                        ctx.setDebugInfo("lastx",lastx);
                    }
                if(ii>=nextStationIndex)
                {let lt = g.getTransform();
                    let scalex = 1;
                    if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                        {
                            scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                            g.scale(scalex,1);
                        }
                        g.translate(loc / scalex-InterWidth / 2,0);
                        let lastx = 0;
                        for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                        {
                            let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                            let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                            let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                            let Nfont = ROBOTO_REGULAR.deriveFont(28);
                            g.setColor(ircolor);
                            g.setFont(Cfont);
                            let ircl = getStringWidth(ircn,g);
                            g.setFont(Nfont);
                            let irnl = getStringWidth(irnn,g);
                            let TagW = retMax(ircl,irnl) + 40;
                            g.fillRoundRect(lastx,140,TagW,100,30,30);
                            g.setStroke(new BasicStroke(15));
                            g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                            g.setColor(Color.decode(getContrastColor(ircolor)));
                            g.setFont(Cfont);
                            drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                            g.setFont(Nfont);
                            drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                            lastx += TagW;
                            lastx += 20;
                        }
                        g.setTransform(lt);
                        ctx.setDebugInfo("lastx",lastx);
                        
                    g.setColor(routeColor);
                    g.fillOval(loc-40,270,80,80);
                }
                if(ii==now&&ModeI==1)g.setColor(Color.YELLOW);
                else g.setColor(Color.WHITE);
                g.fillOval(loc-30,280,60,60);
                let nameCJK = gcp(stationInfoList[ii].stationName);
                let nameNCJK = gncp(stationInfoList[ii].stationName);
                let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(52);
                let Nfont = ROBOTO_REGULAR.deriveFont(40);
                g.setColor(frontcolor);
                g.setFont(Cfont);
                drawCenteredAndScaledText(g,nameCJK,loc,415,(ii-start==0||ii==end)?retMin(390,stalen - 20):stalen  - 20);
                g.setFont(Nfont);
                drawCenteredAndScaledText(g,nameNCJK,loc,455,(ii-start==0||ii==end)?retMin(390,stalen - 20):stalen  - 20);
                let back = new Color(0,0,0,0.8);
                if(isCJ && isTL)
                {if(background_color == 1) 
                    {
                        g.setColor(back);
                        g.fillRoundRect(loc-45,460,40,40,20,20);
                        g.fillRoundRect(loc+5,460,40,40,20,20);
                    }
                    g.drawImage(TL,loc - 45,460,40,40,null);
                    g.drawImage(CJ,loc +5,460,40,40,null);
                    
                }
                else if(isCJ )
                    { if(background_color == 1) 
                        {
                            g.setColor(back);
                            //g.fillRoundRect(loc-45,460,40,40,20,20);
                            g.fillRoundRect(loc-20,460,40,40,20,20);
                        }
                        //g.drawImage(TL,loc - 45,460,40,40,null);
                        g.drawImage(CJ,loc -20,460,40,40,null);
                       
                    }
                    else if(isTL)
                        {if(background_color == 1) 
                            {
                                g.setColor(back);
                                //g.fillRoundRect(loc-45,460,40,40,20,20);
                                g.fillRoundRect(loc-20,460,40,40,20,20);
                            }
                            g.drawImage(TL,loc -20,460,40,40,null);
                            //g.drawImage(CJ,loc +5,460,40,40,null);
                            
                        }
            }*/
        }
    }
    else if(!isLeftDoor)
    {

        if((OpenSide == 3||OpenSide == 2))
        {
            let artfont =ART_FONT.deriveFont(210),engfont = ROBOTO_REGULAR.deriveFont(58);
            let cjkname=gcp(stationInfoList[nextStationIndex].stationName),ncjkname=gncp(stationInfoList[nextStationIndex].stationName);
            g.setFont(artfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,cjkname,WIDTH/2,320,WIDTH/3);
            g.setFont(engfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,ncjkname,WIDTH/2,430,WIDTH/3);
            drawScrollingDoor(g,0,door_w,door_h,WIDTH/3-door_w*3,180,3,state.doorSec.stateNowDuration(),DOOR,1);
            let doorfontc = SOURCE_HAN_SANS_CN_REGULAR.deriveFont(50),doorfontn = ROBOTO_REGULAR.deriveFont(35);
            g.setColor(frontcolor);
            g.setFont(doorfontc);
            g.drawString("本侧开门",WIDTH/3-door_w*1.5,285);
            g.setFont(doorfontn);
            g.drawString("This side",WIDTH/3-door_w*1.5,325);
        }/*
        if(state.doorWillOpen)
        {
            g.setColor(Color.GREEN);
            g.fillRect(10,10,20,20);
        }*/
            else if(OpenSide == 0)
                {
                    drawTCLine(g,routeColor,routeInfo,stationInfoList,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,stationCount,ctx);
                }
        else
        {
            let artfont =ART_FONT.deriveFont(210),engfont = ROBOTO_REGULAR.deriveFont(58);
            let cjkname=gcp(stationInfoList[nextStationIndex].stationName),ncjkname=gncp(stationInfoList[nextStationIndex].stationName);
            g.setFont(artfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,cjkname,WIDTH/2,320,WIDTH/3);
            g.setFont(engfont);
            g.setColor(frontcolor);
            drawCenteredAndScaledText(g,ncjkname,WIDTH/2,430,WIDTH/3);
            drawScrollingDoor(g,0,door_w,door_h,WIDTH/3*2+door_w*3,180,3,state.doorSec.stateNowDuration(),DOOR,0);
            let doorfontc = SOURCE_HAN_SANS_CN_REGULAR.deriveFont(50),doorfontn = ROBOTO_REGULAR.deriveFont(35);
            
            g.setColor(frontcolor);
            g.setFont(doorfontc);
            g.drawString("对侧开门",WIDTH/3*2+door_w*1.5-getStringWidth("对侧开门",g),285);
            g.setFont(doorfontn);
            g.drawString("Other side",WIDTH/3*2+door_w*1.5-getStringWidth("Other side",g),325);/*
            let linel=WIDTH - 400;
            let stalen=linel*1.0/(stationCount-1);
            let start = 0,end = stationCount - 1,now = nextStationIndex;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(170,295,30+stalen*(now-start-1 < 0?0:now-start-1),30);
            //if(nextStationIndex>2)drawGradientRect(g,70,295,100,30,"r");
            g.setColor(routeColor);
            g.fillRect(stalen*(now-start-1 < 0?0:now-start-1)+200,295,linel-stalen*(now-start-1 < 0?0:now-start-1)+30,30);
            drawScrollingArrow(g,stalen,0,stalen*(now-start-1 < 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration());
            drawScrollingArrow(g,stalen,0,stalen*(now-start-1< 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration() + 0.3);
          //  drawGradientRect(g,200+linel,295,100,30,"l");
            for(let ii =0;ii<stationCount;ii++){
                let loc=200+stalen*ii;
                let InterWidth = 0;
                let isTL = 0,isCJ = 0;
                        for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                        {
                            let nameCJK = gcp(stationInfoList[ii].interchangeInfo[j].name);
                            let nameNCJK = gncp(stationInfoList[ii].interchangeInfo[j].name);
                            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                            let Nfont = ROBOTO_REGULAR.deriveFont(28);
                            g.setFont(Cfont);
                            let cw = getStringWidth(nameCJK,g);
                            g.setFont(Nfont);
                            let nw = getStringWidth(nameNCJK,g);
                            InterWidth += (40 + retMax(cw,nw));
                            if(j !=stationInfoList[ii].interchangeInfo.length-1 )InterWidth += 20;
                            if(stationInfoList[ii].interchangeInfo[j].isRailwayRoute == 1) isTL = 1;
                            if(stationInfoList[ii].interchangeInfo[j].isExpressRoute == 1) isCJ = 1;
                        }
                        
                if(ii<nextStationIndex)
                    {   let lt = g.getTransform();
                        let scalex = 1;
                        if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                            {
                                scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                                g.scale(scalex,1);
                            }
                            g.translate(loc / scalex-InterWidth / 2,0);
                        let lastx = 0;
                        for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                        {
                            let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                            let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                            let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                            let Nfont = ROBOTO_REGULAR.deriveFont(28);
                            g.setColor(Color.LIGHT_GRAY);
                            g.setFont(Cfont);
                            let ircl = getStringWidth(ircn,g);
                            g.setFont(Nfont);
                            let irnl = getStringWidth(irnn,g);
                            let TagW = retMax(ircl,irnl) + 40;
                            g.fillRoundRect(lastx,140,TagW,100,30,30);
                            g.setStroke(new BasicStroke(15));
                            g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                            g.setColor(Color.WHITE);
                            g.setFont(Cfont);
                            drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                            g.setFont(Nfont);
                            drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                            lastx += TagW;
                            lastx += 20;
                        }
                        g.setTransform(lt);
                        g.setColor(Color.LIGHT_GRAY);
                        g.fillOval(loc-40,270,80,80);
                        ctx.setDebugInfo("lastx",lastx);
                    }
                if(ii>=nextStationIndex)
                {let lt = g.getTransform();
                    let scalex = 1;
                    if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                        {
                            scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                            g.scale(scalex,1);
                        }
                        g.translate(loc / scalex-InterWidth / 2,0);
                        let lastx = 0;
                        for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                        {
                            let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                            let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                            let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                            let Nfont = ROBOTO_REGULAR.deriveFont(28);
                            g.setColor(ircolor);
                            g.setFont(Cfont);
                            let ircl = getStringWidth(ircn,g);
                            g.setFont(Nfont);
                            let irnl = getStringWidth(irnn,g);
                            let TagW = retMax(ircl,irnl) + 40;
                            g.fillRoundRect(lastx,140,TagW,100,30,30);
                            g.setStroke(new BasicStroke(15));
                            g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                            g.setColor(Color.decode(getContrastColor(ircolor)));
                            g.setFont(Cfont);
                            drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                            g.setFont(Nfont);
                            drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                            lastx += TagW;
                            lastx += 20;
                        }
                        g.setTransform(lt);
                        ctx.setDebugInfo("lastx",lastx);
                        
                    g.setColor(routeColor);
                    g.fillOval(loc-40,270,80,80);
                }
                if(ii==now&&ModeI==1)g.setColor(Color.YELLOW);
                else g.setColor(Color.WHITE);
                g.fillOval(loc-30,280,60,60);
                let nameCJK = gcp(stationInfoList[ii].stationName);
                let nameNCJK = gncp(stationInfoList[ii].stationName);
                let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(52);
                let Nfont = ROBOTO_REGULAR.deriveFont(40);
                g.setColor(frontcolor);
                g.setFont(Cfont);
                drawCenteredAndScaledText(g,nameCJK,loc,415,(ii-start==0||ii==end)?retMin(390,stalen - 20):stalen  - 20);
                g.setFont(Nfont);
                drawCenteredAndScaledText(g,nameNCJK,loc,455,(ii-start==0||ii==end)?retMin(390,stalen - 20):stalen  - 20);
                let back = new Color(0,0,0,0.8);
                if(isCJ && isTL)
                {if(background_color == 1) 
                    {
                        g.setColor(back);
                        g.fillRoundRect(loc-45,460,40,40,20,20);
                        g.fillRoundRect(loc+5,460,40,40,20,20);
                    }
                    g.drawImage(TL,loc - 45,460,40,40,null);
                    g.drawImage(CJ,loc +5,460,40,40,null);
                    
                }
                else if(isCJ )
                    { if(background_color == 1) 
                        {
                            g.setColor(back);
                            //g.fillRoundRect(loc-45,460,40,40,20,20);
                            g.fillRoundRect(loc-20,460,40,40,20,20);
                        }
                        //g.drawImage(TL,loc - 45,460,40,40,null);
                        g.drawImage(CJ,loc -20,460,40,40,null);
                       
                    }
                    else if(isTL)
                        {if(background_color == 1) 
                            {
                                g.setColor(back);
                                //g.fillRoundRect(loc-45,460,40,40,20,20);
                                g.fillRoundRect(loc-20,460,40,40,20,20);
                            }
                            g.drawImage(TL,loc -20,460,40,40,null);
                            //g.drawImage(CJ,loc +5,460,40,40,null);
                            
                        }
            }*/
        }
    }
}
function drawTCLine(g,routeColor,routeInfo,stationInfoList,nextStationIndex,ModeI,trainStatus,train,isLeftDoor,state,stationCount,ctx){
   
    if(stationCount<=6){
        let linel=WIDTH - 400;
        let stalen=linel*1.0/(stationCount-1);
        let start = 0,end = stationCount - 1,now = nextStationIndex;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(170,295,30+stalen*(now-start-1 < 0?0:now-start-1),30);
        //if(nextStationIndex>2)drawGradientRect(g,70,295,100,30,"r");
        g.setColor(routeColor);
        g.fillRect(stalen*(now-start-1 < 0?0:now-start-1)+200,295,linel-stalen*(now-start-1 < 0?0:now-start-1)+30,30);
        drawScrollingArrow(g,stalen,0,stalen*(now-start-1 < 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration());
        drawScrollingArrow(g,stalen,0,stalen*(now-start-1< 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration() + 0.3);
      //  drawGradientRect(g,200+linel,295,100,30,"l");
        for(let ii =0;ii<stationCount;ii++){
            let loc=200+stalen*ii;
            let InterWidth = 0;
            let isTL = 0,isCJ = 0;
                    for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                    {
                        let nameCJK = gcp(stationInfoList[ii].interchangeInfo[j].name);
                        let nameNCJK = gncp(stationInfoList[ii].interchangeInfo[j].name);
                        let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                        let Nfont = ROBOTO_REGULAR.deriveFont(28);
                        g.setFont(Cfont);
                        let cw = getStringWidth(nameCJK,g);
                        g.setFont(Nfont);
                        let nw = getStringWidth(nameNCJK,g);
                        InterWidth += (40 + retMax(cw,nw));
                        if(j !=stationInfoList[ii].interchangeInfo.length-1 )InterWidth += 20;
                        if(stationInfoList[ii].interchangeInfo[j].isRailwayRoute == 1) isTL = 1;
                        if(stationInfoList[ii].interchangeInfo[j].isExpressRoute == 1) isCJ = 1;
                    }
                    
            if(ii<nextStationIndex)
                {   let lt = g.getTransform();
                    let scalex = 1;
                    if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                        {
                            scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                            g.scale(scalex,1);
                        }
                        g.translate(loc / scalex-InterWidth / 2,0);
                    let lastx = 0;
                    for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                    {
                        let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                        let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                        let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                        let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                        let Nfont = ROBOTO_REGULAR.deriveFont(28);
                        g.setColor(Color.LIGHT_GRAY);
                        g.setFont(Cfont);
                        let ircl = getStringWidth(ircn,g);
                        g.setFont(Nfont);
                        let irnl = getStringWidth(irnn,g);
                        let TagW = retMax(ircl,irnl) + 40;
                        g.fillRoundRect(lastx,140,TagW,100,30,30);
                        g.setStroke(new BasicStroke(15));
                        g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                        g.setColor(Color.WHITE);
                        g.setFont(Cfont);
                        drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                        g.setFont(Nfont);
                        drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                        lastx += TagW;
                        lastx += 20;
                    }
                    g.setTransform(lt);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillOval(loc-40,270,80,80);
                    ctx.setDebugInfo("lastx",lastx);
                }
            if(ii>=nextStationIndex)
            {let lt = g.getTransform();
                let scalex = 1;
                if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                    {
                        scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                        g.scale(scalex,1);
                    }
                    g.translate(loc / scalex-InterWidth / 2,0);
                    let lastx = 0;
                    for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                    {
                        let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                        let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                        let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                        let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(35);
                        let Nfont = ROBOTO_REGULAR.deriveFont(28);
                        g.setColor(ircolor);
                        g.setFont(Cfont);
                        let ircl = getStringWidth(ircn,g);
                        g.setFont(Nfont);
                        let irnl = getStringWidth(irnn,g);
                        let TagW = retMax(ircl,irnl) + 40;
                        g.fillRoundRect(lastx,140,TagW,100,30,30);
                        g.setStroke(new BasicStroke(15));
                        g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                        g.setColor(Color.decode(getContrastColor(ircolor)));
                        g.setFont(Cfont);
                        drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                        g.setFont(Nfont);
                        drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                        lastx += TagW;
                        lastx += 20;
                    }
                    g.setTransform(lt);
                    ctx.setDebugInfo("lastx",lastx);
                    
                g.setColor(routeColor);
                g.fillOval(loc-40,270,80,80);
            }
            if(ii==now&&ModeI==1)g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);
            g.fillOval(loc-30,280,60,60);
            let nameCJK = gcp(stationInfoList[ii].stationName);
            let nameNCJK = gncp(stationInfoList[ii].stationName);
            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(52);
            let Nfont = ROBOTO_REGULAR.deriveFont(40);
            g.setColor(frontcolor);
            g.setFont(Cfont);
            drawCenteredAndScaledText(g,nameCJK,loc,415,(ii-start==0||ii==end)?retMin(390,stalen - 20):stalen  - 20);
            g.setFont(Nfont);
            drawCenteredAndScaledText(g,nameNCJK,loc,455,(ii-start==0||ii==end)?retMin(390,stalen - 20):stalen  - 20);
            let back = new Color(0,0,0,0.8);
            if(isCJ && isTL)
            {if(background_color == 1) 
                {
                    g.setColor(back);
                    g.fillRoundRect(loc-45,460,40,40,20,20);
                    g.fillRoundRect(loc+5,460,40,40,20,20);
                }
                g.drawImage(TL,loc - 45,460,40,40,null);
                g.drawImage(CJ,loc +5,460,40,40,null);
                
            }
            else if(isCJ )
                { if(background_color == 1) 
                    {
                        g.setColor(back);
                        //g.fillRoundRect(loc-45,460,40,40,20,20);
                        g.fillRoundRect(loc-20,460,40,40,20,20);
                    }
                    //g.drawImage(TL,loc - 45,460,40,40,null);
                    g.drawImage(CJ,loc -20,460,40,40,null);
                   
                }
                else if(isTL)
                    {if(background_color == 1) 
                        {
                            g.setColor(back);
                            //g.fillRoundRect(loc-45,460,40,40,20,20);
                            g.fillRoundRect(loc-20,460,40,40,20,20);
                        }
                        g.drawImage(TL,loc -20,460,40,40,null);
                        //g.drawImage(CJ,loc +5,460,40,40,null);
                        
                    }
        }
    }
    else{
        let start,now,end;
        now = nextStationIndex;
        if(now <= 2) start = 0;
        else if(now + 3 >= stationCount) start = stationCount - 6;
        else start = now - 2;
        if(now <= 2) end = 5;
        else end = now + 3;
        if(end >=stationCount) end = stationCount-1;
        let linel=WIDTH-400;
        let stalen=linel*1.0/5;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(170,295,30+stalen*(now-start-1 < 0?0:now-start-1),30);
        if(nextStationIndex>2)drawGradientRect(g,70,295,100,30,"r");
        g.setColor(routeColor);
        g.fillRect(stalen*(now-start-1 < 0?0:now-start-1)+200,295,linel-stalen*(now-start-1 < 0?0:now-start-1)+30,30);
        if((stationCount-nextStationIndex)>4)drawGradientRect(g,230+linel,295,100,30,"l");
        drawScrollingArrow(g,stalen,0,stalen*(now-start-1 < 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration());
        drawScrollingArrow(g,stalen,0,stalen*(now-start-1< 0?0:now-start-1)+200,310,5,state.tcRoll.stateNowDuration() + 0.3);

        for(let ii =start;ii<=end;ii++){
            let InterWidth = 0;
            let isTL = 0,isCJ = 0;
            for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
            {
                let nameCJK = gcp(stationInfoList[ii].interchangeInfo[j].name);
                let nameNCJK = gncp(stationInfoList[ii].interchangeInfo[j].name);
                let Cfont =  SOURCE_HAN_SANS_CN_BOLD.deriveFont(35);
                let Nfont = ROBOTO_REGULAR.deriveFont(28);
                g.setFont(Cfont);
                let cw = getStringWidth(nameCJK,g);
                g.setFont(Nfont);
                let nw = getStringWidth(nameNCJK,g);
                InterWidth += (40 + retMax(cw,nw));
                if(j !=stationInfoList[ii].interchangeInfo.length-1 )InterWidth += 20;
                if(stationInfoList[ii].interchangeInfo[j].isRailwayRoute == 1) isTL = 1;
                        if(stationInfoList[ii].interchangeInfo[j].isExpressRoute == 1) isCJ = 1;
                    
            }
            
            let loc=200+stalen*(ii-start);
            if(ii<(now))
                { let lt = g.getTransform();
                    let scalex = 1;
                if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                    {
                        scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                        g.scale(scalex,1);
                    }
                    g.translate(loc / scalex-InterWidth / 2,0);
                    let lastx = 0;
                    for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                    {
                        let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                        let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                        let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                        let Cfont =  SOURCE_HAN_SANS_CN_BOLD.deriveFont(35);
                        let Nfont = ROBOTO_REGULAR.deriveFont(28);
                        g.setColor(Color.LIGHT_GRAY);
                        g.setFont(Cfont);
                        let ircl = getStringWidth(ircn,g);
                        g.setFont(Nfont);
                        let irnl = getStringWidth(irnn,g);
                        let TagW = retMax(ircl,irnl) + 40;
                        g.fillRoundRect(lastx,140,TagW,100,30,30);
                        g.setStroke(new BasicStroke(15));
                        g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                        g.setColor(Color.WHITE);
                        g.setFont(Cfont);
                        drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                        g.setFont(Nfont);
                        drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                        lastx += TagW;
                        lastx += 20;
                        ctx.setDebugInfo("lastx",lastx);
                    }
                    g.setTransform(lt);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillOval(loc-40,270,80,80);
                }
            
            if(ii>=now)
            {let lt = g.getTransform();
                let scalex = 1;
                if(InterWidth > ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20))
                    {
                        scalex = ((ii-start==0||ii==end)?retMin(390,stalen - 20):stalen - 20)/InterWidth;
                        g.scale(scalex,1);
                    }
                    g.translate(loc / scalex-InterWidth / 2,0);
                    let lastx = 0;
                    for(let j = 0;j < stationInfoList[ii].interchangeInfo.length;j++)
                    {
                        let ircolor = stationInfoList[ii].interchangeInfo[j].color;
                        let ircn = gcp(stationInfoList[ii].interchangeInfo[j].name);
                        let irnn = gncp(stationInfoList[ii].interchangeInfo[j].name);
                        let Cfont =  SOURCE_HAN_SANS_CN_BOLD.deriveFont(35);
                        let Nfont = ROBOTO_REGULAR.deriveFont(25);
                        g.setColor(ircolor);
                        g.setFont(Cfont);
                        let ircl = getStringWidth(ircn,g);
                        g.setFont(Nfont);
                        let irnl = getStringWidth(irnn,g);
                        let TagW = retMax(ircl,irnl) + 40;
                        g.fillRoundRect(lastx,140,TagW,100,30,30);
                        g.setStroke(new BasicStroke(15));
                        g.drawLine(lastx+TagW/2,240,InterWidth / 2,310);
                        g.setColor(Color.decode(getContrastColor(ircolor)));
                        g.setFont(Cfont);
                        drawCenteredAndScaledText(g,ircn,lastx + TagW / 2,190,null);
                        g.setFont(Nfont);
                        drawCenteredAndScaledText(g,irnn,lastx + TagW / 2,220,null);
                        lastx += TagW;
                        lastx += 20;
                        ctx.setDebugInfo("lastx",lastx);
                    }
                    g.setTransform(lt);
                g.setColor(routeColor);
                g.fillOval(loc-40,270,80,80);
            }
            if(ii==now&&ModeI==1)g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);
            g.fillOval(loc-30,280,60,60);
            let nameCJK = gcp(stationInfoList[ii].stationName);
            let nameNCJK = gncp(stationInfoList[ii].stationName);
            let Cfont =  SOURCE_HAN_SANS_CN_REGULAR.deriveFont(52);
            let Nfont = ROBOTO_REGULAR.deriveFont(40);
            g.setColor(frontcolor);
            g.setFont(Cfont);
            drawCenteredAndScaledText(g,nameCJK,loc,415,(ii-start==0||ii==end)?200*2:stalen  - 10);
            g.setFont(Nfont);
            drawCenteredAndScaledText(g,nameNCJK,loc,455,(ii-start==0||ii==end)?200*2:stalen  - 10);
            let back = new Color(0,0,0,0.8);
            if(isCJ && isTL)
            {if(background_color == 1) 
                {
                    g.setColor(back);
                    g.fillRoundRect(loc-45,460,40,40,20,20);
                    g.fillRoundRect(loc+5,460,40,40,20,20);
                }
                g.drawImage(TL,loc - 45,460,40,40,null);
                g.drawImage(CJ,loc +5,460,40,40,null);
                
            }
            else if(isCJ )
                { if(background_color == 1) 
                    {
                        g.setColor(back);
                        //g.fillRoundRect(loc-45,460,40,40,20,20);
                        g.fillRoundRect(loc-20,460,40,40,20,20);
                    }
                    //g.drawImage(TL,loc - 45,460,40,40,null);
                    g.drawImage(CJ,loc -20,460,40,40,null);
                   
                }
                else if(isTL)
                    {if(background_color == 1) 
                        {
                            g.setColor(back);
                            //g.fillRoundRect(loc-45,460,40,40,20,20);
                            g.fillRoundRect(loc-20,460,40,40,20,20);
                        }
                        g.drawImage(TL,loc -20,460,40,40,null);
                        //g.drawImage(CJ,loc +5,460,40,40,null);
                        
                    }
        }
        ctx.setDebugInfo("start",start);
        ctx.setDebugInfo("now",now);
        ctx.setDebugInfo("end",end);
        ctx.setDebugInfo("ratex",ModeI);
    }

    
}
function drawBlueScreen(g, strToDraw) {
    let x = HORIZONTAL_SPACING * 2;
    let y = VERTICAL_SPACING * 4;

    g.setColor(Color.BLUE);
    g.fillRect(0, 0, WIDTH, HEIGHT);

    let font = ROBOTO_BOLD.deriveFont(66.0);
    let fm = g.getFontMetrics(font);
    g.setFont(font);
    g.setColor(Color.WHITE);
    y += fm.getAscent();
    g.drawString(":(", x, y);
    y += fm.getDescent();

    font = SOURCE_HAN_SANS_CN_BOLD.deriveFont(calculateMaxFontSize(g, SOURCE_HAN_SANS_CN_BOLD, getMatching(strToDraw, true), WIDTH - HORIZONTAL_SPACING * 4, 0, 0, 0, false));
    fm = g.getFontMetrics(font);
    g.setFont(font);
    y += fm.getAscent();
    g.drawString(getMatching(strToDraw, true), x, y);
    y += fm.getDescent();

    font = ROBOTO_BOLD.deriveFont(calculateMaxFontSize(g, ROBOTO_BOLD, getMatching(strToDraw, false), WIDTH - HORIZONTAL_SPACING * 4, 0, 0, 0, false));
    fm = g.getFontMetrics(font);
    g.setFont(font);
    y += fm.getAscent();
    g.drawString(getMatching(strToDraw, false), x, y);
    y += fm.getDescent();

    g.setColor(Color.GRAY);
    font = SOURCE_HAN_SANS_CN_BOLD.deriveFont(16.0);
    fm = g.getFontMetrics(font);
    g.setFont(font);
    y += VERTICAL_SPACING * 2 + fm.getAscent();
    g.drawString("LCD" + AUTHOR + ". 检查更新 (For Updates): q1015711254; 使用文档 (For Document):加群1015711254", x, y);

}
function getStationConfig(stations, nextIndex) {
    var result = Object.assign({}, pisConfig["default"]);
    if (nextIndex >= stations.size() || nextIndex < 0) return result;

    var exitStr = "";
    for (var it = stations.get(nextIndex).station.exits.entrySet().iterator(); it.hasNext(); ) {
        var entry = it.next();
        if (entry.getKey().startsWith("Z")) {
            for (var index in entry.getValue()) {
                var stationCfg = JSON.parse(entry.getValue().get(index));
                Object.assign(result, stationCfg);
            }
        } else {
            for (var index in entry.getValue()) {
                exitStr += entry.getValue().get(index) + "\n";
            }
        }
    }
    result["exitStr"] = exitStr.trim();

    var routeCode = ("" + TextUtil.getExtraParts(stations.get(nextIndex).route.name))
        .split("/")[0].toLowerCase();
    var stationCode = result["code"] === (void 0) ? "" : result["code"].toLowerCase();
    result["routeStationCode"] = routeCode + "_" + stationCode;

    if (pisConfig["routeStations"][result["routeStationCode"]] !== (void 0)) {
        Object.assign(result, pisConfig["routeStations"][result["routeStationCode"]]);
    }

    return result;
}
