package com.keimons.dispatcher.core;

import java.util.concurrent.locks.LockSupport;

/**
 * 任务处理器
 * <p>
 * 任务处理器可以用于构造多级任务的处理。任务处理器执行任务，并不严格要求是异步执行，在最简单的情况下，
 * 可以在提交者线程直接执行任务：
 * <pre>{@code
 * class DirectHandler implements Handler<Runnable> {
 *     public void handle(Wrapper<Runnable> wrapperTask) {
 *         wrapperTask.invoke();
 *     }
 * }
 * }</pre>
 * 同样，也可以适配 {@link java.util.concurrent.ThreadPoolExecutor 线程池执行器} 和
 * {@link java.util.concurrent.ScheduledThreadPoolExecutor 调度线程池执行器}，
 * 使用线程池来处理任务。
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface Handler<T> {

	/**
	 * 处理任务
	 * <p>
	 * 将在调用者线程或将来的某个时刻执行这个任务。
	 *
	 * @param wrapperTask 任务/消息的包装
	 */
	void handle0(Wrapper<T> wrapperTask);

	/**
	 * 处理任务
	 * <p>
	 * 将在调用者线程或将来的某个时刻执行这个任务。
	 * 判断是否抛出{@link BlockingCaller#INVALID 占位}异常，如果是占位异常，则重新执行。
	 *
	 * @param wrapperTask 任务/消息的包装
	 */
	default void handle(Wrapper<T> wrapperTask) {
		for (; ; ) {
			try {
				handle0(wrapperTask);
				break;
			} catch (Throwable cause) {
				if (cause == BlockingCaller.INVALID) {
					LockSupport.parkNanos(1);
					continue;
				}
				throw cause;
			}
		}
	}
}
