package com.lxpfunny.enterprise.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lxpfunny.enterprise.model.DetailModel;
import com.lxpfunny.enterprise.model.QuotationModel;
import com.lxpfunny.enterprise.util.BeanToMapUtil;
import com.lxpfunny.enterprise.util.ExportExcelUtil;
import com.lxpfunny.enterprise.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lixiaopeng on 2017/7/22.
 */
@RestController
@RequestMapping("/excel")
public class ExcelController {
    Logger logger = LoggerFactory.getLogger(ExcelController.class);

    /**
     * 亿匠
     * @param file
     * @param response
     * @param request
     * @return
     */
    @RequestMapping("/upload")
    @ResponseBody
    public Map<String,Object> upload(@RequestParam("file")MultipartFile file,HttpServletResponse response,HttpServletRequest request){
        Map<String,Object> returnMap = new HashMap<>();
        String exportFileName = request.getParameter("fileName");
        String quotation = request.getParameter("quotation");//报价
        String fileName = file.getOriginalFilename();
        List<DetailModel> models = Lists.newArrayList();
        logger.info("上传excel：{}",fileName);
        if(!validateExcel(fileName)){
            logger.error("请上传正确的excel格式");
            returnMap.put("errorMsg", "请上传正确的excel格式");
            return returnMap;
        }
        Workbook workbook = null;
        try {
            InputStream is  = file.getInputStream();

            if(isExcel2003(fileName)) {
                HSSFWorkbook   hssfWorkbook = new HSSFWorkbook(is);
                models = readExcelValue(hssfWorkbook, quotation);
            }else {
                //TODO
                workbook = new XSSFWorkbook(is);
            }
//           models = readExcelValue(workbook);
        } catch (IOException e) {
            logger.error("导入excel失败！",e);
            returnMap.put("errorMsg", "导入excel失败！");
        }
        try {
            exportExcel(response, exportFileName, models);
        } catch (Exception e) {
            logger.error("导出excel失败！",e);
            returnMap.put("errorMsg", "导出excel失败！");
        }

        logger.info("上传excel");
        return returnMap;
    }


    /**
     * 伯昌
     * @param file
     * @param response
     * @param request
     * @return
     */
    @RequestMapping("/uploadBochang")
    @ResponseBody
    public Map<String,Object> uploadBochang(@RequestParam("file")MultipartFile file,HttpServletResponse response,HttpServletRequest request){
        Map<String,Object> returnMap = new HashMap<>();
        String exportFileName = request.getParameter("fileName");
        String fileName = file.getOriginalFilename();
        List<DetailModel> models = Lists.newArrayList();
        logger.info("上传excel：{}",fileName);
        if(!validateExcel(fileName)){
            logger.error("请上传正确的excel格式");
            returnMap.put("errorMsg", "请上传正确的excel格式");
            return returnMap;
        }
        Workbook workbook = null;
        try {
            InputStream is  = file.getInputStream();

            if(isExcel2003(fileName)) {
                HSSFWorkbook   hssfWorkbook = new HSSFWorkbook(is);
                models = readBochangExcel(hssfWorkbook);
            }
//            else {
//                //TODO
//                workbook = new XSSFWorkbook(is);
//            }
        } catch (IOException e) {
            logger.error("导入excel失败！",e);
            returnMap.put("errorMsg", "导入excel失败！");
        }
        try {
            exportExcel(response, exportFileName, models);
        } catch (Exception e) {
            logger.error("导出excel失败！",e);
            returnMap.put("errorMsg", "导出excel失败！");
        }

        logger.info("上传excel");
        return returnMap;
    }


