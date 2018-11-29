package com.lxpfunny.enterprise.controller;

import com.lxpfunny.enterprise.util.HttpUtils;
import com.lxpfunny.enterprise.util.JsonUtils;
import com.lxpfunny.enterprise.util.pk10;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by lixiaopeng on 2018/11/24.
 */
@RestController
@RequestMapping("/pk10")
public class Pk10Controller {
    static int currentNumber = 0;
    static int minute = 60000;
    static boolean isStop = false;
    static boolean isSleep = true;
    static String cookieCache = "";
    static Map<String, Thread> threadMap = new HashMap<>();
    @GetMapping("/getcookie")
    public Map<String,String> getCookie() {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("cookieCache", cookieCache);
        returnMap.put("isStop", !isStop ? "运行中":"停止");
        return returnMap;
    }
    @GetMapping("/start")
    public void start(@RequestParam("cookie")String cookie) {
        cookieCache = cookie;
        isStop = false;
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Random r = new Random();
                for (int i = 0;;i++) {

                    if(isStop){
                        System.out.println("线程中断退出");
                        return;
                    }
                    Date now = new Date();
                    int hours = now.getHours();
                    if(hours < 9 || hours >23){
                        continue;
                    }
                    if(i>0 && isSleep){
                        int m = r.nextInt(30 * minute) + (15 * minute);
                        try {
                            System.out.println("休息：" + (m/minute)+"分钟");
                            Thread.sleep(m);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    touzhu(cookie);

                }
            }
        });
        threadMap.put(t.getName(), t);
        t.start();
    }
    @GetMapping("/stop")
    public void stop() {
        Set<String> set = threadMap.keySet();
        for (String s : set) {
            Thread t = threadMap.get(s);
            t.interrupt();
            t.stop();
            isStop = true;
        }
        threadMap.clear();
    }

    public static void main(String[] args) {

        getKaijiang();
    }

    private static void touzhu(String cookie) {


        String xiadanhao = "";
        for (int i = 0; i < 10; i++) {
            try {
                if(i>0){
                    Thread.sleep(2 * minute);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<String> historyKaijiang = getKaijiang();
            if (historyKaijiang == null) {
                System.out.println("没有最新开奖");
                continue;
            }
            xiadanhao = pk10.createHaoma(historyKaijiang);
            if (StringUtils.isEmpty(xiadanhao)) {
                System.out.println("没有符合投注条件");
                continue;
            } else {
                break;
            }
        }
        int qihao = currentNumber + 1;
        if (StringUtils.isNotBlank(xiadanhao)) {
            xiadan("3", "1", cookie, xiadanhao, qihao);
        }

    }

    /**
     * 第一次下单 倍数1
     *
     * @param qian
     * @param bei
     * @param cookie
     * @param xiadanhao
     * @param qihao
     */
    private static void xiadan(String qian, String bei, String cookie, String xiadanhao, int qihao) {
        //        <ol><item><ms>3</ms><bs>1</bs><num>|||2,3,5,6,8,9,10||||||</num><pid>11</pid></item></ol>
        qian = "3";
        bei = "1";
        if(StringUtils.isEmpty(cookie)){
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String template = "<ol><item><ms>qian</ms><bs>bei</bs><num>haoma</num><pid>11</pid></item></ol>";
        String url = "https://www.aob10.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "3");
        param.put("t", "6");
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
                getMyrecord(qihao, xiadanhao, cookie);
            } else if(String.valueOf(map.get("code")).equals("-100")){
                System.out.println("登录超时");
                isStop = true;
            }else if(String.valueOf(map.get("code")).equals("-12")) {
                System.out.println("操作失败，您的余额不足！");
                isStop = true;
            } else if(String.valueOf(map.get("code")).equals("-15") || String.valueOf(map.get("code")).equals("-14") ) {
                System.out.println("操作失败，此期已经停止销售，请选择当前可购买的期号！");
                isSleep = false;
                return;
            }else if(String.valueOf(map.get("code")).equals("-1")){
                System.out.println("下单失败重新下单:" + response);
                isSleep = false;
                return;
            }else {
                System.out.println("下单失败:" + response);
                isStop = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void getMyrecord(int qihao, String xiadanhao, String cookie) {

        if(StringUtils.isEmpty(cookie)){
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";
        }
        String url = "https://www.aob10.com/httphandle/LotteryHandler.ashx";
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
                System.out.println("期号：" + qishu +"查询中奖状态");
                if (qishu.equals(String.valueOf(qihao))) {
                    if (status.equals("2")) {
                        //中奖退出
                        System.out.println("期号：" + qishu + "已中奖");
                        isSleep = true;
                        return;
                    } else if (status.equals("3")) {
                        //未中奖 追单一次
                        System.out.println("期号：" + qishu + "未中奖,第一次加倍");
                        xiadan2("", "", cookie, xiadanhao, qihao + 1);
                    }else if(status.equals("1")){
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
        qian = "3";
        bei = "3";
        if(StringUtils.isEmpty(cookie)){
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String template = "<ol><item><ms>qian</ms><bs>bei</bs><num>haoma</num><pid>11</pid></item></ol>";
        String url = "https://www.aob10.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "3");
        param.put("t", "6");
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
            } else if(String.valueOf(map.get("code")).equals("-100")){
                System.out.println("登录超时");
                isStop = true;
            }else if(String.valueOf(map.get("code")).equals("-12")) {
                System.out.println("操作失败，您的余额不足！");
                isStop = true;
            } else if(String.valueOf(map.get("code")).equals("-15") || String.valueOf(map.get("code")).equals("-14") ) {
                System.out.println("操作失败，此期已经停止销售，请选择当前可购买的期号！");
                isSleep = false;
                return;
            }else if(String.valueOf(map.get("code")).equals("-1")){
                System.out.println("下单失败重新下单:" + response);
                isSleep = false;
                return;
            }else {
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

        if(StringUtils.isEmpty(cookie)){
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String url = "https://www.aob10.com/httphandle/LotteryHandler.ashx";
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
                        xiadan3("", "", cookie, xiadanhao, qihao + 1);
                    }else if(status.equals("1")){
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
    private static void xiadan3(String qian, String bei, String cookie, String xiadanhao, int qihao) {
        //        <ol><item><ms>3</ms><bs>1</bs><num>|||2,3,5,6,8,9,10||||||</num><pid>11</pid></item></ol>
        qian = "3";
        bei = "8";
        if(StringUtils.isEmpty(cookie)){
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String template = "<ol><item><ms>qian</ms><bs>bei</bs><num>haoma</num><pid>11</pid></item></ol>";
        String url = "https://www.aob10.com/httphandle/LotteryHandler.ashx";
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "3");
        param.put("t", "6");
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
                getMyrecord3(qihao, xiadanhao, cookie);
            } else if(String.valueOf(map.get("code")).equals("-100")){
                System.out.println("登录超时");
                isStop = true;
            }else if(String.valueOf(map.get("code")).equals("-12")) {
                System.out.println("操作失败，您的余额不足！");
                isStop = true;
            } else if(String.valueOf(map.get("code")).equals("-15") || String.valueOf(map.get("code")).equals("-14") ) {
                System.out.println("操作失败，此期已经停止销售，请选择当前可购买的期号！");
                isSleep = false;
                return;
            }else if(String.valueOf(map.get("code")).equals("-1")){
                System.out.println("下单失败重新下单:" + response);
                isSleep = false;
                return;
            }else {
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

        if(StringUtils.isEmpty(cookie)){
            cookie = "__cfduid=db75aec07a6d7c6d2cd3b0d4ba11a76521543043842; ASP.NET_SessionId=1mvipwwoysdoojy42qqznd2m";

        }
        String url = "https://www.aob10.com/httphandle/LotteryHandler.ashx";
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
                    }else if(status.equals("1")){
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
    public List<Map<String, String>> start(@RequestBody Map<String,String> map) {
        String cookie = map.get("cookie");
        String startTime = map.get("startTime");
        String endTime = map.get("endTime");
        if(StringUtils.isEmpty(startTime)){
            startTime = DateFormatUtils.format(new Date(),"yyyy-MM-dd");
            startTime += " 03:00:00";
        }
        if(StringUtils.isEmpty(endTime)){
            endTime = DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        }
        String url = "https://www.aob10.com/httphandle/UcenterHandler.ashx";
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", cookie);
        Map<String, Object> param = new HashMap<>();
        param.put("action", "1");
        param.put("startTime",startTime);
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
            resonse = HttpUtils.doPost(url,  headers,param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(resonse.equals("-1")){
            throw new RuntimeException("登陆超时");
        }
        resonse = resonse.replace("'", "\"");
        Map<String, Object> data = JsonUtils.fromJson(resonse, Map.class);
        List<Map<String, String>> record = (List<Map<String, String>>) data.get("record");
        cookieCache = cookie;

        return record;
    }
    /**
     * 获取历史开奖号
     *
     * @return
     */
    private static List<String> getKaijiang() {
        String url = "https://www.aob10.com/Bjpk10Chart.aspx";
        String resonse = HttpUtils.doGet(url, null, null);
        return parseHtml(resonse);
    }

    private static List<String> parseHtml(String resposne) {
        List<String> historyHao = new ArrayList<>();
        Document doc = Jsoup.parse(resposne);
        Element tbody = doc.getElementById("J-chart-content");
        Elements elements = tbody.getElementsByTag("tr");
        Element currentTR = elements.get(0);
        Elements elementsTds = currentTR.getElementsByTag("td");
        int number = Integer.parseInt(elementsTds.get(1).text());
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
