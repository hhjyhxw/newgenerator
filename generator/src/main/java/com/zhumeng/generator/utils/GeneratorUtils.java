package com.zhumeng.generator.utils;

import com.zhumeng.generator.entity.ColumnEntity;
import com.zhumeng.generator.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 *
 * @date 2016年12月19日 下午11:40:24
 */
public class GeneratorUtils {

    public static void main(String[] args){
       System.out.println(columnToJava2("ss_yy_mm"));
        String className = tableToJava("t_subject_message","t_");
//        System.out.println(columnToJava("ss_yy_mm"));
        System.out.println(columnToJava2(className));
        System.out.println(StringUtils.uncapitalize(className));
//        System.out.println("ss_yy_mm".toUpperCase());
    }


    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
//        templates.add("template/index.js.vm");
//        templates.add("template/index.vue.vm");
//        templates.add("template/biz.java.vm");

        templates.add("template/mapper.xml.vm");
        templates.add("template/service.java.vm");
        templates.add("template/entity.java.vm");
        templates.add("template/list.ftl.vm");
        templates.add("template/input.ftl.vm");
        templates.add("template/mapper.java.vm");
        templates.add("template/controller.java.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();

        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setJdbcType(column.get("dataType").toUpperCase());
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setAttrname(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);

            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }

            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("package", config.getString("package"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        map.put("moduleName", config.getString("mainModule"));
        map.put("secondModuleName", toLowerCaseFirstOne(className));//className  首字母小写
        String sonModule = null;
        String sonModule2 = null;
        String[] sonList = null;//组成表的每个单词
        String tablePrefix = config.getString("tablePrefix");
        if(tableEntity.getTableName().startsWith("t_")){
            sonModule = tableEntity.getTableName().split("_")[1];
            sonModule2 = tableEntity.getTableName().split("_")[2];
            sonList = tableEntity.getTableName().replaceFirst(tablePrefix,"").split("_");
//            map.put("sonModule",tableEntity.getTableName().split("_")[1]);//每个表的去掉前缀 的首个单词 作为模块的 包名
//            map.put("sonModule2",tableEntity.getTableName().split("_")[2]);
        }else {
            sonModule = tableEntity.getTableName().split("_")[0];
            sonModule2 = tableEntity.getTableName().split("_")[1];
            sonList = tableEntity.getTableName().replaceFirst(tablePrefix,"").split("_");
//            map.put("sonModule",tableEntity.getTableName().split("_")[0]);//每个表的去掉前缀 的首个单词 作为模块的 包名
//            map.put("sonModule2",tableEntity.getTableName().split("_")[1]);
        }
        map.put("sonModule",sonModule);//实体 第一个单词  小写
        map.put("sonModule2", sonModule2);//实体 第二个单词 小写
        map.put("sonList", sonList);//实体 第二个单词 小写

        map.put("pageTotal", config.getString("pageTotal"));
        map.put("pagePages", config.getString("pagePages"));
        map.put("pageSize", config.getString("pageSize"));
        map.put("pagePageNum", config.getString("pagePageNum"));
        map.put("hasPreviousPage", config.getString("hasPreviousPage"));
        map.put("hasNextPage", config.getString("hasNextPage"));
        map.put("prePage", config.getString("prePage"));
        map.put("nextPage", config.getString("nextPage"));
        map.put("startif", config.getString("startif"));
        map.put("forend", config.getString("forend"));
        map.put("forend2", config.getString("forend2"));
        map.put("endif", config.getString("endif"));
        map.put("forelse", config.getString("forelse"));

        map.put("dollarstart0", config.getString("dollarstart0"));
        map.put("dollarstart", config.getString("dollarstart"));
        map.put("dollarsend", config.getString("dollarsend"));

        map.put("dollarstart2", config.getString("dollarstart2"));
        map.put("dollarsend2", config.getString("dollarsend2"));


        VelocityContext context = new VelocityContext(map);
        System.out.println("sonModule===="+sonModule);
        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), config.getString("package"), config.getString("mainModule"),sonModule,sonModule2,sonList)));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    public static String columnToJava2(String columnName) {
        if(columnName.indexOf("_")>-1){
            String[] arary = columnName.split("_");
            String result = null;
            for (int i=0;i<arary.length;i++){
                if(i==0){
                    result =  toUpperCaseFirstOne(arary[0]);
                }else {
                    result+= toUpperCaseFirstOne(arary[i]);
                }
            }
            return  result;
        }else{
            return  toUpperCaseFirstOne(columnName);
        }
//        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix) && tableName.startsWith(tablePrefix)) {
            tableName=tableName.replaceFirst(tablePrefix,"");
//            tableName = tableName.replace(tablePrefix, "");
        }
//        return columnToJava(tableName);
        return columnToJava2(tableName);

    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName,String sonModule,String sonModule2,String[] sonList) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        String frontPath = "ui" + File.separator;
        String backPagePath = "tempalte" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }

//        if (template.contains("index.js.vm")) {
//            return frontPath + "api" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.js";
//        }
//
//        if (template.contains("index.vue.vm")) {
//            return frontPath + "views" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.vue";
//        }

        if (template.contains("list.ftl.vm")) {
            String filtPath = backPagePath+sonModule+ File.separator;
            for (int i=0;i<sonList.length;i++){
                if(i==sonList.length-1){
                    filtPath+=sonList[i]+"_list.ftl";
                }else{
                    filtPath+=sonList[i]+"_";
                }
            }
            return filtPath;
//            return backPagePath+sonModule + File.separator +sonModule+ "_" + sonModule2 + "_"+ "list.ftl";
        }
        if (template.contains("input.ftl.vm")) {
            String filtPath = backPagePath+sonModule+ File.separator;
            for (int i=0;i<sonList.length;i++){
                if(i==sonList.length-1){
                    filtPath+=sonList[i]+"_input.ftl";
                }else{
                    filtPath+=sonList[i]+"_";
                }
            }
            return filtPath;
//            return backPagePath+sonModule + File.separator +sonModule+ "_" + sonModule2 + "_"+ "input.ftl";

        }

        if (template.contains("service.java.vm")) {
            return packagePath + File.separator +sonModule+File.separator +"service"+ File.separator +className + "Service.java";
        }
        if (template.contains("mapper.java.vm")) {
            return packagePath +File.separator + sonModule+File.separator+"dao"+File.separator+ className + "Mapper.java";
        }
        if (template.contains("entity.java.vm")) {
            return packagePath +  File.separator + sonModule+File.separator+ "model" +File.separator+ className + ".java";
        }
        if (template.contains("controller.java.vm")) {
            return packagePath+File.separator+sonModule+File.separator+"web" + File.separator+ className + "Controller.java";
        }
        if (template.contains("mapper.xml.vm")) {
            return "mapper" + File.separator + sonModule+ File.separator + className + "Mapper.xml";
        }

        return null;
    }

    //首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    //首字母转大写
    public static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }
}


