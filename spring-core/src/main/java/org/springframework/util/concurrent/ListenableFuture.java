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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Extend {@link Future} with the capability to accept completion callbacks.
 * If the future has completed when the callback is added, the callback is
 * triggered immediately.
 *
 * <p>Inspired by {@code com.google.common.util.concurrent.ListenableFuture}.
 *
 * @author Arjen Poutsma
 * @author Sebastien Deleuze
 * @author Juergen Hoeller
 * @since 4.0
 * @param <T> the result type returned by this Future's {@code get} method
 */

/**
 * 备注
 * Future.java
 * public interface Future<V> {
 *
 *     // 获取异步执行的结果，如果没有结果可用，此方法会阻塞直到异步计算完成。
 *     V get() throws InterruptedException, ExecutionException;
 *
 *     // 获取异步执行结果，如果没有结果可用，此方法会阻塞，但是会有时间限制，如果阻塞时间超过设定的 timeout 时间，该方法将抛出异常。
 *     V get(long timeout, TimeUnit unit)
 *         throws InterruptedException, ExecutionException, TimeoutException;
 *
 *     // 如果任务执行结束，无论是正常结束或是中途取消还是发生异常，都返回 true 。
 *     boolean isDone();
 *
 *     // 如果任务完成前被取消，则返回 true 。
 *     boolean isCancelled();
 *
 *     // 如果任务还没开始，执行 cancel(...) 方法将返回 false；
 *     // 如果任务已经启动，执行 cancel(true) 方法将以中断执行此任务线程的方式来试图停止任务，如果停止成功，返回 true ；
 *     // 当任务已经启动，执行c ancel(false) 方法将不会对正在执行的任务线程产生影响(让线程正常执行到完成)，此时返回 false ；
 *     // 当任务已经完成，执行 cancel(...) 方法将返回 false 。
 *     // mayInterruptRunning 参数表示是否中断执行中的线程。
 *     boolean cancel(boolean mayInterruptIfRunning);
 *
 * }
 *
 * @param <T>
 */
public interface ListenableFuture<T> extends Future<T> {

	/**
	 * Register the given {@code ListenableFutureCallback}.
	 * @param callback the callback to register
	 */
	// 添加回调方法，统一处理成功和异常的情况。
	void addCallback(ListenableFutureCallback<? super T> callback);

	/**
	 * Java 8 lambda-friendly alternative with success and failure callbacks.
	 * @param successCallback the success callback
	 * @param failureCallback the failure callback
	 * @since 4.1
	 */
	// 添加成功和失败的回调方法，分别处理成功和异常的情况。
	void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback);


	/**
	 * Expose this {@link ListenableFuture} as a JDK {@link CompletableFuture}.
	 * @since 5.0
	 */
	// 将 ListenableFuture 转换成 JDK8 提供的 CompletableFuture 。
	// 这样，后续我们可以使用 ListenableFuture 来设置回调
	// 不了解 CompletableFuture 的胖友，可以看看 https://colobu.com/2016/02/29/Java-CompletableFuture/ 文章。
	default CompletableFuture<T> completable() {
		CompletableFuture<T> completable = new DelegatingCompletableFuture<>(this);
		// todo why this can work?
		addCallback(completable::complete, completable::completeExceptionally);
		return completable;
	}

}
