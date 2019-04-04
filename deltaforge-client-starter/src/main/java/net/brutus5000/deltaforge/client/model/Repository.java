package net.brutus5000.deltaforge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name"})
public class Repository {
    public static final String DELTAFORGE_CURRENT_TAG_FOLDER = "currentTag";
    public static final String DELTAFORGE_INITIAL_BASELINE_FOLDER = "initialBaseline";
    public static final String DELTAFORGE_PATCHES_SUBFOLDER = "patches";
    public static final String DELTAFORGE_TMP_SUBFOLDER = "temp";
    public static final String DELTAFORGE_INFO_FILE = "info.json";
    public static final String DELTAFORGE_PATCH_FILE_PATTERN = "{0}__to__{1}.{2}";

    @JsonIgnore
    private Path mainDirectory;
    private String id;
    private String name;
    private String currentTag;
    private Tag initialBaseline;
    private Integer protocolVersion;
    private String url;
    private String strategy;
    @JsonManagedReference("channels")
    private Set<Channel> channels = new HashSet<>();
    @JsonManagedReference("tags")
    private Set<Tag> tags = new HashSet<>();
    @JsonManagedReference("patches")
    private Set<Patch> patches = new HashSet<>();
    private String patchGraph;

    @JsonIgnore
    public Path getInfoPath() {
        return mainDirectory.resolve(DELTAFORGE_INFO_FILE);
    }

    @JsonIgnore
    public Path getCurrentTagFolder() {
        return mainDirectory.resolve(DELTAFORGE_CURRENT_TAG_FOLDER);
    }

    @JsonIgnore
    public Path getInitialBaselineFolder() {
        return mainDirectory.resolve(DELTAFORGE_INITIAL_BASELINE_FOLDER);
    }

    /**
     * @param fromVersion base version
     * @param toVersion   target version
     * @return path to path file
     */
    public Path getPatchPath(String fromVersion, String toVersion, String fileExtension) {
        return mainDirectory
                .resolve(DELTAFORGE_PATCHES_SUBFOLDER)
                .resolve(MessageFormat.format(DELTAFORGE_PATCH_FILE_PATTERN, fromVersion, toVersion, fileExtension));
    }

    /**
     * @param fromVersion base version
     * @param toVersion   target version
     * @return URL to the remote patch file
     */
    @SneakyThrows
    public URL getRemotePatchURL(String fromVersion, String toVersion) {
        return new URL(url + "/" + DELTAFORGE_PATCHES_SUBFOLDER + "/" +
                MessageFormat.format(DELTAFORGE_PATCH_FILE_PATTERN, fromVersion, toVersion));
    }

    public Optional<Channel> getChannel(String name) {
        return channels.parallelStream()
                .filter(channel -> Objects.equals(channel.getName(), name))
                .findAny();
    }

    public Optional<Tag> getLatestTag(String channelName) {
        return getChannel(channelName)
                .map(Channel::getCurrentTag);
    }

    public Optional<Tag> findTagByName(String name) {
        return tags.stream()
                .filter(tag -> Objects.equals(tag.getName(), name))
                .findFirst();
    }
}
