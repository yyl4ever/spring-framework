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

package org.springframework.scheduling.annotation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.lang.Nullable;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;

/**
 * A pass-through {@code Future} handle that can be used for method signatures
 * which are declared with a {@code Future} return type for asynchronous execution.
 *
 * <p>As of Spring 4.1, this class implements {@link ListenableFuture}, not just
 * plain {@link java.util.concurrent.Future}, along with the corresponding support
 * in {@code @Async} processing.
 *
 * <p>As of Spring 4.2, this class also supports passing execution exceptions back
 * to the caller.
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.0
 * @param <V> the value type
 * @see Async
 * @see #forValue(Object)
 * @see #forExecutionException(Throwable)
 */
// AsyncResult 实现了 ListenableFuture 接口，提供异步执行结果的回调处理
// ListenableFuture 继承了 Future 接口，所以 AsyncResult 也需要实现 Future 接口
public class AsyncResult<V> implements ListenableFuture<V> {

	@Nullable
	private final V value;

	@Nullable
	private final Throwable executionException;


	/**
	 * Create a new AsyncResult holder.
	 * @param value the value to pass through
	 */
	public AsyncResult(@Nullable V value) {
		this(value, null);
	}

	/**
	 * Create a new AsyncResult holder.
	 * @param value the value to pass through
	 */
	private AsyncResult(@Nullable V value, @Nullable Throwable ex) {
		this.value = value;
		this.executionException = ex;
	}


	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// 因为是 AsyncResult 是执行结果，所以直接返回 false 表示取消失败。
		return false;
	}

	@Override
	public boolean isCancelled() {
		// 因为是 AsyncResult 是执行结果，所以直接返回 false 表示未取消。
		return false;
	}

	@Override
	public boolean isDone() {
		// 因为是 AsyncResult 是执行结果，所以直接返回 true 表示已完成。
		return true;
	}

	@Override
	@Nullable
	public V get() throws ExecutionException {
		// 如果发生异常，则抛出该异常。
		if (this.executionException != null) {
			throw (this.executionException instanceof ExecutionException ?
					(ExecutionException) this.executionException :
					new ExecutionException(this.executionException));
		}
		// 如果执行成功，则返回该 value 结果
		return this.value;
	}

	@Override
	@Nullable
	public V get(long timeout, TimeUnit unit) throws ExecutionException {
		return get();
	}

	@Override
	public void addCallback(ListenableFutureCallback<? super V> callback) {
		addCallback(callback, callback);
	}

	@Override
	public void addCallback(SuccessCallback<? super V> successCallback, FailureCallback failureCallback) {
		try {
			if (this.executionException != null) {
				// 如果是异常的结果，调用 FailureCallback 的回调。
				failureCallback.onFailure(exposedException(this.executionException));
			}
			else {
				// 如果是正常的结果，调用 SuccessCallback 的回调。
				successCallback.onSuccess(this.value);
			}
		}
		catch (Throwable ex) {
			// 如果回调的逻辑发生异常，直接忽略。
			// 所以，如果如果有多个回调，如果有一个回调发生异常，不会影响后续的回调。
			// Ignore
		}
	}

	@Override
	public CompletableFuture<V> completable() {
		if (this.executionException != null) {
			// 直接将结果包装成 CompletableFuture 对象。
			CompletableFuture<V> completable = new CompletableFuture<>();
			completable.completeExceptionally(exposedException(this.executionException));
			return completable;
		}
		else {
			return CompletableFuture.completedFuture(this.value);
		}
	}


	/**
	 * Create a new async result which exposes the given value from {@link Future#get()}.
	 * @param value the value to expose
	 * @since 4.2
	 * @see Future#get()
	 */
	public static <V> ListenableFuture<V> forValue(V value) {
		return new AsyncResult<>(value, null);
	}

	/**
	 * Create a new async result which exposes the given exception as an
	 * {@link ExecutionException} from {@link Future#get()}.
	 * @param ex the exception to expose (either an pre-built {@link ExecutionException}
	 * or a cause to be wrapped in an {@link ExecutionException})
	 * @since 4.2
	 * @see ExecutionException
	 */
	public static <V> ListenableFuture<V> forExecutionException(Throwable ex) {
		return new AsyncResult<>(null, ex);
	}

	/**
	 * Determine the exposed exception: either the cause of a given
	 * {@link ExecutionException}, or the original exception as-is.
	 * @param original the original as given to {@link #forExecutionException}
	 * @return the exposed exception
	 */
	// 从 ExecutionException 中，获得原始异常。
	private static Throwable exposedException(Throwable original) {
		if (original instanceof ExecutionException) {
			Throwable cause = original.getCause();
			if (cause != null) {
				return cause;
			}
		}
		return original;
	}

}
