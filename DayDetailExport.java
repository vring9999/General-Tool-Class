package com.loan.spmkt.v1.util.manage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hrcx.common.util.StringUtil;

public class DayDetailExport<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(DayDetailExport.class);

	/**
	 * 
	 * @param title  标题
	 * @param store  门店
	 * @param timeMap 日期范围
	 * @param dtoList  要导出的目标集合
	 * @param listTile  中文表头
	 * @param listCloumn  匹配的实体字段
	 * @param response
	 * @throws IOException
	 */
    @SuppressWarnings({ "deprecation", "null" })
//	public void exportExcel(String title,String store,Map<String,Object> timeMap, List<Object> dtoList, List<String> listTile,List<String> listCloumn,HttpServletResponse response) 
//			throws IOException {
	public void exportExcel(HSSFWorkbook wb,String title,String store,Map<String,Object> timeMap, List<Object> dtoList, String[] listTile,String[] listCloumn,HttpServletResponse response)throws IOException { 
		
    	int[] cloumn = new int[listCloumn.length-1];
    	for(int i:cloumn) {
    		i = 0;
    	}
        //表格title
        Map<Integer, String> listTileMap = new HashMap<>();
        int key=0;
        for (int i = 0; i < listTile.length; i++) {
            if (!listTile[i].equals(null)) {
                listTileMap.put(key, listTile[i]);
                key++;
            }
        }
        // 实体字段
        Map<Integer, String> titleFieldMap = new HashMap<>();
        int value = 0;
        for (int i = 0; i < listCloumn.length; i++) {
            if (!listCloumn[i].equals(null)) {
                titleFieldMap.put(value, listCloumn[i]);
                value++;
            }
        }
        // 声明一个工作薄(构建工作簿、表格、样式)
//        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = null;
//        BufferedOutputStream fos = null;
        try {
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
            *firstRow  起始行号
            *lastRow 结束行号
            *firstCol 起始列号
            *lastCol 结束列号
            **/
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,(listTile.length-1)));
            
          //在第2行创建备注行
            HSSFCellStyle remarkcellStyle = wb.createCellStyle();
            remarkcellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
            remarkcellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
            HSSFFont remarkfontStyle = wb.createFont();
            remarkfontStyle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            remarkfontStyle.setFontHeightInPoints((short)15);  //设置标题字体大小
            remarkcellStyle.setFont(remarkfontStyle);
            CellRangeAddress csRemark1 = new CellRangeAddress(1,1,0,2);
            CellRangeAddress csRemark2 = new CellRangeAddress(1,1,2,4);
          //创建备注行
            HSSFRow ramrkrow = sheet.createRow(1);
            ramrkrow.setHeightInPoints(20);//行高
            HSSFCell cellstore = ramrkrow.createCell(0);
            cellstore.setCellValue("查询门店："+store);
            cellstore.setCellStyle(remarkcellStyle);
            setRegionStyle( sheet, csRemark1, remarkcellStyle);
            setRegionStyle( sheet, csRemark2, remarkcellStyle);
            String time = "";
            if(StringUtil.isEmpty(timeMap.get("endDate"))) {
            	time = (String) timeMap.get("dayDate");
            }else {
            	time = (String) timeMap.get("openDate")+"~"+(String) timeMap.get("endDate");
            }
            HSSFCell celltime = ramrkrow.createCell(4);
            celltime.setCellValue("日期范围："+time);
            celltime.setCellStyle(remarkcellStyle);

            // 生成一个表头样式
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setFontHeightInPoints((short) 15);
            font.setColor(HSSFColor.DARK_RED.index);//设置字体颜色 (红色)
            style.setFont(font);
            // 创建表头行
            HSSFRow row = sheet.createRow(2);
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
            int rowNum = 2;//列序号
            while (iterator.hasNext()) {//记录的迭代器，遍历总记录
                int zdCell = 0;
                rowNum++;
                row = sheet.createRow(rowNum);
                row.setHeightInPoints(15);//行高
                @SuppressWarnings("unchecked")
				T t = (T) iterator.next();
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
                                HSSFCell paramCell = row.createCell(zdCell);//单元格cell
                                if (val!= null) {
                                	if(val instanceof Date ) {
                                    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            			val = sdf.format(val);
                                    }else if(fieldName.equals("sumTurnover")|fieldName.equals("sumExpend")
                                    		|fieldName.equals("endSurplus")|fieldName.equals("sumCash")
                                    		|fieldName.equals("sumWechat")|fieldName.equals("sumAilpay")
                                    		|fieldName.equals("sumBank")|fieldName.equals("itemSumDeduct")
                                    		|fieldName.equals("productSumDeduct")|fieldName.equals("cardOtherSum")
                                    		|fieldName.equals("managerSumPay")|fieldName.equals("cardAnnulCount")){
//                                    	val = (int)val/100;
                                    	val = Double.parseDouble(val+"")/100;
                                    }
                                	//如果是金额
                                	textVal = String.valueOf(val);
                                }else{
                                    textVal = null;
                                }
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
        	for(int j = 0 ; j < listCloumn.length-1 ; j++) {  //x轴
    			for(int i = 3 ; i <= sheet.getLastRowNum(); i++) {   //y轴
//            		int cloumnValue = Integer.valueOf(sheet.getRow(i).getCell(j+1).getStringCellValue());
    				String tempValue  = sheet.getRow(i).getCell(j+1).getStringCellValue();
    				double cloumnValue = 0;
    				if(!tempValue.isEmpty() && cloumn != null) {
    					cloumnValue = Double.valueOf(tempValue);
    					cloumn[j] += cloumnValue;
    				}
        		}
            }
            //创建合计行
            int lastRow = sheet.getLastRowNum() + 1;
            row = sheet.createRow(lastRow);
            row.createCell(0).setCellValue("合计");
            row.getCell(0).setCellStyle(cellParamStyle);
            row.setHeightInPoints(25);//行高
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            for(int i = 0 ; i < cloumn.length; i++) {
            	HSSFCell celllast = row.createCell(i+1);
            	celllast.setCellValue(String.valueOf(cloumn[i]));//给单元格赋值
            	celllast.setCellStyle(cellParamStyle);//设置样式
            }
//            response.reset();
////            response.setContentType("application/octet-stream");
//            response.setContentType("application/force-download; charset=utf-8");
////            response.setContentType("application/vnd.ms-excel; charset=utf-8");
//            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
//            fos = new BufferedOutputStream(response.getOutputStream());
//            wb.write(fos);
        }catch(Throwable t){ 
        	t.printStackTrace();
        } 
//        finally {
//            if (fos != null) {
//            	fos.flush();
//                fos.close();
//            }
//            
//        }

    }
    
    /**
     * 
     * @Description: TODO(合并单元格后边框不显示问题) 
     * @param @param sheet
     * @param @param region
     * @param @param cs
     * @throws
     */
    public static void setRegionStyle(HSSFSheet sheet, CellRangeAddress region, HSSFCellStyle cs) {
        for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
        	HSSFRow row = (HSSFRow) CellUtil.getRow(i, sheet);
            for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
            	HSSFCell cell = (HSSFCell) CellUtil.getCell(row, (short) j);
                cell.setCellStyle(cs);
            }
        }
    }
}

