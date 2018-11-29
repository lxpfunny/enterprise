package com.lxpfunny.enterprise.util;

import com.google.common.collect.Maps;
import com.lxpfunny.enterprise.model.DetailModel;
import com.lxpfunny.enterprise.model.TotalModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lixiaopeng on 2017/7/22.
 */
public class ExportExcelUtil {

    Logger logger = LoggerFactory.getLogger(ExportExcelUtil.class);
    /**
     * 描述：根据文件路径获取项目中的文件
     * @param fileDir 文件路径
     * @return
     * @throws Exception
     */
    public File getExcelDemoFile(String fileDir) throws Exception{
        String classDir = null;
        File file = null;

//        classDir = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        classDir = ExportExcelUtil.class.getClassLoader().getResource("/").getPath();
//        fileBaseDir = classDir.substring(0, classDir.lastIndexOf("classes"));
        logger.info(classDir + fileDir);
        file = new File(classDir+fileDir);
        if(!file.exists()){
            throw new Exception("模板文件不存在！");
        }
        return file;
    }

    /**
     * 账单明细写入excel
     * @param models
     * @return
     * @throws Exception
     */
    public Workbook writeNewExcel(String template,List<DetailModel> models) throws Exception{
        if(models.size() == 0){
            logger.error("没有可以导出的数据！");
            return null;
        }
        Workbook wb = new HSSFWorkbook(ExportExcelUtil.class.getResourceAsStream(template));    //获取工作薄
        Sheet sheet = wb.getSheetAt(1);
        Sheet totlesheet = wb.getSheetAt(0);
        exportDetail(wb, sheet, models);//导出明细
        try{
            exportTotals(wb, totlesheet, convertTotal(models));//导出汇总
        }catch (Exception e){
            logger.error("导出汇总失败",e);
        }

        return wb;
    }

    private List<TotalModel> convertTotal(List<DetailModel> models) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Map<String, TotalModel> totalMap = Maps.newLinkedHashMap();
        for (DetailModel model : models) {
            String key = model.getTypeName() + "|" + model.getSize();
            TotalModel modelData = totalMap.get(key);
            Integer day=null;
            try {
                Date date = format.parse(model.getDeliveryDate());
                 day = date.getDate();

            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (modelData != null) {
                Map<Integer, Integer> sum = modelData.getSum();
                if(sum.get(day) != null) {
                    sum.put(day, sum.get(day) + Integer.parseInt(model.getSum()));
                }else {
                    sum.put(day, Integer.parseInt(model.getSum()));
                }

            } else {
                TotalModel totalModel = new TotalModel();
                totalModel.setTypeName(model.getTypeName());
                totalModel.setDeliveryDate(model.getDeliveryDate());
                totalModel.setSize(model.getSize());
                Map<Integer, Integer> sum = new HashMap<Integer, Integer>();
                sum.put(day,Integer.parseInt(model.getSum()));
                totalModel.setSum(sum);
                totalModel.setProperty("成品");
                totalMap.put(key, totalModel);
            }
            String plusKey = model.getTypeName() + " 加P" + "|" + model.getSize();
            TotalModel plusData = totalMap.get(plusKey);
            if (StringUtils.isNotBlank(model.getpPlus()) && !"0".equals(model.getpPlus())) {
                if (plusData != null) {
                    Map<Integer, Integer> sum = plusData.getSum();
                    if(sum.get(day) != null) {
                        sum.put(day, sum.get(day) + Integer.parseInt(model.getpPlus()));
                    }else {
                        sum.put(day, Integer.parseInt(model.getpPlus()));
                    }
                } else {
                    TotalModel totalModel = new TotalModel();
                    totalModel.setTypeName(model.getTypeName() + " 加P");
                    totalModel.setDeliveryDate(model.getDeliveryDate());
                    totalModel.setSize(model.getSize());
                    Map<Integer, Integer> sum = new HashMap<Integer, Integer>();
                    sum.put(day, Integer.parseInt(model.getpPlus()));
                    totalModel.setSum(sum);
                    totalModel.setProperty("加工费");
                    totalMap.put(plusKey, totalModel);
                }
            }

        }
        Set<Map.Entry<String,TotalModel>> entry = totalMap.entrySet();
        List<TotalModel> list = new LinkedList<>();
        for (Map.Entry<String, TotalModel> stringTotalModelEntry : entry) {
            list.add(stringTotalModelEntry.getValue());
        }
        return list;
    }

