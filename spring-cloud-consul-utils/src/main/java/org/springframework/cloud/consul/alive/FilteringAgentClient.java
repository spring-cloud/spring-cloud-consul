package org.springframework.cloud.consul.alive;

import org.springframework.cloud.consul.model.Member;

import java.util.Collection;
import java.util.Set;

/**
 * A CatalogClient which decorates some methods of CatalogClient with filtering, retaining info pertaining to live nodes.
 * @author nicu on 10.03.2015.
 */
public interface FilteringAgentClient {
    /**
     * @return the set of alive gossip pool members (client or server consul agents).
     */
    Collection<Member> getAliveAgents();
    /**
     * @return the set of alive gossip pool members addresses.
     */
    Set<String> getAliveAgentsAddresses();
}
