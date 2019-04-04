package net.brutus5000.deltaforge.server.repository;

import net.brutus5000.deltaforge.server.model.Channel;
import net.brutus5000.deltaforge.server.model.Repository;
import net.brutus5000.deltaforge.server.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    Optional<Channel> findByRepositoryAndName(Repository repository, String name);

    Set<Channel> findAllByCurrentTag(Tag tag);
}