    private void exportDetail(Workbook wb,Sheet sheet,List<DetailModel> models){
        Row row = null;
        Cell cell = null;
        //循环插入数据
        int lastRow = sheet.getLastRowNum()+1;    //插入数据的数据ROW
        CellStyle cs = setErrorRowStyle(wb);    //Excel错误行样式
        DecimalFormat format = new DecimalFormat("0.00");
        for (int i = 0; i < models.size(); i++) {
            row = sheet.createRow(i+4); //创建新的ROW，用于数据插入
            DetailModel model = models.get(i);

            //Cell赋值开始
            cell = row.createCell(0);
            cell.setCellValue(model.getReceiveDate());


            cell = row.createCell(1);
            cell.setCellValue(model.getTradeNo());

            cell = row.createCell(2);
            cell.setCellValue(model.getTypeName());


            cell = row.createCell(3);
            cell.setCellValue(model.getSize());

            cell = row.createCell(4);
            cell.setCellValue(model.getProperty());


            cell = row.createCell(5);
            if(StringUtils.isNotBlank(model.getpTotal())) {
                cell.setCellValue(Integer.parseInt(model.getpTotal()));

            }else{
                cell.setCellValue("");
            }

            cell = row.createCell(6);
            if(StringUtils.isNotBlank(model.getpPlus())) {
                cell.setCellValue(Integer.parseInt(model.getpPlus()));

            }else{
                cell.setCellValue("");
            }
            cell = row.createCell(7);
            cell.setCellValue(model.getUnit());
            cell = row.createCell(8);
            if(StringUtils.isNotBlank(model.getSum())){
                cell.setCellValue(Integer.parseInt(model.getSum()));
            }

            cell = row.createCell(9);
            if(StringUtils.isNotBlank(model.getPrice())){
                cell.setCellValue(Double.parseDouble(model.getPrice()));
            }

            cell = row.createCell(10);
            if(StringUtils.isNotBlank(model.getPrice())){
                cell.setCellValue(Double.parseDouble(format.format(new BigDecimal(model.getPrice()).multiply(new BigDecimal(model.getSum())))));

            }

            cell = row.createCell(11);
            cell.setCellValue(model.getCustomerName());

            cell = row.createCell(12);
            cell.setCellValue(model.getDeliveryDate());

            if (StringUtils.isNotBlank(model.getMemo())) {
                cell = row.createCell(13);
                cell.setCellValue(model.getMemo());
            }

            if(model.getType() == 0){//如果有单价计算错误，或者导入错误，把整行标红
                cell.setCellStyle(cs);
            }
        }
    }
    private void exportTotals(Workbook wb,Sheet sheet,List<TotalModel> models){
        Row row = null;
        Cell cell = null;
        //循环插入数据
        int lastRow = sheet.getLastRowNum()+1;    //插入数据的数据ROW
        CellStyle cs = setErrorRowStyle(wb);    //Excel错误行样式
        DecimalFormat format = new DecimalFormat("0.00");
        for (int i = 0; i < models.size(); i++) {
            row = sheet.createRow(i + 2); //创建新的ROW，用于数据插入
            TotalModel model = models.get(i);

            //Cell赋值开始
            cell = row.createCell(0);
            cell.setCellValue(model.getTypeName());
            cell = row.createCell(1);
            cell.setCellValue(model.getSize());

            cell = row.createCell(2);
            cell.setCellValue(model.getProperty());

//            cell = row.createCell(3);//单价
//            cell.setCellValue(model.getProperty());

            Map<Integer,Integer> sum = model.getSum();
            Set<Map.Entry<Integer,Integer>> entry = sum.entrySet();
            for (Map.Entry<Integer, Integer> integerIntegerEntry : entry) {
                Integer key = integerIntegerEntry.getKey();
                Integer value = integerIntegerEntry.getValue();
                cell = row.createCell(3+key);
                cell.setCellValue(value);
            }
        }
    }

    /**
     * 描述：设置简单的Cell样式
     * @return
     */
    public  CellStyle setSimpleCellStyle(Workbook wb){
        CellStyle cs = wb.createCellStyle();
        cs.setBorderBottom(CellStyle.BORDER_THIN); //下边框
        cs.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        cs.setBorderTop(CellStyle.BORDER_THIN);//上边框
        cs.setBorderRight(CellStyle.BORDER_THIN);//右边框

        cs.setAlignment(CellStyle.ALIGN_CENTER); // 居中

        return cs;
    }

    /**
     * 描述：设置简单的Cell样式
     * @return
     */
    public  CellStyle setErrorRowStyle(Workbook wb){
        HSSFCellStyle style = (HSSFCellStyle) wb.createCellStyle();
        style
                .setFillForegroundColor(new HSSFColor.RED()
                        .getIndex());
        style
                .setFillBackgroundColor(new HSSFColor.RED()
                        .getIndex());
        style.setFillPattern(HSSFCellStyle.SPARSE_DOTS);

        return style;
    }

}
