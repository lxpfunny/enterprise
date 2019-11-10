package com.lxpfunny.enterprise.controller;

import com.asprise.ocr.Ocr;
import com.lxpfunny.enterprise.util.HttpUtils;
import com.lxpfunny.enterprise.util.JsonUtils;
import com.lxpfunny.enterprise.util.pk10;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lixiaopeng on 2018/11/24.
 */
@RestController
@RequestMapping("/pk10")
public class Pk10Controller {
    static Long currentNumber = 0L;
    static int minute = 60000;
    static boolean isStop = false;
    static boolean isLogin = false;
    static boolean isSleep = true;
    static String balance = "";
    static String cookieCache = "";
    static String qianType = "3";
    static Integer bei = 1;
    static Integer jiabei = bei;
    static Integer jiabeibeishu = 2;
    static int zhongCount = 0;
    static int guaCount = 0;
    static int zhuiCount = 1;
    static String xiadanhao = "";
    public static Integer mingci = null;
    static Map<String, Thread> threadMap = new HashMap<>();
    static List<ExecutorService> services = new ArrayList<>();


    @GetMapping("/getcookie")
    public Map<String, String> getCookie() {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("cookieCache", cookieCache);
        returnMap.put("isStop", !isStop ? "运行中" : "停止");
        String balance = getAccount();
        returnMap.put("balance", balance);
        return returnMap;
    }
    private  String getAccount(){
        String url = "https://www.aub06.com/httphandle/UserHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookieCache);
        Map<String, Object> param = new HashMap<>();
        param.put("action", 10);
        String response = "";
        try {
             response = HttpUtils.doPost(url, header, param);
             if("-100".equals(response)){
                 return "未登录";
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("/start")
    public Map<String,String> start(@RequestParam("cookie")  String cookie,@RequestParam("qianType")String qianType,@RequestParam("bei")Integer bei) {
        Map<String, String> map = new HashMap<>();

        if(bei == null){
            map.put("code", "1");
            map.put("msg", "请输入倍数");
        }
        if(StringUtils.isEmpty(cookie)){
            map.put("code", "1");
            map.put("msg", "请登录");
        }
        cookieCache = cookie;
        isStop = false;
        if(StringUtils.isNotEmpty(qianType)){
            Pk10Controller.qianType = qianType;
        }
            Pk10Controller.bei = bei;
//        services.clear();
        if(services.size() > 0){
            map.put("code", "1");
            map.put("msg", "下单程序正在运行，先终止程序，在下单");
            return map;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        services.add(executor);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Random r = new Random();
                String loginCookie = "";
                for (int i = 0; ; i++) {
//                    if(isLogin){
//                        isLogin = false;
//                        try{
//                            Map<String,String> loginRes = login();
//                            String code = loginRes.get("respCode");
//                            if("0".equals(code)){
//                                loginCookie = loginRes.get("cookieCache");
//                                System.out.println("登陆成功");
//                                isSleep = false;
//                            }else{
//                                isStop = true;
//                            }
//                        }catch (Exception e){
//                            isStop = true;
//                            e.printStackTrace();
//                        }
//                    }


                    if (isStop) {
                        System.out.println("线程中断退出");
                        return;
                    }
   //aaa
                    if (i > 0 && isSleep) {
                        int m = r.nextInt(30 * minute);

                        try {
                            System.out.println("休息：" + (m / minute) + "分钟");
                            Thread.sleep(m);
                        } catch (InterruptedException e) {
                            isStop = true;
                            continue;
                        }
                    }
                    Date now = new Date();
                    int hours = now.getHours();
                    if (hours < 12 && hours > 3) {
//                        System.out.println("开始下单："+i+",isSleep:"+isSleep);
                        System.out.println("当天暂停下单");
                        try {
                            Thread.sleep(9*60*minute);
                            zhongCount=0;
                            guaCount = 0;
                            jiabei = bei;
                            zhuiCount=1;
                            mingci = null;
                        } catch (InterruptedException e) {
                            isStop = true;
                            e.printStackTrace();
                            continue;
                        }
                        isSleep = false;
                        continue;
                    }
                    zhongCount=0;
                    guaCount = 0;
                    jiabei = bei;
                    zhuiCount=1;
                    mingci = null;
                    if(StringUtils.isNotEmpty(loginCookie)){
                        touzhu(loginCookie);
                    }else{
                        touzhu(cookie);
                    }

                }
            }
        });
        map.put("code", "0");
        return map;
    }

    @GetMapping("/stop")
    public void stop() {
        for (ExecutorService service : services) {
            service.shutdownNow();
        }
        zhongCount =0;
        guaCount = 0;
        services.clear();
    }

    public static void main(String[] args) {
//        getAccount();
        String cookie = "__cfduid=d148398fc818dccf482ce2cbd4af8d12e1573297047; ASP.NET_SessionId=sj3yfkepbobqk3bd3wjjyxpk";
        getMyrecord(20191110008L,cookie,xiadanhao);
    }

    private  void touzhu(String cookie)  {

            xiadan(qianType, bei, cookie,"",null,null);
    }
    private static String createXiadanhao(Integer mingci){
        String xiadanhao = "";
        for (int i = 0; i < 10; i++) {
            try {
                if (i > 0) {
                    Thread.sleep(2 * minute);
                }
            } catch (InterruptedException e) {
                isStop = true;
                e.printStackTrace();
                return "";
            }
            List<String> historyKaijiang = getKaijiang();
            if (historyKaijiang == null) {
                System.out.println("没有最新开奖");
                continue;
            }
            xiadanhao = pk10.createHaoma1(historyKaijiang,mingci);
            if (StringUtils.isEmpty(xiadanhao)) {
                System.out.println("没有符合投注条件");
                continue;
            } else {
                break;
            }
        }
        return xiadanhao;
    }

    /**
     * 第一次下单 倍数1
     *
     * @param qian
     * @param bei
     * @param cookie

     */
    private  static void xiadan(String qian, Integer bei, String cookie,String xiadanhao,Long qihao,Integer mingci) {
        //        <ol><item><ms>3</ms><bs>1</bs><num>|||2,3,5,6,8,9,10||||||</num><pid>11</pid></item></ol>
//        qian = "3";
        if (StringUtils.isEmpty(cookie)) {
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }

        if(StringUtils.isEmpty(xiadanhao)){
            xiadanhao = createXiadanhao(mingci);

        }
        if(qihao == null){
            qihao = currentNumber + 1;
        }else {
            qihao = qihao + 1;
        }

        String template = "<ol><item><ms>qian</ms><bs>bei</bs><num>haoma</num><pid>11</pid></item></ol>";
        String url = "https://www.aub06.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "3");
        param.put("t", "89");
        param.put("n", qihao);
        //3==分 1=倍数
        template = template.replace("haoma", xiadanhao);
        template = template.replace("qian", qian);
        template = template.replace("bei", String.valueOf(bei));
        param.put("ol", template);
        try {
            String response = HttpUtils.doPost(url, header, param);
            Map<String, Object> map = JsonUtils.fromJson(response, Map.class);
            if (String.valueOf(map.get("code")).equals("0")) {
                System.out.println("期号：" + qihao + "下单成功");
                //检测是否中奖
                getMyrecord(qihao,  cookie,xiadanhao);
            } else if (String.valueOf(map.get("code")).equals("-100")) {
                System.out.println("登录超时");
                isLogin = true;
            } else if (String.valueOf(map.get("code")).equals("-12")) {
                System.out.println("操作失败，您的余额不足！");
                isStop = true;
            } else if (String.valueOf(map.get("code")).equals("-15") || String.valueOf(map.get("code")).equals("-14")) {
                System.out.println("操作失败，此期已经停止销售，请选择当前可购买的期号！");
                isSleep = false;
                return;
            } else if (String.valueOf(map.get("code")).equals("-1")) {
                System.out.println("下单失败重新下单:" + response);
                isSleep = false;
                return;
            } else {
                System.out.println("下单失败:" + response);
                isStop = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void getMyrecord(long qihao, String cookie,String xiadanhao) {

        if (StringUtils.isEmpty(cookie)) {
            cookie = "__cfduid=d148398fc818dccf482ce2cbd4af8d12e1573297047; ASP.NET_SessionId=sj3yfkepbobqk3bd3wjjyxpk";
        }
        String url = "https://www.aub06.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", 6);
        param.put("lotteryType", "89");
        param.put("betType", 1);
        for (int i = 0; i < 20; i++) {
            try {

                Thread.sleep(minute /2);

                String response = HttpUtils.doPost(url, header, param);
                String[] rs = response.split("\\|");
                String currentRecord = rs[0];
                String[] currentData = currentRecord.split(",");
                String qishu = currentData[0];
                String status = currentData[2];
                System.out.println("期号：" + qishu + "查询中奖状态");
                if (qishu.equals(String.valueOf(qihao))) {
                    if (status.equals("2")) {
                        //中奖退出
                        System.out.println("期号：" + qishu + "已中奖");
                        isSleep = true;
                        zhongCount ++;
                        jiabei = bei;
                        zhuiCount=1;
                        mingci = null;
                        return;
                    } else if (status.equals("3")) {
                        //未中奖 追单一次
                        System.out.println("期号：" + qishu + "未中奖,第"+zhuiCount+"次加倍");
                        jiabei = jiabei*jiabeibeishu;
                        xiadan(qianType, jiabei, cookie,null,qihao,mingci);
                        zhuiCount++;
                        guaCount++;
                        System.out.println("期号：" + qishu + "未中奖");
                        break;
                    } else if (status.equals("1")) {
                        System.out.println("期号：" + qishu + "未开奖");
                        continue;
                    }
                } else {
                    System.out.println("期号：" + qishu + "未查询开奖记录");
                    continue;
                }


            } catch (Exception e) {
                e.printStackTrace();
                isStop = true;
                return;
            }
        }

    }

    /**
     * 第二次追单 倍数3
     *
     * @param qian
     * @param bei
     * @param cookie
     * @param xiadanhao
     * @param qihao
     */
    private static void xiadan2(String qian, String bei, String cookie, String xiadanhao, int qihao) {
        //        <ol><item><ms>3</ms><bs>1</bs><num>|||2,3,5,6,8,9,10||||||</num><pid>11</pid></item></ol>
        bei = String.valueOf(3 * Integer.parseInt(bei));
        if (StringUtils.isEmpty(cookie)) {
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String template = "<ol><item><ms>qian</ms><bs>bei</bs><num>haoma</num><pid>11</pid></item></ol>";
        String url = "https://www.aub06.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "3");
        param.put("t", "89");
        param.put("n", qihao);
        //3==分 1=倍数
        template = template.replace("haoma", xiadanhao);
        template = template.replace("qian", qian);
        template = template.replace("bei", bei);
        param.put("ol", template);
        try {
            String response = HttpUtils.doPost(url, header, param);
            Map<String, Object> map = JsonUtils.fromJson(response, Map.class);
            if (String.valueOf(map.get("code")).equals("0")) {
                System.out.println("期号：" + qihao + "下单成功");
                //检测是否中奖
                getMyrecord2(qihao, xiadanhao, cookie);
            } else if (String.valueOf(map.get("code")).equals("-100")) {
                System.out.println("登录超时");
                isStop = true;
            } else if (String.valueOf(map.get("code")).equals("-12")) {
                System.out.println("操作失败，您的余额不足！");
                isStop = true;
            } else if (String.valueOf(map.get("code")).equals("-15") || String.valueOf(map.get("code")).equals("-14")) {
                System.out.println("操作失败，此期已经停止销售，请选择当前可购买的期号！");
                isSleep = false;
                return;
            } else if (String.valueOf(map.get("code")).equals("-1")) {
                System.out.println("下单失败重新下单:" + response);
                isSleep = false;
                return;
            } else {
                System.out.println("下单失败:" + response);
                isStop = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询第二次追单的记录
     *
     * @param qihao
     * @param xiadanhao
     */
    private static void getMyrecord2(int qihao, String xiadanhao, String cookie) {

        if (StringUtils.isEmpty(cookie)) {
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String url = "https://www.aub06.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", 6);
        param.put("lotteryType", "6");
        param.put("betType", 1);
        for (int i = 0; i < 5; i++) {
            try {

                Thread.sleep(minute);

                String response = HttpUtils.doPost(url, header, param);
                String[] rs = response.split("\\|");
                String currentRecord = rs[0];
                String[] currentData = currentRecord.split(",");
                String qishu = currentData[0];
                String status = currentData[2];
                if (qishu.equals(String.valueOf(qihao))) {
                    if (status.equals("2")) {
                        //中奖退出
                        isSleep = true;
                        System.out.println("期号：" + qishu + "已中奖");
                        return;
                    } else if (status.equals("3")) {
                        //未中奖 追单一次
                        System.out.println("期号：" + qishu + "未中奖,第二次加倍");
                        xiadan3(qianType, bei, cookie, xiadanhao, qihao + 1);
                    } else if (status.equals("1")) {
                        System.out.println("期号：" + qishu + "未开奖");
                        continue;
                    }
                } else {
                    System.out.println("期号：" + qishu + "未查询开奖记录");
                    continue;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 第三次追单 倍数9
     *
     * @param qian
     * @param bei
     * @param cookie
     * @param xiadanhao
     * @param qihao
     */
    private static void xiadan3(String qian, Integer bei, String cookie, String xiadanhao, int qihao) {
        //        <ol><item><ms>3</ms><bs>1</bs><num>|||2,3,5,6,8,9,10||||||</num><pid>11</pid></item></ol>
//        qian = "3";
        bei = 9 * bei;
        if (StringUtils.isEmpty(cookie)) {
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String template = "<ol><item><ms>qian</ms><bs>bei</bs><num>haoma</num><pid>11</pid></item></ol>";
        String url = "https://www.aub06.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "3");
        param.put("t", "89");
        param.put("n", qihao);
        //3==分 1=倍数
        template = template.replace("haoma", xiadanhao);
        template = template.replace("qian", qian);
        template = template.replace("bei", String.valueOf(bei));
        param.put("ol", template);
        try {
            String response = HttpUtils.doPost(url, header, param);
            Map<String, Object> map = JsonUtils.fromJson(response, Map.class);
            if (String.valueOf(map.get("code")).equals("0")) {
                System.out.println("期号：" + qihao + "下单成功");
                //检测是否中奖
                getMyrecord3(qihao, xiadanhao, cookie);
            } else if (String.valueOf(map.get("code")).equals("-100")) {
                System.out.println("登录超时");
                isStop = true;
            } else if (String.valueOf(map.get("code")).equals("-12")) {
                System.out.println("操作失败，您的余额不足！");
                isStop = true;
            } else if (String.valueOf(map.get("code")).equals("-15") || String.valueOf(map.get("code")).equals("-14")) {
                System.out.println("操作失败，此期已经停止销售，请选择当前可购买的期号！");
                isSleep = false;
                return;
            } else if (String.valueOf(map.get("code")).equals("-1")) {
                System.out.println("下单失败重新下单:" + response);
                isSleep = false;
                return;
            } else {
                System.out.println("下单失败:" + response);
                isStop = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询第三次追单的中奖状态
     *
     * @param qihao
     * @param xiadanhao
     */
    private static void getMyrecord3(int qihao, String xiadanhao, String cookie) {

        if (StringUtils.isEmpty(cookie)) {
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String url = "https://www.aub06.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", 6);
        param.put("lotteryType", "6");
        param.put("betType", 1);
        for (int i = 0; i < 5; i++) {

            try {

                Thread.sleep(minute);

                String response = HttpUtils.doPost(url, header, param);
                String[] rs = response.split("\\|");
                String currentRecord = rs[0];
                String[] currentData = currentRecord.split(",");
                String qishu = currentData[0];
                String status = currentData[2];
                if (qishu.equals(String.valueOf(qihao))) {
                    if (status.equals("2")) {
                        //中奖退出
                        System.out.println("期号：" + qishu + "已中奖");
                        isSleep = true;
                        return;
                    } else if (status.equals("3")) {
                        isSleep = true;
                        System.out.println("期号：" + qishu + "未中奖");
                        return;
                    } else if (status.equals("1")) {
                        System.out.println("期号：" + qishu + "未开奖");
                        continue;
                    }
                } else {
                    System.out.println("期号：" + qishu + "未查询开奖记录");
                    continue;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取投注记录
     *
     * @return
     */
    @PostMapping("/jilu")
    public List<Map<String, String>> jilu(@RequestBody Map<String, String> map) {
        String cookie = map.get("cookie");
        String startTime = map.get("startTime");
        String endTime = map.get("endTime");
        if (StringUtils.isEmpty(startTime)) {
            startTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            startTime += " 03:00:00";
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        }
        String url = "https://www.aub06.com/httphandle/UcenterHandler.ashx";
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "1");
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        param.put("lotteryType", "0");
        param.put("page", 1);
        param.put("pageSize", 1000);
        param.put("orderState", "");
        param.put("orderNum", "");
        param.put("termNum", "");
        param.put("chaseType", "NaN");
        String resonse = null;
        try {
            resonse = HttpUtils.doPost(url, headers, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (resonse.equals("-1")|| resonse.equals("-100")) {
            System.out.println("登录超时");
            return new ArrayList<>();
        }
        resonse = resonse.replace("'", "\"");
        Map<String, Object> data = JsonUtils.fromJson(resonse, Map.class);
        List<Map<String, String>> record = (List<Map<String, String>>) data.get("record");
        cookieCache = cookie;

        return record;
    }
    @Scheduled(cron = "0 0/10 * * * ?")
    public void getJilu(){
        Map<String,String> param = new HashMap<>();
        param.put("cookie",cookieCache);
        System.out.println("刷新纪录");
        jilu(param);
    }

    /**
     * 获取历史开奖号
     *
     * @return
     */
    private static List<String> getKaijiang() {
        String url = "http://www.aub06.com/Bjpk10Chart.aspx?lotteryType=89";
        String resonse = HttpUtils.doGet(url, null, null);
        return parseHtml(resonse);
    }

    /**
     * 获取历史开奖号
     *
     * @return
     */
    @GetMapping("/login")
    public Map<String, String> login() throws Exception{
        String respCode = "";
        Map<String, String> respMap = new HashMap<>();
        try {
            String loginurl = "https://www.aub06.com/aobei/login.aspx";
            CloseableHttpResponse resp = HttpUtils.doGetSSLResponse(loginurl, null, null);
            Header[] headers = resp.getHeaders("Set-Cookie");
            String uid = headers[0].getValue().split(";")[0];
            String sissionId = headers[1].getValue().split(";")[0];
            String cookie = uid + ";" + sissionId;
            HttpEntity entity = resp.getEntity();
            if (entity == null) {
                respCode = "1";
                respMap.put("respCode", respCode);
                return respMap;
            }

//        cookie = "__cfduid=d78da6e786da48a75f87e0109b464756a1543198173; ASP.NET_SessionId=alxjwvnluww4iumunqtjuxq0";
            Map<String, String> header = new HashMap<>();
            header.put("Cookie", cookie);
            String code = "";
            String httpStr = HttpUtils.doGet("https://www.aub06.com/aobei/login.aspx", header, null);
            Document document = Jsoup.parse(httpStr);
            Element element = document.getElementById("captcha-img");
            String img = element.attr("src");
            img = img.replace("..", "");
            String imgurl = "https://www.aub06.com" + img;
            CloseableHttpResponse coderesp = HttpUtils.doGetSSLResponse(imgurl, header, null);
            HttpEntity codeEntity = coderesp.getEntity();
            File file = new File("C://Users//Administrator//Desktop//img//code.png");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(codeEntity.getContent(), outputStream);
            code = parseCodePic(file);


            String url = "https://www.aub06.com/httphandle/UserHandler.ashx";

            Map<String, Object> param = new HashMap<>();
            param.put("action", "1");
            param.put("type", "2");
            param.put("uname", "aaa111");
            param.put("pwd", "aaa111");
            param.put("code", code);
            String resonse = null;
            if (StringUtils.isNotEmpty(code)) {
                resonse = HttpUtils.doPost(url, header, param);
//                0|107.33|aaa111|93680|1|http://daili.aub88.com
                String[] res = resonse.split("\\|");
//                if (result == -2) {
//                    layer.alert("账号不存在！", 2);
//                } if (result == -4) {
//                    layer.alert("系统当前正在维护！", 2);
//                } else if (result == -5) {
//                    layer.alert("您的账户当前处于锁定状态，系统将于两个小时之后解除锁定！", 2);
//                } else if (result == -6) {
//                    layer.alert("您的账户当前处于锁定状态，系统将于两个小时之后解除锁定！", 2);
//                } else if (result == -7) {
//                    layer.alert("您的密码输入有误，您还有【" + limitTime + "】次输入机会，系统将自动锁定您的账户！", 2);
//                } else if (result == -101) {
//                    layer.alert("验证码已经失效", 2);
//                } else if (result == -102) {
//                    layer.alert("验证码输入有误", 2);
//                } else if (result == -103) {
//                    layer.alert("验证码不能为空", 2);
//                } else {
//                    layer.alert("请确认您输入的用户名或密码输入是否正确！", 2);
//                }
                if ("0".equals(res[0])) {
                    System.out.println("登陆成功");
                    balance = res[1];
                    respMap.put("balance", res[1]);
                    respCode = "0";
                    respMap.put("cookieCache", cookie);
                    cookieCache = cookie;
                } else {
                    respCode = "1";
                    System.out.println("登陆失败:" + resonse);
                }

            }
        } catch (Exception e) {
            respCode = "1";
            System.out.println("登陆失败");
            throw e;
        }
        respMap.put("respCode", respCode);
        return respMap;

    }

    /**
     * 获取历史开奖号
     *
     * @return
     */
    @GetMapping("/authCode")
    public Integer login(@RequestParam("authCode")String authCode)  {
        if("123456".equals(authCode)){
            return 1;
        }
        return 0;
    }

    private static String parseCodePic(File file) {
        Ocr.setUp(); // one time setup
        Ocr ocr = new Ocr(); // create a new OCR engine
        ocr.startEngine("eng", Ocr.SPEED_FASTEST); // English
        String s = ocr.recognize(new File[]{file}, Ocr.RECOGNIZE_TYPE_TEXT, Ocr.OUTPUT_FORMAT_PLAINTEXT);
        System.out.println("Result: " + s);
        System.out.println("图片文字为:" + s.replace(",", "").replace("i", "1").replace(" ", "").replace("'", "").replace("o", "0").replace("O", "0").replace("g", "6").replace("B", "8").replace("s", "5").replace("z", "2"));
        // ocr more images here ...
        ocr.stopEngine();
        return s.substring(0,4);
    }

    private static List<String> parseHtml(String resposne) {
        List<String> historyHao = new ArrayList<>();
        Document doc = Jsoup.parse(resposne);
        Element tbody = doc.getElementById("J-chart-content");
        Elements elements = tbody.getElementsByTag("tr");
        Element currentTR = elements.get(0);
        Elements elementsTds = currentTR.getElementsByTag("td");
        Long number = Long.parseLong(elementsTds.get(1).text());
        if (number > currentNumber) {
            currentNumber = number;
        } else {
            System.out.println("未开奖");
            return null;
        }
        for (Element elementsTr : elements) {
            Elements tds = elementsTr.getElementsByTag("td");
            String kaijianghao = tds.get(4).text();
            historyHao.add(kaijianghao);
        }

        return historyHao;
    }
}
