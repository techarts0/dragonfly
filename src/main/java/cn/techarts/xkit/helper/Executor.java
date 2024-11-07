package cn.techarts.xkit.helper;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;

public class Executor {
	
	private static boolean asyncModeEnabled = true;
	
	public static void disableAsyncMode() {
		asyncModeEnabled = false;
	}
	
	public static void enableAsyncMode() {
		asyncModeEnabled = true;
	}
	
	public static boolean isAsyncMode() {
		return asyncModeEnabled;
	}
	
	public static void execute(Runnable task, Runnable resultHandler) {
		if(!asyncModeEnabled) {
			task.run(); 
		}else {
			Optional<Runnable> handler = Optional.of(resultHandler);
			CompletableFuture.runAsync(task).thenAccept(result->handler.get().run());
		}
	}
	
	public static<T> void execute(Supplier<T> task) {
		if(!asyncModeEnabled) {
			task.get();
		}else {
			CompletableFuture.supplyAsync(task);
		}
	}
}