    @RequestMapping("/uploadCover")
    @ResponseBody
    public Map<String,Object> uploadCover(@RequestParam("coverFile")MultipartFile file,HttpServletResponse response,HttpServletRequest request){
        Map<String,Object> returnMap = new HashMap<>();
        String exportFileName = request.getParameter("coverName");
        String fileName = file.getOriginalFilename();
        List<DetailModel> models = Lists.newArrayList();
        logger.info("上传excel：{}",fileName);
        if(!validateExcel(fileName)){
            logger.error("请上传正确的excel格式");
            returnMap.put("errorMsg", "请上传正确的excel格式");
            return returnMap;
        }
        Workbook workbook = null;
        try {
            InputStream is  = file.getInputStream();

            if(isExcel2003(fileName)) {
                HSSFWorkbook   hssfWorkbook = new HSSFWorkbook(is);
                models = readCoverExcel(hssfWorkbook);
            }else {
                //TODO
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            logger.error("导入excel失败！",e);
            returnMap.put("errorMsg", "导入excel失败！");
        }
        try {
            exportExcel(response, exportFileName, models);
        } catch (Exception e) {
            logger.error("导出excel失败！",e);
            returnMap.put("errorMsg", "导出excel失败！");
        }

        logger.info("上传excel");
        return returnMap;
    }


    /**
     * 读取亿匠导入excel
     * @param book
     * @param quotation
     * @return
     */
    private List<DetailModel> readExcelValue(HSSFWorkbook book,String quotation) {

        Map<String,Map<String,String>> modelMaps = getBaseData(quotation);
        HSSFSheet sheet = book.getSheetAt(0);
        //行数
        int rowSize = sheet.getPhysicalNumberOfRows();

        int totalCells = 0;
        if (rowSize >= 2 && sheet.getRow(1) != null) {
            totalCells = sheet.getRow(1).getPhysicalNumberOfCells();
        }

        HSSFRow titleRow = sheet.getRow(0);
        List<String> titleList = new ArrayList<>();
        for (int j = 0; j < totalCells; j++) {
            titleList.add(titleRow.getCell(j).getStringCellValue());
        }
        logger.info("导入excel  title：{}", JsonUtils.toJson(titleList));
        List<DetailModel> models = Lists.newArrayList();
        String deliveryDate = "";//发货日期
        String receiveDate = "";//收货日期
        String tradeNo = "";//送货编号

        DecimalFormat format = new DecimalFormat("0.00");
        for (int i = 1; i < rowSize; i++) {
            DetailModel model = new DetailModel();
            try {
                HSSFRow row = sheet.getRow(i);
                if (checkRowNull(row) >= 11) {//判断excel末尾
                    break;
                }
                HSSFCell c = row.getCell(2);
                HSSFCell Cell0 = row.getCell(0);//发货单元格
                String cellValue = "";

                if (StringUtils.isBlank(c.getStringCellValue())) {//发件行
                    cellValue = Cell0.getStringCellValue();
                    String[] s = cellValue.split(" ");
                    deliveryDate = s[0];
                    HSSFCell cell1 = row.getCell(1);
                    tradeNo = cell1.getStringCellValue();
                    continue;
                }

                cellValue = Cell0.getStringCellValue();
                if (StringUtils.isNotBlank(cellValue)) {
                    String[] s = cellValue.split(" ");
                    receiveDate = s[0];
                }


                HSSFCell cell2 = row.getCell(2);
                String typeName = cell2.getStringCellValue();//品名

                HSSFCell cell3 = row.getCell(3);
                String size = "";
                String unit = "P";//单位
                if(cell3 != null){
                     size = cell3.getStringCellValue();//尺寸规格
                    if(StringUtils.isEmpty(size.trim())){
                        unit = "只";
                    }
                }

                HSSFCell cell4 = row.getCell(4);
                String pTotal = getCellValue(cell4);//总p数

                String pPlus = "";
                if (StringUtils.isNotBlank(pTotal.trim())) {
                    pPlus = String.valueOf(Integer.parseInt(pTotal) - 10);//加p数
                }

                HSSFCell cell7 = row.getCell(7);
                String sum = getCellValue(cell7);//数量

                HSSFCell cell8 = row.getCell(8);
                String price = getCellValue(cell8);//单价
                //TODO 根据品名计算单价  用Bigdecimal计算
                if(modelMaps != null){
                    Map<String, String> map = modelMaps.get(typeName);
                    if (map != null) {
                        QuotationModel model1 = (QuotationModel) BeanToMapUtil.convertMap(QuotationModel.class, map);
                        if(model1 != null){
                            //单价 = 成品报价  + 加P数 * 换P单价
                            String finishedPrice = model1.getFinishedPrice();//成品报价
                            int pPrice = 0;
                            if (StringUtils.isNotBlank(pPlus) && StringUtils.isNotBlank(model1.getpPlusPrice())) {//加P数
                                pPrice = Integer.parseInt(pPlus) * Integer.parseInt(model1.getpPlusPrice());
                            }
                            price = String.valueOf(Integer.parseInt(finishedPrice) + pPrice);
                        }


                    } else {
                        // 根据品名 没有找到对应的 报价， 给整行 表红提示
                        model.setType(0);
                    }
                }else{
                    model.setType(0);
                }



                //TODO   用Bigdecimal计算
                String cash = String.valueOf(Integer.parseInt(price) * Integer.parseInt(sum));//应收金额

                HSSFCell cell10 = row.getCell(10);
                String customerName = cell10.getStringCellValue();//顾客姓名
                if(StringUtils.isNotBlank(customerName) && customerName.indexOf("样册")>=0) {
                    if(StringUtils.isNotBlank(price) && StringUtils.isNotBlank(cash)){
                        BigDecimal priceB = new BigDecimal(price);
                        BigDecimal cashB = new BigDecimal(cash);
                        price = format.format(priceB.multiply(new BigDecimal(0.6)));
                        cash = format.format(cashB.multiply(new BigDecimal(0.6)));
                    }

                }
                if(StringUtils.isNotBlank(customerName)){
                    if(customerName.indexOf("返工")>=0 || customerName.indexOf("计费")>=0){
                        model.setType(0);
                    }
                }



                model.setDeliveryDate(deliveryDate);
                model.setReceiveDate(receiveDate);
                model.setTypeName(typeName);
                model.setSize(size);
                model.setCash(cash);
                model.setpPlus(pPlus);
                model.setpTotal(String.valueOf(pTotal));
                model.setPrice(String.valueOf(price));
                model.setCustomerName(customerName);
                model.setProperty("成品");
                model.setSum(String.valueOf(sum));
                model.setTradeNo(tradeNo);
                model.setUnit(unit);
            } catch (Exception e) {
                model.setType(0);
                logger.error("解析excel失败！失败行数：{}", i + 1, e);
            }
            models.add(model);

        }
        return models;
    }

    /**
     * 读取伯昌导入excel
     * @param book
     * @return
     */
    private List<DetailModel> readBochangExcel(HSSFWorkbook book) {

        HSSFSheet sheet = book.getSheetAt(0);
        //行数
        int rowSize = sheet.getPhysicalNumberOfRows();

        List<DetailModel> models = Lists.newArrayList();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        for (int i = 0; i < rowSize; i++) {
            DetailModel model = new DetailModel();
            try {
                HSSFRow row = sheet.getRow(i);
                if(row == null){
                    continue;
                }
                if (checkRowNull(row) >= 11) {//判断excel末尾
                    break;
                }

                HSSFCell cell2 = row.getCell(2);
               Date  receiveDate = cell2.getDateCellValue();//接件日期

                HSSFCell cell10 = row.getCell(10);
                String[] cs = cell10.getStringCellValue().split(" ");
                String typeName = cs[1];
                String unit = "个";
                String size = cs[0];
                String sum = "1";

                if(typeName.equals("单片")) {
                    unit = "张";
                }else if(typeName.equals("照片墙")) {
                    unit = "套";
                }
                if(size.equals("无")){
                    size = "";
                }
                if(cs[2].contains("张")) {
                    sum = cs[2].replace("张", "");
                }
                HSSFCell cell7 = row.getCell(7);
                Date deliveryDate = cell7.getDateCellValue();//发件日期

                HSSFCell cell5 = row.getCell(5);
                String customName = cell5.getStringCellValue();

                HSSFCell cell16 = row.getCell(16);
                String memo = "";
                if(cell16!=null) {
                    memo = getCellValue(cell16);

                }

                model.setDeliveryDate(format.format(deliveryDate));
                model.setReceiveDate(format.format(receiveDate));
                model.setTypeName(typeName);
                model.setSize(size);
                model.setCash("");
                model.setpPlus("");
                model.setpTotal("");
                model.setPrice("");
                model.setCustomerName(customName);
                model.setProperty("成品");
                model.setSum(sum);
                model.setTradeNo("");
                model.setUnit(unit);
                model.setMemo(memo);
            } catch (Exception e) {
                model.setType(0);
                logger.error("解析excel失败！失败行数：{}", i + 1, e);
            }
            models.add(model);

        }
        return models;
    }



    private List<DetailModel> readCoverExcel(HSSFWorkbook book) {

        HSSFSheet sheet = book.getSheetAt(0);
        //行数
        int rowSize = sheet.getPhysicalNumberOfRows();

        int totalCells = 0;
        if (rowSize >= 3 && sheet.getRow(3) != null) {
            totalCells = sheet.getRow(3).getPhysicalNumberOfCells();
        }

        HSSFRow titleRow = sheet.getRow(3);
        List<String> titleList = new ArrayList<>();
        for (int j = 0; j < totalCells; j++) {
            titleList.add(titleRow.getCell(j).getStringCellValue());
        }
        logger.info("导入excel  title：{}", JsonUtils.toJson(titleList));
        List<DetailModel> models = Lists.newArrayList();
        String deliveryDate = "";//发货日期
        String receiveDate = "";//收货日期
        DecimalFormat format = new DecimalFormat("0.00");
        for (int i = 4; i < rowSize; i++) {
            DetailModel model = new DetailModel();
            try {
                HSSFRow row = sheet.getRow(i);

                HSSFCell Cell0 = row.getCell(0);//发货单元格
                String cellValue = Cell0.getStringCellValue();
                if("合计".equals(cellValue)){
                    return models;
                }
                HSSFCell c = row.getCell(2);
                deliveryDate = c.getStringCellValue();//发货时间


                HSSFCell cell3 = row.getCell(3);
                String typeName = cell3.getStringCellValue();//品名

                HSSFCell cell4 = row.getCell(4);
                String size = "";
                if(cell4 != null){
                    size = cell4.getStringCellValue();//尺寸规格

                }

                String pTotal = "";//总p数

                String pPlus = "";//加p数

                HSSFCell cell5 = row.getCell(5);
                String sum = getCellValue(cell5);//数量

                HSSFCell cell7 = row.getCell(7);
                String price = getCellValue(cell7);//单价

                String cash =(new BigDecimal(sum).multiply(new BigDecimal(price))).toString();//应收金额

                String customerName = "";//顾客姓名



                model.setDeliveryDate(deliveryDate);
                model.setReceiveDate(receiveDate);
                model.setTypeName(typeName);
                model.setSize(size);
                model.setCash(cash);
                model.setpPlus(pPlus);
                model.setpTotal(String.valueOf(pTotal));
                model.setPrice(String.valueOf(price));
                model.setCustomerName(customerName);
                model.setProperty("半成品");
                model.setSum(String.valueOf(sum));

            } catch (Exception e) {
                model.setType(0);
                logger.error("解析excel失败！失败行数：{}", i + 1, e);
            }
            models.add(model);

        }
        return models;
    }


    public  String  exportExcel(HttpServletResponse response,String exportFileName,List<DetailModel> models) throws Exception {
        System.out.println("导出文件！");
        OutputStream os = null;
        Workbook wb = null;    //工作薄

        try {


            //导出Excel文件数据
            ExportExcelUtil util = new ExportExcelUtil();
            String template = "/templates/H杭州LUNA6月份账单  上海咏鹤.xls";
//            File file =util.getExcelDemoFile("templates/H杭州LUNA6月份账单  上海咏鹤.xls");
//            String sheetName="H杭州LUNA6月份账单明细";
            //写入账单明细
            wb = util.writeNewExcel(template,models);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename="+ URLEncoder.encode(exportFileName+".xls", "utf-8"));
            os = response.getOutputStream();
            wb.write(os);
        } catch (Exception e) {
            logger.error("导出账单失败！",e);
        }
        finally{
            os.flush();
            os.close();
            if(wb != null){
                wb.close();

            }
        }
        return null;
    }



    private Workbook createImportExcel() {
        Workbook workbook = new HSSFWorkbook();
        return workbook;
    }
    /**
     * 验证EXCEL文件
     * @param filePath
     * @return
     */
    public static boolean validateExcel(String filePath){
        if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))){
            return false;
        }
        return true;
    }

    // @描述：是否是2003的excel，返回true是2003
    public static boolean isExcel2003(String filePath)  {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    //@描述：是否是2007的excel，返回true是2007
    public static boolean isExcel2007(String filePath)  {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

    /**
     * 根据sheetName获取报价数据
     * @param sheetName
     * @return
     */
    private  Map<String,Map<String,String>> getBaseData(String sheetName){
        Map<String,Map<String,String>> modelMaps = Maps.newHashMap();
        try {
            //获取报价基础数据
            String template = "/templates/base.properties";
//            String filePath = Thread.currentThread().getContextClassLoader().getResource(template).getPath();
//            FileInputStream inputStream = new FileInputStream(filePath);
            String str = IOUtils.toString(ExcelController.class.getResourceAsStream(template));
            Map<String, Map<String, Map<String,String>>> properties = JsonUtils.fromJson(str, Map.class);
            //根据报价表中sheetName 获取 基础数据
            modelMaps = properties.get(sheetName);

        } catch (Exception e) {
            logger.error("读取报价基础数据失败！");
            e.printStackTrace();
        }
        return modelMaps;
    }




    /**
     * 对Excel的各个单元格的格式进行判断并转换
     */
    private String getCellValue(Cell cell) {
        String cellValue = "";
        DecimalFormat df = new DecimalFormat("#");
        if(cell == null){
            return cellValue;
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                cellValue =cell.getRichStringCellValue().getString().trim();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                cellValue =df.format(cell.getNumericCellValue()).toString();
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellValue =String.valueOf(cell.getBooleanCellValue()).trim();
                break;
            case Cell.CELL_TYPE_FORMULA:
                cellValue =cell.getCellFormula();
                break;
            default:
                cellValue = "";
        }
        return cellValue;
    }

    //判断行为空
    private int checkRowNull(HSSFRow hssfRow){
        int num = 0;
        Iterator<Cell> cellItr =hssfRow.iterator();
        while(cellItr.hasNext()){
            Cell c =cellItr.next();
            if ("".equals(getCellValue(c))) {
                num++;
            }
        }
        return num;
    }


    /**
     * 导入报价数据
     * @param file
     * @param response
     * @param request
     * @return
     */
    @RequestMapping("/importBase")
    @ResponseBody
    public Map<String,Object> importBase(@RequestParam("baseFile")MultipartFile file,HttpServletResponse response,HttpServletRequest request){
        Map<String,Object> returnMap = new HashMap<>();
        String exportFileName = request.getParameter("fileName");
        String fileName = file.getOriginalFilename();
        Map<String,Map<String,QuotationModel>> models = Maps.newHashMap();
        logger.info("导入报价数据：{}",fileName);
        if(!validateExcel(fileName)){
            logger.error("请上传正确的excel格式");
            returnMap.put("errorMsg", "请上传正确的excel格式");
            return returnMap;
        }
        Workbook workbook = null;
        try {
            InputStream is  = file.getInputStream();
            if(isExcel2003(fileName)) {
                HSSFWorkbook   hssfWorkbook = new HSSFWorkbook(is);
                models = readBaseExcelValue(hssfWorkbook);
            }else {
                workbook = new XSSFWorkbook(is);
            }
            String template = "templates/base.properties";
            String filePath = Thread.currentThread().getContextClassLoader().getResource(template).getPath();
//            Properties properties = new Properties();
            File baseFile = new File(filePath);
            FileWriter fileWritter = new FileWriter(baseFile);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(JsonUtils.toJson(models));
            bufferWritter.close();
//            FileOutputStream outputStream = new FileOutputStream(baseFile);
//            properties.store(outputStream,"fff");
//            outputStream.close();
//            FileInputStream inputStream = new FileInputStream(baseFile);
//            properties.load(inputStream);
//            inputStream.close();
        } catch (IOException e) {
            logger.error("导入excel失败！",e);
            returnMap.put("errorMsg", "导入excel失败！");
        }

        return null;
    }


    private Map<String,Map<String,QuotationModel>> readBaseExcelValue(HSSFWorkbook book) {
        int sheetSize = book.getNumberOfSheets();
        Map<String,Map<String,QuotationModel>> base = Maps.newHashMap();
        for(int j=0 ;j<sheetSize;j++) {

            HSSFSheet sheet = book.getSheetAt(j);
            String sheetName = sheet.getSheetName();
            //行数
            int rowSize = sheet.getPhysicalNumberOfRows();
            Map<String,QuotationModel> modelMap = Maps.newHashMap();
            for (int i = 1; i < rowSize; i++) {
                try {
                    QuotationModel model = new QuotationModel();
                    HSSFRow row = sheet.getRow(i);
                    HSSFCell cell0 = row.getCell(0);
                    String category = cell0.getStringCellValue();

                    HSSFCell cell3 = row.getCell(3);
                    String typeName = cell3.getStringCellValue();

                    HSSFCell cell4 = row.getCell(4);
                    if(cell4 != null){
                        String size = cell4.getStringCellValue();
                        model.setSize(size);
                    }

                    HSSFCell cell5 = row.getCell(5);
                    String coverPrice = getCellValue(cell5);

                    HSSFCell cell6 = row.getCell(6);
                    String processingFee = getCellValue(cell6);

                    HSSFCell cell7 = row.getCell(7);
                    String pPlusPrice = getCellValue(cell7);

                    HSSFCell cell8 = row.getCell(8);
                    String finishedPrice = getCellValue(cell8);

                    model.setCategory(category);
                    model.setTypeName(typeName);

                    model.setCoverPrice(coverPrice);
                    model.setProcessingFee(processingFee);
                    model.setpPlusPrice(pPlusPrice);
                    model.setFinishedPrice(finishedPrice);
                    modelMap.put(typeName, model);
                } catch (Exception e) {
                    logger.error("解析excel失败！失败行数：{}", sheetName+(i + 1), e);

                }
            }
            base.put(sheetName, modelMap);
        }

        return base;
    }



//    /**
//     * 描述：通过 jquery.form.js 插件提供的ajax方式导出Excel
//     * @param request
//     * @param response
//     * @throws Exception
//     */
//    @RequestMapping(value="export",method={RequestMethod.GET,RequestMethod.POST})
//    public  String  ajaxUploadExcel(HttpServletRequest request,HttpServletResponse response,String exportFileName) throws Exception {
//        System.out.println("导出文件！");
//        OutputStream os = null;
//        Workbook wb = null;    //工作薄
//
//        try {
//
//            //导出Excel文件数据
//            ExportExcelUtil util = new ExportExcelUtil();
//            File file =util.getExcelDemoFile("templates/H杭州LUNA6月份账单  上海咏鹤.xls");
////            String sheetName="H杭州LUNA6月份账单明细";
//            //写入 账单明细
//            wb = util.writeNewExcel(file,new ArrayList<>());
//            response.setContentType("application/vnd.ms-excel");
//            response.setHeader("Content-disposition", "attachment;filename="+ URLEncoder.encode(exportFileName+".xls", "utf-8"));
//            os = response.getOutputStream();
//            wb.write(os);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally{
//            os.flush();
//            os.close();
//        }
//        return null;
//    }
}
