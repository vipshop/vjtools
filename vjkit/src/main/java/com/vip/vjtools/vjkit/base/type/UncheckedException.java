package com.vip.vjtools.vjkit.base.type;

/**
 * CheckedException的wrapper.
 * 
 * 返回Message时, 将返回内层Exception的Message.
 */
public class UncheckedException extends RuntimeException {

	private static final long serialVersionUID = 4140223302171577501L;

	public UncheckedException(Throwable wrapped) {
		super(wrapped);
	}

	@Override
	public String getMessage() {
		return super.getCause().getMessage();
	}
}