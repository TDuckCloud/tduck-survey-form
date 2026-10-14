package com.tduck.cloud.account.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 账号表数据库结构自动升级自检组件
 * <p>
 * 在服务启动时自动检测 ac_user 表是否存在 need_change_password 字段，
 * 若不存在则根据所使用的数据库类型自动执行 ALTER TABLE 进行平滑升级。
 *
 * @author tduck
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class AccountTableUpgradeRunner implements ApplicationRunner {

    private static final String TABLE_NAME = "ac_user";
    private static final String COLUMN_NAME = "need_change_password";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@tduckcloud.com";

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            if (!isColumnExist(metaData, TABLE_NAME, COLUMN_NAME)) {
                log.info("检测到表 {} 缺失 {} 字段，准备自动升级数据库表结构...", TABLE_NAME, COLUMN_NAME);
                addColumn(connection, metaData.getDatabaseProductName());
                initDefaultAdminFlag(connection);
            } else {
                log.info("数据库表 {} 已包含 {} 字段，无需自动升级", TABLE_NAME, COLUMN_NAME);
            }
        } catch (Exception e) {
            log.error("检查或自动升级表 {} 字段 {} 失败，请检查数据库权限或手动执行升级 SQL", TABLE_NAME, COLUMN_NAME, e);
        }
    }

    /**
     * 判断指定表的某列是否存在（兼容各数据库元数据大小写）
     */
    private boolean isColumnExist(DatabaseMetaData metaData, String tableName, String columnName) {
        String[] tableVariants = {tableName, tableName.toUpperCase(), tableName.toLowerCase()};
        for (String table : tableVariants) {
            try (ResultSet columns = metaData.getColumns(null, null, table, null)) {
                while (columns.next()) {
                    String existingColumn = columns.getString("COLUMN_NAME");
                    if (columnName.equalsIgnoreCase(existingColumn)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.warn("通过元数据查询表 {} 列信息异常: {}", table, e.getMessage());
            }
        }
        return false;
    }

    /**
     * 根据不同的数据库产品类型执行适配的 ALTER TABLE 语句
     */
    private void addColumn(Connection connection, String databaseProductName) throws Exception {
        String dbName = (databaseProductName == null ? "" : databaseProductName).toLowerCase();
        String alterSql;

        if (dbName.contains("oracle")) {
            alterSql = "ALTER TABLE " + TABLE_NAME + " ADD (" + COLUMN_NAME + " NUMBER(1) DEFAULT 0)";
        } else if (dbName.contains("sql server") || dbName.contains("microsoft")) {
            alterSql = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME + " TINYINT DEFAULT 0";
        } else if (dbName.contains("postgresql")) {
            alterSql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " SMALLINT DEFAULT 0";
        } else {
            // 默认兼容 MySQL / MariaDB / DM（达梦）/ Kingbase（人大金仓）
            alterSql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " TINYINT(1) DEFAULT 0 COMMENT '是否需要修改密码 0否 1是'";
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(alterSql);
            log.info("表 {} 成功自动添加字段 {}，执行 SQL: {}", TABLE_NAME, COLUMN_NAME, alterSql);
        }
    }

    /**
     * 为默认管理员账号初始化需改密标识
     */
    private void initDefaultAdminFlag(Connection connection) {
        String updateSql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME + " = 1 WHERE email = '" + DEFAULT_ADMIN_EMAIL + "'";
        try (Statement statement = connection.createStatement()) {
            int rows = statement.executeUpdate(updateSql);
            if (rows > 0) {
                log.info("已将默认管理员 {} 的 {} 设置为 1（首次登录强制改密）", DEFAULT_ADMIN_EMAIL, COLUMN_NAME);
            }
        } catch (Exception e) {
            log.warn("初始化默认管理员改密标识异常: {}", e.getMessage());
        }
    }
}
