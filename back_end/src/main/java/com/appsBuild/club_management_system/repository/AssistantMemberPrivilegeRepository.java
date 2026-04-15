package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.AssistantMemberPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistantMemberPrivilegeRepository extends JpaRepository<AssistantMemberPrivilege, Long> {
}
