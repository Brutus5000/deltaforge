package net.brutus5000.deltaforge.server.repository;

import net.brutus5000.deltaforge.server.model.Channel;
import net.brutus5000.deltaforge.server.model.Tag;
import net.brutus5000.deltaforge.server.model.TagAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagAssignmentRepository extends JpaRepository<TagAssignment, UUID> {
    Optional<TagAssignment> findByChannelAndTag(Channel channel, Tag tag);
}
