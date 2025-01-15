package cn.techarts.xkit.app.llm;

import javax.inject.Singleton;

import cn.techarts.whale.Bind;

@Singleton
@Bind(value=GptHelper.class, target=DefaultGptHelper.class)
public class DefaultGptHelper extends LLMConfig implements GptHelper {
	
	private GptHelper executor;
	
	@Override
	public Answer ask(Question question) {
		this.initExecutor();
		question.setModel(getModel());
		return executor.ask(question);
	}
	
	private void initExecutor() {
		if(executor != null) return;
		switch(getProvider()) {
			case "KIMI":
				executor = new OpenAICompatibleExecutor(this); break;
			case "OPENAI":
				executor = new OpenAICompatibleExecutor(this); break;
			case "ZHIPU":
				executor = new OpenAICompatibleExecutor(this); break;
			case "QWEN":
				executor = new OpenAICompatibleExecutor(this); break;
			case "WENXIN":
				executor = new OpenAICompatibleExecutor(this); break;
			default:
				throw new RuntimeException("Unsupported LLM: " + getProvider());
		}
	}
}