package cn.staitech.fr.utils;


import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author: wangfeng
 * @create: 2024-02-21 16:37:52
 * @Description: 操作 RocksDB
 */
@Slf4j
public class RocksDBUtil {
    // 数据库列族(表)集合
    public static final ConcurrentMap<String, ColumnFamilyHandle> COLUMNFAMILYHANDLE_MAP = new ConcurrentHashMap<>();
    public static int GET_KEYS_BATCH_SIZE = 100000;
    private static RocksDB rocksDB;

    /*
      初始化RocksDB
     */
    static {
        try {
            String osName = System.getProperty("os.name");
            log.info("osName:{}", osName);
            //RocksDB文件目录
            String rocksDBPath;
            if (osName.toLowerCase().contains("windows")) {
                // 指定windows系统下RocksDB文件目录
                rocksDBPath = "D:\\RocksDB";
            } else {
                // 指定linux系统下RocksDB文件目录
                rocksDBPath = "/home/pat_saas/rocksdb";
            }


            // 重启项目时先删除原有的文件
            File file = new File(rocksDBPath);
            deleteFolder(file);

            RocksDB.loadLibrary();
            Options options = new Options();
            options.setCreateIfMissing(true); //如果数据库不存在则创建
            List<byte[]> cfArr = RocksDB.listColumnFamilies(options, rocksDBPath); // 初始化所有已存在列族
            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>(); //ColumnFamilyDescriptor集合
            if (!ObjectUtils.isEmpty(cfArr)) {
                for (byte[] cf : cfArr) {
                    columnFamilyDescriptors.add(new ColumnFamilyDescriptor(cf, new ColumnFamilyOptions()));
                }
            } else {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
            }
            DBOptions dbOptions = new DBOptions();
            dbOptions.setCreateIfMissing(true);
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>(); //ColumnFamilyHandle集合
            rocksDB = RocksDB.open(dbOptions, rocksDBPath, columnFamilyDescriptors, columnFamilyHandles);
            for (int i = 0; i < columnFamilyDescriptors.size(); i++) {
                ColumnFamilyHandle columnFamilyHandle = columnFamilyHandles.get(i);
                String cfName = new String(columnFamilyDescriptors.get(i).getName(), StandardCharsets.UTF_8);
                COLUMNFAMILYHANDLE_MAP.put(cfName, columnFamilyHandle);
            }
            log.info("RocksDB init success!! path:{}", rocksDBPath);
            log.info("----------->cfNames:{}", COLUMNFAMILYHANDLE_MAP.keySet());
            // deleteAllColumnFamily();
        } catch (Exception e) {
            log.error("RocksDB init failure!! error:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    private RocksDBUtil() {
    }

    public static void init() {
        new RocksDBUtil();
    }

    public static boolean deleteFolder(File folder) {
        // 检查文件夹是否存在，如果不存在，说明删除成功；如果存在，继续删除子文件夹和文件
        if (!folder.exists()) {
            return true;
        }

        // 如果文件夹是一个文件，直接删除它
        if (folder.isFile()) {
            return folder.delete();
        }

        // 如果文件夹是一个目录，遍历其下的所有子文件夹和文件，并递归地调用删除本身的方法
        boolean result = true; // 定义布尔变量，用于判断删除是否成功
        File[] files = folder.listFiles(); // 获取文件夹中的所有文件和子文件夹
        if (files != null) {
            for (File f : files) { // 遍历每个文件或子文件夹
                if (!deleteFolder(f)) { // 对文件夹进行递归删除
                    result = false; // 如果删除失败，设置布尔变量为false
                }
            }
        }

        // 删除文件夹本身
        if (!folder.delete()) {
            result = false;
        }

        return result;
    }


    /**
     * 列族，创建（如果不存在）
     */
    public static ColumnFamilyHandle cfAddIfNotExist(String cfName) {
        try {
            ColumnFamilyHandle columnFamilyHandle;
            if (!COLUMNFAMILYHANDLE_MAP.containsKey(cfName)) {
                columnFamilyHandle = rocksDB.createColumnFamily(new ColumnFamilyDescriptor(cfName.getBytes(), new ColumnFamilyOptions()));
                COLUMNFAMILYHANDLE_MAP.put(cfName, columnFamilyHandle);
                log.info("cfAddIfNotExist success!! cfName:{}", cfName);
            } else {
                columnFamilyHandle = COLUMNFAMILYHANDLE_MAP.get(cfName);
            }
            return columnFamilyHandle;
        } catch (RocksDBException e) {
            log.info("cfAddIfNotExist:{}", e);
        }
        return null;
    }

    /**
     * 列族，删除（如果存在）
     */
    public static void cfDeleteIfExist(String cfName) throws RocksDBException {
        if (COLUMNFAMILYHANDLE_MAP.containsKey(cfName)) {
            rocksDB.dropColumnFamily(COLUMNFAMILYHANDLE_MAP.get(cfName));
            COLUMNFAMILYHANDLE_MAP.remove(cfName);
            log.info("cfDeleteIfExist success!! cfName:{}", cfName);
        } else {
            log.warn("cfDeleteIfExist containsKey!! cfName:{}", cfName);
        }
    }

    /**
     * 增
     */
    public static void put(String cfName, String key, String value) throws RocksDBException {
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        rocksDB.put(columnFamilyHandle, key.getBytes(), value.getBytes());
    }

    /**
     * 增（批量）
     */
    public static void batchPut(String cfName, Map<String, String> map) throws RocksDBException {
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        WriteOptions writeOptions = new WriteOptions();
        WriteBatch writeBatch = new WriteBatch();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writeBatch.put(columnFamilyHandle, entry.getKey().getBytes(), entry.getValue().getBytes());
        }
        rocksDB.write(writeOptions, writeBatch);
    }

    /**
     * 删
     */
    public static void delete(String cfName, String key) throws RocksDBException {
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        rocksDB.delete(columnFamilyHandle, key.getBytes());
    }

    /**
     * 查
     */
    public static String get(String cfName, String key) throws RocksDBException {
        String value = null;
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        byte[] bytes = rocksDB.get(columnFamilyHandle, key.getBytes());
        if (!ObjectUtils.isEmpty(bytes)) {
            value = new String(bytes, StandardCharsets.UTF_8);
        }
        return value;
    }

    /**
     * 查（多个键值对）
     */
    public static Map<String, String> multiGetAsMap(String cfName, List<String> keys) throws RocksDBException {
        Map<String, String> map = new HashMap<>(keys.size());
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        List<ColumnFamilyHandle> columnFamilyHandles;
        List<byte[]> keyBytes = keys.stream().map(String::getBytes).collect(Collectors.toList());
        columnFamilyHandles = IntStream.range(0, keys.size()).mapToObj(i -> columnFamilyHandle).collect(Collectors.toList());
        List<byte[]> bytes = rocksDB.multiGetAsList(columnFamilyHandles, keyBytes);
        for (int i = 0; i < bytes.size(); i++) {
            byte[] valueBytes = bytes.get(i);
            String value = "";
            if (!ObjectUtils.isEmpty(valueBytes)) {
                value = new String(valueBytes, StandardCharsets.UTF_8);
            }
            map.put(keys.get(i), value);
        }
        return map;
    }

    /**
     * 查（多个值）
     */
    public static List<String> multiGetValueAsList(String cfName, List<String> keys) throws RocksDBException {
        List<String> values = new ArrayList<>(keys.size());
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
        List<byte[]> keyBytes = keys.stream().map(String::getBytes).collect(Collectors.toList());
        for (int i = 0; i < keys.size(); i++) {
            columnFamilyHandles.add(columnFamilyHandle);
        }
        List<byte[]> bytes = rocksDB.multiGetAsList(columnFamilyHandles, keyBytes);
        for (byte[] valueBytes : bytes) {
            String value = "";
            if (!ObjectUtils.isEmpty(valueBytes)) {
                value = new String(valueBytes, StandardCharsets.UTF_8);
            }
            values.add(value);
        }
        return values;
    }

    /**
     * 查（所有键）
     */
    public static List<String> getAllKey(String cfName) throws RocksDBException {
        List<String> list = new ArrayList<>();
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        try (RocksIterator rocksIterator = rocksDB.newIterator(columnFamilyHandle)) {
            for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
                list.add(new String(rocksIterator.key(), StandardCharsets.UTF_8));
            }
        }
        return list;
    }

