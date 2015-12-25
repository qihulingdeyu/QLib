package com.qing.utils;

/**
 * Created by zwq on 2015/10/18 17:55.<br/><br/>
 */
public class SqlUtil {

    private static final String TAG = SqlUtil.class.getName();
    private static SqlUtil sqlUtils;// = new SqlUtil();
    private static StringBuffer stringBuffer;
    private int columnCount;
    private String currentColumnType;

    private boolean print;

    public enum ColumnType {
        Integer("integer"),
        Float("float"),
        Double("double"),
        Varchar("varchar"),
        Text("text");

        private String typeName;
        ColumnType(String type) {
            this.typeName = type;
        }
    }

    private SqlUtil(){
        stringBuffer = new StringBuffer();
    }

    public static SqlUtil getInstance() {
        if (sqlUtils == null){
            synchronized (SqlUtil.class){
                if (sqlUtils == null){
                    sqlUtils = new SqlUtil();
                }
            }
        }
        stringBuffer.setLength(0);
//        stringBuffer.delete(0, stringBuffer.length());
        return sqlUtils;
    }

    /**
     * 添加了换行符
     * @return
     */
    public SqlUtil print(){
        print = true;
        return sqlUtils;
    }

    public SqlUtil createTable(Class<?> entityClass){
        if (entityClass == null){
            throw new IllegalArgumentException("clazz is null");
        }
        return createTable(entityClass.getSimpleName());
    }

    public SqlUtil createTable(String tableName){
        if (StringUtil.isNullOrEmpty(tableName)){
            throw new IllegalArgumentException("tableName is null");
        }
        columnCount = 0;
        currentColumnType = null;
        dropTable(tableName);
        if (print && stringBuffer.length() > 0){
            stringBuffer.append("\n");
        }
        stringBuffer.append("create table if not exists "+tableName+" (");
        return sqlUtils;
    }

    public SqlUtil dropTable(String tableName){
        stringBuffer.append("drop table if exists " + tableName + ";");
        return sqlUtils;
    }

    /**
     * 添加列
     * @param columnName 列名
     * @param type 类型
     * @return
     */
    public SqlUtil addColumn(String columnName, ColumnType type){
        return addColumn(columnName, type, getDefaultLength(type));
    }

    private int getDefaultLength(ColumnType type){
        int length = -1;
        if (type.typeName.equals(ColumnType.Integer)){
//            length = -1;//11
        }else if (type.typeName.equals(ColumnType.Varchar)){
            length = 255;
        }else if (type.typeName.equals(ColumnType.Text)){
//            length = -1;
        }
        return length;
    }

    private String getLengthText(int length){
        if (length > 0){
            return "("+length+")";
        }
        return "";
    }

    /**
     * 添加列
     * @param columnName 列名
     * @param type 类型
     * @param length 长度、大小
     * @return
     */
    public SqlUtil addColumn(String columnName, ColumnType type, int length){
        check(columnName);
        if (type == null){
            throw new IllegalArgumentException("column type is null");
        }
        if (columnCount > 0){
            stringBuffer.append(",");
        }
        if (print){
            stringBuffer.append("\n");
        }
        currentColumnType = type.typeName;
        stringBuffer.append(columnName+" "+currentColumnType + getLengthText(length));
        columnCount ++;
        return sqlUtils;
    }

    public SqlUtil primaryKey(){
        stringBuffer.append(" primary key");
        return sqlUtils;
    }

    public SqlUtil autoincrement(){
        if (currentColumnType.equals(ColumnType.Integer.typeName)){
            stringBuffer.append(" autoincrement");
        }
        return sqlUtils;
    }

    public SqlUtil notNull(){
        stringBuffer.append(" not null");
        return sqlUtils;
    }

    /**
     * 添加外键关联
     * @param columnName 列名
     * @param referenceTableName 关联的表名
     * @param referenceTableColumnName 关联的表名的列名
     * @return
     */
    public SqlUtil addForeignKey(String columnName, String referenceTableName, String referenceTableColumnName){
        check(columnName);
        check(referenceTableName);
        check(referenceTableColumnName);
        if (columnCount > 0){
            stringBuffer.append(",");
            if (print){
                stringBuffer.append("\n");
            }
        }
        //constraint user_FK foreign key(user) references "+DBConstants.TABLE_USER+"(_id)," +
        stringBuffer.append("constraint "+columnName+"_FK foreign key("+columnName+") references "+referenceTableName+" ("+referenceTableColumnName+")");
        return sqlUtils;
    }

    private void alterAdd(String tableName, String columnName, ColumnType type){
        alterAdd(tableName, columnName, type, getDefaultLength(type));
    }
    private void alterAdd(String tableName, String columnName, ColumnType type, int length){
        // alter table tableName add column columnName type.typeName
        stringBuffer.append("alter table "+tableName+" add column "+columnName+" "+type.typeName+getLengthText(length)+";");
    }

    public String build(){
        if (columnCount > 0){
            if (print){
                stringBuffer.append("\n");
            }
            stringBuffer.append(");");
        }
        return stringBuffer.toString();
    }

    private void check(String name){
        if (StringUtil.isNullOrEmpty(name)){
            throw new IllegalArgumentException("name is null");
        }
    }
}
