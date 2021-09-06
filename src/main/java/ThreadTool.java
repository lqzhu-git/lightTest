import java.util.concurrent.*;

/**
 * @author EFL-tjl
 */
public class ThreadTool {
    /**
     * 普通线程池
     */
    private static ExecutorService threadPool;

    /**
     * 一次执行一个任务的线程池，同时等待队列也只能有一个，多余提交会被忽略
     * 目前只用于gcode变化相应任务和三维界面重绘
     */
    private static ExecutorService singleThreadPool;

    public static void init() {
        threadPool = new ThreadPoolExecutor(10, 10,
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.DiscardPolicy());

        singleThreadPool = new ThreadPoolExecutor(1, 1,
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.DiscardPolicy());

    }

    /**
     * 用于可以取消的提交，不在意执行返回值
     * @param task runnable任务
     * @return 可以调用取消方法的Future类，使用Future.cancel(true)，runnable任务会取消
     */
    public static Future<?> submit(Runnable task) {
        return threadPool.submit(task);
    }

    /**
     * 用于需要等待并获取执行结果的提交，也可以取消
     * @param task 可以抛出异常的Callable
     * @param <T> 泛型-指定返回值的类型
     * @return 可以使用get()获取执行结果的Future类，也可以取消
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return threadPool.submit(task);
    }

    /**
     * 不在意结果和运行情况的提交，似乎不能取消
     * @param command Runnable任务
     */
    public static void execute(Runnable command) {
        threadPool.execute(command);
    }

    /**
     * 提交单线程任务
     * @param runnable 任务
     */
    public static void runOrCancel(Runnable runnable) {
        singleThreadPool.execute(runnable);
    }
}
