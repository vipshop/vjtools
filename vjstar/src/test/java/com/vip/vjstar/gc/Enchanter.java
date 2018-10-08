package com.vip.vjstar.gc;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vip.vjtools.vjkit.number.RandomUtil;

public class Enchanter {
	private static final Logger log = LoggerFactory.getLogger(Enchanter.class);
	private static final String[] ha = { "苟利国家生死以%d岂因祸福趋避之", "煮豆燃豆萁豆在釜中泣%d本是同根生相煎何太急", "利欲驱人万火牛%d江湖浪迹一沙鸥",
			"且持梦笔书奇景%d日破云涛万里红", "春来我不先开口%d哪个虫儿敢作声" };

	private List<String> garbage = new ArrayList<>();

	public void makeGarbage(String val) {
		log.info("trying to occupy oldGen");
		int size = Integer.parseInt(val);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			// randomize a little bit to avoid duplicate strings
			sb.append(String.format(ha[RandomUtil.nextInt(i % ha.length + 1)], i));
		}
		// 大对象直接进old gen，用list hold住不释放
		garbage.add(sb.toString());
		log.info("Enchanter is littering around, garbage size: {}", sb.length());
	}

	public void clearGarbage() {
		// 清空list以便cms可以回收
		garbage.clear();
		log.info("garbage cleared from list...");
	}

}