package common;

import java.io.File;
import java.util.function.Consumer;

public final class DiscoveryCommandSupport {
    private DiscoveryCommandSupport() {}
    public static void execute(String[] args, PluginUpdater updater, File list,
                               Consumer<Runnable> dispatcher, Consumer<String> send) {
        if (args.length > 1 || (args.length == 1 && !"apply".equalsIgnoreCase(args[0]))) {
            send.accept("用法：discover [apply]；預覽或儲存已確認的下載來源。");
            return;
        }
        send.accept("正在上網辨識已安裝插件的更新來源，完成後會顯示結果。");
        updater.discoverSourcesAsync(list, args.length == 1, lines -> {
            try { dispatcher.accept(() -> lines.forEach(send)); }
            catch (RuntimeException stopped) { /* The server may have stopped while discovery was running. */ }
        });
    }
}
