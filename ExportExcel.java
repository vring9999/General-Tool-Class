package com.loan.spmkt.v1.util.manage;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hrcx.common.util.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
/**
   *  表格数据导出至excel
 * 
 * @date 2019/8/20
 * @author chenzhen
 */
@SuppressWarnings("hiding")
public class ExportExcel<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(ExportExcel.class);

	
	/**
	 * 
	 * @param diaryInfoList  要导出的目标集合
	 * @param listTile  中文表头
	 * @param listCloumn  匹配的实体字段
	 * @param response
	 */
			
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//	public static void print(List<DiaryDeductInfo> diaryInfoList,List<String> listTile,List<String> listCloumn,HttpServletResponse response) {
//        try{
//			ExportExcel exportBeanExcelUtil = new ExportExcel();
//            exportBeanExcelUtil.exportExcel("日绩报表",diaryInfoList,listTile,listCloumn, response);
//        }catch (Exception E){
//        	getErrorInfo(e,className);
//        }
//    }
	/**
	 * 
	 * @param title  标题
	 * @param dtoList  要导出的目标集合
	 * @param listTile  中文表头
	 * @param listCloumn  匹配的实体字段
	 * @param response
	 * @throws IOException
	 */
    @SuppressWarnings("deprecation")
	public void exportExcel(String title,List<Object> dtoList, List<String> listTile,List<String> listCloumn,HttpServletResponse response) 
			throws IOException {
        //表格title
        Map<Integer, String> listTileMap = new HashMap<>();
        int key=0;
        for (int i = 0; i < listTile.size(); i++) {
            if (!listTile.get(i).equals(null)) {
                listTileMap.put(key, listTile.get(i));
                key++;
            }
        }
        // 实体字段
        Map<Integer, String> titleFieldMap = new HashMap<>();
        int value = 0;
        for (int i = 0; i < listCloumn.size(); i++) {
            if (!listCloumn.get(i).equals(null)) {
                titleFieldMap.put(value, listCloumn.get(i));
                value++;
            }
        }
        // 声明一个工作薄(构建工作簿、表格、样式)
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = null;
        BufferedOutputStream fos = null;
        try {
            String name = DateUtil.getCurrentTime("yyyyMMddHHmmss")+ ".xls";
            String fileName = URLEncoder.encode(name, "UTF-8");
            sheet = wb.createSheet(title);
            sheet.setDefaultColumnWidth(20);
            
            // 生成一个标题样式
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
            cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
            HSSFFont fontStyle = wb.createFont();
            fontStyle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            fontStyle.setFontHeightInPoints((short)25);  //设置标题字体大小
            cellStyle.setFont(fontStyle);

            //在第0行创建标题行
            HSSFRow titlerow = sheet.createRow(0);
            titlerow.setHeightInPoints(40);//行高
            HSSFCell cellValue = titlerow.createCell(0);
            cellValue.setCellValue(title);
            cellValue.setCellStyle(cellStyle);
            /**合并单元格
            * CellRangeAddress(firstRow, lastRow, firstCol, lastCol)
            *firstRow 区域中第一个单元格的行号
            *lastRow 区域中最后一个单元格的行号
            *firstCol 区域中第一个单元格的列号
            *lastCol 区域中最后一个单元格的列号
            **/
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,(listTile.size()-1)));
            
            // 生成一个表头样式
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setFontHeightInPoints((short) 15);
            font.setColor(HSSFColor.DARK_RED.index);//设置字体颜色 (红色)
            style.setFont(font);
            HSSFRow row = sheet.createRow(1);
            row.setHeightInPoints(25);//行高
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            
            // 生成一个数据单元格样式
            HSSFCellStyle cellParamStyle = wb.createCellStyle();
            HSSFFont ParamFontStyle = wb.createFont();
            ParamFontStyle.setFontHeightInPoints((short) 12);
            cellParamStyle.setFont(ParamFontStyle);
            cellParamStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            cellParamStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            
            HSSFCell cell;//表头cell
            HSSFCell paramCell;//单元格cell
            Collection<String> title_value = listTileMap.values();//拿到表格所有标题的value的集合
            Iterator<String> ititle = title_value.iterator();//表格标题的迭代器
            //根据选择的字段生成表头
            int size = 0;
            while (ititle.hasNext()) {
                cell = row.createCell(size);
                cell.setCellValue(ititle.next().toString());
                cell.setCellStyle(style);
                size++;
            }
            //表格标题一行的字段的集合
            Collection<String> titleColl = titleFieldMap.values();
            Iterator<Object> iterator = dtoList.iterator();//总记录的迭代器
            int rowNum = 1;//列序号
            while (iterator.hasNext()) {//记录的迭代器，遍历总记录
                int zdCell = 0;
                rowNum++;
                row = sheet.createRow(rowNum);
                row.setHeightInPoints(15);//行高
                @SuppressWarnings("unchecked")
				T t = (T) iterator.next();//得到类
                // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
                Field[] fields = t.getClass().getDeclaredFields();//获得JavaBean全部属性
                for (short i = 0; i < fields.length; i++) {//遍历属性，比对
                    Field field = fields[i];
                    String fieldName = field.getName();     //属性名
                    Iterator<String> columnIter = titleColl.iterator(); //一条字段的集合的迭代器
                    while (columnIter.hasNext()) {                //遍历要导出的字段集合
                        if (columnIter.next().equals(fieldName)) {//比对JavaBean的属性名是否一致
                            String getMethodName = "get"
                                    + fieldName.substring(0, 1).toUpperCase()
                                    + fieldName.substring(1);//拿到属性的get方法
                            Class<? extends Object> dto = t.getClass();//拿到JavaBean对象
                            try {
                                Method getMethod = dto.getMethod(getMethodName,  new Class[] {});//通过JavaBean对象拿到该属性的get方法
                                Object val = getMethod.invoke(t, new Object[] {});//操控该对象属性的get方法，拿到属性值
                                String textVal = null;
                                if (val!= null) {
                                	if(val instanceof Date ) {
                                    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            			val = sdf.format(val);
                                    }
                                    textVal = String.valueOf(val);
//                                    textVal = new String(textVal.getBytes("GBK"),"ISO-8859-1");
                                }else{
                                    textVal = null;
                                }
                                paramCell = row.createCell(zdCell);
                                paramCell.setCellValue(textVal);//写进excel对象
                                paramCell.setCellStyle(cellParamStyle);
                                zdCell++;
                            } catch (Exception e) {
                            	logger.error("{}",e);
                            } 
                        }
                    }
                }
            }
            response.reset();
            response.setContentType("application/octet-stream");
//            response.setContentType("application/force-download; charset=utf-8");
//            response.setContentType("application/vnd.ms-excel; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
            fos = new BufferedOutputStream(response.getOutputStream());
            wb.write(fos);
        }catch(Throwable t){ 
        	t.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

    }

}
