
package com.loan.spmkt.v1.util.manage;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.util.CellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hrcx.common.util.DateUtil;
import com.hrcx.common.util.StringUtil;

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
 * @date 2019/9/6
 * @author chenzhen
 */
@SuppressWarnings("hiding")
public class DiaryExcel<T>  {
	
	private static final Logger logger = LoggerFactory.getLogger(DiaryExcel.class);

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
    @SuppressWarnings("deprecation")
	public void exportExcel(HSSFWorkbook wb,String title,String store,Map<String,Object> timeMap, List<Object> dtoList, List<String> listTile,List<String> listCloumn,HttpServletResponse response) 
			throws IOException {
    	
    	String [] sortNameArr = {"businessType","subclassType"};
        boolean [] isAscArr = {true,true};
        ListUtils.sort(dtoList,sortNameArr,isAscArr);
        
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
//        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = null;
//        BufferedOutputStream fos = null;
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
            *firstRow  起始行号
            *lastRow 结束行号
            *firstCol 起始列号
            *lastCol 结束列号
            **/
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,(listTile.size()-1)));
            
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
            //合计变量
            double businessNum = 0;  
            double sumMoney = 0;
            double cashCount = 0;
            double savingCount = 0;
            double courseCount = 0;
            double sumElse = 0;
            double tempVal = 0;
            List<Map<Integer,String>> resMaplist = new ArrayList<>();
            while (iterator.hasNext()) {//记录的迭代器，遍历总记录
                int zdCell = 0;
                rowNum++;
                row = sheet.createRow(rowNum);
                row.setHeightInPoints(15);//行高
                @SuppressWarnings("unchecked")
				T t = (T) iterator.next();
                Map<Integer,String> rowMap = new HashMap<>();
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
                                    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            			val = sdf.format(val);
                                    }
                                	switch(fieldName) {
                                		case "businessType":
	                                		if(Integer.parseInt(val.toString()) == 1) {
	                                			textVal = "项目";
	                                		}else if(Integer.parseInt(val.toString()) == 2) {
	                                			textVal = "产品";
	                                		}else if(Integer.parseInt(val.toString()) == 3) {
	                                			textVal = "疗程";
	                                		}else if(Integer.parseInt(val.toString()) == 4) {
	                                			textVal = "售卡";
	                                		}else if(Integer.parseInt(val.toString()) == 5) {
	                                			textVal = "充值";
	                                		}else if(Integer.parseInt(val.toString()) == 6) {
	                                			textVal = "兑换";
	                                		}
	                                		break;
                                		case "subclassType":
	                                		if(Integer.parseInt(val.toString()) == 1) {
	                                			textVal = "美容服务类";
	                                		}else if(Integer.parseInt(val.toString()) == 2) {
	                                			textVal = "美发服务类";
	                                		}else if(Integer.parseInt(val.toString()) == 3) {
	                                			textVal = "会员卡开卡";
	                                		}else if(Integer.parseInt(val.toString()) == 4) {
	                                			textVal = "会员卡充值";
	                                		}else if(Integer.parseInt(val.toString()) == 5) {
	                                			textVal = "兑换";
	                                		}
	                                		break;
                                		case "businessNum":
                                			businessNum += StringUtil.isEmpty(val)?0:Integer.parseInt(val.toString());
                                			textVal = String.valueOf(val);
	                                		break;
                                		case "sumMoney":
                                			tempVal = Double.parseDouble(val+"")/100;
                                			textVal = String.valueOf(tempVal);
                                			sumMoney += tempVal;
	                                		break;
                                		case "cashCount":
                                			tempVal = Double.parseDouble(val+"")/100;
                                			textVal = String.valueOf(tempVal);
                                			cashCount += tempVal;
	                                		break;
                                		case "savingCount":
                                			tempVal = Double.parseDouble(val+"")/100;
                                			textVal = String.valueOf(tempVal);
                                			savingCount += tempVal;
	                                		break;
                                		case "courseCount":
                                			tempVal = Double.parseDouble(val+"")/100;
                                			textVal = String.valueOf(tempVal);
                                			courseCount += tempVal;
	                                		break;
                                		case "sumElse":
                                			tempVal = Double.parseDouble(val+"")/100;
                                			textVal = String.valueOf(tempVal);
                                			sumElse += tempVal;
	                                		break;
	                                	default :
	                                		textVal = String.valueOf(val);
                                	}
                                    rowMap.put(zdCell,textVal);
                                    
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
                resMaplist.add(rowMap);
            }
            //创建合计行
            int lastRow = sheet.getLastRowNum() + 1;   //从0开始
            Object[] lastList = {"合计","","",businessNum,sumMoney,cashCount,savingCount,courseCount,sumElse};
            row = sheet.createRow(lastRow);
            row.setHeightInPoints(25);//行高
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            for(int i = 0 ; i <lastList.length; i++) {
            	HSSFCell celllast = row.createCell(i);
            	celllast.setCellValue(lastList[i].toString());//给单元格赋值
            	celllast.setCellStyle(cellParamStyle);//设置样式
            }
            
            CellRangeAddress region = null; // 加_  防止变量名重复  
            // 一列列遍历 就是需要先循环列   然后再 判断当前列与上一列是否重复  需要定义一个变量来记录这重复连续出现的长度 
            // 由于是先比较完一列再比较下一列 所以外面的循环是先遍历map(rowMap) 所以就要先得到第一个map值 也作为上一行
            Map<Integer,String> first_map = resMaplist.get(0);// key 存放的是列顺序下标 
            Map<Integer,String> curr_map = null; // 当前行的map
            int num = 0;
            List<Integer> index_ = new ArrayList<>();
            for(int x = 0 ; x < first_map.size() ; x++){
                int len = 0;
                int y_len = resMaplist.size();// 得到总列数
                if(x > 1){
                    break;
                }
                for(int y = 1 ; y < y_len ; y ++){
                    first_map = resMaplist.get(y - 1);
                    curr_map = resMaplist.get(y);// 得到当前行
                    // 如果当前行的值与上一行的值重复就记录长度
                    if(x == 1 && num < index_.size() && index_.get(num) == y){
                    	num++;
                    	 if(len != 0){
                             // 表示 之前有要合并的
                             int end = y - 1 + 3;
                             region = new CellRangeAddress(end - len ,end,x,x);
                             sheet.addMergedRegion(region);
                             len = 0;
                         }
                         continue;
                    }
                    // 当前行的值与上一行的值重复
                    if(curr_map.get(x).toString().equals(first_map.get(x).toString())){
                    	len++;
                    }else {//临界值
                    	
                    	// 如果不为空 先判断是不是第一行的值 第一行不为空 就不记录合并
                        if(len != 0){
                            int end_len = y - 1 + 3 ; // 表示 之前有要合并的
                            region = new CellRangeAddress(end_len - len,end_len,x,x);// 结束的y坐标 减去 连续空长度
                            sheet.addMergedRegion(region);
                            if(x==0){
                            	index_.add(y);
                            }
                            len = 0;
                        }
                    }
                }
	             // 如果最后一行是需要合并的 就有可能 len 还有 长度
	            if(len != 0){
	                int end_len = y_len - 1 + 3;
	                region = new CellRangeAddress(end_len - len,end_len,x,x);
	                sheet.addMergedRegion(region);
	            }
	        }
//            response.reset();
//            response.setContentType("application/octet-stream");
//            response.setContentType("application/force-download; charset=utf-8");
//            response.setContentType("application/vnd.ms-excel; charset=utf-8");
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
