package com.vip.vjtools.vjkit.enums;

/**
 * ����ģʽ
 * @author haven.zhang
 */
public enum EncryptedMode {

	/**
	 * ���뱾ģʽ��Electronic Codebook Book (ECB) ÿһ�鶼ʹ����ͬ����Կ���м���
	 */
	ECB,
	/**
	 * �����������ģʽ��Cipher Block Chaining (CBC)�� �ڵ�һ��������Ҫʹ�ó�ʼ������IV
	 * ÿ�����Ŀ�����ǰһ�����Ŀ���������ٽ��м���
	 */
	CBC,
	/**
	 * �����������ӣ�PCBC��Propagating cipher-block chaining�����Ϊ������������ӣ�Plaintext cipher-block chaining��
	 */
	PCBC,
	/**
	 * ������ģʽ��Counter (CTR)��
	 */
	CTR,

	/**
	 * ���뷴��ģʽ��Cipher FeedBack (CFB)��
	 * CFB�ļ��ܹ�����Ϊ�����֣�
	 * 1.��һǰ�μ��ܵõ��������ټ��ܣ�
	 * 2.����1�����ܵõ��������뵱ǰ�ε��������
	 */
	CFB,
	/**
	 * �� CFB һ��������ʹ�� 8 λģʽ�����Ƽ���
	 */
	CFB8,
	/**
	 * �������ģʽ��Output FeedBack (OFB)��
	 */
	OFB,
	/**
	 * �� OFB һ��������ʹ�� 8 λģʽ�����Ƽ���
	 */
	OFB8;
}
