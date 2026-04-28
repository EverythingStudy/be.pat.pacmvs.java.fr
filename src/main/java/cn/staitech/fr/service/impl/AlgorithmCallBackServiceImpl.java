package cn.staitech.fr.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.staitech.fr.service.AlgorithmCallBackService;
import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author admin
 * @description 针对表【fr_ai_forecast】的数据库操作Service实现
 * @createDate 2024-04-09 14:42:38
 */

@Service
@Slf4j
public class AlgorithmCallBackServiceImpl  implements AlgorithmCallBackService {
	private static final ExecutorService EXECUTOR = ExecutorBuilder.create().setCorePoolSize(Runtime.getRuntime().availableProcessors()).setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2).setKeepAliveTime(0).setWorkQueue(new LinkedBlockingQueue<Runnable>(4096)).build();

	@Resource
	private JsonTaskParserService jsonTaskParserService;

	@Override
	public void input(String data) {
		EXECUTOR.submit(new RecognitionThread(data));
	}

	class RecognitionThread implements Runnable {
		private final String data;

		public RecognitionThread(String data) {
			this.data = data;
		}

		@Override
		public void run() {
			try {
				process(this.data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void process(String data){
		Long startTime = System.currentTimeMillis();
		jsonTaskParserService.input(data);
		log.info("AsyncSave-Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
	}

}




