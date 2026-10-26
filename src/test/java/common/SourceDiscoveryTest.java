package common;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import static org.junit.Assert.*;

public class SourceDiscoveryTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private ProviderTestServer server;
    @Before public void start() throws Exception { server = new ProviderTestServer(); }
    @After public void stop() { server.close(); }

    @Test public void exactFileHashRegistersProjectAndLoaderWithoutDownloadingPayload() throws Exception {
        Path plugins = temporary.newFolder("plugins").toPath();
        jar(plugins, "Example", null);
        Path list = plugins.resolve("list.yml");
        server.json("{\"project_id\":\"AbCd1234\",\"loaders\":[\"paper\"]}");
        new SourceDiscovery(server.url("/v2")).discover(plugins, list.toFile(), true);
        assertEquals("https://modrinth.com/plugin/AbCd1234?loader=paper", ListEntryLoader.loadEnabledLinks(list.toFile()).get("Example"));
        assertTrue(server.lastRequest.get().getPath().startsWith("/v2/version_file/"));
        assertEquals(1, server.hits.get());
    }
    @Test public void previewDoesNotChangeListAndDisabledEntriesStayDisabled() throws Exception {
        Path plugins = temporary.newFolder("plugins").toPath();
        jar(plugins, "Example", null);
        Path list = plugins.resolve("list.yml");
        Files.write(list, "# Example: https://modrinth.com/plugin/example\n".getBytes(StandardCharsets.UTF_8));
        byte[] before = Files.readAllBytes(list);
        new SourceDiscovery(server.url("/v2")).discover(plugins, list.toFile(), true);
        assertArrayEquals(before, Files.readAllBytes(list));
        assertEquals(0, server.hits.get());
    }
    @Test public void officialEmbeddedWebsiteWorksWhenHashIsUnknown() throws Exception {
        Path plugins = temporary.newFolder("plugins").toPath();
        jar(plugins, "Example", "https://github.com/Author/Example/releases");
        Path list = plugins.resolve("list.yml");
        server.failure(404, "{}");
        new SourceDiscovery(server.url("/v2")).discover(plugins, list.toFile(), false);
        assertFalse(Files.exists(list));
        new SourceDiscovery(server.url("/v2")).discover(plugins, list.toFile(), true);
        assertEquals("https://github.com/Author/Example", ListEntryLoader.loadEnabledLinks(list.toFile()).get("Example"));
    }
    @Test public void nameSearchOnlyProducesCandidatesAndNeverInstallsThem() throws Exception {
        Path plugins = temporary.newFolder("plugins").toPath();
        jar(plugins, "Example", null);
        Path list = plugins.resolve("list.yml");
        server.json("{\"hits\":[{\"slug\":\"example\",\"title\":\"Example\",\"author\":\"Somebody\"}]}");
        List<String> lines = new SourceDiscovery(server.url("/v2")).discover(plugins, list.toFile(), true);
        assertFalse(Files.exists(list));
        assertTrue(lines.stream().anyMatch(line -> line.contains("需核對作者")));
        assertEquals(2, server.hits.get());
    }
    @Test public void rejectsLookalikeHostsCredentialsAndPlainHttp() {
        assertNull(SourceDiscovery.canonicalSource("https://github.com.attacker.test/user/plugin"));
        assertNull(SourceDiscovery.canonicalSource("https://secret@github.com/user/plugin"));
        assertNull(SourceDiscovery.canonicalSource("http://github.com/user/plugin"));
        assertNull(SourceDiscovery.canonicalSource("https://github.com/user"));
        assertEquals("https://modrinth.com/plugin/example", SourceDiscovery.canonicalSource("https://modrinth.com/plugin/example/versions"));
    }
    @Test public void appendRechecksCaseInsensitiveDisabledNames() throws Exception {
        Path list = temporary.newFile("list.yml").toPath();
        Files.write(list, "# eXample: https://example.org\r\n".getBytes(StandardCharsets.UTF_8));
        assertFalse(SourceDiscovery.appendIfUnmanaged(list.toFile(), "Example", "https://github.com/Author/Example"));
        assertTrue(SourceDiscovery.appendIfUnmanaged(list.toFile(), "Other", "https://modrinth.com/plugin/other"));
        String text = new String(Files.readAllBytes(list), StandardCharsets.UTF_8);
        assertTrue(text.contains("\r\nOther:"));
    }
    private static void jar(Path folder, String name, String website) throws Exception {
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(folder.resolve(name + ".jar")))) {
            out.putNextEntry(new JarEntry("plugin.yml"));
            out.write(("name: " + name + "\nversion: 1.0\nmain: test.Example\n" + (website == null ? "" : "website: " + website + "\n")).getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
    }
}
