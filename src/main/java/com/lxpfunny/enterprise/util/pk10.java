package com.lxpfunny.enterprise.util;

import com.lxpfunny.enterprise.controller.Pk10Controller;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @author lixiaopeng
 * @date 2018/11/22 10:59
 */
public class pk10 {

    public static void main(String[] args) {
//        File file = new File("D:\\pk1.txt");
//        try {
//            FileInputStream inputStream = new FileInputStream(file);
//            List<String> datas = IOUtils.readLines(inputStream);
////            plan1(datas);
//            for (int i = 0; i < 10000; i++) {
////                plan2(datas);
//                plan1(datas);
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

    private static void plan2(List<String> datas) {
        int zhong = 0;
        int gua = 0;
        Random random = new Random();
        List<String> result = new ArrayList<>();
        for (int i = datas.size() - 2; i >= 0; i--) {
            List<String> currentData = Arrays.asList(datas.get(i).trim().split(" "));
            List<String> pre1 = Arrays.asList(datas.get(i + 1).trim().split(" "));
            Integer randomValue = random.nextInt(10);
            for (int j = 0; j < currentData.size(); j++) {
                if (randomValue == j) {
                    List<Integer> haoma = createHaoma(Integer.parseInt(pre1.get(j)));
                    if (haoma.contains(Integer.parseInt(currentData.get(j)))) {
//                            System.out.println((i+1)+"期第"+(j+1)+"名:中");
                        result.add("中");
                        zhong++;
                    } else {
                        result.add("挂");
//                            System.out.println((i+1)+"期第"+(j+1)+"名:挂");
                        gua++;
                    }
                    break;
                }

            }

        }
        System.out.println("中：" + zhong + "挂：" + gua);
        String r = JsonUtils.toJson(result);
        r = r.replace("[", "");
        r = r.replace("]", "");
        r = r.replace("\"", "");
        r = r.replace(",", "");
        String[] rs = r.split("中");
        int guaCount = 0;
        for (int i = 0; i < rs.length; i++) {
            if (!StringUtils.isEmpty(rs[i])) {
                if (rs[i].length() > guaCount) {
                    guaCount = rs[i].length();
                }
                if (guaCount >= 9) {
                    System.out.println("fff");
                }
            }
        }
        System.out.println("最大连挂:" + guaCount);
        System.out.println("=================");
    }

    private static void plan1(List<String> datas) {
        int zhong = 0;
        int gua = 0;
        Map<Integer, List<Integer>> haomaM = new HashMap<>();
        List<String> result = new ArrayList<>();
        for (int i = datas.size() - 3; i >= 0; i--) {
            List<String> currentData = Arrays.asList(datas.get(i).trim().split(" "));
            List<String> pre1 = Arrays.asList(datas.get(i + 1).trim().split(" "));
            List<String> pre2 = Arrays.asList(datas.get(i + 2).trim().split(" "));
//                List<String> pre3 = Arrays.asList(datas.get(i+3).trim().split(" "));
//                List<String> pre4 = Arrays.asList(datas.get(i+4).trim().split(" "));
            haomaM.clear();
            for (int j = 0; j < pre1.size(); j++) {
                if (pre1.get(j).equals(pre2.get(j))) {
//                        if(checkAbs(Integer.parseInt(pre1.get(j)),Integer.parseInt(pre2.get(j)))){
//                        if(checkAbs(Integer.parseInt(pre1.get(j)),Integer.parseInt(pre2.get(j))) && checkAbs(Integer.parseInt(pre2.get(j)),Integer.parseInt(pre3.get(j)))){
                    List<Integer> haoma = createHaoma(Integer.parseInt(pre1.get(j)));

                    haomaM.put(j, haoma);
//                        }
                }
            }
            for (int j = 0; j < currentData.size(); j++) {
                if (haomaM.size() == 0) {
                    break;
                }
                Integer[] keys = haomaM.keySet().toArray(new Integer[0]);
                Random random = new Random();
                Integer randomKey = keys[random.nextInt(keys.length)];
                if (randomKey == j) {
                    List<Integer> haoma = haomaM.get(j);
                    if (haoma != null) {
                        if (haoma.contains(Integer.parseInt(currentData.get(j)))) {
                            result.add("中");
//                            System.out.println((i+1)+"期第"+(j+1)+"名:中");
                            zhong++;
                        } else {
                            result.add("挂");
//                            System.out.println((i+1)+"期第"+(j+1)+"名:挂");
                            gua++;
                        }

                    }
                    break;
                }


            }

        }
        System.out.println("中：" + zhong + "挂：" + gua);
        String r = JsonUtils.toJson(result);
        r = r.replace("[", "");
        r = r.replace("]", "");
        r = r.replace("\"", "");
        r = r.replace(",", "");
        String[] rs = r.split("中");
        int guaCount = 0;
        for (int i = 0; i < rs.length; i++) {
            if (!StringUtils.isEmpty(rs[i])) {
                if (rs[i].length() > guaCount) {
                    guaCount = rs[i].length();
                }
                if (guaCount >= 9) {
                    System.out.println("fff");
                }
            }
        }
        System.out.println("最大连挂:" + guaCount);
    }

    private static boolean checkAbs(Integer pre1, Integer pre2) {
        if ((pre2 == 1 && pre1 == 8)
                || (pre2 == 10 && pre1 == 3)
                || (pre2 == 2 && pre1 == 9)
                || (pre2 == 3 && pre1 == 10)
                || (pre2 == 8 && pre1 == 1)
                || (pre2 == 9 && pre1 == 2)
                || (Math.abs(pre2 - pre1) == 3)) {
            return true;
        }
        return false;
    }

    private static List<Integer> createHaoma(Integer pre) {
        List<Integer> haoma = new ArrayList<>();
        int h = pre - 1;
        int h1 = pre - 2;
        int h2 = pre + 1;
        int h3 = pre + 2;
        if (pre == 1) {
            h = 10;
            h1 = 9;
        } else if (pre == 2) {
            h1 = 10;
        }
        if (pre == 10) {
            h2 = 1;
            h3 = 2;
        } else if (pre == 9) {
            h3 = 1;
        }
        int d = 0;
        if (pre > 5) {
            d = pre - 5;
        } else {
            d = pre + 5;
        }
        int d1 = d - 1;
        int d2 = d + 1;
        if (d == 1) {
            d1 = 10;
        } else if (d == 10) {
            d2 = 1;
        }
        haoma.add(h);
//        haoma.add(h1);
        haoma.add(h2);
//        haoma.add(h3);
//        haoma.add(pre);
        haoma.add(d);
        haoma.add(d1);
        haoma.add(d2);
//        haoma.add(2);
//        haoma.add(3);
//        haoma.add(4);
//        haoma.add(5);
//        haoma.add(7);
//        haoma.add(6);
//        haoma.add(8);
        return haoma;
    }


    public static String createHaoma1(List<String> datas,Integer mingci) {
        String num = "1|1|1|1|1|1|1|1|1|";
//        if(true){
//            return num;
//
//        }
        Random random = new Random();
        Integer randomKey = mingci;
        if(mingci == null){
            randomKey= random.nextInt(10);
        }
        Pk10Controller.mingci = randomKey;
        List<String> pre1 = Arrays.asList(datas.get(0).trim().split(","));

        List<Integer> haoma = createHaoma(Integer.parseInt(pre1.get(randomKey)));

        String qian = "";
        String hou = "";
        for (int i = 0; i <randomKey ; i++) {
            qian += "|";
        }
        for (int i = 1; i <10-randomKey ; i++) {
            hou += "|";
        }
        if (haoma != null) {
//            |||2,3,5,6,8,9,10||||||
             num = qian+JsonUtils.toJson(haoma).substring(1,JsonUtils.toJson(haoma).length()-1)+hou;
           System.out.println("第"+(randomKey+1)+"名投注号码:"+num);
            return num;
        }
        return num;

    }

    public static String createHaoma(List<String> datas) {
        String num = "1|1|1|1|1|1|1|1|1|";

        Map<Integer, List<Integer>> haomaM = new HashMap<>();
        List<String> pre1 = Arrays.asList(datas.get(0).trim().split(","));
        List<String> pre2 = Arrays.asList(datas.get(1).trim().split(","));
        List<String> pre3 = Arrays.asList(datas.get(2).trim().split(","));
        for (int j = 0; j < pre1.size(); j++) {
            if (pre1.get(j).equals(pre2.get(j)) && pre1.get(j).equals(pre3.get(j))) {
                List<Integer> haoma = createHaoma(Integer.parseInt(pre1.get(j)));
                haomaM.put(j, haoma);
            }
        }
        if(haomaM.size() ==0){
            return null;
        }
        Integer[] keys = haomaM.keySet().toArray(new Integer[0]);
        Random random = new Random();
        Integer randomKey = keys[random.nextInt(keys.length)];
        List<Integer> haoma = haomaM.get(randomKey);
        String qian = "";
        String hou = "";
        for (int i = 0; i <randomKey ; i++) {
            qian += "|";
        }
        for (int i = 1; i <10-randomKey ; i++) {
            hou += "|";
        }
        if (haoma != null) {
//            |||2,3,5,6,8,9,10||||||
            num = qian+JsonUtils.toJson(haoma).substring(1,JsonUtils.toJson(haoma).length()-1)+hou;
            System.out.println("第"+(randomKey+1)+"名投注号码:"+num);
            return num;
        }
        return num;

    }

}
