package com.vip.vjtools.vjkit.base.type;

import com.vip.vjtools.vjkit.base.ExceptionUtil;

/**
 * 适用于异常信息需要变更的情况, 可通过clone()，不经过构造函数（也就避免了获得StackTrace）地从之前定义的静态异常中克隆，再设定新的异常信息
 * 
 * private static CloneableException TIMEOUT_EXCEPTION = new CloneableException("Timeout") .setStackTrace(My.class,
 * "hello"); ...
 * 
 * throw TIMEOUT_EXCEPTION.clone("Timeout for 40ms");
 * 
 */
public class CloneableException extends Exception implements Cloneable {

	private static final long serialVersionUID = -6270471689928560417L;

	protected String message; // NOSONAR

	public CloneableException() {
		super((Throwable) null);
	}

	public CloneableException(String message) {
		super((Throwable) null);
		this.message = message;
	}

	public CloneableException(String message, Throwable cause) {
		super(cause);
		this.message = message;
	}

	@Override
	public CloneableException clone() { // NOSONAR
		try {
			return (CloneableException) super.clone();
		} catch (CloneNotSupportedException e) {// NOSONAR
			return null;
		}
	}

	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * 简便函数，定义静态异常时使用
	 */
	public CloneableException setStackTrace(Class<?> throwClazz, String throwMethod) {
		ExceptionUtil.setStackTrace(this, throwClazz, throwMethod);
		return this;
	}

	/**
	 * 简便函数, clone并重新设定Message
	 */
	public CloneableException clone(String message) {
		CloneableException newException = this.clone();
		newException.setMessage(message);
		return newException;
	}

	/**
	 * 简便函数, 重新设定Message
	 */
	public CloneableException setMessage(String message) {
		this.message = message;
		return this;
	}
}