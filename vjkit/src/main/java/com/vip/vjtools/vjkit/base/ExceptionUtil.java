package com.vip.vjtools.vjkit.base;

import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Throwables;
import com.vip.vjtools.vjkit.base.annotation.NotNull;
import com.vip.vjtools.vjkit.base.annotation.Nullable;
import com.vip.vjtools.vjkit.base.type.UncheckedException;
import com.vip.vjtools.vjkit.io.type.StringBuilderWriter;

/**
 * 关于异常的工具类.
 * 
 * 1. Checked/Uncheked及Wrap(如ExecutionException)的转换.
 * 
 * 2. 打印Exception的辅助函数. (其中一些来自Common Lang ExceptionUtils)
 * 
 * 3. 查找Cause(其中一些来自Guava Throwables)
 * 
 * 4. StackTrace性能优化相关，尽量使用静态异常避免异常生成时获取StackTrace(Netty)
 * 
 * @see com.vip.vjtools.vjkit.base.type.CloneableException
 */
public class ExceptionUtil {

	private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

	///// Checked/Uncheked及Wrap(如ExecutionException)的转换/////

	/**
	 * 将CheckedException转换为RuntimeException重新抛出, 可以减少函数签名中的CheckExcetpion定义.
	 * 
	 * CheckedException会用UndeclaredThrowableException包裹，RunTimeException和Error则不会被转变.
	 * 
	 * copy from Commons Lange 3.5 ExceptionUtils.
	 * 
	 * 虽然unchecked()里已直接抛出异常，但仍然定义返回值，方便欺骗Sonar。因此本函数也改变了一下返回值
	 * 
	 * 示例代码:
	 * 
	 * <pre>
	 * try{ ... }catch(Exception e){ throw unchecked(t); }
	 * </pre>
	 * 
	 * @see ExceptionUtils#wrapAndThrow(Throwable)
	 */
	public static RuntimeException unchecked(@Nullable Throwable t) {
		if (t instanceof RuntimeException) {
			throw (RuntimeException) t;
		}
		if (t instanceof Error) {
			throw (Error) t;
		}

		throw new UncheckedException(t);
	}

	/**
	 * 如果是著名的包裹类，从cause中获得真正异常. 其他异常则不变.
	 * 
	 * Future中使用的ExecutionException 与 反射时定义的InvocationTargetException， 真正的异常都封装在Cause中
	 * 
	 * 前面 unchecked() 使用的UncheckedException同理.
	 */
	public static Throwable unwrap(@Nullable Throwable t) {
		if (t instanceof UncheckedException || t instanceof java.util.concurrent.ExecutionException
				|| t instanceof java.lang.reflect.InvocationTargetException
				|| t instanceof UndeclaredThrowableException) {
			return t.getCause();
		}

		return t;
	}

	/**
	 * 组合unwrap与unchecked，用于处理反射/Callable的异常
	 */
	public static RuntimeException unwrapAndUnchecked(@Nullable Throwable t) {
		throw unchecked(unwrap(t));
	}

	////// 输出内容相关 //////

	/**
	 * 将StackTrace[]转换为String, 供Logger或e.printStackTrace()外的其他地方使用.
	 * 
	 * 为了使用StringBuilderWriter，没有用Throwables#getStackTraceAsString(Throwable)
	 */
	public static String stackTraceText(@NotNull Throwable t) {
		StringBuilderWriter stringWriter = new StringBuilderWriter();
		t.printStackTrace(new PrintWriter(stringWriter)); // NOSONAR
		return stringWriter.toString();
	}

	/**
	 * 拼装 短异常类名: 异常信息.
	 * 
	 * 与Throwable.toString()相比使用了短类名
	 * 
	 * @see ExceptionUtils#getMessage(Throwable)
	 */
	public static String toStringWithShortName(@Nullable Throwable t) {
		return ExceptionUtils.getMessage(t);
	}

	/**
	 * 拼装 短异常类名: 异常信息 <-- RootCause的短异常类名: 异常信息
	 */
	public static String toStringWithRootCause(@Nullable Throwable t) {
		if (t == null) {
			return StringUtils.EMPTY;
		}

		final String clsName = ClassUtils.getShortClassName(t, null);
		final String message = StringUtils.defaultString(t.getMessage());
		Throwable cause = getRootCause(t);

		StringBuilder sb = new StringBuilder(128).append(clsName).append(": ").append(message);
		if (cause != t) {
			sb.append("; <---").append(toStringWithShortName(cause));
		}

		return sb.toString();
	}

	////////// Cause 相关 /////////

	/**
	 * 获取异常的Root Cause.
	 * 
	 * 如无底层Cause, 则返回自身
	 * 
	 * @see Throwables#getRootCause(Throwable)
	 */
	public static Throwable getRootCause(@NotNull Throwable t) {
		return Throwables.getRootCause(t);
	}

	/**
	 * 获取某种类型的cause，如果没有则返回空
	 * 
	 * copy from Jodd ExceptionUtil
	 */
	public static <T extends Throwable> T findCause(@NotNull Throwable throwable, Class<T> cause) {
		while (throwable != null) {
			if (throwable.getClass().equals(cause)) {
				return (T) throwable;
			}
			throwable = throwable.getCause();
		}
		return null;
	}

	/**
	 * 判断异常是否由某些底层的异常引起.
	 */
	@SuppressWarnings("unchecked")
	public static boolean isCausedBy(@Nullable Throwable throwable,
			Class<? extends Exception>... causeExceptionClasses) {
		Throwable cause = throwable;

		while (cause != null) {
			for (Class<? extends Exception> causeClass : causeExceptionClasses) {
				if (causeClass.isInstance(cause)) {
					return true;
				}
			}
			cause = cause.getCause();
		}
		return false;
	}
	/////////// StackTrace 性能优化相关////////

	/**
	 * copy from Netty, 为静态异常设置StackTrace.
	 * 
	 * 对某些已知且经常抛出的异常, 不需要每次创建异常类并很消耗性能的并生成完整的StackTrace. 此时可使用静态声明的异常.
	 * 
	 * 如果异常可能在多个地方抛出，使用本函数设置抛出的类名和方法名.
	 * 
	 * <pre>
	 * private static RuntimeException TIMEOUT_EXCEPTION = ExceptionUtil.setStackTrace(new RuntimeException("Timeout"),
	 * 		MyClass.class, "mymethod");
	 * </pre>
	 */
	public static <T extends Throwable> T setStackTrace(@NotNull T throwable, Class<?> throwClass, String throwClazz) {
		throwable.setStackTrace(
				new StackTraceElement[] { new StackTraceElement(throwClass.getName(), throwClazz, null, -1) });
		return throwable;
	}

	/**
	 * 清除StackTrace. 假设StackTrace已生成, 但把它打印出来也有不小的消耗.
	 * 
	 * 如果不能控制StackTrace的生成，也不能控制它的打印端(如logger)，可用此方法暴力清除Trace.
	 * 
	 * 但Cause链依然不能清除, 只能清除每一个Cause的StackTrace.
	 */
	public static <T extends Throwable> T clearStackTrace(@NotNull T throwable) {
		Throwable cause = throwable;
		while (cause != null) {
			cause.setStackTrace(EMPTY_STACK_TRACE);
			cause = cause.getCause();
		}
		return throwable;
	}
}
