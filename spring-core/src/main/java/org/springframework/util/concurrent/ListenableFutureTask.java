/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.springframework.lang.Nullable;

/**
 * Extension of {@link FutureTask} that implements {@link ListenableFuture}.
 *
 * @author Arjen Poutsma
 * @since 4.0
 * @param <T> the result type returned by this Future's {@code get} method
 */
public class ListenableFutureTask<T> extends FutureTask<T> implements ListenableFuture<T> {

	private final ListenableFutureCallbackRegistry<T> callbacks = new ListenableFutureCallbackRegistry<>();


	/**
	 * Create a new {@code ListenableFutureTask} that will, upon running,
	 * execute the given {@link Callable}.
	 * @param callable the callable task
	 */
	public ListenableFutureTask(Callable<T> callable) {
		super(callable);
	}

	/**
	 * Create a {@code ListenableFutureTask} that will, upon running,
	 * execute the given {@link Runnable}, and arrange that {@link #get()}
	 * will return the given result on successful completion.
	 * @param runnable the runnable task
	 * @param result the result to return on successful completion
	 */
	public ListenableFutureTask(Runnable runnable, @Nullable T result) {
		super(runnable, result);
	}


	@Override
	public void addCallback(ListenableFutureCallback<? super T> callback) {
		this.callbacks.addCallback(callback);
	}

	@Override
	public void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {
		// 暂存回调到 ListenableFutureCallbackRegistry 中
		this.callbacks.addSuccessCallback(successCallback);
		this.callbacks.addFailureCallback(failureCallback);
	}

	@Override
	public CompletableFuture<T> completable() {
		CompletableFuture<T> completable = new DelegatingCompletableFuture<>(this);
		this.callbacks.addSuccessCallback(completable::complete);
		this.callbacks.addFailureCallback(completable::completeExceptionally);
		return completable;
	}


	@Override
	protected void done() {
		Throwable cause;
		try {
			// <1> 获得执行结果
			T result = get();
			// <2.1> 执行成功，执行成功的回调
			this.callbacks.success(result);
			return;
		}
		catch (InterruptedException ex) {
			// 如果有中断异常 InterruptedException ，则打断当前线程，并直接返回
			Thread.currentThread().interrupt();
			return;
		}
		catch (ExecutionException ex) {
			// 如果有 ExecutionException 异常，获得其真实的异常，并设置到 cause 中
			cause = ex.getCause();
			if (cause == null) {
				cause = ex;
			}
		}
		catch (Throwable ex) {
			// 设置异常到 cause 中
			cause = ex;
		}
		// 执行异常，执行异常的回调
		this.callbacks.failure(cause);
	}

}
