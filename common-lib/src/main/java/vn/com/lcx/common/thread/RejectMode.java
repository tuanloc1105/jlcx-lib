package vn.com.lcx.common.thread;

public enum RejectMode {
    /**
     * <p>AbortPolicy</p>
     * <ol>
     *     <li>
     *         <p><b>Behavior:</b> When the thread pool is saturated, {@code AbortPolicy} throws a {@code RejectedExecutionException}.</p>
     *         <p><b>Purpose:</b> This is the default policy for {@code ThreadPoolExecutor}. It's designed to signal immediately that the system is overloaded and cannot process additional requests.</p>
     *         <p>
     *             <b>When to Use</b>:
     *             <ul>
     *                 <li>When you want a fail-fast approach to avoid potential system hangs or incorrect processing due to overload.</li>
     *                 <li>When you have an exception handling mechanism in place to catch RejectedExecutionException and take appropriate actions (e.g., logging, alerting, using a fallback).</li>
     *             </ul>
     *         </p>
     *         <p><b>Example:</b></p>
     *         <pre> {@code
     *             ExecutorService executor = new ThreadPoolExecutor(
     *                 10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(5), new ThreadPoolExecutor.AbortPolicy());
     *
     *             for (int i = 0; i < 20; i++) {
     *                 try {
     *                     executor.execute(() -> {
     *                         // Some work
     *                         try {
     *                             Thread.sleep(1000);
     *                         } catch (InterruptedException e) {
     *                             Thread.currentThread().interrupt();
     *                         }
     *                     });
     *                 } catch (RejectedExecutionException e) {
     *                     System.out.println("Task rejected due to thread pool overload!");
     *                 }
     *             }
     *         }</pre>
     *     </li>
     * </ol>
     */
    ABORT_POLICY,

    /**
     * <p>CallerRunsPolicy</p>
     * <ol>
     *     <li>
     *         <p><b>Behavior:</b> Instead of rejecting the task, {@code CallerRunsPolicy} executes the task directly on the thread that called the {@code execute()} method.</p>
     *         <p><b>Purpose:</b> To reduce the load on the thread pool by "borrowing" the caller's thread to process the task. It doesn't lose the task but slows down task submission.</p>
     *         <p>
     *             <b>When to Use</b>:
     *             <ul>
     *                 <li>When you want to avoid losing tasks without throwing an exception.</li>
     *                 <li>When you can tolerate a slight delay in the caller thread's execution.</li>
     *                 <li>When you want to create a natural back-pressure mechanism. When the thread pool is full, adding tasks slows down, creating back pressure on the caller.</li>
     *             </ul>
     *         </p>
     *         <p><b>Example:</b></p>
     *         <pre> {@code
     *             ExecutorService executor = new ThreadPoolExecutor(
     *                 10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(5), new ThreadPoolExecutor.CallerRunsPolicy());
     *
     *             for (int i = 0; i < 20; i++) {
     *                 executor.execute(() -> {
     *                     System.out.println("Task executed on thread: " + Thread.currentThread().getName());
     *                     try {
     *                         Thread.sleep(1000);
     *                     } catch (InterruptedException e) {
     *                     Thread.currentThread().interrupt();
     *                     }
     *                 });
     *             }
     *         }</pre>
     *     </li>
     * </ol>
     */
    CALLER_RUNS_POLICY,

    /**
     * <p>DiscardOldestPolicy</p>
     * <ol>
     *     <li>
     *         <p><b>Behavior:</b> When the thread pool is full, {@code DiscardOldestPolicy} discards the <i>oldest</i> task currently waiting in the queue and adds the new task.</p>
     *         <p><b>Purpose:</b> Prioritizes newer tasks, assuming they might be more important than older tasks that have been waiting longer.</p>
     *         <p>
     *             <b>When to Use</b>:
     *             <ul>
     *                 <li>When you have tasks that are time-sensitive, where older tasks may become irrelevant.</li>
     *                 <li>When you want to keep the queue from getting clogged by old tasks and prioritize newer ones.</li>
     *                 <li>Note that this policy may result in some tasks never being executed (the discarded ones).er.</li>
     *             </ul>
     *         </p>
     *         <p><b>Example:</b></p>
     *         <pre> {@code
     *             ExecutorService executor = new ThreadPoolExecutor(
     *                 10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardOldestPolicy());
     *             for(int i = 0; i < 20; i++) {
     *             final int taskNumber = i;
     *             executor.execute(()->{
     *                 System.out.println("Task " + taskNumber + " is running");
     *                 try {
     *                         Thread.sleep(1000);
     *                     } catch (InterruptedException e) {
     *                     Thread.currentThread().interrupt();
     *                     }
     *             });
     *             }
     *         }</pre>
     *     </li>
     * </ol>
     */
    DISCARD_OLDEST_POLICY,

    /**
     * <p>DiscardPolicy</p>
     * <ol>
     *     <li>
     *         <p><b>Behavior:</b> When the thread pool is full, {@code DiscardPolicy} silently discards the <i>newest</i> task that was just submitted.</p>
     *         <p><b>Purpose:</b> To simply ignore the newest task to avoid overload, without any exception handling or notification.</p>
     *         <p>
     *             <b>When to Use</b>:
     *             <ul>
     *                 <li>When you are willing to lose some tasks without any indication.</li>
     *                 <li>When the tasks are not critical and their omission does not have serious consequences.</li>
     *                 <li>When you have another system to ensure data integrity (e.g., using a retry mechanism at the task submission).</li>
     *             </ul>
     *         </p>
     *         <p><b>Example:</b></p>
     *         <pre> {@code
     *             ExecutorService executor = new ThreadPoolExecutor(
     *             10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardPolicy());
     *             for(int i = 0; i < 20; i++) {
     *                 final int taskNumber = i;
     *                 executor.execute(()->{
     *                 System.out.println("Task " + taskNumber + " is running");
     *                 try {
     *                     Thread.sleep(1000);
     *                 } catch (InterruptedException e) {
     *                     Thread.currentThread().interrupt();
     *                 }
     *                 });
     *             }
     *         }</pre>
     *     </li>
     * </ol>
     */
    DISCARD_POLICY,
}
