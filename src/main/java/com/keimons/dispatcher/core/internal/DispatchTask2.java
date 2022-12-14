package com.keimons.dispatcher.core.internal;

import com.keimons.dispatcher.core.DispatchTask;
import com.keimons.dispatcher.core.Handler;
import com.keimons.dispatcher.core.Sequencer;

/**
 * 带有2个执行屏障的调度任务
 *
 * @author houyn[monkey@keimons.com]
 * @version 1.0
 * @since 17
 */
public class DispatchTask2 extends AbstractDispatchTask {

	final Object fence0;

	final Object fence1;

	final Sequencer sequencer0;

	final Sequencer sequencer1;

	public DispatchTask2(Handler<Runnable> handler, Runnable task, Object fence0, Object fence1,
						 Sequencer sequencer0, Sequencer sequencer1) {
		super(handler, task, sequencer0 == sequencer1 ? 1 : 2);
		this.fence0 = fence0;
		this.fence1 = fence1;
		this.sequencer0 = sequencer0;
		this.sequencer1 = sequencer1;
	}

	@Override
	public Object[] fences() {
		return new Object[]{fence0, fence1};
	}

	@Override
	public void activateTask() {
		sequencer0.activate(this);
		sequencer1.activate(this);
	}

	@Override
	public boolean dependsOn(DispatchTask task) {
		return task.dependsOn(fence0, fence1);
	}

	@Override
	public boolean dependsOn(Object fence) {
		return fence0.equals(fence) || fence1.equals(fence);
	}
}