    /**
     * 分片查（键）
     */
    public static List<String> getKeysFrom(String cfName, String lastKey) throws RocksDBException {
        List<String> list = new ArrayList<>(GET_KEYS_BATCH_SIZE);
        // 获取列族Handle
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName);
        try (RocksIterator rocksIterator = rocksDB.newIterator(columnFamilyHandle)) {
            if (lastKey != null) {
                rocksIterator.seek(lastKey.getBytes(StandardCharsets.UTF_8));
                rocksIterator.next();
            } else {
                rocksIterator.seekToFirst();
            }
            // 一批次最多 GET_KEYS_BATCH_SIZE 个 key
            while (rocksIterator.isValid() && list.size() < GET_KEYS_BATCH_SIZE) {
                list.add(new String(rocksIterator.key(), StandardCharsets.UTF_8));
                rocksIterator.next();
            }
        }
        return list;
    }

    /**
     * 查（所有键值）
     */
    public static Map<String, String> getAll(String cfName) throws RocksDBException {
        Map<String, String> map = new HashMap<>();
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        try (RocksIterator rocksIterator = rocksDB.newIterator(columnFamilyHandle)) {
            for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
                map.put(new String(rocksIterator.key(), StandardCharsets.UTF_8), new String(rocksIterator.value(), StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    /**
     * 查总条数
     */
    public static int getCount(String cfName) throws RocksDBException {
        int count = 0;
        ColumnFamilyHandle columnFamilyHandle = cfAddIfNotExist(cfName); //获取列族Handle
        try (RocksIterator rocksIterator = rocksDB.newIterator(columnFamilyHandle)) {
            for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 删除所有列族及数据
     */
    public static void deleteAllColumnFamily() {
        // 删除旧数据
        for (String cfName : COLUMNFAMILYHANDLE_MAP.keySet()) {
            if (!cfName.equals("default")) {
                try {
                    cfDeleteIfExist(cfName);
                } catch (RocksDBException e) {
                    log.info("初始化清空rocksdb数据:{},{}", cfName, e);
                }
            }
        }
    }
}
