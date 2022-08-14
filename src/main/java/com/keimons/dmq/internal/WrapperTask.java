package com.keimons.dmq.internal;

import com.keimons.dmq.core.Interceptor;
import com.keimons.dmq.core.Wrapper;

/**
 * 可运行的拦截器
 * <p>
 * 它包含了任务相关的所有信息，根据这些信息，可以对任务的执行做一些小手脚。这些任务信息包含：
 * <ol>
 *     <li>任务唯一序列，任务被添加到事件总线时，会被分配一个唯一ID；</li>
 *     <li>任务；</li>
 *     <li>任务执行屏障（可能有多个）；</li>
 *     <li>任务执行线程（可能有多个）；</li>
 *     <li>拦截器信息。</li>
 * </ol>
 * 在多线程环境下，它有可能会被多个工作线程持有，持有的形式包括：
 * <ul>
 *     <li>执行屏障，此时仅作为屏障，当拦截器释放时，屏障移除。</li>
 *     <li>缓存节点，当节点无法重排序到屏障之前时，将节点缓存，等待屏障释放后才能开始处理此节点。</li>
 *     <li>执行任务，此任务由最后一个碰到它的线程执行。</li>
 * </ul>
 * 同一个拦截器可以被多个线程持有，但每个线程所持的形式同时只会存在一种，这三种状态是相互冲突的。
 * <p>
 * 同时，可运行的拦截器可以判断一个任务是否能由此线程处理，只有指定的执行线程才能处理这个任务，否则，忽略这个它。
 * <p>
 * <p>
 * 可运行的拦截器，是整个设计的核心，它体现了最重要的两个概念：
 * <ol>
 *     <li>带有相同执行屏障的任务必须串行执行；</li>
 *     <li>带有不同执行屏障的任务可以重排序执行。</li>
 * </ol>
 * 串行设计，保证执行的稳定性，提升系统的吞吐量。重排序执行
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public interface WrapperTask extends Interceptor, Wrapper<Runnable> {

	/**
	 * 返回任务屏障的数量
	 * <p>
	 * Explorer的任务执行时，需要一个任务屏障，这个方法返回任务屏障数量。
	 *
	 * @return 任务屏障的数量
	 */
	int size();

	/**
	 * 返回其它可执行的拦截器是否能越过当前的提前执行
	 * <p>
	 * 设计语言：
	 * 如果任务屏障完全不同，则可以重排序执行，这对最终的结果不会产生影响。
	 *
	 * @param other 尝试越过此节点的其它节点
	 * @return {@code true}允许越过当前节点重排序运行，{@code false}禁止越过当前节点重排序运行。
	 */
	boolean isAdvance(WrapperTask other);

	/**
	 * 唤醒任务
	 */
	void weakUp();

	/**
	 * 装载任务
	 * <p>
	 * 将任务装载至任务处理器，并在将来的合适的时机执行。
	 */
	void load();
}