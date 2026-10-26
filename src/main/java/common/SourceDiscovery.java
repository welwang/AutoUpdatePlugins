package common;

import com.fasterxml.jackson.databind.JsonNode;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** Finds sources from installed bytes and embedded project URLs, never from a name alone. */
public final class SourceDiscovery {
    private final String apiBase;
    public SourceDiscovery() { this("https://api.modrinth.com/v2"); }
    SourceDiscovery(String apiBase) { this.apiBase = apiBase; }

    public List<String> discover(Path plugins, File listFile, boolean apply) throws IOException {
        List<String> messages = new ArrayList<>();
        if (plugins == null || !Files.isDirectory(plugins)) return Collections.singletonList("找不到插件資料夾。");
        Set<String> managed = managedNames(listFile);
        List<Path> jars = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(plugins, "*.jar")) {
            for (Path path : stream) if (Files.isRegularFile(path)) jars.add(path);
        }
        jars.sort(Comparator.comparing(path -> path.getFileName().toString()));
        int added = 0;
        for (Path jar : jars) {
            if (Thread.currentThread().isInterrupted()) break;
            Descriptor descriptor;
            try { descriptor = readDescriptor(jar); }
            catch (IOException | RuntimeException invalid) { continue; }
            if (descriptor == null || managed.contains(normalize(descriptor.name))
                    || "autoupdateplugins".equals(normalize(descriptor.name))) continue;
            String source = null;
            String evidence = "";
            String note = null;
            try {
                JsonNode version = ProviderSupport.getJson(apiBase + "/version_file/" + JarMetadata.sha1(jar) + "?algorithm=sha1");
                String project = ProviderSupport.text(version, "project_id");
                if (project != null && project.matches("[A-Za-z0-9_-]+")) {
                    source = "https://modrinth.com/plugin/" + project;
                    List<String> loaders = ProviderSupport.strings(version.path("loaders"));
                    if (loaders.size() == 1) source += "?loader=" + ProviderSupport.encode(loaders.get(0));
                    evidence = "已安裝檔案的 SHA-1 與 Modrinth 完全相符";
                }
            } catch (IOException unavailable) {
                note = "Modrinth 未辨識此檔案或暫時無法連線";
            }
            if (source == null) {
                source = canonicalSource(descriptor.website);
                if (source != null) evidence = "來自已安裝插件內的官方專案網址";
            }
            if (source == null) {
                messages.add(descriptor.name + "：尚未確認來源；保留原檔。" + (note == null ? "" : " " + note));
                try {
                    JsonNode results = ProviderSupport.getJson(apiBase + "/search?query="
                            + ProviderSupport.encode(descriptor.name) + "&limit=3&facets="
                            + ProviderSupport.encode("[[\"project_type:plugin\"]]"));
                    for (JsonNode hit : results.path("hits")) {
                        String slug = ProviderSupport.text(hit, "slug");
                        if (slug != null && slug.matches("[A-Za-z0-9_-]+"))
                            messages.add("  候選（需核對作者）：" + ProviderSupport.text(hit, "title")
                                    + " / " + ProviderSupport.text(hit, "author") + " https://modrinth.com/plugin/" + slug);
                    }
                } catch (IOException unavailable) { /* An unverified name never becomes an update source. */ }
                continue;
            }
            if (apply && appendIfUnmanaged(listFile, descriptor.name, source)) {
                added++;
                managed.add(normalize(descriptor.name));
            }
            messages.add(descriptor.name + " -> " + source + "（" + evidence + "）");
        }
        messages.add(apply ? "來源搜尋完成，新增 " + added + " 個來源；已設定或停用的項目均保留。"
                : "來源預覽完成；使用 discover apply 儲存可確認的來源。");
        return messages;
    }

    static synchronized boolean appendIfUnmanaged(File listFile, String name, String source) throws IOException {
        if (managedNames(listFile).contains(normalize(name))) return false;
        if (name == null || !name.matches("[A-Za-z0-9_. -]+")) return false;
        Path path = listFile.toPath().toAbsolutePath();
        Files.createDirectories(path.getParent());
        byte[] before = Files.exists(path) ? Files.readAllBytes(path) : new byte[0];
        String content = new String(before, StandardCharsets.UTF_8);
        String newline = content.contains("\r\n") ? "\r\n" : "\n";
        content += newline + name + ": \"" + source.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" + newline;
        Path temporary = Files.createTempFile(path.getParent(), "aup-discovery-", ".tmp");
        try {
            Files.write(temporary, content.getBytes(StandardCharsets.UTF_8));
            byte[] current = Files.exists(path) ? Files.readAllBytes(path) : new byte[0];
            if (!Arrays.equals(before, current)) throw new IOException("list.yml 已被其他操作修改，請重新搜尋。");
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unsupported) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
        return true;
    }

    private static Set<String> managedNames(File list) {
        Set<String> names = new HashSet<>();
        for (String name : ListEntryLoader.loadEntries(list).keySet()) names.add(normalize(name));
        return names;
    }

    static String canonicalSource(String website) {
        if (website == null) return null;
        try {
            URI uri = new URI(website.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || (uri.getPort() != -1 && uri.getPort() != 443)) return null;
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            String path = uri.getPath();
            if (path == null) return null;
            String[] parts = path.replaceFirst("^/", "").split("/");
            if ("github.com".equals(host) && parts.length >= 2
                    && parts[0].matches("[A-Za-z0-9_-]+") && parts[1].matches("[A-Za-z0-9_.-]+"))
                return "https://github.com/" + parts[0] + "/" + parts[1].replaceFirst("\\.git$", "");
            if ("modrinth.com".equals(host) && parts.length >= 2
                    && Arrays.asList("plugin", "project", "mod").contains(parts[0]) && parts[1].matches("[A-Za-z0-9_-]+"))
                return "https://modrinth.com/plugin/" + parts[1];
            if ("hangar.papermc.io".equals(host) && parts.length >= 2
                    && parts[0].matches("[A-Za-z0-9_-]+") && parts[1].matches("[A-Za-z0-9_.-]+"))
                return "https://hangar.papermc.io/" + parts[0] + "/" + parts[1];
            if (("www.spigotmc.org".equals(host) || "spigotmc.org".equals(host))
                    && path.matches("/resources/[^/]*[0-9]+/?"))
                return "https://www.spigotmc.org" + path.replaceAll("/+$", "") + "/";
        } catch (Exception invalid) { return null; }
        return null;
    }

    private static Descriptor readDescriptor(Path path) throws IOException {
        try (JarFile jar = new JarFile(path.toFile())) {
            for (String file : Arrays.asList("plugin.yml", "paper-plugin.yml", "bungee.yml", "velocity-plugin.json")) {
                JarEntry entry = jar.getJarEntry(file);
                if (entry == null || entry.getSize() > 65536) continue;
                byte[] bytes;
                try (InputStream in = jar.getInputStream(entry); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        if (out.size() + count > 65536) throw new IOException("Plugin metadata is too large");
                        out.write(buffer, 0, count);
                    }
                    bytes = out.toByteArray();
                }
                if (file.endsWith(".json")) {
                    JsonNode json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(bytes);
                    String name = ProviderSupport.text(json, "id");
                    if (name == null) name = ProviderSupport.text(json, "name");
                    if (name != null) return new Descriptor(name, ProviderSupport.text(json, "url"));
                } else {
                    Object yaml = new Yaml().load(new String(bytes, StandardCharsets.UTF_8));
                    if (!(yaml instanceof Map)) continue;
                    Map<?, ?> values = (Map<?, ?>) yaml;
                    if (values.get("name") != null) return new Descriptor(String.valueOf(values.get("name")),
                            values.get("website") == null ? null : String.valueOf(values.get("website")));
                }
            }
        }
        return null;
    }

    private static String normalize(String name) { return name == null ? "" : name.trim().toLowerCase(Locale.ROOT); }
    private static final class Descriptor {
        final String name, website;
        Descriptor(String name, String website) { this.name = name; this.website = website; }
    }
}
