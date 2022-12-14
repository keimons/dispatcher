package com.keimons.dispatcher.test;

import com.keimons.dispatcher.core.CompositeHandler;
import com.keimons.dispatcher.core.Dispatchers;
import com.keimons.dispatcher.core.internal.DefaultCompositeHandler;
import com.keimons.dispatcher.core.internal.SerialMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 调度器退出测试
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class CompositeHandlerExitTest {

	private static final int N_THREAD = 10;

	@Test
	public void test() {
		ThreadGroup group = new ThreadGroup("test");
		int count = group.activeCount();
		CompositeHandler<?> dispatch = new DefaultCompositeHandler<>(N_THREAD, 0, N_THREAD,
				2028, 8192, SerialMode.producer(), ThreadUtils.createThreadFactory(group),
				Dispatchers.DEFAULT_DIRECT_HANDLER
		);
		int newCount = group.activeCount();
		Assertions.assertEquals(newCount, count + N_THREAD, "[调度器][正常退出] 创建线程数量错误");
		long startTime = System.currentTimeMillis();
		dispatch.dispatch(() -> TimeUtils.SECONDS.sleep(3));
		dispatch.shutdown();
		long useTime = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(3000 <= useTime && useTime <= 3020, "[调度器][正常退出] 线程关闭时长：" + useTime);
		Assertions.assertEquals(count, group.activeCount(), "[调度器][正常退出] 销毁线程数量错误");
	}

	@Test
	public void testTimeout() {
		CompositeHandler<?> dispatch = Dispatchers.newCompositeHandler(N_THREAD, Dispatchers.DEFAULT_DIRECT_HANDLER);
		dispatch.dispatch(() -> TimeUtils.SECONDS.sleep(3));
		Assertions.assertThrows(
				TimeoutException.class,
				() -> dispatch.shutdown(0, TimeUnit.MILLISECONDS),
				"[调度器][超时退出] 超时退出异常"
		);
	}
}
