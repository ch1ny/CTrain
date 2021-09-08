package Utils;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/16 0:13
 * @Description 雪花算法工具类
 * @Version 1.0
 */

public class SnowFlakeUtil {
    // 起始时间戳
    private final static long START_STMP = 1609480800000L; // 2021-01-01 00:00:00

    // 每部分的位数
    private final static long SEQUENCE_BIT = 12; // 序列号占用位数
    private final static long MACHINE_BIT = 1; // 机器id占用位数
    private final static long DATACENTER_BIT = 1; // 机房id占用位数

    // 每部分最大值
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    private long datacenterId; // 机房id
    private long machineId; // 机器id
    private long sequence = 0L; // 序列号
    private long lastStmp = -1L; // 上次的时间戳

    private static SnowFlakeUtil SFU;

    // 双重校验锁
    public static SnowFlakeUtil getInstance() {
        if (SFU == null) {
            synchronized (SnowFlakeUtil.class) {
                if (SFU == null) {
                    SFU = new SnowFlakeUtil(1, 1);
                }
            }
        }
        return SFU;
    }

    private SnowFlakeUtil(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    // 产生下一个ID
    public synchronized long getNextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.Refusing to generate id");
        }
        if (currStmp == lastStmp) {
            System.out.print(((currStmp - START_STMP) << (DATACENTER_BIT + MACHINE_BIT + SEQUENCE_BIT) | datacenterId << DATACENTER_BIT | machineId << MACHINE_BIT | sequence << SEQUENCE_BIT) + " add -> ");
            // 若在相同毫秒内 序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            // 若在不同毫秒内 则序列号置为0
            sequence = 0L;
        }
        lastStmp = currStmp;
        return (currStmp - START_STMP) << (DATACENTER_BIT + MACHINE_BIT + SEQUENCE_BIT) // 时间戳部分
                | datacenterId << DATACENTER_BIT // 机房id部分
                | machineId << MACHINE_BIT // 机器id部分
                | sequence << SEQUENCE_BIT; // 序列号部分
    }

    // 获取新的毫秒数
    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    // 获取当前的毫秒数
    private long getNewstmp() {
        return System.currentTimeMillis();
    }
}
